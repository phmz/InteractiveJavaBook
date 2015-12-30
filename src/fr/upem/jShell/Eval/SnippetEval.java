package fr.upem.jShell.Eval;

import java.util.List;
import java.util.Map;

import jdk.jshell.JShell;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

/**
 * A helper class for using jShell.
 * It provides two methods for testing pieces of code. It is the only class aware of jShell existence.
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
	 * Evaluates a method or class on jShell and compares it with a set of unit tests.
	 * @param input The method or class to evaluate
	 * @param map pairs of test, expected result
	 * @return true if all tests are passed, else false.
	 */
	public static boolean compareMethod(String input, Map<String, String> map) {
		System.out.println("compareMethod: input=" + input);
		List<SnippetEvent> events = jshell.get().eval(input);
		SnippetEvent se = events.get(0);
		if (se.status().equals(Status.REJECTED))
			throw new IllegalArgumentException("Snippet status is rejected");
		
		for (Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println("Evaluating: " + entry.getKey());
			SnippetEvent currentSnippet = jshell.get().eval(entry.getKey()).get(0);
			String value = currentSnippet.value();
			System.out.println("Output: " + value);
			System.out.println("Expected: " + entry.getValue());
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
