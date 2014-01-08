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
	
	public TestComponentCli(TestComponent testComponent) {
		this.testComponent = testComponent;
	}
	
	@Command
	public Response subscribe(String filename, int downloadFileNr) {
		//TODO
		return null;
	}
	
	@Command 
	public Response list() {
		return testComponent.list();
	}
}
