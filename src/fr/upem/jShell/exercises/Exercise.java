package fr.upem.jShell.exercises;

import java.util.Map;
import java.util.Objects;

public class Exercise {
	
	static enum ExerciseKind {
		LINE,
		METHOD,
		CLASS;
	}

	private final int id;
	private final ExerciseKind kind;
	private final String question;
	private final Map<String, String> tests;
	
	public Exercise(int id, ExerciseKind kind, String question, Map<String, String> tests){
		if(id < 0){
			throw new IllegalArgumentException("Exercise id must be positive: " + id);
		}
		this.id = id; 
		this.kind = Objects.requireNonNull(kind);
		this.question = Objects.requireNonNull(question);
		//TODO insert Objects.requireNotNull when we have the tests
		this.tests = tests;
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
}
