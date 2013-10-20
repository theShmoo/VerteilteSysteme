package example;

import util.Config;
import util.TestUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.MissingResourceException;

/**
 * Contains a small showcase of how to use {@link Config}.
 */
public class ConfigExample {

	static {
		/*
		 * Note that the following code block just demonstrates that the configuration file resides in the classpath.
		 * You do not have to deal with ClassLoaders nor retrieve it via getResource() or any other method.
		 * Instead, please use the Config class as used in the main() method below.
		 */
		try {
			URL url = ClassLoader.getSystemClassLoader().getResource("client.properties");
			assert url != null : "URL must not be null.";
			System.out.printf("The configuration file used in this example is loaded from '%s'%n", new File(url.toURI()).getAbsolutePath());
			System.out.println(TestUtils.repeat('=', 80));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String... args) {
		/*
		 * Loads the file client.properties from the classpath.
		 * It is located within the directory src/main/resources.
		 */
		Config config = new Config("client");

		/*
		 * Retrieve the String value associated with the 'download.dir' key
		 * and print the absolute path of the directory.
		 */
		String downloadPath = config.getString("download.dir");
		File downloadDir = new File(downloadPath);
		System.out.println("Download directory: " + downloadDir.getAbsoluteFile());

		/*
		 * Retrieve host and port of the proxy component to connect to.
		 * Note that port is an Integer and parsed automatically.
		 * If it does not contain a parsable integer, a NumberFormatException is thrown.
		 */
		String proxyHost = config.getString("proxy.host");
		int proxyPort = config.getInt("proxy.tcp.port");
		System.out.printf("ClientCli connects to Proxy at '%s:%d'%n", proxyHost, proxyPort);
		System.out.println(TestUtils.repeat('=', 80));


		/*
		 * Furthermore, if the file is invalid i.e., a value is not defined, a MissingResourceException is thrown.
		 */
		String key = "undefined";
		try {
			System.out.printf("%s = %s%n", key, config.getString(key));
		} catch (MissingResourceException e) {
			System.out.printf("Key '%s' is not defined. Thus a MissingResourceException was thrown.%n", key);
		}


		/*
		 * The same is true for a file, which is not located within the classpath.
		 */
		try {
			new Config("abc");
		} catch (Exception e) {
			System.out.printf(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
