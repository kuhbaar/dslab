package channels;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import convert.ObjectByteConverter;

public class AESChannel extends ChannelDecorator {
	private Cipher crypt;
	private boolean active;
	private SecretKey key;
	private IvParameterSpec iv;
	
	public AESChannel(Channel c) throws Exception{
		super(c);
		this.crypt = Cipher.getInstance("AES/CTR/NoPadding");
		this.active = false;
	}
	
	@Override
	public void sendObject(Object o) throws Exception{
		if(active){ //normal communication
			byte[] msg = ObjectByteConverter.serObject(o);
			crypt.init(Cipher.ENCRYPT_MODE, key, iv);
			byte[] enMsg = crypt.doFinal(msg);
			super.sendObject(enMsg);
		}
		else{
			super.sendObject(o);
		}
	}
	
	@Override
	public Object readObject() throws Exception{
		byte[] enMsg = (byte[]) super.readObject();
		if(active){
			crypt.init(Cipher.DECRYPT_MODE, key, iv);
			byte[] msg = crypt.doFinal(enMsg);
			return ObjectByteConverter.deserObject(msg);
		}
		return enMsg;
	}

	public void aesInit(SecretKey key, byte[] v){
		this.key = key;
		this.iv = new IvParameterSpec(v);
	}
	
	public void setActive(boolean b){
		this.active = b;
	}
}
