package message.request;

import message.Request;

public class B64Request implements Request{
	private static final long serialVersionUID =  8567241767079930421L;
	
	private final byte[] b;
	
	public B64Request(byte[] b) {
		this.b = b;
	}
	public byte[] getData() {
		return b;
	}

	@Override
	public String toString() {
		return "Base64Encoded Request: " + getData();
	}
}
