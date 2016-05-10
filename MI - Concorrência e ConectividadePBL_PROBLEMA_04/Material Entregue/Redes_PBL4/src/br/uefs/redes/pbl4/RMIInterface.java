package br.uefs.redes.pbl4;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote{
	String setVelocity(double new_velocity) throws RemoteException;
}
