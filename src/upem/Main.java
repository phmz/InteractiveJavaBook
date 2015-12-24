package upem;

import java.util.HashMap;

import fr.upem.jShell.Eval.SnippetEval;

public class Main {

	public static void main(String[] args) {
		HashMap<String, String> map = new HashMap<>();
		map.put("test(1,2)", "false");
		map.put("test(1,1)", "true");
		SnippetEval eval = new SnippetEval();
		// compareMethod will run both test(1,2) and test(1,1) and comparing their results with their expected results
		System.out.println(eval.compareMethod("public boolean test(int a, int b) { return a==b;}", map));
		// compareSnippet will run the snippet and compare its result with the expected one
		System.out.println(eval.compareSnippet("1==2", "false"));
		System.out.println(eval.compareSnippet("1==2", "true"));
		// eval returns the result of the snippet
		System.out.println(eval.eval("System.out.println(\"Hello World!\");"));
		HashMap<String, String> map2 = new HashMap<>();
		// compareMethod also works with classes
		map.put("Foo.main()", "Hello World");
		System.out.println(eval.compareMethod("public class Foo {public static void main(String[] args) {System.out.println(\"Hello World\");}}", map2));
		eval.close();
	}

}
