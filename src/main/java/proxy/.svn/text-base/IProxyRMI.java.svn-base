package proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;

import cli.Command;
import client.IClientRMI;


public interface IProxyRMI extends Remote {
	/**
	 * This command returns the number of Read-Quorums that are currently used for the replication mechanism. 
	 * This command does not require the user to be logged in!
	 * @return 
	 * @throws RemoteException
	 */
	@Command
	String readQuorum() throws RemoteException; 
	
	/**
	 * This command returns the number of Write-Quorums that are currently used for the replication mechanism. 
	 * This command does not require the user to be logged in!
	 * @return 
	 * @throws RemoteException
	 */
	@Command
	String writeQuorum() throws RemoteException;
	
	/**
	 * This command retrieves a sorted list that contains the 3 files that got downloaded the most. Where the first file in the list, represents the file that got downloaded the most.
	 * This command does not require the user to be logged in!
	 * @return
	 * @throws RemoteException
	 */
	@Command
	String topThreeDownloads() throws RemoteException;
	@Command
	String subscribe(String file, IClientRMI rmi, int x, String username) throws RemoteException;
	@Command
	String getProxyPublicKey() throws RemoteException;
	@Command
	String setUserPublicKey(String a, String b) throws RemoteException;
}
