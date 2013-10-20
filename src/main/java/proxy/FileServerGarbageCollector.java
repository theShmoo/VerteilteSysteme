package proxy;

import java.util.Map;

import model.FileServerInfo;

public class FileServerGarbageCollector implements Runnable {

	private Map<FileServerInfo, Long> fileservers;
	private long fileserverTimeout;
	private long checkPeriod;
	private boolean running;

	public FileServerGarbageCollector(Map<FileServerInfo, Long> fileservers,
			long fileserverTimeout, long checkPeriod) {
		this.fileservers = fileservers;
		this.fileserverTimeout = fileserverTimeout;
		this.checkPeriod = checkPeriod;
		this.running = true;
	}

	@Override
	public void run() {
		while(running){
			try {
				Thread.sleep(checkPeriod);
				for(Long f : fileservers.values()){
					if(f < System.currentTimeMillis()-fileserverTimeout){
						System.out.println("Fileserver out of time!");
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
