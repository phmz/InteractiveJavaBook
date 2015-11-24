package upem;
import java.io.IOException;

import upem.parser.Parser;

public class Main {

	public static void main(String[] args) throws IOException {
		Parser parser = new Parser();
		parser.createHTMLFileFromMarkdown("test.md", "test.html");
		String htmlString = parser.createHTMLStringFromMarkdown("test.md");
		System.out.println(htmlString);
	}

}
