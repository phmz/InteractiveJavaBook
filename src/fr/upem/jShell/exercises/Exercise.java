package fr.upem.jShell.exercises;

import java.util.Map;
import java.util.Objects;

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

	
	public int getId() {
		return id;
	}

	public ExerciseKind getKind() {
		return kind;
	}


	public String getQuestion() {
		return question;
	}

	public Map<String, String> getTests() {
		return tests;
	}
	
	public String getJUnitTests() {
		return junit;
	}
}
