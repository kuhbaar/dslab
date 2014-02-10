package proxy;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import convert.ConversionService;
import proxy.Statistics;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

public class UdpThread extends Thread{
	private DatagramSocket socket;
	private int checkPeriod;
	private Statistics stats;
	private boolean shutdown;

	public UdpThread(DatagramSocket s, int c, Statistics stats){
		this.socket = s;
		this.checkPeriod = c;
		this.stats = stats;
		this.shutdown = false;
	}

	public void run(){
		byte[] b;
		b = new byte[1024];
		DatagramPacket p = new DatagramPacket(b,b.length);
		String addr = "";
		String in = "";
		int port = 0;
		ConversionService convertservice = new ConversionService();
		Timer t = new Timer(true);
		TimerTask altask = new IsAliveTask();
		t.schedule(altask, new Date(), (long) checkPeriod);
		
		while(!shutdown){
			try{
				socket.receive(p);	//blocks till data received
				in = convertservice.convert(p.getData(), String.class);		//convert the data to String
				if(in.indexOf("alive") >= 0){	//check if it is an alive packet (else ignore)
					addr = p.getAddress().toString().substring(1);
					Double d = Double.parseDouble(in.substring(in.indexOf(" ")+1)); //get the tcp port from the alive message
					port = d.intValue();
					//put server onto list and sets aliveReceived true
					boolean newServer = stats.fileserverPut(addr+" "+port);
					if(newServer) {
						stats.retrieveAllFileserverFiles(p.getAddress(), port);
					}
				}
			} catch (SocketException e) {
				t.cancel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void shutdown(){
		shutdown = true;
		socket.close();
	}

	public class IsAliveTask extends TimerTask{
		public void run(){
			stats.setFileserversStatus();
		}
	}
}