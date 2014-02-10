package message.response;

import message.Response;

/**

 */
public class HMacResponse implements Response {
	private static final long serialVersionUID = -7058325034457705550L;

	private Response response;
	private byte[] HMac;

	public HMacResponse(Response response, byte[] HMac) {
		this.response = response;
		this.HMac = HMac;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public byte[] getHMac() {
		return HMac;
	}

	public void setHMac(byte[] hMac) {
		HMac = hMac;
	}

	@Override
	public String toString() {
		return "<HMac><Response> " + response.getClass();
	}
}
