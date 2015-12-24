package fr.upem.jShell.Eval;
import java.util.Map;

import jdk.jshell.JShell;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

public class SnippetEval {
	
	private JShell jshell;
	
	public SnippetEval() {
		this.jshell = JShell.create();
	}

	public boolean compareMethod(String input, Map<String, String> map) {
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

	public boolean compareSnippet(String input, String result) {
			SnippetEvent se = jshell.eval(input).get(0);
			if (se.status().equals(Status.REJECTED)) {
				throw new IllegalArgumentException("Snippet status is rejected");
			}
			return se.value().equals(result);
	}

	public String eval(String input) {
			SnippetEvent se = jshell.eval(input).get(0);
			if (se.status().equals(Status.REJECTED)) {
				throw new IllegalArgumentException("Snippet status is rejected");
			}
			return se.value();
	}
	
	public void close() {
		jshell.close();
	}

}
