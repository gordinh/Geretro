package br.uefs.redes.pbl4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * Classe responsável por gerenciar as conexões
 * 
 * Nesta classe, os métodos connect e accept são feitos para 2 sockets chamados
 * de first e last;
 * 
 * Cada par tem 1 conexão para outro par.
 * 
 * 
 * @author Gordinh e Sillas
 * 
 */
public class ConnectionHandler {

	private static int PORT = 8887; // Porta de conexão

	private static ServerSocket serverSocket; // Sockets de conexão
	private static Socket firstConnection;
	private static Socket secondConnection;

	/**
	 * Conecta o primeiro socket
	 * 
	 * @param address
	 * @throws IOException
	 */
	public static void connectFirst(InetAddress address) throws IOException {
		ConnectionHandler.firstConnection = new Socket(address, PORT);
	}

	/**
	 * Conecta o segundo socket
	 * 
	 * 
	 * @param address
	 * @throws IOException
	 */
	public static void connectLast(InetAddress address) throws IOException {
		ConnectionHandler.secondConnection = new Socket(address, PORT);
	}

	public static Socket getFirst() {
		return firstConnection;
	}

	public static Socket getLast() {
		return secondConnection;
	}

	/**
	 * 
	 * Abre o primeiro socket pra escuta
	 * 
	 * @throws IOException
	 */
	public static void acceptFirst() throws IOException {
		if (serverSocket == null)
			serverSocket = new ServerSocket(PORT);
		firstConnection = serverSocket.accept();
	}

	/**
	 * Abre o segundo socket pra escuta
	 * 
	 * @throws IOException
	 */
	public static void acceptLast() throws IOException {
		if (serverSocket == null)
			serverSocket = new ServerSocket(PORT);
		secondConnection = serverSocket.accept();
	}

}
