package message.request;

import message.Request;

public class LoginChallenge implements Request {
	private static final long serialVersionUID = -1591236158259072949L;
	
	private final String username;
	private final byte[] challenge;
	
	public LoginChallenge(String s, byte[] b) {
		this.username = s;
		this.challenge = b;
	}

	public String getUsername() {
		return username;
	}

	public byte[] getChallenge() {
		return challenge;
	}
	@Override
	public String toString() {
		return String.format("!login %s %s", getUsername(), new String(getChallenge()));
	}

}
