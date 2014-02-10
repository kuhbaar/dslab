package testEnvironment;

import java.io.IOException;
import java.util.TimerTask;

import client.ClientThread;

public class DownloadTimerTask extends TimerTask{

	private LoadTestClientThread parent;
	private ClientThread cThread;
	
	public DownloadTimerTask(LoadTestClientThread parent) {
		this.parent = parent;
		cThread = parent.getcLogic().getClientThread();
	}
	
	@Override
	public void run() {
		try {
			System.out.println(cThread.download("short.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
