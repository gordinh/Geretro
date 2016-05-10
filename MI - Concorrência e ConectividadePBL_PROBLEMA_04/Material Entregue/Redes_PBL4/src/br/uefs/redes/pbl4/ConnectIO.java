package br.uefs.redes.pbl4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.NotActiveException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Victor, Diego, Ênio
 * 
 *         Responsável pelo canal de comunicação dos sockets
 * 
 * 
 */
public class ConnectIO {
	BufferedReader in; // Stream de entrada
	PrintWriter out; // Stream de saída
	RC4 cypher; // Instância do algoritmo de criptografia

	/**
	 * Inicializa os streams
	 * 
	 * 
	 * @param clientEndPoint
	 * @throws IOException
	 */
	public ConnectIO(Socket clientEndPoint) throws IOException {
		in = new BufferedReader(new InputStreamReader(
				clientEndPoint.getInputStream()));

		out = new PrintWriter(clientEndPoint.getOutputStream(), true);
	}

	/**
	 * Inicializa os streams com possibilidade de criptografia
	 * 
	 * 
	 * @param clientEndPoint
	 * @param cypherKey
	 * @throws IOException
	 */
	public ConnectIO(Socket clientEndPoint, String cypherKey)
			throws IOException {
		this(clientEndPoint);
		this.cypher = new RC4(cypherKey.getBytes());
	}

	/**
	 * 
	 * Recupera uma mensagem do stream
	 * 
	 * @return
	 * @throws IOException
	 */
	public String get() throws IOException {
		return in.readLine();
	}

	/**
	 * 
	 * Envia uma mensagem ao stream
	 * 
	 * @param message
	 */
	public void post(String message) {
		out.println(message);
	}

	/**
	 * Envia uma mensagem criptografada no stream.
	 * 
	 * 
	 * @param message
	 * @throws NotActiveException
	 */
	public void postSecure(String message) throws NotActiveException {
		//System.out.println("POST -> " + message);
		if (cypher == null)
			throw new NotActiveException();
		out.println(Base64.encodeBytes(cypher.encrypt(message.getBytes())));
	}

	/**
	 * Recebe uma mensagem criptografada do stream.
	 * 
	 * @return
	 * @throws IOException
	 * @throws NotActiveException
	 */
	public String getSecure() throws IOException, NotActiveException {
		if (cypher == null)
			throw new NotActiveException();
		String out = new String(cypher.decrypt(Base64.decode(in.readLine())));
		//System.out.println("GET -> " + out);
		return out;
	}
}
