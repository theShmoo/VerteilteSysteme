package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

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

	/**
	 * Creates a new Temporary Directory
	 * 
	 * @param dirname
	 *            the name of the directory
	 * @return the Created Temp Directory
	 * @throws IOException
	 */
	public static File createTempDirectory(String dirname) throws IOException {
		final File temp;
		temp = new File("temp"+File.separator+dirname, Long.toString(System.nanoTime()));
		temp.mkdirs();
		temp.deleteOnExit();
		return (temp);
	}

	/**
	 * @param fileSizeKB
	 *            the size of the file in KB
	 * @param f
	 *            the directory
	 * @return the file destination
	 * @throws IOException
	 */
	public static File createRandomFile(int fileSizeKB, File f)
			throws IOException {
		f.createNewFile();
		File temp = new File(f,Long.toString(System.nanoTime())+".txt");
		temp.createNewFile();
		RandomAccessFile rand = new RandomAccessFile(temp, "rw");
		rand.setLength(fileSizeKB * 1024);
		byte[] r = new byte[fileSizeKB * 1024]; //dont fill whole file
		new Random().nextBytes(r);
		rand.writeBytes(new String(r));
		rand.close();
		temp.deleteOnExit();
		return temp;
	}

	/**
	 * Copies one file to an other destination
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 */
	public static synchronized void copyFile(File sourceFile,
			File destinationFile) {
		try {
			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			FileOutputStream fileOutputStream = new FileOutputStream(
					destinationFile);

			int bufferSize;
			byte[] bufffer = new byte[512];
			while ((bufferSize = fileInputStream.read(bufffer)) > 0) {
				fileOutputStream.write(bufffer, 0, bufferSize);
			}
			fileInputStream.close();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes Folder
	 * @param folder the folder
	 */
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

}
