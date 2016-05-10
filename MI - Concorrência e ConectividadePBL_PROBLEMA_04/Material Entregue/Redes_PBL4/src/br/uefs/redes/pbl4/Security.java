package br.uefs.redes.pbl4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.UUID;

/**
 * Classe responsável por prover a camada de segurança Contém os métodos de
 * troca de chaves e validação da senha
 * 
 * 
 * @author Gordinh
 * 
 */
public class Security {

	public static PublicKey firstKey; // Chaves públicas dos pares
	public static PublicKey lastKey;

	public static String symKey = ""; // Chave simétrica da criptografia

	/**
	 * 
	 * Envia a requisição da chave pública pelo canal inseguro
	 * 
	 * @param endPoint
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static PublicKey requestPublicKey(Socket endPoint) //
			throws IOException, ClassNotFoundException {
		ConnectIO insecure_channel = new ConnectIO(endPoint);

		insecure_channel.post("BEGIN_KEY_EXCHANGE:");
		if (insecure_channel.get().startsWith("KEY_READY:")) {
			ObjectInputStream keyInputStream = new ObjectInputStream(
					endPoint.getInputStream());
			return (PublicKey) keyInputStream.readObject(); // Recebe a chave
		}

		return null;
	}

	/**
	 * Envia a chave pública pelo canal inseguro
	 * 
	 * 
	 * @param endPoint
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void sendPublicKey(Socket endPoint) throws IOException,
			ClassNotFoundException {

		ConnectIO insecure_channel = new ConnectIO(endPoint);

		if (insecure_channel.get().startsWith("BEGIN_KEY_EXCHANGE")) {
			if (!RSA.areKeysPresent())
				RSA.generateKey();
			insecure_channel.post("KEY_READY:");
			ObjectOutputStream keyOutputStream = new ObjectOutputStream(
					endPoint.getOutputStream());
			keyOutputStream.writeObject(RSA.getPublicKey()); // Envia a chave
		}
	}

	/**
	 * Valida a senha de entrada O hash equivale a 12345
	 * 
	 * 
	 * @param input
	 * @return
	 */
	public static boolean validatePassword(String input) {
		String pass = "8cb2237d0679ca88db6464eac60da96345513964";
		try {
			String digest = Tools.SHA1(input); // Executa o SHA-1 sobre a
												// entrada

			if (digest.equalsIgnoreCase(pass)) // compara os hashes.
				return true;
			else
				return false;

		} catch (NoSuchAlgorithmException e2) {
			e2.printStackTrace();
			return false;
		}
	}

}
