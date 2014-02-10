package proxy;

import java.lang.IllegalArgumentException;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import proxy.ProxyLogic;
import proxy.TcpThread;
import proxy.Statistics;
import cli.*;
import util.*;
import proxy.ProxyCommands;
import util.ShellThread;

public class Proxy{

	public Proxy(){
	}

	public static void main(String[] args){
		try{
			ShellThread st = new ShellThread(new Shell("proxyShell", System.out, System.in));
			ProxyLogic logic = new ProxyLogic(new Config("proxy"),new Config("mc"), st);
			logic.initUserStats();	//Reads the user's data into memory
			Statistics stats = logic.getStats();
			ProxyCommands cli = new ProxyCommands(logic);
			st.getShell().register(cli);
			
			ProxyRMI pRMI = new ProxyRMI(logic);
			pRMI.init();
		
			ServerSocket ssocket = new ServerSocket(logic.getTcp());
			DatagramSocket dsocket = new DatagramSocket(logic.getUdp());
			TcpThread listening = new TcpThread(ssocket, stats, logic);
			UdpThread isalive = new UdpThread(dsocket, logic.getCheckInt(), stats);
			logic.setUdpThread(isalive);
			logic.setTcpThread(listening);
			listening.start();
			isalive.start();
      st.start();
			
		} catch (IllegalArgumentException e){
      System.out.println("Wrong Command\nPossible commands: !fileservers, !users, !exit");
    } catch (Exception e) {
			e.printStackTrace();
		}

	}
}