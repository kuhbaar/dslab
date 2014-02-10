package server;

import util.*;
import cli.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;

public class FileServerLogic{
  private Shell s;
  private String dir, proxyHost, hmacKey;
  private int tcp, proxyUdp, aliveInt;
  private FsThread fsThread;
  private FsAliveThread aliveThread;
  private ConcurrentHashMap<String, Integer> files;
  private ShellThread st;

  public FileServerLogic(Config c, ShellThread st){
    this.st = st;
    this.s = st.getShell();
    try{
      this.dir = c.getString("fileserver.dir");
      this.proxyHost = c.getString("proxy.host");
      this.tcp = c.getInt("tcp.port");
      this.proxyUdp = c.getInt("proxy.udp.port");
      this.aliveInt = c.getInt("fileserver.alive");
      this.hmacKey = c.getString("hmac.key");
    } catch (Exception e) {
      System.out.println("parameters in config file missing or invalid");
      System.exit(0); // no threads has been started yet
    }
    this.fsThread = null;
    this.aliveThread = null;
    this.files = new ConcurrentHashMap<String, Integer>();
  }
  
  public String getHmacKey() {
	return hmacKey;
  }
  public void setHmacKey(String hmacKey) {
	this.hmacKey = hmacKey;
  }
  public String getDir(){
    return new String(dir);
  }
  public String getProxyHost(){
    return new String(proxyHost);
  }
  public int getTcp(){
    return new Integer(tcp);
  }
  public int getProxyUdp(){
    return new Integer(proxyUdp);
  }
  public int getAliveInt(){
    return new Integer(aliveInt);
  }
  public Shell getShell(){
    return this.s;
  }
  public void setShell(Shell s){
    this.s = s;
  }
  public void setFsThread(FsThread t){
    this.fsThread = t;
  }
  public void setAliveThread(FsAliveThread t){
    this.aliveThread = t;
  }
  public void shutdown(){
    fsThread.shutdown();
    aliveThread.shutdown();

    st.interrupt();
    st.getShell().close();
  }
  public void initFilesList(){
    File folder = new File(dir);
    if(folder.isDirectory())
      for(File f : folder.listFiles())
        files.put(f.getName(), 0);
  }
  public ConcurrentHashMap<String, Integer> getFilesList(){
    return files;
  }
}