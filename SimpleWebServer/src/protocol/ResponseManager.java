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
import java.util.Date;

import util.DateUtil;

//TODO: Add more functionality to this class. (file tracking)
/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ResponseManager {
	private OutputStream out;

	public ResponseManager(OutputStream out) {
		this.out = out;
	}

	/**
	 * @param request
	 */
	public void respond(HttpRequest request) {
		HttpResponse response = null;

		if (!request.getVersion().equalsIgnoreCase(Protocol.VERSION)) {
			response = HttpResponseFactory.create505NotSupported(Protocol.OPEN);
		} else {
			final String requestMethod = request.getMethod();
			if (requestMethod.equalsIgnoreCase(Protocol.GET)) {
				// Map<String, String> header = request.getHeader();
				// String date = header.get("if-modified-since");
				// String hostName = header.get("host");
				//
				// Handling GET request here
				// Get relative URI path from request
				String uri = request.getUri();
				File file = new File(uri);
				// Check if the file exists
				if (file.isDirectory()) {
					// Look for default index.html file in a directory
					file = new File(file, Protocol.DEFAULT_FILE);
				}
				if (!file.exists()) {
					response = HttpResponseFactory.create404NotFound(Protocol.OPEN);
				} else {
					final String dateModifiedField = request.getHeader().get(Protocol.IF_MODIFIED_SINCE);
					if (dateModifiedField == null) {
						// Client has not specified if_modified_since
						response = HttpResponseFactory.create200OK(file, Protocol.OPEN);
					} else {
						// See if client already has a recent enough file
						final long fileDateModified = file.lastModified();
						Date requestDate = DateUtil.getDateFromHttpRequestString(dateModifiedField);

						if (requestDate == null || requestDate.getTime() < fileDateModified) {
							// We could not understand the date, or the file has changed since then, send it
							response = HttpResponseFactory.create200OK(file, Protocol.OPEN);
						} else {
							// Client has an up-to-date version
							response = HttpResponseFactory.create304NotModified(Protocol.OPEN);
						}

					}
				}
			} else {
				// request method not implemented
				response = HttpResponseFactory.create501NotImplemented(Protocol.OPEN);
			}
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
		}
	}
}
