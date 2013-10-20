package proxy;

import cli.Command;
import message.Response;
import message.response.MessageResponse;

import java.io.IOException;

/**
 * This interface defines the functionality for the proxy.
 */
public interface IProxyCli {
	/**
	 * Prints out some information about each known file server, online or offline.<br/>
	 * A file server is known if it has sent a single <i>isAlive</i> packet since the Proxy's last startup.
	 * The information shall contain the file server's IP, TCP port, online status (online/offline) and usage.
	 * E.g.:
	 * <pre>
	 * &gt; !fileservers
	 * 1. IP:127.0.0.1 Port:10000 offline Usage: 752
	 * 2. IP:127.0.0.2 Port:10000 online Usage: 220
	 * </pre>
	 *
	 * @return a {@link message.response.FileServerInfoResponse FileServerInfoResponse} with monitoring information<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	@Command
	Response fileservers() throws IOException;

	/**
	 * Prints out some information about each user, containing username, login status (online/offline) and credits.<br/>
	 * E.g.:
	 * <pre>
	 * &gt; !users
	 * 1. alice online Credits: 200
	 * 2. bill offline Credits: 180
	 * </pre>
	 *
	 * @return a {@link message.response.UserInfoResponse UserInfoResponse} containing user information<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	@Command
	Response users() throws IOException;

	/**
	 * Performs a shutdown of the proxy and release all resources.<br/>
	 * Shutting down an already terminated proxy has no effect.
	 * <p/>
	 * Do not forget to logout each logged in user.
	 * <p/>
	 * Note that as long as there is any non-daemon thread alive, the application won't shut down, so you need to stop
	 * them. Therefore call {@link java.net.ServerSocket#close()}, which will throw a {@link java.net.SocketException}
	 * in the thread blocked in {@link java.net.ServerSocket#accept()}, and {@link java.net.DatagramSocket#close()},
	 * which will throw a {@link java.net.SocketException} in the thread blocked in
	 * {@link java.net.DatagramSocket#receive(java.net.DatagramPacket)}.<br/>
	 * All other threads currently alive should simply run out.<br/>
	 * If you are using an {@link java.util.concurrent.ExecutorService} you have to call its
	 * {@link java.util.concurrent.ExecutorService#shutdown()} method and in case of a {@link java.util.Timer Timer},
	 * call {@link java.util.Timer#cancel()}.
	 * <p/>
	 * <b>Anyway you may not call {@link java.lang.System#exit(int)}, instead free all acquired resources orderly.</b>
	 * <p/>
	 * E.g.:
	 * <pre>
	 * &gt; !exit
	 * Shutting down proxy now
	 * </pre>
	 *
	 * @return any message indicating that the proxy is going to terminate
	 * @throws IOException if an I/O error occurs
	 */
	@Command
	MessageResponse exit() throws IOException;
}
