package fr.upem.jshell.watching;
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
 */

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import fr.upem.jshell.watching.DirObserverFactory.DirObserver;
import fr.upem.jshell.watching.DirObserverFactory.ExerciseWatcher;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

class WatchDir implements DirObserver{

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private final Map<Path, Set<ExerciseWatcher>> registered;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    
    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDir(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;
        this.registered = new HashMap<>();

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            watch(dir);
        }

        // enable trace after initial registration
        this.trace = true;
        new Thread(this::processEvents).start();
    }

    /**
     * Register the given directory with the WatchService
     */
    private void watch(Path dir) throws IOException {
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
     * Let a Verticle to register for a file
     */
    @Override
    public void register(Path fileToWatch, ExerciseWatcher toRegister) {
    	Set<ExerciseWatcher> values;
    	if(registered.keySet().contains(fileToWatch)){
    		values = registered.get(fileToWatch);
    		System.out.println("Already registered");
    	} else {
    		values = new HashSet<>();
    		registered.put(fileToWatch, values);
    		System.out.println("Just registered for file " + fileToWatch);
    	}
    	values.add(toRegister);
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
                watch(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);
                alert(child);
            }

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
    }

    private void alert(Path name) {
    	Set<ExerciseWatcher> values = registered.get(name);
    	System.out.println("alert: path=" + name);
		if(values != null){
			System.out.println("alert: set found");
			for(ExerciseWatcher watcher : values){
				watcher.alert();
			}
		}
	}

	static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }

}
