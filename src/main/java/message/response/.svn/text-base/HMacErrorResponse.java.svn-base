package message.response;

import message.Response;

/**
 * Buys additional credits for the authenticated user.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !buy &lt;credits&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !credits &lt;total_credits&gt;}<br/>
 *
 * @see message.request.BuyRequest
 */
public class HMacErrorResponse implements Response {
	private static final long serialVersionUID = -7058325034457705550L;

	private String message;

	public HMacErrorResponse(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "HMac error - message tempered!";
	}
}
