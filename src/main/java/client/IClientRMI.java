package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientRMI extends Remote {
	void receiveSubscriptionAltert(String re) throws RemoteException;
}
