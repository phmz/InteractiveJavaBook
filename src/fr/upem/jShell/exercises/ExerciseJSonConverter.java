package fr.upem.jShell.exercises;

import io.vertx.core.json.JsonObject;

public class ExerciseJSonConverter {

	public static String exercise2JSon(Exercise exercise) {
		JsonObject json = new JsonObject();
		json.put("question", exercise.getQuestion());
		json.put("tests", exercise.getJUnitTests());
		return json.encode();
	}
}
