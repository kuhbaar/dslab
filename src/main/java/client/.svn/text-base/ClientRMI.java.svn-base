package client;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import cli.Command;

import proxy.IProxyRMI;

public class ClientRMI implements IClientRMI {
	/**
	 * 
	 */
	private ClientLogic logic;
	private Registry registry;
	private IProxyRMI remote;
	private IClientRMI remoteCliRMI = null;
	
	
	public ClientRMI(ClientLogic l) {
		this.logic = l;
	}
	
	public void init() {
		try {
			registry = LocateRegistry.getRegistry(logic.getRmiHost(), logic.getRmiPort());
			remote = (IProxyRMI)registry.lookup(logic.getBindingName());
			logic.setCliRMI(this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void receiveSubscriptionAltert(String re) throws RemoteException {
		try {
			logic.getShell().writeLine(re);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Command
	public String readQuorum() throws RemoteException {
		return remote.readQuorum();
	}
	
	@Command
	public String writeQuorum() throws RemoteException {
		return remote.writeQuorum();
	}
	
	@Command
	public String topThreeDownloads() throws RemoteException {
		return remote.topThreeDownloads();
	}
	
	@Command
	public String subscribe(String filename, int numberOfDownloads) throws RemoteException {
		if(this.remoteCliRMI == null) {
			remoteCliRMI = new ClientRMI(logic);
			//remoteCliRMI = (IClientRMI)UnicastRemoteObject.exportObject(new ClientRMI(logic), 0);
			UnicastRemoteObject.exportObject(remoteCliRMI, 0);
		}
		return remote.subscribe(filename, remoteCliRMI, numberOfDownloads, logic.getUsername());
	}
	
	@Command
	public String getProxyPublicKey() throws RemoteException {
		String key = remote.getProxyPublicKey();
		File file = new File(logic.getKeysDirectory() + "/proxySavedByClient.pub.pem");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(key);
			writer.close();
		} catch (IOException e) {
			return "An error occured at saving the key!";
		}
		
		return "Successfully received public key of Proxy.";
		
	}
	/**
	 * 
	 * @param username case-sensitive username of the user whose key should be transmitted.
	 * @return Success-Message
	 * @throws RemoteException
	 */
	@Command
	public String setUserPublicKey(String username) throws RemoteException {
		String pathToPublicKey = logic.getKeysDirectory()+"/" + username + ".pub.pem";
		System.out.println(pathToPublicKey);
		String result = "";
		File file = new File(pathToPublicKey);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = reader.readLine()) != null) {
				line += System.getProperty("line.separator");
				result+=line;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			return "File not found";
		} catch (IOException e) {
			return "Error at reading file";
		}
		return remote.setUserPublicKey(username, result);
	}
	
	
	public IProxyRMI getProxyRMI() {
		return this.remote;
	}
	
	public void shutdownClientRMI() {
		if(remoteCliRMI != null) {
			try {
				UnicastRemoteObject.unexportObject(remoteCliRMI, true);
				remoteCliRMI = null;
			} catch (NoSuchObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 

}
