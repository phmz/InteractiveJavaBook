package fr.upem.jShell.exercises;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.upem.jShell.JShellServer.Parser;
import fr.upem.jShell.exercises.Exercise.ExerciseKind;

public class ExerciseFactory {

	public static Exercise readExercise(Path dir, int id) throws IOException {
		Path toRead = Paths.get(dir.toString(), id + ".md");
		String question = new Parser().createHTMLStringFromMarkdown(toRead.toString());
		return new Exercise(id, ExerciseKind.METHOD, question, null);
	}
}
