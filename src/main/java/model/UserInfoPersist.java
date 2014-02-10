package model;

public class UserInfoPersist{
	private String name;
	private String pwd;
	private long credits;
	private boolean online;

	public UserInfoPersist(String name, long credits, String pwd){
		this.name = name;
		this.pwd = pwd;
		this.credits = credits;
		this.online = false;
	}

	public void setName(String s){
		this.name = s;
	}
	public void setPwd(String s){
		this.pwd = s;
	}
	public void changeCredits(long i){
		this.credits += i;
	}
	public void setOnline(boolean b){
		this.online = b;
	}
	public String getName(){
		return new String(name);
	}
	public String getPwd(){
		return new String(pwd);
	}
	public long getCredits(){
		return new Long(credits);
	}
	public boolean isOnline(){
		return online==true? true:false;
	}
}