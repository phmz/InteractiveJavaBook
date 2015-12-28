package fr.upem.jShell.Eval;
import java.util.Map;

import jdk.jshell.JShell;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

public class SnippetEval {
	
	private static JShell jshell = JShell.create();
	
	public static boolean compareMethod(String input, Map<String, String> map) {
			SnippetEvent se = jshell.eval(input).get(0);
			if (se.status().equals(Status.REJECTED))
				throw new IllegalArgumentException("Snippet status is rejected");
			for (Map.Entry<String, String> entry : map.entrySet()) {
				SnippetEvent currentSnippet = jshell.eval(entry.getKey()).get(0);
				String value = currentSnippet.value();
				if (value == null || !value.equals(entry.getValue())) {
					return false;
				}
			}
			return true;
	}

	public static boolean compareSnippet(String input, String result) {
			SnippetEvent se = jshell.eval(input).get(0);
			if (se.status().equals(Status.REJECTED)) {
				throw new IllegalArgumentException("Snippet status is rejected");
			}
			return se.value().equals(result);
	}

	public static String eval(String input) {
			SnippetEvent se = jshell.eval(input).get(0);
			if (se.status().equals(Status.REJECTED)) {
				throw new IllegalArgumentException("Snippet status is rejected");
			}
			return se.value();
	}
	
	public static void close() {
		jshell.close();
	}

}
