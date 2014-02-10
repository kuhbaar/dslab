package channels;

import java.io.FileReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import client.ClientLogic;

import convert.ConversionService;
import convert.ObjectByteConverter;
import message.request.LoginChallenge;
import message.request.LoginRequest;
import message.request.OkChallenge;
import message.response.MessageResponse;

public class AuthClient extends ChannelDecorator {
	private boolean hshComplete = false;
	private String uname;
	private ClientLogic cl;
	private byte[] challenge;

	public AuthClient(Channel c) {
		super(c);
	}
	
	@Override
	public void sendObject(Object o) throws Exception{
		if(!hshComplete){
			byte[] enMsg = this.firstMessage((LoginRequest) o);
			super.sendObject(enMsg);
			OkChallenge okc = (OkChallenge) this.readObject();
			//when challenge wrong
			if(!Arrays.equals(this.challenge, okc.getCChallenge()))
				throw new WrongChallengeException();
			enMsg = thirdMessage(okc);
			hshComplete = true;
			super.sendObject(enMsg);
		}
		else{
			super.sendObject(o);
		}
	}
	
	@Override
	public Object readObject() throws Exception{
		Object o = super.readObject();
		if(!hshComplete){
			byte[] enMsg = (byte[]) o;
			String pathToPrivateKey = cl.getKeysDirectory()+"/"+uname+".pem";
			PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PWDFinderClient());
			KeyPair keyPair = (KeyPair) in.readObject(); 
			PrivateKey privateKey = keyPair.getPrivate();
			in.close();
			//init the cipher
			Cipher crypti = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			crypti.init(Cipher.DECRYPT_MODE, privateKey);
			//decrypt RSA
			byte[] msg = crypti.doFinal(enMsg);
			//decrypt it
			ConversionService convertService = new ConversionService();
			String s = convertService.convert(msg, String.class);
			//remove "!ok "
			s = s.substring(s.indexOf(" ")+1);
			byte[][] b = new byte[4][];
			for(int i=0; i<4; i++){
				if(s.indexOf("=")==s.indexOf("==")){
					b[i]= convertService.convert(s.substring(0,s.indexOf("==")+2), byte[].class);
					s = s.substring(s.indexOf("==")+2);						
				}
				else{
					b[i]= convertService.convert(s.substring(0,s.indexOf("=")+1), byte[].class);
					s = s.substring(s.indexOf("=")+1);				
				}
			}
			byte[] enCchallenge = b[0];
			byte[] enPchallenge = b[1];
			byte[] enSecretKey = b[2];
			byte[] enIV = b[3];
			byte[] cchallenge = Base64.decode(enCchallenge);
			byte[] pchallenge = Base64.decode(enPchallenge);
			byte[] secretKey = Base64.decode(enSecretKey);
			byte[] iv = Base64.decode(enIV);
			
			return new OkChallenge(cchallenge, pchallenge, secretKey, iv); //returns deciphered OkChallenge
		}
		else{
			if(o instanceof MessageResponse && ((MessageResponse) o).getMessage().contains("logout")){
				hshComplete = false;
				((AESChannel) super.channel).setActive(false);
			}
			return o;
		}
	}
	
	private byte[] firstMessage(LoginRequest r)throws Exception{ //send by client
		this.uname = r.getUsername();
		// generates a 32 byte secure random number 
		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] number = new byte[32]; 
		secureRandom.nextBytes(number);
		this.challenge = number;
		//encrypt challenge with Base64
		byte[] enNumber = Base64.encode(number);
		//Read Proxy's public key
		String pathToPublicKey = cl.getProxykey();
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey proxyPub = (PublicKey) in.readObject();
		in.close();
		//Initialize the cipher
		Cipher crypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		crypt.init(Cipher.ENCRYPT_MODE, proxyPub);
		//convert the Object to byte array
		LoginChallenge resp = new LoginChallenge(uname, enNumber);
		byte[] msg = ObjectByteConverter.serObject(resp);
		
		//assert
		final String B64 = "a-zA-Z0-9/+";
		assert resp.toString().matches("!login \\w+ ["+B64+"]{43}="); //true
		
		//encrypt whole msg
		byte[] enMsg = crypt.doFinal(msg);
		return enMsg;
	}
	
	private byte[] thirdMessage(OkChallenge okc)throws Exception{
		byte[] pchallenge = okc.getPChallenge();
		byte[] iv = okc.getIV();
		SecretKey key = new SecretKeySpec(okc.getSecretKey(), 0, okc.getSecretKey().length, "AES");
		//init client's aes
		AESChannel aesc = (AESChannel) super.channel;
		aesc.aesInit(key, iv);
		aesc.setActive(true);
		byte[] enPchallenge = Base64.encode(pchallenge);
		
		//assert
		final String B64 = "a-zA-Z0-9/+";
		ConversionService convertService = new ConversionService();
		String s = convertService.convert(enPchallenge, String.class);
		assert s.matches("["+B64+"]{43}=") : "3rd message";

		return enPchallenge;
	}

	public boolean isHshComplete() {
		return hshComplete;
	}

	public void setHshComplete(boolean hshComplete) {
		this.hshComplete = hshComplete;
	}
	
	public void setClientLogic(ClientLogic l){
		this.cl = l;
	}
	
	public ClientLogic getClientLogic(){
		return cl;
	}

	class PWDFinderClient implements PasswordFinder{
		@Override
		public char[] getPassword() {
			return cl.getPwd().toCharArray();
		}
	}
}
