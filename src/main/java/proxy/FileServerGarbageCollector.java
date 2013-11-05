package proxy;

import java.util.Map;

import model.FileServerInfo;

/**
 * @author David
 */
public class FileServerGarbageCollector implements Runnable {

	private Map<FileServerInfo, Long> fileservers;
	private long fileserverTimeout;
	private long checkPeriod;
	private boolean running;

	/**
	 * Initialize a new File Server Garbage Collector that sets a fileserver to
	 * offline if it has not sent an UDP isAlive package in the given time
	 * intervall
	 * 
	 * @param fileservers
	 *            the fileservers to check
	 * @param fileserverTimeout
	 *            the intervall
	 * @param checkPeriod
	 *            the period to check
	 */
	public FileServerGarbageCollector(Map<FileServerInfo, Long> fileservers,
			long fileserverTimeout, long checkPeriod) {
		this.fileservers = fileservers;
		this.fileserverTimeout = fileserverTimeout;
		this.checkPeriod = checkPeriod;
		this.running = true;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(checkPeriod);
				synchronized (this) {
					for (FileServerInfo f : fileservers.keySet()) {
						if (f.isOnline()
								&& fileservers.get(f) < System
										.currentTimeMillis()
										- fileserverTimeout) {
							System.out.println("Fileserver \"" + f.getPort()
									+ "\" out of time!");
							fileservers.remove(f);
							fileservers.put(new FileServerInfo(f.getAddress(),
									f.getPort(), f.getUsage(), false), 0l);
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Closes all resources
	 */
	public void close() {
		running = false;
	}

}
