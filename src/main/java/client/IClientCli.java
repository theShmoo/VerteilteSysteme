package client;

import message.Response;
import message.response.*;

import java.io.IOException;

/**
 * This interface defines the functionality for the client.
 */
public interface IClientCli {

	/**
	 * Authenticates the client with the provided username and password.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !login &lt;username&gt; &lt;password&gt;}<br/>
	 * <b>Response:</b><br/>
	 * {@code !login success}<br/>
	 * or<br/>
	 * {@code !login wrong_credentials}
	 *
	 * @param username the name of the user
	 * @param password the password
	 * @return status whether the authentication was successful or not
	 * @throws IOException if an I/O error occurs
	 */
	LoginResponse login(String username, String password) throws IOException;

	/**
	 * Retrieves the current amount of credits of the authenticated user.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !credits}<br/>
	 * <b>Response:</b><br/>
	 * {@code !credits &lt;total_credits&gt;}<br/>
	 *
	 * @return a {@link message.response.CreditsResponse CreditsResponse} containing the amount of credits<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response credits() throws IOException;

	/**
	 * Buys additional credits for the authenticated user.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !buy &lt;amount&gt;}<br/>
	 * <b>Response:</b><br/>
	 * {@code !credits &lt;total_credits&gt;}<br/>
	 *
	 * @param credits the amount of credits to buy
	 * @return a {@link message.response.BuyResponse BuyResponse} containing the current amount of credits<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response buy(long credits) throws IOException;

	/**
	 * Lists all files available on all file servers.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !list}<br/>
	 * <b>Response:</b><br/>
	 * {@code No files found.}<br/>
	 * or<br/>
	 * {@code &lt;filename1&gt;}<br/>
	 * {@code &lt;filename2&gt;}<br/>
	 * {@code ...}<br/>
	 *
	 * @return a {@link message.response.ListResponse ListResponse} containing file names (can be empty)<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response list() throws IOException;

	/**
	 * Downloads the file with the given name (if possible).
	 * <p/>
	 * <b>Request (client to proxy)</b>:<br/>
	 * {@code !download &lt;filename&gt;}<br/>
	 * <b>Response (proxy to client):</b><br/>
	 * {@code !download &lt;ticket&gt;}<br/>
	 * <b>Request (client to server)</b>:<br/>
	 * {@code !download &lt;ticket&gt;}<br/>
	 * <b>Response (server to client):</b><br/>
	 * {@code !data &lt;content&gt;}<br/>
	 *
	 * @param filename the name of the file to download
	 * @return a {@link message.response.DownloadFileResponse DownloadFileResponse} containing the content<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 * @see model.DownloadTicket
	 */
	Response download(String filename) throws IOException;

	/**
	 * Uploads the file with the given name.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !upload &lt;filename&gt;}<br/>
	 * <b>Response:</b><br/>
	 * {@code !upload &lt;message&gt;}<br/>
	 *
	 * @param filename the name of the file to upload from the client's shared folder
	 * @return message stating whether the upload was successful
	 * @throws IOException if an I/O error occurs
	 */
	MessageResponse upload(String filename) throws IOException;

	/**
	 * Performs a logout if necessary and closes open connections between client and proxy.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !logout}<br/>
	 * <b>Response:</b><br/>
	 * {@code !logout &lt;message&gt;}<br/>
	 *
	 * @return message stating whether the logout was successful
	 * @throws IOException if an I/O error occurs
	 */
	MessageResponse logout() throws IOException;

	/**
	 * Performs a shutdown of the client and release all resources.<br/>
	 * Shutting down an already terminated client has no effect.
	 * <p/>
	 * Logout the user if necessary and be sure to release all resources, stop all threads and close any open sockets.
	 * <p/>
	 * E.g.:
	 * <pre>
	 * &gt; !exit
	 * Shutting down client now
	 * </pre>
	 *
	 * @return exit message
	 * @throws IOException if an I/O error occurs
	 */
	MessageResponse exit() throws IOException;
}
