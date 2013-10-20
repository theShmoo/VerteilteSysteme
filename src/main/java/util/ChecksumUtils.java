package util;

import java.io.File;

/**
 * Provides checksum related utility methods.
 */
public final class ChecksumUtils {

	private static final char SEPARATOR = '_';

	private ChecksumUtils() {
	}

	/**
	 * Generates a new checksum of for a file to be downloaded by a certain user.
	 *
	 * @param user     the name of the user able to use the checksum
	 * @param filename the unqualified name (without any path information) of the file
	 * @param version  the version of the file to download
	 * @param fileSize the size of the file
	 * @return the checksum
	 */
	public static String generateChecksum(String user, String filename, int version, long fileSize) {
		return user + SEPARATOR + filename + SEPARATOR + fileSize;
	}

	/**
	 * Checks whether the given {@code checksum} is valid.<br/>
	 * If it is not, it might have been modified by the user or a third-party to download a resource without permission.
	 *
	 * @param file     the file
	 * @param version  the version of the file to download
	 * @param user     the the name of the user able to use the checksum
	 * @param checksum the checksum to verify
	 * @return {@code true} if the checksum is valid, {@code false} otherwise
	 */
	public static boolean verifyChecksum(String user, File file, int version, String checksum) {
		String actual = generateChecksum(user, file.getName(), version, file.length());
		return actual.equals(checksum);
	}
}
