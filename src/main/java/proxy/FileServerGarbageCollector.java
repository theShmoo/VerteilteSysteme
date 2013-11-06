package proxy;

import java.util.Set;

import model.FileServerStatusInfo;

/**
 * @author David
 */
public class FileServerGarbageCollector implements Runnable {

	private Set<FileServerStatusInfo> fileservers;
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
	public FileServerGarbageCollector(Set<FileServerStatusInfo> fileservers,
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
					for(FileServerStatusInfo f : fileservers){
						if (f.isOnline() && f.getActive() < System.currentTimeMillis()
										- fileserverTimeout) {
							System.out.println("Fileserver \"" + f.getPort()
									+ "\" out of time!");
							f.setOnline(false);
						} else{
							f.setOnline(true);
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
