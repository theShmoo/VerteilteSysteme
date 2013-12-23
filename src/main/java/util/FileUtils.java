package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

/**
 * Utilities for file operations
 * 
 * @author David
 */
public class FileUtils {

	/**
	 * A FilenameFilter that only allows ".txt"-Files
	 */
	public static FilenameFilter TEXTFILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			String lowercaseName = name.toLowerCase();
			if (lowercaseName.endsWith(".txt")) {
				return true;
			} else {
				return false;
			}
		}
	};

	/**
	 * Get the File with the given name as byte array
	 * 
	 * @param filename
	 *            the filename of the file
	 * @param path
	 *            the path of the file
	 * @return the bytearray of the given file
	 * @throws IOException
	 */
	public synchronized static byte[] read(String path, String filename)
			throws IOException {
		File file = new File(path, filename);
		byte content[] = new byte[(int) file.length()];

		FileInputStream fin = null;
		try {
			// create FileInputStream object
			fin = new FileInputStream(file);
			// Reads up to certain bytes of data from this input stream into an
			// array of bytes.
			fin.read(content);

		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException ioe) {
			System.out.println("Exception while reading file " + ioe);
		} finally {
			// close the streams using close method
			try {
				if (fin != null) {
					fin.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}

		return content;
	}

	/**
	 * Writes a byte array in a file
	 * 
	 * @param content
	 *            the content of the file
	 * @param path
	 *            the pathname of the file
	 * @param filename
	 *            the filename
	 */
	public synchronized static void write(byte[] content, String path,
			String filename) {
		File file = new File(path, filename);
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
			stream.write(content);
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while writing file " + ioe);
		}

		finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}

		}
	}

	/**
	 * Returns if the file exists
	 * 
	 * @param path
	 *            the Path of the file
	 * @param filename
	 *            the filename
	 * @return <code>true</code> if the file exists
	 */
	public static boolean check(String path, String filename) {
		File file = new File(path, filename);
		return file.isFile();
	}

	
}
