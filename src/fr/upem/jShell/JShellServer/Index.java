package fr.upem.jShell.JShellServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Index {
	
	static class Entry{
		final int id;
		final String name;
		
		Entry(int id, String name){
			this.id = id;
			this.name = name;
		}
	}
	
	/** 
	 * 
	 * @param indexPath
	 * The path of the index to read
	 * @return 
	 * The index file as a Json
	 * @throws IOException 
	 * If an error in reading the file occurs
	 */
	static String readIndex(Path indexPath) throws IOException {
		return  Files.readAllLines(indexPath)
				.stream()
				.collect(Collectors.joining("\n"));
	}
}
