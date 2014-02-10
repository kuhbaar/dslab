package client;

import channels.AESChannel;
import channels.AuthClient;
import channels.Base64Channel;
import channels.Channel;
import channels.TCPChannel;
import channels.WrongChallengeException;
import cli.*;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import message.Response;
import message.response.*;
import message.response.LoginResponse.Type;
import message.request.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import convert.ConversionService;
import model.*;
import java.net.SocketException;

public class ClientThread extends Thread implements IClientCli{
  private Channel channel;
  private ClientLogic logic;
  private boolean shutdown;

  public ClientThread(Socket s, ClientLogic l) throws Exception{
  	AuthClient ac = new AuthClient( new AESChannel(new Base64Channel(new TCPChannel(s))));
		ac.setClientLogic(l);
  	this.channel = ac;
    this.logic = l;
    this.shutdown = false;
  }

  public void run(){
    try{
      while(!shutdown){Thread.sleep(100);}      
    } catch (Exception e) {
    	e.printStackTrace();
    }
  }

  @Command
  public LoginResponse login(String username, String password) throws IOException{
  	if(logic.getUsername() !="")
  		System.err.println("You have to logout first!");
  	else
	  	try{
	    	logic.setUsername(username);
	    	logic.setPwd(password);
	    	channel.sendObject(new LoginRequest(username,password));
	    	return (LoginResponse) channel.readObject();
	    } catch(SocketException e){
	    	e.printStackTrace();
	      System.err.println("Connection closed"); exit();
	    } catch(WrongChallengeException e){ 
	    	System.err.println("Challenge received wrong!"); 
	    } catch(Exception e){ 
	    	e.printStackTrace(); 
	    }
    return new LoginResponse(Type.WRONG_CREDENTIALS);
  }
  
  @Command
  public Response credits() throws IOException{
  	if(logic.getUsername() == "")
  		return new MessageResponse("You have to log in first!");
  	else
	  	try{
	  		channel.sendObject(new CreditsRequest());
		    return (Response) channel.readObject();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    	return new MessageResponse("An Error has occurred!");}
  }
  
  @Command
  public Response buy(long credits) throws IOException{
  	if(logic.getUsername() == "")
  		return new MessageResponse("You have to log in first!");
  	else
	    try{
	    	channel.sendObject(new BuyRequest(credits));
	      return (Response) channel.readObject();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    	return new MessageResponse("An Error has occurred!");}
  }
  
  @Command
  public Response list() throws IOException{
  	if(logic.getUsername() == "")
  		return new MessageResponse("You have to log in first!");
  	else
	  	try{
	  		channel.sendObject(new ListRequest());
	      return (Response) channel.readObject();
	    } catch(Exception e) {e.printStackTrace();
	    return new MessageResponse("An Error has occurred!");}
  }
  
  @Command
  public synchronized Response download(String filename) throws IOException{
  	if(logic.getUsername() == "")
  		return new MessageResponse("You have to log in first!");
  	else
	  	try{
	  		channel.sendObject(new DownloadTicketRequest(filename));
	      Object o = channel.readObject();
	      if(o instanceof DownloadTicketResponse){
	        DownloadTicket ticket = ((DownloadTicketResponse) o).getTicket();
	        Socket fsc = new Socket(ticket.getAddress(), ticket.getPort());
	        ObjectOutputStream outfsc = new ObjectOutputStream(fsc.getOutputStream());
	        ObjectInputStream infsc = new ObjectInputStream(fsc.getInputStream());
	        outfsc.writeObject(new DownloadFileRequest(ticket));
	        DownloadFileResponse response = (DownloadFileResponse) infsc.readObject();
	
	        infsc.close();
	        outfsc.close();
	        fsc.close();
	        
	        File f = new File(logic.getDDir()+"/"+filename);
	        if(!f.createNewFile()){
	          f.delete();
	          f.createNewFile();
	        }
	        ConversionService convertService = new ConversionService();
	        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
	        bw.write(convertService.convert(response.getContent(), String.class));
	        bw.close();
	        return response;
	      }
	      else
	        return (MessageResponse) o;
	
	    } catch(Exception e) {e.printStackTrace();
	    return new MessageResponse("An Error has occurred!");}
   
  }

  @Command
  public synchronized MessageResponse upload(String filename) throws IOException{
  	if(logic.getUsername() == "")
  		return new MessageResponse("You have to log in first!");
  	else
	  	try{
	      BufferedReader fin = new BufferedReader(new FileReader(logic.getDDir()+File.separator+filename));
	      ConversionService convertService = new ConversionService();
	      byte[] b = new byte[1024];
	      b = convertService.convert(fin.readLine(), byte[].class);
	      fin.close();
	      
	      channel.sendObject(new UploadRequest(filename, 0, b));
	      return (MessageResponse) channel.readObject();
	    } catch (FileNotFoundException e) {
	      return new MessageResponse("File not Found!");
	    } catch(Exception e) {
	      return new MessageResponse("An Error has occurred");
	    }
  }

  @Command
  public MessageResponse logout() throws IOException{
  	if(logic.getUsername() == "")
  		return new MessageResponse("You have to log in first!");
  	else
	  	try{
	      logic.setUsername("");
	      logic.getCliRMI().shutdownClientRMI();
	      channel.sendObject(new LogoutRequest());
	      return (MessageResponse) channel.readObject();
	    } catch(Exception e) { 
	    	e.printStackTrace();
	    	logic.setUsername("");
	    	return new MessageResponse("An Error has occurred!");
	    }
  	
  }
  
  @Command
  public MessageResponse exit() throws IOException{
    this.shutdown();
    logic.shutdown();
    return new MessageResponse("Exiting");
  }
  
  public void shutdown(){
    shutdown = true;
    try {
			logout();
		} catch (IOException e) {
			e.printStackTrace();
		}
    channel.shutdown();
  }
}
/*Wenn im client der Login Befehl eingegeben wurde (!login <user> <pwd>) wird dieser nicht wie zuvor an den proxy gesendet, sondern am client geparsed um mit dem Benutzernamen und dem Kennwort (für die Keys) den RSA und anschließend den AES channel aufzubauen.

Nachdem der Channel aufgebaut wurde kennen ja sowohl der Proxy als auch der Client den User. Ob Sie nun wirklich den Login Befehl nochmal mit Kennwort übertragen wollen bleibt Ihnen überlassen, da die Angabe dort keine genauen Vorgaben gibt. Aber Sie müssen halt Ihre Entscheidung bei den Interviews rechtfertigen können und sich auch über daraus folgenden Nachteile Gedanken gemacht haben.

viele Grüße,
Andreas (Tutor)*/
  