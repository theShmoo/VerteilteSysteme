/**
 * 
 */
package test;

import message.Response;
import cli.Command;

/**
 * 
 * @author Astrid
 */
public class TestComponentCli {

	private TestComponent testComponent;
	
	/**
	 * Initialize a new TestComponent Command Line
	 * @param testComponent the TestComponent
	 */
	public TestComponentCli(TestComponent testComponent) {
		this.testComponent = testComponent;
	}
	
	@Command
	public void upload(String filename) {
		testComponent.uploadFiles(filename);
	}
	
	@Command 
	public void download(String filename) {
		testComponent.downloadFiles(filename);
	}
	
	@Command
	public Response subscribe(String filename, int downloadFileNr) {
		return testComponent.subscribe(filename, downloadFileNr);
	}
	
}
