package fr.upem.jShell.exercises;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.upem.jShell.JShellServer.Parser;
import fr.upem.jShell.exercises.Exercise.ExerciseKind;
import io.vertx.core.json.JsonObject;

public class ExerciseFactory {

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
				.stream().map(line -> line.replaceAll("\t", " ")).collect(Collectors.joining("\n"));
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
