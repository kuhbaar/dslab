package util;

import cli.Shell;
import client.IClientCli;
import proxy.IProxyCli;
import server.IFileServerCli;
import client.*;
import server.*;
import proxy.*;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.ConnectException;
import java.net.InetAddress;

/**
 * Provides methods for starting an arbitrary amount of various components.
 */
public class ComponentFactory {
	/**
	 * Creates and starts a new client instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IClientCli startClient(Config config, Shell shell) throws Exception {
		try{
		ShellThread st = new ShellThread(shell);
		ClientLogic logic = new ClientLogic(config, st, new Config("mc"));
		Socket sclient = new Socket(InetAddress.getByName(logic.getProxyHost()), logic.getProxyTcp());
		ClientThread ct = new ClientThread(sclient, logic);
     
    ct.start();

    logic.getShell().register(ct);
    st.start();

		return ct;
		} catch (ConnectException e){
      System.out.println("No Proxy Running");
    }
    return null;
	}

	/**
	 * Creates and starts a new proxy instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IProxyCli startProxy(Config config, Shell shell) throws Exception {
		try{
			ShellThread	st = new ShellThread(shell);
			ProxyLogic logic = new ProxyLogic(config,new Config("mc"), st);
			logic.initUserStats();	//Reads the user's data into memory
			ProxyCommands cli = new ProxyCommands(logic);
			logic.getShell().register(cli);

			ServerSocket ssocket = new ServerSocket(logic.getTcp());
			DatagramSocket dsocket = new DatagramSocket(logic.getUdp());
			TcpThread listening = new TcpThread(ssocket, logic.getStats(), logic);
			UdpThread isalive = new UdpThread(dsocket, logic.getCheckInt(), logic.getStats());
			logic.setUdpThread(isalive);
			logic.setTcpThread(listening);

			listening.start();

			isalive.start();
			st.start();			
		return cli;
				} catch (IllegalArgumentException e){
      System.out.println("Wrong Command\nPossible commands: !fileservers, !users, !exit");
    } catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates and starts a new file server instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IFileServerCli startFileServer(Config config, Shell shell) throws Exception {
		ShellThread st = new ShellThread(shell);
		FileServerLogic logic1 = new FileServerLogic(config, st);
    logic1.initFilesList();
    logic1.setShell(shell);
    FileServerCommands cli = new FileServerCommands(logic1);
    logic1.getShell().register(cli);
    try{
      ServerSocket fs1socket = new ServerSocket(logic1.getTcp());
      DatagramSocket fs1aliveSocket = new DatagramSocket();
      FsThread fs1 = new FsThread(fs1socket, logic1);
      FsAliveThread fsa1 = new FsAliveThread(fs1aliveSocket, logic1.getProxyHost(), logic1.getProxyUdp(), logic1.getAliveInt(), logic1.getTcp());
      logic1.setFsThread(fs1);
      logic1.setAliveThread(fsa1);
      fs1.start();
      fsa1.start();

      st.start();
		return cli;
		    } catch (IllegalArgumentException e){
      System.out.println("Wrong Command\nPossible commands: !exit");
    } catch (Exception e) {
    	e.printStackTrace();
      System.out.println("An Error (FS main)");
    }
    return null;
	}
}
