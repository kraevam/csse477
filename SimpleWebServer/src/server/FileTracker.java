/*
 * FileTracker.java
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
 
package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class provides access to the contents of files
 * It caches the most frequently requested files in order to minimize I/O operations.
 * @author Marina Kraeva (kraevam@rose-hulman.edu)
 */
public enum FileTracker {

	INSTANCE;
	
	private static final int MIN_REQUESTS_FOR_CACHING = 100;
	private static final int MAX_FILE_SIZE = 100*1024*1024; // 100 MB
	
	private ConcurrentHashMap<String, AtomicLong> requestsPerFile;
	private ConcurrentHashMap<String, ByteBuffer> fileToContents;
	private ConcurrentHashMap<String, Long> fileToDateModified;
	
	private FileTracker() {
		this.requestsPerFile = new ConcurrentHashMap<String, AtomicLong>();
		this.fileToContents = new ConcurrentHashMap<String, ByteBuffer>();
		this.fileToDateModified = new ConcurrentHashMap<String, Long>();
	}
	
	public long getFileSize(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException();
		}
		return file.length();
	}
	
	public ByteBuffer getFileContents(String filePath) throws FileNotFoundException{		
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException();
		}
		
		incrementFileRequests(file);
		if (fileToContents.containsKey(file.getAbsolutePath())) {
			long lastModified = file.lastModified();
			if (lastModified == fileToDateModified.get(file.getAbsolutePath())) {
				// File has not been changed since last update
				return fileToContents.get(file.getAbsolutePath());
			} else {
				// File has changed since last update; update it in the maps
				ByteBuffer contents = getFileContents(file);
				fileToContents.put(file.getAbsolutePath(), contents);
				fileToDateModified.put(file.getAbsolutePath(), lastModified);
				return contents;
			}
		} else {
			return getFileContents(file);
		}
	}
	
	private ByteBuffer getFileContents(File file) throws FileNotFoundException{
		FileChannel channel = null;
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		channel = raf.getChannel();

		ByteBuffer buf = null;
		try {
			buf = channel.map(MapMode.READ_ONLY, 0L, file.length());
		} catch (IOException e) {
			// Log problem to a file?
			e.printStackTrace();
		}
		buf.order(ByteOrder.LITTLE_ENDIAN);
		try {
			raf.close();
		} catch (IOException e) {
			// not such a big deal
		}
		return buf;

	}

	private void incrementFileRequests(File file) throws FileNotFoundException {
		long requestsCount = 1;
		AtomicLong oldValue = requestsPerFile.putIfAbsent(file.getAbsolutePath(), new AtomicLong(1));
		if (oldValue != null) {
			requestsCount = oldValue.incrementAndGet();
		}
		
		if(requestsCount >= MIN_REQUESTS_FOR_CACHING) {
			addFileToCache(file);
		}
	}
	
	private void addFileToCache(File file) throws FileNotFoundException {
		if (!file.exists()) {
			// shouldn't come to this, since we already check file existence before calling this method
			throw new FileNotFoundException();
		}
		if (file.length() > MAX_FILE_SIZE) {
			// We don't want big files in memory!
			return;
		}
		
		long lastModified = file.lastModified();
		ByteBuffer contents = getFileContents(file);
		fileToContents.put(file.getAbsolutePath(), contents);
		fileToDateModified.put(file.getAbsolutePath(), lastModified);		
	}
}
