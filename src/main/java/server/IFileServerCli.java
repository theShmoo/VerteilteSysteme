package server;

import cli.Command;
import message.response.MessageResponse;

import java.io.IOException;

/**
 * This interface defines the functionality for the file server.
 */
public interface IFileServerCli {
	/**
	 * Performs a shutdown of the file server and release all resources.<br/>
	 * Shutting down an already terminated file server has no effect.
	 * <p/>
	 * Note that as long as there is any non-daemon thread alive, the application won't shut down, so you need to stop
	 * them. Therefore call {@link java.net.ServerSocket#close()}, which will throw a {@link java.net.SocketException}
	 * in the thread blocked in {@link java.net.ServerSocket#accept()}, and {@link java.net.DatagramSocket#close()},
	 * which will throw a {@link java.net.SocketException} in the thread blocked in
	 * {@link java.net.DatagramSocket#receive(java.net.DatagramPacket)}.<br/>
	 * All other threads currently alive should simply run out.<br/>
	 * If you are using an {@link java.util.concurrent.ExecutorService} you have to call its
	 * {@link java.util.concurrent.ExecutorService#shutdown()} method and in case of a {@link java.util.Timer},
	 * call {@link java.util.Timer#cancel()}.
	 * <p/>
	 * <b>Anyway you may not call {@link System#exit(int)}, instead free all acquired resources orderly.</b>
	 * <p/>
	 * E.g.:
	 * <pre>
	 * &gt; !exit
	 * Shutting down file server now
	 * </pre>
	 *
	 * @return any message indicating that the file server is going to terminate
	 * @throws IOException if an I/O error occurs
	 */
	@Command
	MessageResponse exit() throws IOException;
}
