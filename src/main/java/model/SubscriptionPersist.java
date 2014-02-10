package model;



import client.IClientRMI;

public class SubscriptionPersist {
	private int downloadedCount;
	private IClientRMI callbackObject;
	private String username;
	private boolean notified;
	
	public SubscriptionPersist(String username, int downloadedCount, IClientRMI callbackObject) {
		this.downloadedCount = downloadedCount;
		this.callbackObject = callbackObject;
		this.username = username;
		notified = false;
	}

	public IClientRMI getCallbackObject() {
		return callbackObject;
	}

	public void setCallbackObject(IClientRMI callbackObject) {
		this.callbackObject = callbackObject;
	}

	public int getDownloadedCount() {
		return downloadedCount;
	}

	public void setDownloadedCount(int downloadedCount) {
		this.downloadedCount = downloadedCount;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}
}
