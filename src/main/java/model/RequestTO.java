package model;

import java.io.Serializable;

import message.Request;

/**
 * A Data Transfer Object for Requests
 * 
 * @author David
 */
public class RequestTO implements Serializable {
	
	private static final long serialVersionUID = -8231031376970771623L;
	private Request request;
	private RequestType type;

	/**
	 * Initialize a new Request Transfer Object
	 * 
	 * @param request
	 *            the {@link Request} object
	 * @param type
	 *            the type of the {@link Request} object
	 */
	public RequestTO(Request request, RequestType type) {
		this.request = request;
		this.type = type;
	}

	/**
	 * Returns the {@link Request} object
	 * 
	 * @return the {@link Request} object
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * Returns the Type of the {@link Request} object
	 * 
	 * @return the Type of the {@link Request} object
	 */
	public RequestType getType() {
		return type;
	}
}
