package fr.upem.jShell.exercises;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.upem.jShell.exercises.Exercise.ExerciseKind;
import fr.upem.jShell.parser.Parser;
import io.vertx.core.json.JsonObject;

/**
 * Static Factory for the class Exercise. At the moment we have a lone class of exercises (Exercise)
 * but constructing one is a complex task which involves reading and parsing several files.
 * With this factory we place the construction of an Exercise in only one point in the code and simplify
 * the process of building one.
 * @author mattia
 *
 */
public class ExerciseFactory {

	/**
	 * Create an Exercise by specifying the directory where it is located and its id.
	 * @param dir Th directory containing the exercise
	 * @param id The unique id of the exercise
	 * @return The Exercise with the specified id.
	 * @throws IOException if there are problems reading the directory
	 */
	public static Exercise readExercise(Path dir, int id) throws IOException {
		Path jsonPath = Paths.get(dir.toString(), ""+id, id+".json");
		JsonObject files = new JsonObject(
				Files.readAllLines(jsonPath).stream().collect(Collectors.joining("\n")));
		String question = Parser.createHTMLStringFromMarkdown(Paths.get(dir.toString(), ""+id)
				.resolve(files.getString("question")));
		Map<String, String> results = Parser.
				readResultFile(Paths.get(dir.toString(), ""+id)
						.resolve(files.getString("tests")));
		String junit = Files.readAllLines(Paths.get(dir.toString(), ""+id).resolve(files.getString("junit")))
				.stream().collect(Collectors.joining("\n"));
		ExerciseKind kind = getKind(files.getString("kind"));
		return new Exercise(id, kind, question, junit, results);
	}
	
	private static ExerciseKind getKind(String kind){
		switch(Objects.requireNonNull(kind)){
		case "snippet":
			return ExerciseKind.Snippet;
		case "method":
			return ExerciseKind.Method;
		default:
			throw new IllegalArgumentException("Illegal exercise kind: " + kind);
		}
	}
}
