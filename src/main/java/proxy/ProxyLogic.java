package proxy;

import util.*;
import cli.*;
import model.UserInfoPersist;

import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Exception;
import java.io.IOException;

public class ProxyLogic{
	private Shell s;
	private int tcp, udp, alive, check, rmiPort;
	private String bindingName, host, keys, privKey, hmacKey;
	private TcpThread tcpThread;
	private UdpThread udpThread;
	private Statistics stats;
	private ShellThread st;
	private IProxyRMI proxyRemote;

	public ProxyLogic(Config c,Config rmi, ShellThread st) throws IOException{
		this.st = st;
		this.s = st.getShell();
		try{
			this.tcp = c.getInt("tcp.port");
			this.udp = c.getInt("udp.port");
			this.alive = c.getInt("fileserver.timeout");
			this.check = c.getInt("fileserver.checkPeriod");
			
			this.privKey = c.getString("key");
			this.hmacKey = c.getString("hmac.key");
			
			this.bindingName= rmi.getString("binding.name");
			this.host = rmi.getString("proxy.host");
			this.rmiPort = rmi.getInt("proxy.rmi.port");
			this.keys = rmi.getString("keys.dir");
		} catch (Exception e) {
			System.out.println("parameters in config file missing or invalid");
			System.exit(0); // no threads has been started yet
		}
		this.tcpThread = null;
		this.udpThread = null;
		this.stats = new Statistics();
	}

	public int getTcp(){
		return new Integer(tcp);
	}
	public int getUdp(){
		return new Integer(udp);
	}
	public int getAliveInt(){
		return new Integer(alive);
	}
	public int getCheckInt(){
		return new Integer(check);
	}
	public Shell getShell(){
		return s;
	}
	public Statistics getStats(){
		return this.stats;
	}
	public void setShell(Shell s){
		this.s = s;
	}
	//read users' information from user.properties into the users list
	public void initUserStats(){
		ConcurrentHashMap<String, UserInfoPersist> usersmap = new ConcurrentHashMap<String, UserInfoPersist>();
		ResourceBundle bundle = ResourceBundle.getBundle("user");
		Enumeration<String> e = bundle.getKeys();
		String key;
		UserInfoPersist value;
		while(e.hasMoreElements()){
			key = e.nextElement();
			key = key.substring(0,key.indexOf('.'));
			if(!usersmap.containsKey(key)){
				value = new UserInfoPersist(key, Long.parseLong(bundle.getString(key+".credits")), bundle.getString(key+".password"));
				usersmap.put(key, value);
			}
		}
		this.stats.setUsers(usersmap);
	}
	public void setTcpThread(TcpThread t){
		this.tcpThread = t;
	}
	public void setUdpThread(UdpThread t){
		this.udpThread = t;
	}
	
	public String getBindingName() {
		return this.bindingName;
	}
	public String getProxyHost() {
		return this.host;
	}
	public int getRmiPort() {
		return this.rmiPort;
	}
	public String getKeysDirectory() {
		return this.keys;
	}
	public void shutdown() throws IOException{
		if(tcpThread!=null){
			this.tcpThread.shutdown();
			this.udpThread.shutdown();
		}
		UnicastRemoteObject.unexportObject(proxyRemote, true);
		st.interrupt();
		st.getShell().close();
		System.in.close();
	}

	public void setProxyRemote(IProxyRMI remote) {
		this.proxyRemote = remote;
		
	}

	public String getPrivKey() {
		return privKey;
	}

	public void setPrivKey(String privKey) {
		this.privKey = privKey;
	}

	public String getHmacKey() {
		return hmacKey;
	}

	public void setHmacKey(String hmacKey) {
		this.hmacKey = hmacKey;
	}
}