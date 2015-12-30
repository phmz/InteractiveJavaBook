package fr.upem.jShell.exercises;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

/**
 * Provide a static method for converting an exercise into the data requested by the web client
 * @author mattia
 *
 */
public class ExerciseJSonConverter {

	/**
	 * Converts a given exercise into a String containing a JSon object with the exercise's question 
	 * and tests.
	 * @param exercise The exercise to convert into JSon
	 * @return A String containing the JSon object
	 * @throws NullPointerException if exercise is null
	 */
	public static String exercise2JSon(Exercise exercise) {
		Objects.requireNonNull(exercise);
		JsonObject json = new JsonObject();
		json.put("question", exercise.getQuestion());
		json.put("tests", exercise.getJUnitTests());
		return json.encode();
	}
}
