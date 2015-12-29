package fr.upem.jShell.JShellServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import fr.upem.jShell.Eval.SnippetEval;
import fr.upem.jShell.exercises.Exercise;
import fr.upem.jShell.exercises.ExerciseFactory;
import fr.upem.jShell.exercises.ExerciseJSonConverter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Receive code snippets, evaluates them and produces a result. For each
 * exercise that is currently done by an user, stores it in central memory
 * together with the unit tests.
 * 
 * @author mattia
 *
 */
public class ExerciseManagerVerticle extends AbstractVerticle {
	
	private class Entry{
		Exercise exercise;
		MessageConsumer<String> consumer;
		
		public Entry(Exercise exercise, MessageConsumer<String> consumer) {
			this.exercise = exercise;
			this.consumer = consumer;
		}
	}

	private final List<Entry> exercises = new LinkedList<>();
	private final int maxNumExercises = 20;
	//public static final String CLIENT_REGISTER = "fr.upem.jShell.eval.register";
	/**
	 * Used to ask to evaluate an exercise
	 */
	public static final String EVAL_EXERCISE = "fr.upem.jShell.eval.eval";
	/**
	 * Address of evaluation responses
	 */
	public static final String EVALUATED = "fr.upem.jShell.eval.evaluated";
	/**
	 * Address to get an exercise
	 */
	public static final String GET_EXERCISE = "fr.upem.jShell.eval.get";
	/**
	 * Address to get only the question of an exercise
	 */
	public static final String GET_EXERCISE_QUESTION = "fr.upem.jShell.eval.get_question";
	/**
	 * Address of response to get an exercise
	 */
	public static final String RETURN_EXERCISE = "fr.upem.jShell.eval.return";
	/**
	 * Address of response to a request of the question of an exercise
	 */
	public static final String RETURN_EXERCISE_QUESTION = "fr.upem.jShell.eval.return_question";
	
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		EventBus eb = vertx.eventBus();
		
		eb.consumer(EVAL_EXERCISE, message -> {
			String[] s = message.body().toString().split(":");
			int id = Integer.valueOf(s[0]);
			System.out.println("EVAL_EXERCISE: id=" + id + "code="+s[1]);
			try {
				if (isCorrect(getExerciseById(id), s[1])) {
					eb.send(EVALUATED+":"+id, "Correct");
				} else {
					eb.send(EVALUATED+":"+id, "Incorrect or incomplete");
				}
			} catch (IOException e) {
				eb.send(EVALUATED+":"+id, "IO Error");
			}
		});
		
		eb.consumer(GET_EXERCISE, message -> {
			int id = Integer.valueOf(message.body().toString());
			try {
				eb.publish(RETURN_EXERCISE+":"+id, ExerciseJSonConverter.exercise2JSon(getExerciseById(id)));
			} catch (IOException e) {
				eb.publish(RETURN_EXERCISE+":"+id, null);
			}
		});
		
		eb.consumer(GET_EXERCISE_QUESTION, message -> {
			int id = Integer.valueOf(message.body().toString());
			try {
				eb.publish(RETURN_EXERCISE_QUESTION+":"+id, getExerciseById(id).getQuestion());
			} catch (IOException e) {
				eb.publish(RETURN_EXERCISE_QUESTION+":"+id, null);
			}
		});
	}

	//Return an exercise already loaded in memory
	private Exercise getExerciseById(int id) throws IOException {
		return getEntryById(id).exercise;
	}
	
	private Entry getEntryById(int id) throws IOException {
		Optional<Entry> optionalExercise;
		synchronized (exercises) {
			optionalExercise = exercises.stream().filter(exer -> exer.exercise.getId() == id).findFirst();
		}
		if (optionalExercise.isPresent()) {
			return optionalExercise.get();
		} else {
			return loadExercise(Paths.get("webroot/exercises"), id);
		}
	}

	private Entry loadExercise(Path path, int id) throws IOException{
		Exercise exercise = ExerciseFactory.readExercise(path, id);
		MessageConsumer<String> consumer = vertx.eventBus().consumer(
				WatchDirVerticle.DIR_EDITED + ":" + path.resolve(String.valueOf(id)),
				message -> {
					invalidExercise(id);
					try {
						loadExercise(path, id);
					} catch (Exception e) {
						throw new IllegalStateException("Recalling loadExercise produces exception!");
					}
				});
		Entry newEntry = new Entry(exercise, consumer);
		synchronized (exercises) {
			if(exercises.size() > maxNumExercises){
				exercises.remove(maxNumExercises);
			}
			exercises.add(newEntry);
		}
		return newEntry;
	}
	
	private void invalidExercise(int id) {
		Entry entry;
		try {
			entry = getEntryById(id);
		} catch (IOException e) {
			throw new IllegalStateException("The entry doesn't exists! " + id);
		}
		synchronized (exercises) {
			exercises.remove(entry);
			entry.consumer.unregister();
		}
	}
	
	private boolean isCorrect(Exercise exercise, String solution) {
		try{
			return SnippetEval.compareMethod(solution, exercise.getTests());
		} catch (IllegalArgumentException e){
			if(e.getMessage().equals("Snippet status is rejected")){
				return false;
			} else throw e;
		}
	}

}
