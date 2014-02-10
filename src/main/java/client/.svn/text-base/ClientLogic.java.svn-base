package client;

import cli.*;
import util.*;
import java.io.IOException;

public class ClientLogic{
  private Shell s;
  private String ddir, proxyHost, bindingName, keysDirectory, host, username, keysdir, proxykey;
  private int proxyTcp, rmiPort;
  private ClientThread ct;
  private ShellThread st;
  private ClientRMI cliRMI;
	private String pwd;

  public ClientLogic(Config c, ShellThread st, Config rmi){ 
  	this.username = "";
  	this.pwd = "";
    this.st = st;
    this.s = st.getShell();
    try{
      this.ddir = c.getString("download.dir");
      this.proxyHost = c.getString("proxy.host");
      this.proxyTcp = c.getInt("proxy.tcp.port");
      this.keysdir= c.getString("keys.dir");
      this.proxykey= c.getString("proxy.key");
      
      this.bindingName= rmi.getString("binding.name");
      this.host = rmi.getString("proxy.host");
      this.rmiPort = rmi.getInt("proxy.rmi.port");
      this.keysDirectory = rmi.getString("keys.dir");
    } catch (Exception e) {
      System.err.println("parameters in config file missing or invalid");
      e.printStackTrace();
      System.exit(0); // no threads has been started yet
    }
  }

  public Shell getShell(){
    return s;
  }
  public String getProxyHost(){
    return new String(proxyHost);
  }
  public int getProxyTcp(){
    return this.proxyTcp;
  }
  public String getDDir(){
    return new String(ddir);
  }
  public ClientThread getClientThread(){
    return this.ct;
  }
  public void setClientThread(ClientThread ct){
    this.ct = ct;
  }
  public String getBindingName() {
	  return this.bindingName;
  }
  public String getKeysDirectory() {
	  return this.keysDirectory;
  }
  public int getRmiPort() {
	  return this.rmiPort;
  }
  public String getRmiHost() {
	  return this.host;
  }
  
  public String getUsername() {
	  return this.username;
  }
  
  public void setUsername(String username) {
	  this.username = username;
  }
  public void shutdown() throws IOException{
	cliRMI.shutdownClientRMI();
    st.interrupt();
    this.s.close();
    System.in.close();
  }

public ClientRMI getCliRMI() {
	return cliRMI;
}

public void setCliRMI(ClientRMI cliRMI) {
	this.cliRMI = cliRMI;
}

public String getKeysdir() {
	return keysdir;
}

public void setKeysdir(String keysdir) {
	this.keysdir = keysdir;
}

public String getProxykey() {
	return proxykey;
}

public void setProxykey(String proxykey) {
	this.proxykey = proxykey;
}

public String getPwd() {
	return pwd;
}

public void setPwd(String pwd) {
	this.pwd = pwd;
}
}