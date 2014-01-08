/**
 * Utilities for Integrity Tasks 
 * @author group66
 */
package util;

import javax.crypto.Mac;

import org.bouncycastle.util.encoders.Base64;

public class IntegrityUtils {

	/**
	 * prepends a message with a base64 encoded hash
	 * 
	 * @param hash
	 * @param message
	 * @return prepended message
	 */
	public static String prependmessage(byte[] hash, String message) {
		message = new String(Base64.encode(hash)) + " " + message;
		return message;
	}

	/**
	 * verifies a message
	 * 
	 * @param hMac
	 * @param wholemessage
	 * @return null if an error occurred or the message without hash
	 * @throws IntegrityException 
	 */
	public static byte[] verify(String wholemessage, Mac hMac)
			throws IntegrityException {

		int index = wholemessage.indexOf(' ');

		if (index == -1) {
			System.out.println("Error message format error!\n The message: "
					+ wholemessage);
			throw new IntegrityException(
					"Error message format error!\n The message: "
							+ wholemessage);
		}

		String hashFM = wholemessage.substring(0, index);

		assert hashFM.matches("a-zA-Z0-9/+");

		// verify

		String messageWithoutHash = wholemessage.substring(index + 1);
		String hashNG = new String(Base64.encode(createHashforMessage(
				messageWithoutHash, hMac)));
		if (!hashFM.equals(hashNG)) {
			System.out.println("Message has been tempered!\n The message: "
					+ wholemessage);
			throw new IntegrityException(
					"Message has been tempered!\n The message: " + wholemessage);
		}

		return messageWithoutHash.getBytes();
	}

	/**
	 * creates a hash for a given message
	 * 
	 * @param message
	 * @param hMac
	 * @return hash
	 */
	public static byte[] createHashforMessage(String message, Mac hMac) {
		hMac.update(message.getBytes());
		byte[] hash = hMac.doFinal(message.getBytes());
		return hash;
	}

}
