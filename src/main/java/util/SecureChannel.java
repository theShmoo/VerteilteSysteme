package util;

/**
 * 
 * @author David
 */
public interface SecureChannel {

	/**
	 * Set the AES key
	 * @param key the aESkey to set
	 */
	public void setKey(byte[] key);
	
	/**
	 * Set the IV for the AES encryption
	 * @param iV the iV to set
	 */
	public void setIV(byte[] iV);
	
	/**
	 * Activate Security
	 */
	public void activateSecureConnection();
	
	/**
	 * Deactivate Security
	 */
	public void deactivateSecureConnection();
	
}
