package fr.upem.jShell.parser;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

import org.pegdown.PegDownProcessor;

public class Parser {
	
	private static final PegDownProcessor processor = new PegDownProcessor();;

	private static String readFile(Path path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, encoding);
	}
	
	public static void createHTMLFileFromMarkdown(Path pathIn, Path pathOut) throws IOException {
		String markdownSource = readFile(pathIn, StandardCharsets.UTF_8);
		String htmlFile = processor.markdownToHtml(markdownSource);
		PrintWriter writer = new PrintWriter(pathOut.toString(), "UTF-8");
		writer.println(htmlFile);
		writer.close();
	}
	
	public static String createHTMLStringFromMarkdown(Path path) throws IOException {
		String markdownSource = readFile(path, StandardCharsets.UTF_8);
		String htmlFile = processor.markdownToHtml(markdownSource);
		return htmlFile;
	}
	
	public static HashMap<String, String> readResultFile(Path src) {
		HashMap<String, String> map = new HashMap<>();
		try (Stream<String> lines = Files.lines(src)) {
			lines.forEach(s -> {
				String[] tokens = s.split(" ");
				if (tokens.length < 3) {
					map.put(tokens[0], tokens[1]);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
}