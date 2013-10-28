package model;

/**
 * This Class represents a file with its name, its
 * 
 * @author David
 */
public class FileInfo {

	private int version;
	private String filename;
	private long filesize;

	/**
	 * Initializes a new FileInfo
	 * 
	 * @param version
	 *            the version number of the file
	 * @param filename
	 *            the name of the file
	 * @param filesize
	 *            the size of the file
	 * 
	 */
	public FileInfo(int version, String filename, long filesize) {
		this.version = version;
		this.filename = filename;
		this.filesize = filesize;
	}

	/**
	 * Initializes a new FileInfo
	 * 
	 * @param filename
	 *            the name of the file
	 * @param filesize
	 *            the size of the file
	 * 
	 */
	public FileInfo(String filename, long filesize) {
		this.version = 0;
		this.filename = filename;
		this.filesize = filesize;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the filesize
	 */
	public long getFilesize() {
		return filesize;
	}

	/**
	 * @param filesize
	 *            the filesize to set
	 */
	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Increases the version number by one
	 */
	public void increaseVersionNumber() {
		version++;
	}

	/**
	 * Resets the version number to 0
	 */
	public void resetVersionNumber() {
		version = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileInfo other = (FileInfo) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		return true;
	}

}
