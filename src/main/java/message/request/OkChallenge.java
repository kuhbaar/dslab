package message.request;

import message.Request;

public class OkChallenge implements Request {
	private static final long serialVersionUID = -1590000158259072949L;
	
	private final byte[] cchallenge;
	private final byte[] pchallenge;
	private final byte[] secretKey;
	private final byte[] iv;
	
	public OkChallenge(byte[] b) {
		this.cchallenge = b;
		this.pchallenge = null;
		this.secretKey = null;
		this.iv = null;
	}
	
	public OkChallenge(byte[] b, byte[] bp, byte[] k, byte[] i) {
		this.cchallenge = b;
		this.pchallenge = bp;
		this.secretKey = k;
		this.iv = i;
	}

	public byte[] getCChallenge() {
		return cchallenge;
	}
	public byte[] getPChallenge() {
		return pchallenge;
	}
	public byte[] getSecretKey() {
		return secretKey;
	}
	public byte[] getIV() {
		return iv;
	}
	@Override
	public String toString() {
		return String.format("!ok %s %s %s %s", new String(getCChallenge()), new String(getPChallenge()), new String(getSecretKey()), new String(getIV()));
	}

}
