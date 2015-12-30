package fr.upem.jShell.exercises;

import java.util.Map;
import java.util.Objects;

/**
 * Stores data related to an exercise and provides get methods for accessing to it.
 * @author mattia
 *
 */
public class Exercise {
	
	static enum ExerciseKind {
		Snippet,
		Method;
	}

	private final int id;
	private final ExerciseKind kind;
	private final String question;
	private final Map<String, String> tests;
	private final String junit;
	
	Exercise(int id, ExerciseKind kind, String question, String junit, Map<String, String> tests){
		if(id < 0){
			throw new IllegalArgumentException("Exercise id must be positive: " + id);
		}
		this.id = id; 
		this.kind = Objects.requireNonNull(kind);
		this.question = Objects.requireNonNull(question);
		this.junit = Objects.requireNonNull(junit);
		this.tests = Objects.requireNonNull(tests);
	}

	/**
	 * 
	 * @return The unique id of the exercise
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return the stored kind of the exercise
	 */
	public ExerciseKind getKind() {
		return kind;
	}


	/**
	 * 
	 * @return the question of the exercise
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * 
	 * @return a map containing some unit tests as keys and their exected results as values
	 */
	public Map<String, String> getTests() {
		return tests;
	}
	
	/**
	 * 
	 * @return the class JUnit as a String
	 */
	public String getJUnitTests() {
		return junit;
	}
}
