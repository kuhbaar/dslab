package proxy;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import message.request.*;
import proxy.Statistics;
import proxy.IProxy;
import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.HMacRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.*;
import message.response.LoginResponse.Type;

import java.util.concurrent.TimeUnit;
import java.net.InetAddress;

import util.*;
import model.*;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import channels.AESChannel;
import channels.AuthServer;
import channels.Base64Channel;
import channels.Channel;
import channels.TCPChannel;
import channels.WrongChallengeException;


public class TcpThread extends Thread{
	private ServerSocket socket; //server side of a connection
	private Statistics stats;
	private boolean shutdown;
	private ExecutorService pool;
	private ArrayList<TcpHandler> handlers;
	private ProxyLogic logic;
	private Mac hMac;


	public TcpThread(ServerSocket s, Statistics stat, ProxyLogic logic){
		this.socket = s;
		this.stats = stat;
		this.shutdown = false;
		this.pool = Executors.newFixedThreadPool(10);
		this.handlers = new ArrayList<TcpHandler>();
		this.logic = logic;
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
			stats.sethMac(hMac);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Hmac-Key-Initialization failed");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
	}

	public void run(){
		while(!shutdown){
			try{
				TcpHandler t = new TcpHandler(socket.accept());
				handlers.add(t);
				pool.execute(t);
			} catch (Exception e) {
				shutdown();
			}
		}
	}

	public void shutdown(){
		shutdown = true;
		try{
				socket.close();
				for(TcpHandler t : handlers){
					t.shutdownHandler();
				}
				shutdownAndAwaitTermination(pool);
			} catch (Exception e){
				System.out.println("Error while shutting down (TCP)");
			}
	}

	
	
	private void shutdownAndAwaitTermination(ExecutorService pool) {
   pool.shutdown(); // Disable new tasks from being submitted
   try {
     // Wait a while for existing tasks to terminate
     if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
       pool.shutdownNow(); // Cancel currently executing tasks
       // Wait a while for tasks to respond to being cancelled
       if (!pool.awaitTermination(1, TimeUnit.SECONDS))
           System.out.println("Pool did not terminate");
     }
   } catch (InterruptedException ie) {
     // (Re-)Cancel if current thread also interrupted
     pool.shutdownNow();
     // Preserve interrupt status
     Thread.currentThread().interrupt();
   }
 }

	class TcpHandler implements Runnable, IProxy{
		private Channel channel;
		private String user;
		private boolean shutdownHandler;

		TcpHandler(Socket s) throws Exception{
			AuthServer as = new AuthServer( new AESChannel(new Base64Channel(new TCPChannel(s))));
			as.setProxyLogic(logic);
	  	this.channel = as;
			user = null;
		}
		
		public void run(){
			try{
				while(!shutdownHandler){
					Object o = channel.readObject();
					if(o instanceof LoginRequest){
						channel.sendObject(login((LoginRequest) o));
					}
					else if(o instanceof BuyRequest){
						channel.sendObject(buy((BuyRequest) o));
					}
					else if(o instanceof CreditsRequest){
						channel.sendObject(credits());
					}
					else if(o instanceof DownloadTicketRequest){ //TODO Remember even if online in the list, server could've gone offline in between
					 channel.sendObject(download((DownloadTicketRequest) o));
					}
					else if(o instanceof ListRequest){
						channel.sendObject(list());
					}
					else if(o instanceof LogoutRequest){
						channel.sendObject(logout());
					}
					else if(o instanceof UploadRequest){
						channel.sendObject(upload((UploadRequest) o));
					}
					else if(o instanceof LoginChallenge){
						LoginChallenge lc = (LoginChallenge) o;
						user = lc.getUsername();
						stats.setUserOnline(user, true);
						channel.sendObject(new OkChallenge(lc.getChallenge()));
					}
				} 
			
				channel.shutdown();
			} catch (WrongChallengeException e){
				System.err.println("Challenge received wrong!"); 
			} catch (Exception e) {
				e.printStackTrace();
				if(user != null){
					stats.setUserOnline(user, false);
					stats.unsubscribeUser(user);
				}
				this.shutdownHandler();
			}
		}
		public void shutdownHandler(){
			try{
				this.shutdownHandler = true;
				channel.shutdown();//this.s.close();
			} catch (Exception e) {}
		}
		public LoginResponse login(LoginRequest request) throws IOException{
			if(stats.checkCredentials(request.getUsername(), request.getPassword())){
				user = request.getUsername();
				stats.setUserOnline(user, true);
				return new LoginResponse(Type.SUCCESS);
			}
			else return new LoginResponse(Type.WRONG_CREDENTIALS);

  	}
		
	  public Response credits() throws IOException{
	    return new CreditsResponse(stats.getUsersCredits(user));
	  }
	  public Response buy(BuyRequest credits) throws IOException{
	  	stats.changeUsersCredits(user, credits.getCredits());
	    return credits();
	  }
	  public Response list() throws IOException{
	  	try{
	  		Set<String> filenames = new HashSet<String>();
	  		List<String> fsaddrs = stats.getAllFileserversAddrs();
	  		List<Integer> fstcps = stats.getAllFileserversPorts();
	  		if(fsaddrs.size() == 0)
	  			return new MessageResponse("No files available");
	  		for(int i = 0; i < fsaddrs.size();i++)	{
	  			
	  			byte[] hmac;
				ListRequest lRequest = new ListRequest();	
				hMac.update(Serializer.serialize(lRequest));
				byte[] hash = hMac.doFinal();
				hmac = Base64.encode(hash);
				HMacRequest request = new HMacRequest(lRequest, hmac);
				
				Object o;
				do	{
					Socket fsc = new Socket(InetAddress.getByName(fsaddrs.get(i)),fstcps.get(i));
		  			ObjectOutputStream outfsc = new ObjectOutputStream(fsc.getOutputStream());
					ObjectInputStream infsc = new ObjectInputStream(fsc.getInputStream());
					
					outfsc.writeObject(request);
					o = infsc.readObject();
					if(o instanceof HMacResponse){
						
						HMacResponse hResp = (HMacResponse) o;
						if(!HMacChecker.verifySignedMessage(hMac, hResp)){
							System.out.println("Message tempered: " + hResp.getResponse());
							o = new HMacErrorResponse("repeat");
							continue;
						}
						if(hResp.getResponse() instanceof ListResponse)	{
							  ListResponse lResp = (ListResponse) hResp.getResponse();
							  for(String item: lResp.getFileNames()){
									if(!filenames.contains(item))
										filenames.add(item);
							  }
						}
					}
					else if(o instanceof HMacErrorResponse)	{
						System.out.println(o);
					}
					outfsc.close();
					infsc.close();
					fsc.close();
				}while(o instanceof HMacErrorResponse);
				
	  		}
	  		return new ListResponse(filenames);
			} catch (Exception e) { e.printStackTrace();}
	    return new MessageResponse("Something went terribly wrong (list, proxy)");
	  }
	  public Response download(DownloadTicketRequest request) throws IOException{
		  
		  try{
			  String fsaddr = "";
			  int fstcp = 0;
			  int highestVersion=0;
			  int lowestUsage=0;
			  boolean initLowestUsage = false;
			  if(stats.getNr() == 0)stats.setNr((int)stats.calculateNumberOfFileservers());
		  	List<FileserverInfoPersist> addrs = stats.getLowestXFileserversAddrs(stats.getNr());
			for(FileserverInfoPersist fsinfo: addrs)	{
				if(fsinfo.getAddr()==null)
			  		continue;
				
				
				byte[] hmac;
				VersionRequest vRequest = new VersionRequest(request.getFilename());	
				hMac.update(Serializer.serialize(vRequest));
				byte[] hash = hMac.doFinal();
				hmac = Base64.encode(hash);
				
				HMacRequest hRequest = new HMacRequest(vRequest, hmac);
				Object o;
				do	{
					Socket fsc = new Socket(InetAddress.getByName(fsinfo.getAddr()),fsinfo.getPort());
					ObjectOutputStream outfsc = new ObjectOutputStream(fsc.getOutputStream());
					ObjectInputStream infsc = new ObjectInputStream(fsc.getInputStream());
					
					outfsc.writeObject(hRequest);
					o = infsc.readObject();
					if(o instanceof HMacResponse)	{
						HMacResponse hResp = (HMacResponse) o;
						if(hResp.getResponse() instanceof VersionResponse){
							VersionResponse resp = (VersionResponse) hResp.getResponse();
							//check if message was tempered
							if(!HMacChecker.verifySignedMessage(hMac, hResp)){
								System.out.println("Message tempered: " + hResp.getResponse());
								o = new HMacErrorResponse("repeat");
								continue;
							}
							
							if(!initLowestUsage)	{
								lowestUsage = (int) fsinfo.getUsage();
								initLowestUsage = true;
							}
							if(resp.getVersion() == highestVersion)	{
								if(fsinfo.getUsage() <= lowestUsage)	{
									fsaddr = fsinfo.getAddr();
							  		fstcp = fsinfo.getPort();
								}
							}
							else if(resp.getVersion() > highestVersion)	{
								highestVersion = resp.getVersion();
								fsaddr = fsinfo.getAddr();
						  		fstcp = fsinfo.getPort();
							}
					  		
						}
					}
					else if(o instanceof HMacErrorResponse){
						System.out.println(((HMacErrorResponse)o));
					}
					outfsc.close();
					infsc.close();
					fsc.close();
				}while (o instanceof HMacErrorResponse);
				
				
				
			
			}
		  	if(fsaddr=="")
			  		return new MessageResponse("No FileServer available for download");
				
		  	byte[] hmac;
		  	InfoRequest iRequest = new InfoRequest(request.getFilename());	
			hMac.update(Serializer.serialize(iRequest));
			byte[] hash = hMac.doFinal();
			hmac = Base64.encode(hash);
			
			HMacRequest hRequest = new HMacRequest(iRequest, hmac);
			

				Object o;
				do	{
					Socket fsc = new Socket(InetAddress.getByName(fsaddr),fstcp);
					ObjectOutputStream outfsc = new ObjectOutputStream(fsc.getOutputStream());
					ObjectInputStream infsc = new ObjectInputStream(fsc.getInputStream());
					
				  	outfsc.writeObject(hRequest);
			  		o = infsc.readObject();
			  		if(o instanceof HMacResponse)	{
			  			HMacResponse hResp = (HMacResponse) o;
			  			if(!HMacChecker.verifySignedMessage(hMac, hResp)){
							System.out.println("Message tempered: " + hResp.getResponse());
							o = new HMacErrorResponse("repeat");
							continue;
						}
				  		if(hResp.getResponse() instanceof InfoResponse){
					  		InfoResponse resp = (InfoResponse) hResp.getResponse();
					  		if(stats.getUsersCredits(user) < resp.getSize())	{
					  			fsc.close();
					  			return new MessageResponse("too few credits to download " + request.getFilename());
					  		}
					  			
					  		else
					  			stats.changeUsersCredits(user, -resp.getSize());
						  	String checksum = ChecksumUtils.generateChecksum(user, request.getFilename(), 0, resp.getSize());
						  	stats.changeFileserversUsage(fsaddr, fstcp, resp.getSize());
						  	stats.increaseFileDownloadCounter(request.getFilename()); //if modifying download don't delete this!! at successful download increasing of counter
						  	stats.notifySubscribers(request.getFilename());
						  	fsc.close();
						  	return new DownloadTicketResponse(new DownloadTicket(user, request.getFilename(), checksum, InetAddress.getByName(fsaddr), fstcp));
						  }
				  		else if (hResp.getResponse() instanceof MessageResponse)	{
				  			fsc.close();
				  			return (MessageResponse) hResp.getResponse();
				  		}
						  	
				  		}
			  		else if (o instanceof HMacErrorResponse)	{
			  			System.out.println(o);
			  		}
			  		outfsc.close();
					infsc.close();
					fsc.close();
				}while(o instanceof HMacErrorResponse);
	  		
			  
	  	} catch (Exception e) { 
	  		e.printStackTrace();
	  		System.out.println("Error in download (Proxy)"); 
	  		}
	  	return new MessageResponse("Error in Proxy");
	  }

	  private UploadRequest getHighestVersion(UploadRequest request)	{
		  int highestVersion=0;
		  try{
			  if(stats.getNr() == 0)stats.setNr((int)stats.calculateNumberOfFileservers());
		  	List<FileserverInfoPersist> addrs = stats.getLowestXFileserversAddrs(stats.getNr());
			for(FileserverInfoPersist fsinfo: addrs)	{
				if(fsinfo.getAddr()==null)
			  		continue;
				byte[] hmac;
				VersionRequest vRequest = new VersionRequest(request.getFilename());	
				hMac.update(Serializer.serialize(vRequest));
				byte[] hash = hMac.doFinal();
				hmac = Base64.encode(hash);
				
				HMacRequest hRequest = new HMacRequest(vRequest, hmac);
				
				Object o;
				do	{
					Socket fsc = new Socket(InetAddress.getByName(fsinfo.getAddr()),fsinfo.getPort());
					ObjectOutputStream outfsc = new ObjectOutputStream(fsc.getOutputStream());
					ObjectInputStream infsc = new ObjectInputStream(fsc.getInputStream());
					
					outfsc.writeObject(hRequest);
					o = infsc.readObject();
					if(o instanceof HMacResponse)	{
						HMacResponse hResp = (HMacResponse) o;
						if(!HMacChecker.verifySignedMessage(hMac, hResp)){
							System.out.println("Message tempered: " + hResp.getResponse());
							o = new HMacErrorResponse("repeat");
							continue;
						}
						if(hResp.getResponse() instanceof VersionResponse){
							VersionResponse resp = (VersionResponse) hResp.getResponse();
							if(resp.getVersion() > highestVersion)	{
								highestVersion = resp.getVersion();
							}
					  		
						}
					}
					else if(o instanceof HMacErrorResponse){
						System.out.println(((HMacErrorResponse)o));
					}
					outfsc.close();
					infsc.close();
					fsc.close();
				}while (o instanceof HMacErrorResponse);
				
				
			}
		  	
	  	} catch (Exception e) { 
	  		System.out.println("Error in VersionRequest (Proxy)"); 
	  		}
	  	return new UploadRequest(request.getFilename(), highestVersion+1, request.getContent());
	  }
	  
	  public MessageResponse upload(UploadRequest request) throws IOException{
		  UploadRequest tmpRequest = getHighestVersion(request);
	  	try{
	  		if(stats.getNw() == 0)stats.calculateReadWriteQuorum();
	  		List<FileserverInfoPersist> addrs = stats.getLowestXFileserversAddrs(stats.getNw());
	  		List<Integer> ports = stats.getAllFileserversPorts();
		  	for(int i = 0; i<addrs.size(); i++){	  		
					
		  		byte[] hmac;	
				hMac.update(Serializer.serialize(tmpRequest));
				byte[] hash = hMac.doFinal();
				hmac = Base64.encode(hash);
				
				HMacRequest hRequest = new HMacRequest(tmpRequest, hmac);
				Object o;
				do	{
					Socket fsc = new Socket(InetAddress.getByName(addrs.get(i).getAddr()),ports.get(i));
					ObjectOutputStream outfsc = new ObjectOutputStream(fsc.getOutputStream());
					ObjectInputStream infsc = new ObjectInputStream(fsc.getInputStream());
					
					outfsc.writeObject(hRequest);
					o = infsc.readObject();
					if(o instanceof HMacResponse)	{
						HMacResponse hResp = (HMacResponse) o;
						if(!HMacChecker.verifySignedMessage(hMac, hResp)){
							System.out.println("Message tempered: " + hResp.getResponse());
							o = new HMacErrorResponse("repeat");
							continue;
						}
						if(hResp.getResponse() instanceof MessageResponse){
							//do something with it!	
						}
					}
					else if(o instanceof HMacErrorResponse){
						System.out.println(((HMacErrorResponse)o));
					}
					
					fsc.close();
				  	infsc.close();
				  	outfsc.close();
				}while (o instanceof HMacErrorResponse);
					
			  	
		  	}
		  	stats.changeUsersCredits(user, 2*(tmpRequest.getContent().length+1));
		  	stats.addFileIfNotContained(tmpRequest.getFilename());
		  	return new MessageResponse("success, current credits:" + stats.getUsersCredits(user));
		  } catch (Exception e){e.printStackTrace();}
	    return new MessageResponse("Error while uploading the file (Proxy)");
	  }
	  public MessageResponse logout() throws IOException{
	  	stats.setUserOnline(user, false);
	  	stats.unsubscribeUser(user);
	  	user = null;
	    return new MessageResponse("Successfully logged out.");
  	}
	}


}
