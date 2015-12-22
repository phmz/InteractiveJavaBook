package fr.upem.jshell.watching;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DirObserverFactory {
	
	public interface ExerciseWatcher{
    	void alert();
    }
	
	public interface DirObserver{
		void register(Path dir, ExerciseWatcher watcher);
	}
	
	//Singleton instance
	private static DirObserverFactory instance = new DirObserverFactory();
	
	//Map that associate a dir to a DirObserver
	private final Map<Path, DirObserver> observers;

	private DirObserverFactory(){
		observers = new HashMap<>();		
	}
	
	/**
	 * Return an instance of the factory
	 */
	public static DirObserverFactory getFactory(){
		return instance;
	}
	
	/**
	 * Returns a DirObserver for the specified directory. If such a DirObserver already exists,
	 * then the existing one is returned
	 * @throws IOException if DirObserver cannot read the directory
	 */
	public DirObserver getDirObserver(Path dir) throws IOException{
		DirObserver observer = observers.get(dir);
		if(observer != null){
			System.out.println("This is an old object");
			return observer;
		}
		System.out.println("This is a new object!");
		observer = new WatchDir(dir, false);
		observers.put(dir, observer);
		return observer;
	}
}
