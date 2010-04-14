package org.vulpe.security.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author <a href="mailto:felipe.matos@activethread.com.br">Felipe Matos</a>
 *
 */
public final class VulpeCriptoUtils {

	private VulpeCriptoUtils() {
	}

	private static final String HEX_DIGITS = "0123456789abcdef";

	/**
	 * Realiza um digest em um array de bytes atrav�s do algoritmo especificado
	 *
	 * @param input
	 *            - O array de bytes a ser criptografado
	 * @param algorithm
	 *            - O algoritmo a ser utilizado
	 * @return byte[] - O resultado da criptografia
	 * @throws NoSuchAlgorithmException
	 *             - Caso o algoritmo fornecido n�o seja v�lido
	 */
	public static byte[] digest(byte[] input, final String algorithm)
			throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		digest.reset();
		return digest.digest(input);
	}

	/**
	 * Converte o array de bytes em uma representa��o hexadecimal.
	 *
	 * @param input
	 *            - O array de bytes a ser convertido.
	 * @return Uma String com a representa��o hexa do array
	 */
	public static String byteArrayToHexString(final byte[] bytes) {
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < bytes.length; i++) {
			final int index = ((int) bytes[i]) & 0xFF;
			buffer.append(HEX_DIGITS.charAt(index / 16));
			buffer.append(HEX_DIGITS.charAt(index % 16));
		}

		return buffer.toString();
	}

	/**
	 * Converte uma String hexa no array de bytes correspondente.
	 *
	 * @param hexa
	 *            - A String hexa
	 * @return O vetor de bytes
	 * @throws IllegalArgumentException
	 *             - Caso a String n�o seja uma representa��o hexadecimal v�lida
	 */
	public static byte[] hexStringToByteArray(final String hexa)
			throws IllegalArgumentException {

		// verifica se a String possui uma quantidade par de elementos
		if (hexa.length() % 2 != 0) {
			throw new IllegalArgumentException("String hexa inv�lida");
		}

		final byte[] bytes = new byte[hexa.length() / 2];

		for (int i = 0; i < hexa.length(); i += 2) {
			bytes[i / 2] = (byte) ((HEX_DIGITS.indexOf(hexa.charAt(i)) << 4) | (HEX_DIGITS
					.indexOf(hexa.charAt(i + 1))));
		}
		return bytes;
	}
}