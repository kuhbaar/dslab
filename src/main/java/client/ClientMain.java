package client;

import java.net.Socket;
import cli.*;
import util.*;
import java.net.InetAddress;
import java.net.ConnectException;
import java.lang.IllegalArgumentException;

import util.ShellThread;

public class ClientMain{
  public static void main(String[] args){
    ShellThread st = new ShellThread(new Shell("clientShell", System.out, System.in));
    ClientLogic logic = new ClientLogic(new Config("client"), st, new Config("mc"));
    
    ClientRMI rmi = new ClientRMI(logic);
    rmi.init();

    try{
      Socket sclient = new Socket(InetAddress.getByName(logic.getProxyHost()), logic.getProxyTcp());
      ClientThread ct = new ClientThread(sclient, logic);
      ct.start();

      st.getShell().register(ct);
      st.getShell().register(rmi);
      st.start();
    } catch (ConnectException e){
      System.err.println("No Proxy Running");
    } catch (IllegalArgumentException e){
      System.out.println("Wrong Command\nPossible commands: !login <name> <pwd>, !credits, !buy <amount>, !list, !download <filename>, !upload <filename>, !logout, !exit");
    } catch (Exception e){
      e.printStackTrace();
    }
  }

}