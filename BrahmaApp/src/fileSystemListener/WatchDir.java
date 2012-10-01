/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Change Log
 * -----------------------------------------------------------------------
 * Modified: Sept 12, 2012
 * The code has been modified from its original version for this plugin application.
 * -Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
package fileSystemListener;

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;



/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private int keyIndex = 0;
    private List<WatchEvent<?>> watchList;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
        
        // make unused initial list
        this.watchList = new ArrayList();
    }
    
    /**
     * Process one event for keys queued to the watcher
     */
    public Map<DirectoryAction, Path> processEvent() {
//    	List<Path> toLoad = new ArrayList<Path>();
//    	List<Path> toUnload = new ArrayList<Path>();
//    	Map<DirectoryAction, List<Path>> loadMap = new HashMap<DirectoryAction, List<Path>>();
//    	loadMap.put(DirectoryAction.LOAD, toLoad);
//    	loadMap.put(DirectoryAction.UNLOAD, toUnload);
    	Map<DirectoryAction, Path> loadMap = new HashMap<DirectoryAction, Path>();
    	
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
            	// Not sure if this is the right thing to do here.
                loadMap.put(DirectoryAction.END, null);
                return loadMap;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            if(watchList.isEmpty())
            	this.watchList = key.pollEvents();
            for (;keyIndex < watchList.size(); keyIndex++) {
            	WatchEvent<?> event = watchList.get(keyIndex);
                loadMap = handleOneEvent(event, dir);
                if (!loadMap.isEmpty()) {
                	this.keyIndex++;
                	return loadMap;
                }
            }
            this.watchList.clear();
            this.keyIndex = 0;

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
        loadMap.put(DirectoryAction.END, null);
        return loadMap;
    }
    
    private Map<DirectoryAction, Path> handleOneEvent(WatchEvent<?> event, Path dir) {
    	WatchEvent.Kind kind = event.kind();
    	Map<DirectoryAction, Path> loadMap = new HashMap<DirectoryAction, Path>();

        // TBD - provide example of how OVERFLOW event is handled
        if (kind == OVERFLOW) {
            return loadMap;
        }

        // Context for directory entry event is the file name of entry
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);

        // print out event
        System.out.format("%s: %s\n", event.kind().name(), child);

		// C.R. Changes
		if (kind == ENTRY_CREATE) {
			// this.manager.loadBundle(child);
			loadMap.put(DirectoryAction.LOAD, child);
			return loadMap;
		} else if (kind == ENTRY_DELETE) {
			// this.manager.unloadBundle(child);
			loadMap.put(DirectoryAction.UNLOAD, child);
			return loadMap;
		}

		handleRecursion(kind, child);
        return loadMap;
    }

	private void handleRecursion(WatchEvent.Kind kind, Path child) {
		// if directory is created, and watching recursively, then
        // register it and its sub-directories
        if (recursive && (kind == ENTRY_CREATE)) {
            try {
                if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                    registerAll(child);
                }
            } catch (IOException x) {
                // ignore to keep sample readbale
            }
        }
	}
    
    static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
        if (args.length == 0 || args.length > 2)
            usage();
        boolean recursive = false;
        int dirArg = 0;
        if (args[0].equals("-r")) {
            if (args.length < 2)
                usage();
            recursive = true;
            dirArg++;
        }

        // register directory and process its events
        Path dir = Paths.get(args[dirArg]);
        // The following line will not work now.
        new WatchDir(dir, recursive).processEvent(); // C.R. Change - Added null parameter
    }
}
