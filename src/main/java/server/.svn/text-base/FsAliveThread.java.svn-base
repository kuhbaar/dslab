package server;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;
import java.net.InetAddress;
import java.util.Date;
import convert.ConversionService;

public class FsAliveThread extends Thread{
  private DatagramSocket socket;
  private String proxyAddr;
  private int proxyUdp, aliveInt, tcp;

  public FsAliveThread(DatagramSocket socket, String s, int udp, int a, int tcp){
    this.socket = socket;
    this.proxyAddr = s;
    this.proxyUdp = udp;
    this.aliveInt = a;
    this.tcp = tcp;
  }

  public void run(){
    byte[] b;
    try{
    InetAddress addr = InetAddress.getByName(proxyAddr);
    b = new byte[1024];
    ConversionService convertService = new ConversionService();
    b = convertService.convert("!alive " + tcp, byte[].class);    
    DatagramPacket p = new DatagramPacket(b, b.length, addr, proxyUdp);
    Timer t = new Timer();
    t.schedule(new SendAliveTask(p, t), new Date(), (long) aliveInt);
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("Error in FsAliveThread");
    }
  }

  public void shutdown(){
    socket.close();
  }

  public class SendAliveTask extends TimerTask{
    private DatagramPacket p;
    private Timer t;
    public SendAliveTask(DatagramPacket p, Timer t){
      this.p = p;
      this.t = t;
    }

    public void run(){
      try{
        //assert
      	ConversionService convertService = new ConversionService();
    		String s = convertService.convert(p.getData(), String.class);
        assert s.matches("!alive 1[0-9]{4}");
        
      socket.send(p);
    } catch (Exception e){
      t.cancel();
    }
    }
  }
}