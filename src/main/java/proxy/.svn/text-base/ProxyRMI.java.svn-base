package proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import cli.Command;
import client.IClientRMI;


public class ProxyRMI implements IProxyRMI {
	private ProxyLogic pLogic;
	
	public ProxyRMI(ProxyLogic l) {
		this.pLogic = l;
	}
	
	public void init() {
		try {
			String name = pLogic.getBindingName();
			IProxyRMI object = new ProxyRMI(pLogic);
			IProxyRMI remote = (IProxyRMI) UnicastRemoteObject.exportObject(object, 0);
			Registry registry = LocateRegistry.createRegistry(pLogic.getRmiPort());
			registry.rebind(name, remote);
			pLogic.setProxyRemote(object);
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 
	}
	
	@Command
	public String readQuorum() throws RemoteException {
		return "Read-Quorum is set to " + pLogic.getStats().getNr() + ".";
	}

	@Command
	public String writeQuorum() throws RemoteException {
		return "Write-Quorum is set to " + pLogic.getStats().getNw() + ".";
	}

	@Command
	public String topThreeDownloads() throws RemoteException {
		return pLogic.getStats().getTopThreeDownloads();
	}

	@Command
	public String subscribe(String file, IClientRMI rmi, int x, String username) throws RemoteException {
		if(!pLogic.getStats().isUserOnline(username)) {
			return "You are currently not logged in!";
		}
		pLogic.getStats().addSubscription(file, rmi, username, x);
		pLogic.getStats().notifySubscribers(file);
		return "Successfully subscribed for file: " + file;
	}

	@Command
	public String getProxyPublicKey() throws RemoteException {
		String pathToPublicKey = pLogic.getKeysDirectory()+"/proxy.pub.pem";
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
		
		return result;

	}

	@Command
	public String setUserPublicKey(String username, String key)
			throws RemoteException {
		File file = new File(pLogic.getKeysDirectory() + "/" + username + "SavedByProxy.pub.pem");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(key);
			writer.close();
		} catch (IOException e) {
			return "An error occured at saving the key!";
		}
		
		return "Successfully transmitted public key of user: " + username;
	}

}
