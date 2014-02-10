package channels;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import message.request.LoginChallenge;
import message.request.OkChallenge;
import message.response.LoginResponse;
import message.response.MessageResponse;
import message.response.LoginResponse.Type;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import convert.ConversionService;
import convert.ObjectByteConverter;
import proxy.ProxyLogic;

public class AuthServer extends ChannelDecorator {
	private boolean hshComplete = false;
	private String uname;
	private ProxyLogic pl;
	private byte[] challenge;

	public AuthServer(Channel c) {
		super(c);
	}
	
	@Override
	public void sendObject(Object o) throws Exception{
		if(!hshComplete){
			byte[] enMsg = this.secondMessage((OkChallenge) o);
			super.sendObject(enMsg);
			//now activate AES encryption
			((AESChannel) super.channel).setActive(true);
			hshComplete = true;
			byte[] enPchallenge = (byte[]) this.readObject();
			byte[] pchallenge = Base64.decode(enPchallenge);
			//when challenge wrong
			if(!Arrays.equals(this.challenge, pchallenge)){
				super.sendObject(new LoginResponse(Type.WRONG_CREDENTIALS));
				throw new WrongChallengeException();
			}
			super.sendObject(new LoginResponse(Type.SUCCESS));
		}
		else
			super.sendObject(o);
			if(o instanceof MessageResponse && ((MessageResponse) o).getMessage().contains("logout")){
				hshComplete = false;
				((AESChannel) super.channel).setActive(false);
		}
	}
	
	@Override
	public Object readObject() throws Exception{
		Object o = super.readObject();
		if(!hshComplete){
			byte[] enMsg = (byte[]) o;
			//Read private key
			String pathToPrivateKey = pl.getPrivKey();
			PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PWDFinderProxy());
			KeyPair keyPair = (KeyPair) in.readObject(); 
			PrivateKey privateKey = keyPair.getPrivate();
			in.close();
			//init the cipher
			Cipher crypti = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			crypti.init(Cipher.DECRYPT_MODE, privateKey);
			//decrypt RSA
			byte[] msg = crypti.doFinal(enMsg);
			LoginChallenge chg = (LoginChallenge) ObjectByteConverter.deserObject(msg);
			this.uname=chg.getUsername();
			byte[] enNumber = chg.getChallenge();
			byte[] number = Base64.decode(enNumber);
			return new LoginChallenge(uname, number); // returns deciphered LoginChallenge
		}
		else
			return o;
	}
	
	private byte[] secondMessage(OkChallenge okc) throws Exception{
		// generates a 32 byte secure random number 
		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] pchallenge = new byte[32];
		final byte[] iv = new byte[16];
		secureRandom.nextBytes(pchallenge);
		secureRandom.nextBytes(iv);
		this.challenge = pchallenge;
		//gen secret key
		KeyGenerator generator = KeyGenerator.getInstance("AES");  
		generator.init(256); 
		SecretKey key = generator.generateKey();
		//init proxy's aes (but not active)
		AESChannel aes = (AESChannel) super.channel;
		aes.aesInit(key, iv);
		//encode the arguments base64
		byte[] enCchallenge = Base64.encode(okc.getCChallenge());
		byte[] enPchallenge = Base64.encode(pchallenge);
		byte[] enSecretKey = Base64.encode(key.getEncoded());
		byte[] enIV = Base64.encode(iv);
		byte[] cmd = ("!ok ".getBytes("UTF-8"));
		byte[] leer = (" ".getBytes("UTF-8"));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write( cmd );
		outputStream.write( enCchallenge );
		outputStream.write( leer );
		outputStream.write( enPchallenge );
		outputStream.write( leer );
		outputStream.write( enSecretKey );
		outputStream.write( leer );
		outputStream.write( enIV );
		byte msg[] = outputStream.toByteArray( );
		
		//assert
		ConversionService convertService = new ConversionService();
		String s = convertService.convert(msg, String.class);
		final String B64 = "a-zA-Z0-9/+";
		assert s.matches("!ok ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{22}==");
		
		//read users pubkey
		String pathToPublicKey = pl.getKeysDirectory()+"/"+uname+".pub.pem";
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey clientPub = (PublicKey) in.readObject();
		in.close();
		//init cipher
		Cipher crypti = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		crypti.init(Cipher.ENCRYPT_MODE, clientPub);
		//encrypt all
		byte[] enMsg = crypti.doFinal(msg);
		return enMsg;
	}

	public void setProxyLogic(ProxyLogic l){
		this.pl = l;
	}
	
	public ProxyLogic getProxyLogic(){
		return pl;
	}
	
	public boolean isHshComplete() {
		return hshComplete;
	}

	public void setHshComplete(boolean hshComplete) {
		this.hshComplete = hshComplete;
	}
	
	class PWDFinderProxy implements PasswordFinder{
		@Override
		public char[] getPassword() {
			return "12345".toCharArray();
		}
	}
}
