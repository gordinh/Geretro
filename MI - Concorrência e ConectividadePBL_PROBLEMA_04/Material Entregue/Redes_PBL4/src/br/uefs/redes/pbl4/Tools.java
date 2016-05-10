package br.uefs.redes.pbl4;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {

	/**
	 * Transforma um byte array em uma string hexadecimal
	 * 
	 * @param b
	 * @return
	 */
	private static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result.toUpperCase();
	}

	/**
	 * Executa o SHA-1 sobre a entrada
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String SHA1(String input) throws NoSuchAlgorithmException {
		byte[] byteArr = input.getBytes();
		MessageDigest md = null;
		md = MessageDigest.getInstance("SHA-1");
		return Tools.byteArrayToHexString(md.digest(byteArr));
	}

}
