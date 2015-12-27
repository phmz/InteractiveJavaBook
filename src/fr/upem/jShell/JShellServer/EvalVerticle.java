package fr.upem.jShell.JShellServer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fr.upem.jShell.Eval.SnippetEval;
import fr.upem.jShell.exercises.Exercise;
import fr.upem.jShell.exercises.ExerciseFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

/**
 * Receive code snippets, evaluates them and produces a result. For each
 * exercise that is currently done by an user, stores it in central memory
 * together with the unit tests.
 * 
 * @author mattia
 *
 */
public class EvalVerticle extends AbstractVerticle {

	Map<String, Exercise> userExerciseMap;
	Set<Exercise> exercises;
	public static final String CLIENT_REGISTER = "fr.upem.jShell.eval.register";
	public static final String EVAL_EXERCISE = "fr.upem.jShell.eval.eval";
	public static final String EVALUATED = "fr.upem.jShell.eval.evaluated";

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		exercises = new HashSet<>();

		EventBus eb = vertx.eventBus();
		
		eb.consumer(EVAL_EXERCISE, message -> {
			String[] s = message.body().toString().split(":");
			try {
				if (isCorrect(getExerciseById(Integer.valueOf(s[0])), s[1])) {
					eb.send(EVALUATED, "Correct");
				} else {
					eb.send(EVALUATED, "Failure");
				}
			} catch (Exception e) {
				eb.send(EVALUATED, "IO Error");
			}
		});
	}

	private Exercise getExerciseById(int id) throws IOException {
		Optional<Exercise> optionalExercise = exercises.stream().filter(exer -> exer.getId() == id).findFirst();
		Exercise toRet;
		if (optionalExercise.isPresent()) {
			toRet = optionalExercise.get();
		} else {
			toRet = ExerciseFactory.readExercise(Paths.get("webroot/exercises"), id);
			exercises.add(toRet);
		}
		return toRet;
	}


	private boolean isCorrect(Exercise exercise, String solution) {
		return new SnippetEval().compareMethod(solution, exercise.getTests());
	}

}
