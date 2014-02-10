package server;

import util.*;
import cli.*;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.lang.IllegalArgumentException;
import util.ShellThread;

public class FileServerMain2{

  public static void main(String[] args){
    ShellThread st = new ShellThread(new Shell("Fs2Shell", System.out, System.in));
    FileServerLogic logic2 = new FileServerLogic(new Config("fs2"), st);
    logic2.initFilesList();
    st.getShell().register(new FileServerCommands(logic2));
    try{
      ServerSocket fs2socket = new ServerSocket(logic2.getTcp());
      DatagramSocket fs2aliveSocket = new DatagramSocket(23000);
      FsThread fs2 = new FsThread(fs2socket, logic2);
      FsAliveThread fsa2 = new FsAliveThread(fs2aliveSocket, logic2.getProxyHost(), logic2.getProxyUdp(), logic2.getAliveInt(), logic2.getTcp());
      logic2.setFsThread(fs2);
      logic2.setAliveThread(fsa2);
      fs2.start();
      fsa2.start();
      st.start();

    } catch (IllegalArgumentException e){
      System.out.println("Wrong Command\nPossible commands: !exit");
    } catch (Exception e) {
      System.out.println("An Error (FS main)");
    }
  }
}