package test;

import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
import org.junit.Before;
import org.junit.Test;
import util.ComponentFactory;
import util.Config;
import util.CliComponent;
import util.Util;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static util.TestUtils.*;

public class ApiTest {

	// Communication
	Map<String, CliComponent> componentMap = new HashMap<String, CliComponent>();
	CliComponent component;


	@Before
	public void setUp() throws Exception {
	}


	@Test
	public void test() throws Throwable {
		ComponentFactory factory = new ComponentFactory();

		// Looking for story files (name pattern: '<nr>_<name>') describing the simulations
		File[] list = new File("src/test/resources").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("\\d+_.*");
			}
		});
		Arrays.sort(list);

		for (File file : list) {
			String fileName = file.getName();

			URL url = getClass().getClassLoader().getResource(fileName);
			if (url == null) {
				throw new IllegalArgumentException(String.format("Resource %s not found.", fileName));
			}

			List<String> lines = readLines(url.openStream(), Charset.defaultCharset());
			System.out.println(fileName);
			for (String line : lines) {
				/*
				 * Comment
				 */
				if (line == null || line.isEmpty() || line.startsWith("#")) {
					// Intentionally do nothing
				}
				/*
				 * Shell
				 */
				else if (line.startsWith("*")) {
					String[] parts = line.split("[:\\s+]", 3);
					String instruction = parts[1];
					String terminal = parts[2];

					Map<String, Class<?>[]> args = new HashMap<String, Class<?>[]>();
					args.put("startClient", new Class<?>[]{Config.class, Shell.class});
					args.put("startProxy", new Class<?>[]{Config.class, Shell.class});
					args.put("startFileServer", new Class<?>[]{Config.class, Shell.class});

					Method method = factory.getClass().getMethod(instruction, args.get(instruction));
					if (method == null) {
						throw new IllegalArgumentException(String.format("Method '%s' not found.", instruction));
					}
					TestInputStream in = new TestInputStream();
					TestOutputStream out = new TestOutputStream(System.out);
					Shell shell = new Shell(terminal, out, in);
					Object component = method.invoke(factory, new Config(terminal), shell);

					CliComponent cliComponent = new CliComponent(component, shell, out, in);
					componentMap.put(terminal, cliComponent);
					Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);
				}
				/*
				 * CLI
				 */
				else {
					String[] parts = line.split("[:\\s+]", 2);
					component = componentMap.get(parts[0]);
					if (component == null) {
						throw new IllegalStateException(String.format(
								"Cannot find component '%s'. Please start it before using it.", parts[0]));
					}
					component.getIn().addLine(parts[1].trim());
					Thread.sleep(500);
				}
			}
			System.out.println(repeat('#', 80));
		}
	}

}
