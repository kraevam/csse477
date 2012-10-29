/*
 * ConnectionHandler.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
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
 */
 
package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.RequestManager;
import protocol.ResponseManager;

/**
 * This class is responsible for handling a incoming request
 * by creating a {@link HttpRequest} object and sending the appropriate
 * response be creating a {@link HttpResponse} object. It implements
 * {@link Runnable} to be used in multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements Runnable {
	private Server server;
	private Socket socket;
	private RequestManager reqManager;
	private ResponseManager resManager;
	private InputStream inStream;
	private OutputStream outStream;
	
	public ConnectionHandler(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		if (setupStreams()) {
			this.reqManager = new RequestManager(this, this.inStream);
			this.resManager = new ResponseManager(this.outStream);
		}
	}
	
	private boolean setupStreams() {
		try {
			inStream = this.socket.getInputStream();
			outStream = this.socket.getOutputStream();
		}
		catch(Exception e) {
			// Cannot do anything if we have exception reading input or output stream
			// May be have text to log this for further analysis?
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}


	/**
	 * The entry point for connection handler. It first parses
	 * incoming request and creates a {@link HttpRequest} object,
	 * then it creates an appropriate {@link HttpResponse} object
	 * and sends the response back to the client (web browser).
	 */
	public void run() {
		// Get the start time
		long start = System.currentTimeMillis();

		// Increment number of connections by 1
		server.incrementConnections(1);
		
		new Thread(this.reqManager).start();
		
		while (!this.reqManager.isOverloaded()) {
			HttpRequest request = this.reqManager.getNextRequest();
			
			if (request == null) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (!this.reqManager.isServiceable()) {
				this.resManager.addBadResponse(Protocol.SERVICE_UNAVAILABLE);
			} else {
				this.resManager.respond(request);
			}
		}
		
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get the end time
		long end = System.currentTimeMillis();
		this.server.incrementServiceTime(end-start);
	}
	
	public void addBadResponse(int status) {
		this.resManager.addBadResponse(status);
	}
}
