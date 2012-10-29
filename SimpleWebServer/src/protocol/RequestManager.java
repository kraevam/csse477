/*
 * RequestManager.java
 * Oct 22, 2012
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */
 
package protocol;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import server.ConnectionHandler;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class RequestManager implements Runnable {
	private ConnectionHandler handler;
	private Queue<HttpRequest> queue;
	private InputStream in;
	private boolean servicable = true;
	private boolean overloaded = false;

	private static final int MAXSERVICABLE = 100;
	private static final int MAXALLOWED = 150;
	
	public RequestManager(ConnectionHandler handler, InputStream in) {
		this.handler = handler;
		this.queue = new LinkedList<HttpRequest>();
		this.in = in;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// At this point we have the input and output stream of the socket
		// Now lets create a HttpRequest object
		HttpRequest request;
		
		while (this.queue.size() < MAXALLOWED) {
			try {
				request = HttpRequest.read(in);
				this.addRequest(request);
				
			} catch (ProtocolException pe) {
				int status = pe.getStatus();
				this.handler.addBadResponse(status);
			} catch (Exception e) {
				e.printStackTrace();
				// For any other error, we will create bad request response
				this.handler.addBadResponse(Protocol.BAD_REQUEST_CODE);
			}
		}
		this.overloaded  = true;
	}
	
	public boolean isServiceable() {
		return this.servicable;
	}
	
	public boolean isOverloaded() {
		return this.overloaded;
	}
	
	public void addRequest(HttpRequest request) {
		if (this.overloaded) {
			this.queue.add(request);
			this.overloaded = true;
		}

		if(this.queue.size() > MAXSERVICABLE) {
			this.servicable = false;
		}
		
	}
	
	public HttpRequest getNextRequest() {
		if (this.queue.isEmpty()) return null;
		
		return this.queue.remove();
	}
}
