package model;

public class FileserverInfoPersist{
	private boolean online;
	private String addr;
	private int port;
	private long usage;
	private boolean aliveReceived;

	public FileserverInfoPersist(String s, String p){
		this.online = true;
		this.addr = s;
		this.port = Integer.parseInt(p);
		this.usage = 0;
		this.aliveReceived = false;
	}

	public void setAddr(String s){
		this.addr = s;
	}
	public void setOnline(boolean b){
		this.online = b;
	}
	public void setPort(int i){
		this.port = i;
	}
	public void setAliveReceived(boolean b){
		this.aliveReceived = b;
	}
	public void changeUsage(long i){
		this.usage+=i;
	}
	public String getAddr(){
		return new String(addr);
	}
	public boolean isAliveReceived(){
		return aliveReceived==true? true:false;
	}
	public boolean isOnline(){
		return online==true? true:false;
	}
	public int getPort(){
		return new Integer(port);
	}
	public long getUsage(){
		return new Long(usage);
	}
}