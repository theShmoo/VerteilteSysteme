package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.LoginException;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.PasswordFinder;

/**
 * Utilities for Security Tasks
 */
public class SecurityUtils {
	/**
	 * Returns the public key at the given position
	 * 
	 * @param pathToPublicKey
	 * @return the public key at the given position
	 */
	public static PublicKey readPublicKey(String pathToPublicKey) {
		PublicKey publicKey = null;
		PEMReader in = null;
		try {
			in = new PEMReader(new FileReader(pathToPublicKey));
			publicKey = (PublicKey) in.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while writing file " + ioe);
		} finally {
			// close the streams using close method
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}

		return publicKey;
	}

	/**
	 * Returns the private key at the given position
	 * 
	 * @param pathToPrivateKey
	 * @param password
	 *            the password of the private key
	 * @return the private key at the given position
	 * @throws LoginException if the key is not valid
	 */
	public static PrivateKey readPrivateKey(String pathToPrivateKey,
			final String password) throws LoginException{
		System.out.println("get private key of "+pathToPrivateKey);
		PrivateKey privateKey = null;
		PEMReader in = null;
		try {
			in = new PEMReader(new FileReader(pathToPrivateKey),
					new PasswordFinder() {
				@Override
				public char[] getPassword() {
					return password.toCharArray();
				}
			});
			KeyPair keyPair = (KeyPair) in.readObject();
			privateKey = keyPair.getPrivate();
		} catch (FileNotFoundException e) {
			throw new LoginException("The username is not registered!");
		} catch (IOException ioe) {
			throw new LoginException("The username or password is wrong!");
		} finally {
			// close the streams using close method
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}

		return privateKey;
	}

	/**
	 * Combines Arrays arrays to c
	 * 
	 * @param arrays
	 *            the arrays to combine
	 * 
	 * @return an array that is a combination of arrays
	 */
	public static byte[] combineByteArrays(byte[]... arrays) {

		int l = 0;
		for (byte[] b : arrays) {
			l += b.length;
		}

		byte[] c = new byte[l];
		int pos = 0;
		for (byte[] b : arrays) {
			System.arraycopy(b, 0, c, pos, b.length);
			pos += b.length;
		}
		return c;
	}

	/**
	 * Encrypt the base64 message with the key
	 * 
	 * @param key
	 *            a Key (public or private)
	 * @param message
	 *            encoded with base64 message
	 * @return the encrypted message. this message is not base 64 encoded
	 */
	public static byte[] encrypt(Key key, byte[] message) {
		// Encrypt the base64 message with the key
		byte[] encryptedMessage = null;
		try {
			Cipher crypt = Cipher
					.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			crypt.init(Cipher.ENCRYPT_MODE, key);
			encryptedMessage = crypt.doFinal(message);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encryptedMessage;
	}

	/**
	 * Encrypt the base64 message with the key with the AES algorithm
	 * 
	 * @param key
	 * @param IV 
	 * @param data
	 * @return the encrypted message
	 */
	public static byte[] encrypt(byte[] key, byte[] IV, byte[] data) {
		// Decrypt the base64 message with the key
		byte[] decryptedMessage = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
			SecretKeySpec secret = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secret,new IvParameterSpec(IV));
			decryptedMessage = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return decryptedMessage;
	}

	/**
	 * Decrypt the base64 message with the key
	 * 
	 * @param key
	 *            a Key (public or private)
	 * @param message
	 *            the encrypted message.
	 * @return the decrypted message.
	 */
	public static byte[] decrypt(Key key, byte[] message) {
		// Decrypt the base64 message with the key
		byte[] decryptedMessage = null;
		try {
			Cipher crypt = Cipher
					.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
			crypt.init(Cipher.DECRYPT_MODE, key);
			decryptedMessage = crypt.doFinal(message);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decryptedMessage;
	}

	/**
	 * Decrypt the base64 message with the key with the AES algorithm
	 * 
	 * @param key
	 * @param IV 
	 * @param data
	 * @return the decrypted message
	 */
	public static byte[] decrypt(byte[] key, byte[] IV, byte[] data) {
		// Decrypt the base64 message with the key
		byte[] decryptedMessage = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
			SecretKeySpec secret = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.DECRYPT_MODE, secret,new IvParameterSpec(IV));
			decryptedMessage = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return decryptedMessage;
	}

	/**
	 * Converts an object to a byte array and encodes it with Base 64
	 * 
	 * @param obj the object to convert
	 * @return an object as a byte array
	 * @throws IOException
	 */
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		byte[] byteArray = out.toByteArray();
		return byteArray;
	}

	/**
	 * Converts a byte array to an object
	 * 
	 * @param data the serialized object as a byte array
	 * @return the serialized object
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		try{
			return is.readObject();
		} catch (EOFException e){
			// thats ok! 
		}
		return null;
	}

	/**
	 * Stores the public key to the given directory 
	 * 
	 * @param publicKey the public key
	 * @param pathToPublicKey the path, where the public key should be stored
	 */
	public static boolean storePublicKey(PublicKey publicKey, String pathToPublicKey) {
		PEMWriter out = null;
		
		try {
			out = new PEMWriter(new FileWriter(pathToPublicKey));
			out.writeObject(publicKey);
		} catch (IOException ioe) {
			System.out.println("Exception while writing file " + ioe);
			return false;
		} finally {
			// close the streams using close method
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}
		return true;
	}
}
