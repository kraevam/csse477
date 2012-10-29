/*
 * ConnectionManager.java
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
 
package server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ConnectionManager {
	private Server server;
	private ExecutorService executor;
	private List<ConnectionHandler> allConnections;

	public ConnectionManager(Server server) {
		this.server = server;
		this.executor = Executors.newCachedThreadPool();
		this.allConnections = new ArrayList<ConnectionHandler>();
	}
	
	public void startHandler(Socket connectionSocket) {
		ConnectionHandler handler = new ConnectionHandler(server, connectionSocket);
		//new Thread(handler).start();
		executor.execute(handler);
		this.allConnections.add(handler);
	}
}
