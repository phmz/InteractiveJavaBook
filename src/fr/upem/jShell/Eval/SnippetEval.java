package fr.upem.jShell.Eval;

import java.util.List;
import java.util.Map;

import jdk.jshell.JShell;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

public class SnippetEval {

	private static ThreadLocal<JShell> jshell = new ThreadLocal<JShell>(){
		@Override protected JShell initialValue() {
			return JShell.create();
		}
	};

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

	public static boolean compareSnippet(String input, String result) {
		SnippetEvent se = jshell.get().eval(input).get(0);
		if (se.status().equals(Status.REJECTED)) {
			throw new IllegalArgumentException("Snippet status is rejected");
		}
		return se.value().equals(result);
	}

	public static String eval(String input) {
		SnippetEvent se = jshell.get().eval(input).get(0);
		if (se.status().equals(Status.REJECTED)) {
			throw new IllegalArgumentException("Snippet status is rejected");
		}
		return se.value();
	}

	public static void close() {
		jshell.get().close();
	}

}
