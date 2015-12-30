package fr.upem.jShell.Eval;

import java.util.List;
import java.util.Map;

import jdk.jshell.JShell;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

/**
 * This class consists exclusively of static methods that allow to evaluate the input String
 * using the JShell library.
 */
public class SnippetEval {
	
	/**
	 * The evaluation engine using the JShell library.
	 */
	private static ThreadLocal<JShell> jshell = new ThreadLocal<JShell>(){
		@Override protected JShell initialValue() {
			return JShell.create();
		}
	};

	/**
	 * Verify that the method is working as intended.
	 * 
	 * @param input the method to evaluate.
	 * @param map the map containing the instruction as the key and the value as the expected result.
	 * @return true if the input is working as intended.
	 * @throw IllegalArgumentException is the snippet failed the compilation.
	 */
	public static boolean compareMethod(String input, Map<String, String> map) {
		System.out.println("compareMethod: input=" + input);
		List<SnippetEvent> events = jshell.get().eval(input);
		if (!events.isEmpty()) { //events is empty when the instruction has already been evaluated
			SnippetEvent se = events.get(0);
			if (se.status().equals(Status.REJECTED))
				throw new IllegalArgumentException("Snippet status is rejected");
		}
		for (Map.Entry<String, String> entry : map.entrySet()) {
			SnippetEvent currentSnippet = jshell.get().eval(entry.getKey()).get(0);
			String value = currentSnippet.value();
			if (value == null || !value.equals(entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Close the JShell evaluation state engine.
	 */
	public static void close() {
		jshell.get().close();
	}

}
