package channels;

import org.bouncycastle.util.encoders.Base64;

public class Base64Channel extends ChannelDecorator {
	public Base64Channel(Channel c) {
		super(c);
	}
	
	@Override
	public void sendObject(Object o) throws Exception{
			byte[] b = (byte[]) o;
	    byte[] enB = Base64.encode(b);
	    super.sendObject(enB);
	}
	
	@Override
	public Object readObject() throws Exception{		
		byte[] enB = (byte[]) super.readObject();
		byte[] b = Base64.decode(enB);
		return b;
	}

}