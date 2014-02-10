package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Response;
import message.response.*;
import message.request.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

import model.DownloadTicket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;

import util.*;

import java.util.concurrent.ConcurrentHashMap;

import convert.ConversionService;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class FsThread extends Thread{
  private ServerSocket s;
  private boolean shutdown;
  private FileServerLogic logic;
  private String dir;
  private ConcurrentHashMap<String, Integer> files;
  private ArrayList<FSHandler> handlers;
  private Mac hMac;

  public FsThread(ServerSocket socket, FileServerLogic logic){
    this.s = socket;
    this.shutdown = false;
    this.logic = logic;
    this.dir = logic.getDir();
    this.files = logic.getFilesList();
    this.handlers = new ArrayList<FSHandler>();
    
    
    try {
		byte[] keyBytes = new byte[1024];
		String pathToSecretKey = logic.getHmacKey();
		FileInputStream fis = new FileInputStream(pathToSecretKey);
		fis.read(keyBytes);	
		fis.close();	
		byte[] input = Hex.decode(keyBytes);
		Key hmac_key = new SecretKeySpec(input,"HmacSHA256");

		hMac = Mac.getInstance("HmacSHA256"); 
		hMac.init(hmac_key);
	} catch (IOException e) {
		System.out.println("Hmac-Key-Initialization failed");
	} catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
	} catch (InvalidKeyException e) {
		e.printStackTrace();
	}
	
  }

  public void run(){
    ExecutorService pool = Executors.newFixedThreadPool(20); //TODO how many?
    while(!shutdown){
      try{
        FSHandler f = new FSHandler(s.accept());
        handlers.add(f);
        pool.execute(f);
      } catch (Exception e) {
        shutdown();
      }
    }
    shutdownAndAwaitTermination(pool);
  }

  public void shutdown(){
    this.shutdown = true;
    try{
        s.close();
        for(FSHandler f : handlers){
          f.shutdownHandler();
        }
      } catch (Exception e){
        System.out.println("Error while shutting down (FSThread)");
      }
  }

  private void shutdownAndAwaitTermination(ExecutorService pool) {
   pool.shutdown(); // Disable new tasks from being submitted
   try {
     // Wait a while for existing tasks to terminate
     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
       pool.shutdownNow(); // Cancel currently executing tasks
       // Wait a while for tasks to respond to being cancelled
       if (!pool.awaitTermination(60, TimeUnit.SECONDS))
           System.out.println("Pool did not terminate");
     }
   } catch (InterruptedException ie) {
     // (Re-)Cancel if current thread also interrupted
     pool.shutdownNow();
     // Preserve interrupt status
     Thread.currentThread().interrupt();
   }
 }

 public class FSHandler implements Runnable, IFileServer{
  private final Socket s; //client side of a connection
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean shutdownHandler;
    FSHandler(Socket s) throws IOException{
      this.s = s;
      in = new ObjectInputStream(s.getInputStream());
      out = new ObjectOutputStream(s.getOutputStream());
      this.shutdownHandler = false;
    }
    public void run(){
    	final String B64 = "a-zA-Z0-9/+";
      try{
        while(!shutdownHandler){
          Object o = in.readObject();
          if (o instanceof HMacRequest)	{
        	  //Check HMAC
        	  HMacRequest obj = (HMacRequest) o;
        	  
        	  //assertion
        	  ConversionService convertService = new ConversionService();
        	  String s = convertService.convert(obj.getHMac(), String.class);
        	  s += " " + obj.getRequest().toString();
        	  assert s.matches("["+B64+"]{43}= [\\s[^\\s]]+");
        	  
        	  hMac.update(Serializer.serialize(obj.getRequest().toString()));
        	  byte[] computedHash = hMac.doFinal();
        	  computedHash = Base64.encode(computedHash);

        	  boolean validHash = MessageDigest.isEqual(computedHash,obj.getHMac());
        	  if(!validHash)	{
        		  System.out.println(obj.getRequest());
        	  	  out.writeObject(new HMacErrorResponse("HMac Mismatch"));
        	  }
        	  else	{
	        	  if(obj.getRequest() instanceof ListRequest)	{
	        		  out.writeObject(getSignedResponse(list()));
	        	  }
	        	  else if(obj.getRequest() instanceof InfoRequest)	{
	        		  out.writeObject(getSignedResponse(info((InfoRequest) obj.getRequest())));
	        	  }
	        	  else if(obj.getRequest() instanceof VersionRequest)	{
	        		  out.writeObject(getSignedResponse(version((VersionRequest) obj.getRequest())));
	        	  }
	        	  else if(obj.getRequest() instanceof UploadRequest)	{
	        		  out.writeObject(getSignedResponse(upload((UploadRequest) obj.getRequest())));
	        	  }
        	  }
        	  
          }
          else if(o instanceof DownloadFileRequest){ //TODO Remember even if online in the list, server could've gone offline in between
            out.writeObject(download((DownloadFileRequest) o));
          }
        }
        s.close();
      } catch (Exception e) {
        shutdownHandler();
      }
    }
    
    private HMacResponse getSignedResponse(Response resp)	{
    	try {
			hMac.update(Serializer.serialize(resp));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   	  	byte[] computedHash = hMac.doFinal();
   	  	computedHash = Base64.encode(computedHash);
    	HMacResponse hResponse = new HMacResponse(resp, computedHash);
    	return hResponse;
    }
    
    
    public void shutdownHandler(){
      try{
        this.shutdownHandler = true;
        this.s.close();
      } catch (Exception e) {}
    }

    public Response list() throws IOException{
      return new ListResponse(files.keySet());
    }
    public Response download(DownloadFileRequest request) throws IOException{
      DownloadTicket ticket = request.getTicket();
      File f = new File(dir +"/" +ticket.getFilename());
      if(ChecksumUtils.verifyChecksum(ticket.getUsername(), f, files.get(f.getName()), ticket.getChecksum())){
        BufferedReader fin = new BufferedReader(new FileReader(f));
        ConversionService convertService = new ConversionService();
        byte[] b = new byte[1024];
        StringBuffer strb = new StringBuffer();
        String line = null;
        while ((line = fin.readLine()) != null)
          strb.append(line).append("\n");
        b = convertService.convert(strb.toString(), byte[].class);
        fin.close();
        return new DownloadFileResponse(ticket, b);
      }
      else
        return new MessageResponse("Checksum wrong: Download denied!");
    }
    public Response info(InfoRequest request) throws IOException{
      File f = new File(dir + "/" +request.getFilename());
      if(f.exists())
        return new InfoResponse(request.getFilename(), f.length());
      else
        return new MessageResponse("file not found!");
    }
    public Response version(VersionRequest request) throws IOException{
      File f = new File(dir + File.separator +request.getFilename());
      if(!f.exists())return new MessageResponse("File not existing");
      return new VersionResponse(f.getName(), files.get(f.getName()));
    }
    public MessageResponse upload(UploadRequest request) throws IOException{
      File f = new File(dir+File.separator+request.getFilename());
        if(!f.createNewFile()){
          f.delete();
          f.createNewFile();
        }
        
        ConversionService convertService = new ConversionService();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(convertService.convert(request.getContent(), String.class));
        bw.close();

        
        files.put(request.getFilename(), request.getVersion());
      return new MessageResponse("Successfully uploaded");
    }
 }

}