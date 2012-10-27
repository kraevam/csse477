/*
 * ResponseManager.java
 * Oct 27, 2012
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

import java.io.File;
import java.io.OutputStream;

import server.Server;

//TODO: Add more functionality to this class. (file tracking)
/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ResponseManager {
	private Server server;
	private OutputStream out;

	public ResponseManager(Server server, OutputStream out) {
		this.server = server;
		this.out = out;
	}

	/**
	 * @param request
	 */
	public void respond(HttpRequest request) {
		HttpResponse response = null;

		// We reached here means no error so far, so lets process further
		try {
			// Fill in the code to create a response for version mismatch.
			// You may want to use constants such as Protocol.VERSION,
			// Protocol.NOT_SUPPORTED_CODE, and more.
			// You can check if the version matches as follows
			if (!request.getVersion().equalsIgnoreCase(Protocol.VERSION)) {
				// Here you checked that the "Protocol.VERSION" string is not
				// equal to the
				// "request.version" string ignoring the case of the letters in
				// both strings
				// TODO: Fill in the rest of the code here
			} else if (request.getMethod().equalsIgnoreCase(Protocol.GET)) {
				// Map<String, String> header = request.getHeader();
				// String date = header.get("if-modified-since");
				// String hostName = header.get("host");
				//
				// Handling GET request here
				// Get relative URI path from request
				String uri = request.getUri();
				// Get root directory path from server
				String rootDirectory = server.getRootDirectory();
				// Combine them together to form absolute file path
				File file = new File(rootDirectory + uri);
				// Check if the file exists
				if (file.exists()) {
					if (file.isDirectory()) {
						// Look for default index.html file in a directory
						String location = rootDirectory + uri
								+ System.getProperty("file.separator")
								+ Protocol.DEFAULT_FILE;
						file = new File(location);
						if (file.exists()) {
							// Lets create 200 OK response
							response = HttpResponseFactory.create200OK(file,
									Protocol.CLOSE);
						} else {
							// File does not exist so lets create 404 file not
							// found code
							response = HttpResponseFactory
									.create404NotFound(Protocol.CLOSE);
						}
					} else { // Its a file
								// Lets create 200 OK response
						response = HttpResponseFactory.create200OK(file,
								Protocol.CLOSE);
					}
				} else {
					// File does not exist so lets create 404 file not found
					// code
					response = HttpResponseFactory
							.create404NotFound(Protocol.CLOSE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO: So far response could be null for protocol version mismatch.
		// So this is a temporary patch for that problem and should be removed
		// after a response object is created for protocol version mismatch.
		if (response == null) {
			response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
		}

		try {
			// Write response and we are all done so close the socket
			response.write(out);
			// System.out.println(response);
		} catch (Exception e) {
			// We will ignore this exception
			e.printStackTrace();
		}
	}

	/**
	 * @param status
	 */
	public void addBadResponse(int status) {
		//TODO: Add other codes.
		
		HttpResponse response = null;
		if(status == Protocol.BAD_REQUEST_CODE) {
			response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
		} else if(status == Protocol.NOT_SUPPORTED_CODE) {
			//TODO
		}
		
		if(response != null) {
			// Means there was an error, now write the response object to the socket
			try {
				response.write(out);
//				System.out.println(response);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return;
		}
	}
}
