package util;

import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;

public class CliComponent {
	private Object component;
	private Shell shell;
	private TestOutputStream out;
	private TestInputStream in;

	public CliComponent(Object component, Shell shell, TestOutputStream out, TestInputStream in) {
		this.component = component;
		this.shell = shell;
		this.out = out;
		this.in = in;
	}

	public Object getComponent() {
		return component;
	}

	public Shell getShell() {
		return shell;
	}

	public TestInputStream getIn() {
		return in;
	}

	public TestOutputStream getOut() {
		return out;
	}
}
