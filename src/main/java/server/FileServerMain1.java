package server;

import util.*;
import cli.*;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.lang.IllegalArgumentException;
import util.ShellThread;

public class FileServerMain1{

  public static void main(String[] args){
    ShellThread st = new ShellThread(new Shell("Fs1Shell", System.out, System.in));
    FileServerLogic logic1 = new FileServerLogic(new Config("fs1"), st);
    logic1.initFilesList();
    st.getShell().register(new FileServerCommands(logic1));
    try{
      ServerSocket fs1socket = new ServerSocket(logic1.getTcp());
      DatagramSocket fs1aliveSocket = new DatagramSocket(13000);
      FsThread fs1 = new FsThread(fs1socket, logic1);
      FsAliveThread fsa1 = new FsAliveThread(fs1aliveSocket, logic1.getProxyHost(), logic1.getProxyUdp(), logic1.getAliveInt(), logic1.getTcp());
      logic1.setFsThread(fs1);
      logic1.setAliveThread(fsa1);
      fs1.start();
      fsa1.start();
      st.start();

    } catch (IllegalArgumentException e){
      System.out.println("Wrong Command\nPossible commands: !exit");
    } catch (Exception e) {
      System.out.println("An Error (FS main)");
    }
  }
}