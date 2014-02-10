package proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import message.request.HMacRequest;
import message.request.ListRequest;
import message.response.HMacErrorResponse;
import message.response.HMacResponse;
import message.response.ListResponse;
import model.FileserverInfoPersist;
import model.SubscriptionPersist;
import model.UserInfoPersist;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import javax.crypto.Mac;

import org.bouncycastle.util.encoders.Base64;

import util.HMacChecker;
import util.Serializer;
import client.IClientRMI;



public class Statistics{
  private ConcurrentHashMap<String, FileserverInfoPersist> fileservers;
  private ConcurrentHashMap<String, UserInfoPersist> users;
  private ConcurrentHashMap<String, Integer> files; //name of file + number of downloads of the specified file
  private ConcurrentHashMap<String, ArrayList<SubscriptionPersist>> subscriptions; //key: filename
  private int Nr;	/** read-quorum-size **/
  private int Nw;	/** write-quorum-size **/
  private Mac hMac;
  
  public Statistics(){
    this.fileservers = new ConcurrentHashMap<String, FileserverInfoPersist>();
    this.users = new ConcurrentHashMap<String, UserInfoPersist>();
    this.files = new ConcurrentHashMap<String, Integer>();
    this.subscriptions = new ConcurrentHashMap<String, ArrayList<SubscriptionPersist>>();
    this.Nr = 0;
    this.Nw = 0;
  }

  public Mac gethMac() {
	return hMac;
  }
	
  public void sethMac(Mac hMac) {
	this.hMac = hMac;
  }

  public void setUsers(ConcurrentHashMap<String, UserInfoPersist> m){
    this.users = m;
  }
  
  public void setServers(ConcurrentHashMap<String, FileserverInfoPersist> m){
    this.fileservers = m;
  }
  
  public boolean fileserverPut(String key){
    if(!fileservers.containsKey(key)) {
		fileservers.put(key, new FileserverInfoPersist( key.substring(0,key.indexOf(" ")), key.substring(key.indexOf(" ")+1) ));
		fileservers.get(key).setAliveReceived(true);
		return true;
    } else {
    	fileservers.get(key).setAliveReceived(true);
    	return false;
    }
    
    
  }
  public void retrieveAllFileserverFiles(InetAddress addr, int port) throws IOException, ClassNotFoundException {
	  byte[] hmac;	
	  ListRequest request = new ListRequest();
	  hMac.update(Serializer.serialize(request));
	  byte[] hash = hMac.doFinal();
	  hmac = Base64.encode(hash);
		
		
	  Object o;
	  HMacRequest hRequest = new HMacRequest(request, hmac);
	  do	{
		  Socket socket = new Socket(addr,port);
		  ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		  ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		  out.writeObject(hRequest);
		  o = in.readObject();
		  if(o instanceof HMacResponse)	{
			  HMacResponse hResp = (HMacResponse) o;
				//check if message was tempered
				if(!HMacChecker.verifySignedMessage(hMac, hResp)){
					System.out.println("Message tempered: " + hResp.getResponse());
					o = new HMacErrorResponse("repeat");
					continue;
				}
			  if(hResp.getResponse() instanceof ListResponse)	{
				  ListResponse response = (ListResponse) hResp.getResponse();
				  socket.close();
				  
				  Iterator<String> iterator = response.getFileNames().iterator();
				  while(iterator.hasNext()) {
					  String filename = iterator.next();
					  if(!files.containsKey(filename)) {
						 files.put(filename, 0); 
					  }
				  }
				  
			  }
		  }
		  else if(o instanceof HMacErrorResponse)	{	  
			  System.err.println(((HMacErrorResponse)o));
		  }
		  out.close();
		  in.close();
		  socket.close();
	  }while(o instanceof HMacErrorResponse);
  }

public String fileserverToString(){
    String s = "";
    synchronized(fileservers){
    for(String key : fileservers.keySet()){
      FileserverInfoPersist f = fileservers.get(key);
      s += String.format("IP: %s Port: %d %s Usage:%d%n\n", f.getAddr(), f.getPort(), f.isOnline() ? "online":"offline", f.getUsage());
    }
    }
    return s;
  }
  public String usersToString(){
    String s = "";
    synchronized(users){
    for(String name : users.keySet()){
      UserInfoPersist u = users.get(name);
      s += String.format("%s %s Credits:%d\n", name, u.isOnline() ? "online":"offline", u.getCredits());
    }
    }
    return s;
  }
  public void setFileserversStatus(){
    synchronized(fileservers){
	  for(String key : fileservers.keySet()){
		FileserverInfoPersist f = fileservers.get(key);
      	if(f.isAliveReceived()){
    	  f.setOnline(true);
    	  f.setAliveReceived(false);
      	}
      	else
      		f.setOnline(false);
	  }
    }
  }
  public boolean checkCredentials(String name, String pwd){
    if(users.containsKey(name))
      return users.get(name).getPwd().equals(pwd)? true:false;
    else
      return false; 
  }
  public void setUserOnline(String name, boolean b){
    users.get(name).setOnline(b);
  }
  public long getUsersCredits(String name){
    return users.get(name).getCredits();
  }
  public void changeUsersCredits(String name, long credits){
    users.get(name).changeCredits(credits);
  }
  public String getFileserverLowestUsage(){
    if(fileservers.isEmpty())
      return null;
    String name = null;
    long usage = 0;
    long currentUsage = 0;
    synchronized(fileservers){
    for(String s : fileservers.keySet()){
      if(fileservers.get(s).isOnline()){
        currentUsage = fileservers.get(s).getUsage();
        if(currentUsage == 0){
          name = s;
          break;
        }
        else if (currentUsage < usage){
          usage = currentUsage;
          name = s;
        }
        else if (usage == 0){
          usage = currentUsage;
          name = s;
        }
      }
    }
    }
    return name;
  }
  public String getFileserverLowestUsageAddr(){
    String out = getFileserverLowestUsage();
    if(out ==null)
      return null;
    return out.substring(0,out.indexOf(" "));
  }
  public int getFileserverLowestUsageTcp(){
    String out = getFileserverLowestUsage();
    if(out ==null)
      return -1;
    return Integer.parseInt(out.substring(out.indexOf(" ")+1));
  }
  public List<String> getAllFileserversAddrs(){
    ArrayList<String> addrs = new ArrayList<String>();
    synchronized(fileservers){
    for(String s : fileservers.keySet()){
      String addr = s.substring(0,s.indexOf(" "));
      if(fileservers.get(s).isOnline())
        addrs.add(addr);
    }
    }
    return addrs;
  }
  public List<Integer> getAllFileserversPorts(){
    ArrayList<Integer> ports = new ArrayList<Integer>();
    synchronized(fileservers){
    for(String s : fileservers.keySet()){
      int port = Integer.parseInt(s.substring(s.indexOf(" ")+1));
      if(fileservers.get(s).isOnline())
        ports.add(port);
    }
    }
    return ports;
  }
  
  public List<FileserverInfoPersist> getLowestXFileserversAddrs(int x){
	  ArrayList<FileserverInfoPersist> tmpList = new ArrayList<FileserverInfoPersist>();
	    
	  ArrayList<FileserverInfoPersist> fsList = new ArrayList<FileserverInfoPersist>();
	    
	synchronized(fileservers){	    
		for(FileserverInfoPersist fs: fileservers.values())	{
	    	tmpList.add(fs);
	    }
	    Collections.sort(tmpList, new Comparator<FileserverInfoPersist>(){
	        public int compare(FileserverInfoPersist fs1, FileserverInfoPersist fs2) {
	            return (int) (fs1.getUsage() - fs2.getUsage());
	        }
	    });
	    for(int i=0;i<x;i++)	{
	    	fsList.add(tmpList.get(i));
	    }  
	    return fsList;
	}
  }
  
  public void changeFileserversUsage(String addr, int port, long usage){
    fileservers.get(addr+" "+port).changeUsage(usage);
  }
  
  public String getTopThreeDownloads() {
	  String result = "Top Three Downloads:" + System.getProperty("line.separator");
	  LinkedHashMap<String,Integer> sortedMap;
	  synchronized(files) {
		  List<String> keys = new ArrayList<String>(files.keySet());
		  List<Integer> vals = new ArrayList<Integer>(files.values());
		  Collections.sort(keys);
		  Collections.sort(vals);
		  
		  sortedMap = new LinkedHashMap<String,Integer>();
		  Iterator<Integer> it = vals.iterator();
		  while(it.hasNext()) {
			  Integer currVal = it.next();
			  Iterator<String> itkeys = keys.iterator();
			  
			  while(itkeys.hasNext()) {
				  String currKey = itkeys.next();
				  Integer comp1 = files.get(currKey);
				  if(comp1.equals(currVal)) {
					  keys.remove(currKey);
					  sortedMap.put(currKey, currVal);
					  break;
				  }
			  }
		  }
	  
		  List<String> sortedKeys = new ArrayList<String>(sortedMap.keySet());
		  int size = sortedKeys.size();
		  for(int i=0; i < 3 && size > 0; i++) {
			  String keyX = sortedKeys.get(--size);
			  Integer valueX = files.get(keyX);
			  result += keyX + " " + valueX + System.getProperty("line.separator");
		  }
	  }
	  return result;
  }
  
  public void increaseFileDownloadCounter(String key) throws Exception {
	  if(files.containsKey(key)) {
		  files.put(key, files.get(key)+1);
	  } else {
		  throw new Exception("Impossible: file Downloaded but not registered in HashMap (Class:Statistics)");
	  }
  }
  
  public boolean notifySubscribers(String filename) {
	  boolean result = false;
	  if(!subscriptions.containsKey(filename) || !files.containsKey(filename)) {
		  return result;
	  }
	  synchronized(subscriptions) {
		  ArrayList<SubscriptionPersist> subscribers = subscriptions.get(filename);
		  Iterator<SubscriptionPersist> it = subscribers.iterator();
		  while(it.hasNext()) {
			  SubscriptionPersist current = it.next();
			  if(current.getDownloadedCount() <= files.get(filename) && !current.isNotified()) {
				  try {
					current.getCallbackObject().receiveSubscriptionAltert("Notification: " + filename + " got downloaded " + current.getDownloadedCount() + " times!");
					current.setNotified(true);
					result = true;
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  
			  }
		  }
	  }
	  return result;
  }
  
  public boolean isUserOnline(String username) {
	  if(users.containsKey(username)) {
		  return users.get(username).isOnline();
	  }
	  return false;
  }
  
  public void addSubscription(String filename, IClientRMI callbackObject, String username, int downloadedCount) {
	  SubscriptionPersist persist = new SubscriptionPersist(username,downloadedCount,callbackObject);
	  if(!subscriptions.containsKey(filename)) {
		  ArrayList<SubscriptionPersist> toPut = new ArrayList<SubscriptionPersist>();
		  toPut.add(persist);
		  subscriptions.put(filename, toPut);
	  } else {
		  subscriptions.get(filename).add(persist);
	  }
  }
  
  public void unsubscribeUser(String username) {
	  synchronized(subscriptions) {
		  ArrayList<String> subscriptionKeys = new ArrayList<String>(subscriptions.keySet());
		  Iterator<String> it = subscriptionKeys.iterator();
		  while(it.hasNext()) {
			  String currKey = it.next();
			  ArrayList<SubscriptionPersist> currVals = subscriptions.get(currKey);
			  Iterator<SubscriptionPersist> valsIt = currVals.iterator();
			  while(valsIt.hasNext()) {
				  SubscriptionPersist currPersist = valsIt.next();
				  if(currPersist.getUsername().equals(username)) {
					  valsIt.remove();
				  }
			  }
		  }
	  }
  }
  public double calculateNumberOfFileservers()	{
	  return fileservers.size();
  }
  public void calculateReadWriteQuorum()	{
	  if(Nw != 0)
		  return;
	  else	{
		  Nr = (int) (Math.ceil(calculateNumberOfFileservers() / 2));
		  Nw = (int) (Math.floor(calculateNumberOfFileservers() / 2) + 1);
		  
	  }
  }

	public int getNr() {
		return Nr;
	}
	
	public void setNr(int Nr) {
		this.Nr=Nr;
	}	
		
	public int getNw() {
		return Nw;
	}  
	
	public void addFileIfNotContained(String file) {
		if(!files.containsKey(file)) {
			files.put(file, 0);
		}
	}
  
}