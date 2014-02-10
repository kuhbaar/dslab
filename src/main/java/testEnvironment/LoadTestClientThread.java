package testEnvironment;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;

import util.Config;
import util.ShellThread;
import cli.Shell;
import client.ClientLogic;
import client.ClientRMI;
import client.ClientThread;


public class LoadTestClientThread extends Thread{
	
	private LoadTestLogic tLogic;
	private ClientLogic cLogic;
	private ShellThread st;
	private long delayUpload, delayDownload;
	private Timer uploadTimer;
	private Timer downloadTimer;
	private String testUser, testPassword;
	private File lastFile;
	private int uploadOverwrites, uploadsTotal;
	
	public LoadTestClientThread(LoadTestLogic tLogic) {
		this.tLogic = tLogic;
		uploadTimer = new Timer();
		downloadTimer = new Timer();
		delayUpload = 60000/tLogic.getUploadsPerMin();
		delayDownload = 60000/tLogic.getDownloadsPerMin();
		uploadOverwrites = 0;
		uploadsTotal = 0;
		//Set testUser and Password
		testUser = "alice";
		testPassword = "12345";
		
		
	}
	
	public synchronized ClientThread getClientThread()	{
		return cLogic.getClientThread();
	}
	
	private void startLoadTest()	{
		performLogin();
		uploadTimer.schedule(new UploadTimerTask(this), delayUpload, delayUpload);
		downloadTimer.schedule(new DownloadTimerTask(this), delayDownload, delayDownload);
	}
	
	public void stopLoadTest()	{
		uploadTimer.cancel();
		downloadTimer.cancel();
	}
	
	public void subscribe()	{
		//TODO subscribe to one or multiple files and print to stdout (in a new Thread)
		while(true);
	}
	
	private void performLogin()	{
		try {
			cLogic.getClientThread().login(testUser, testPassword);
			cLogic.getClientThread().buy(1000000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void sendCommand(String cmd)	{
		try {
			st.getShell().writeLine(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run(boolean start)	{		
		st = new ShellThread(new Shell("LoadTestClientShell", System.out, System.in));
	    cLogic = new ClientLogic(new Config("client"), st, new Config("mc"));
	    
	    ClientRMI rmi = new ClientRMI(cLogic);
	    rmi.init();

	    try{
	      Socket sclient = new Socket(InetAddress.getByName(cLogic.getProxyHost()), cLogic.getProxyTcp());
	      ClientThread ct = new ClientThread(sclient, cLogic);
	      ct.start();

	      cLogic.setClientThread(ct);
	      st.getShell().register(ct);
	      st.getShell().register(rmi);
	      st.start();
	      if(start)
	    	  startLoadTest();
	    } catch (ConnectException e){
	      System.err.println("No Proxy Running");
	    } catch (IllegalArgumentException e){
	      System.out.println("Wrong Command\nPossible commands: !login <name> <pwd>, !credits, !buy <amount>, !list, !download <filename>, !upload <filename>, !logout, !exit");
	    } catch (Exception e){
	      e.printStackTrace();
	    }
	}

	
	
	public int getUploadsTotal() {
		return uploadsTotal;
	}

	public void setUploadsTotal(int uploadsTotal) {
		this.uploadsTotal = uploadsTotal;
	}
	
	public void incUploadsTotal() {
		this.uploadsTotal++;
	}

	public int getUploadOverwrites() {
		return uploadOverwrites;
	}

	public void setUploadOverwrites(int uploadOverwrites) {
		this.uploadOverwrites = uploadOverwrites;
	}
	
	public void incUploadOverwrites() {
		uploadOverwrites++;
	}

	public File getLastFile() {
		return lastFile;
	}

	public void setLastFile(File lastFile) {
		this.lastFile = lastFile;
	}

	public LoadTestLogic gettLogic() {
		return tLogic;
	}

	public void settLogic(LoadTestLogic tLogic) {
		this.tLogic = tLogic;
	}

	public ClientLogic getcLogic() {
		return cLogic;
	}

	public void setcLogic(ClientLogic cLogic) {
		this.cLogic = cLogic;
	}
	
	
	
}
