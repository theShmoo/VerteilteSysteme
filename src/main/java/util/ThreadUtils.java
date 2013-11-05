/**
 * 
 */
package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author David
 */
public class ThreadUtils {

	/**
	 * A CachedThreadPool
	 * 
	 * @see ExecutorService
	 */
	private static ExecutorService executor = null;

	/**
	 * Returns the CachedThreadPool
	 * 
	 * @return the CachedThreadPool
	 */
	public static ExecutorService getExecutor() {
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}
		return executor;
	}

}
