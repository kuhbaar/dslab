package message.request;

import message.Request;

/**
 * Retrieves the highest available version number of a particular file on a certain server.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !version &lt;filename&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !version &lt;filename&gt; &lt;version&gt;}<br/>
 *
 * @see message.response.VersionResponse
 */
public class HMacRequest implements Request {
	private static final long serialVersionUID = 3995314039957433479L;

	private Request request;
	private byte[] HMac;

	public HMacRequest(Request request, byte[] HMac) {
		this.request = request;
		this.HMac = HMac;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public byte[] getHMac() {
		return HMac;
	}

	public void setHMac(byte[] hMac) {
		HMac = hMac;
	}


	@Override
	public String toString() {
		return "<HMac><Request> " + request.getClass();
	}
}
