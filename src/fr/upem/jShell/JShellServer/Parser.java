package fr.upem.jShell.JShellServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

import org.pegdown.PegDownProcessor;

public class Parser {
	
	private final PegDownProcessor processor;
	
	public Parser() {
		this.processor = new PegDownProcessor();
	}

	private String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	public void createHTMLFileFromMarkdown(String pathIn, String pathOut) throws IOException {
		String markdownSource = readFile(pathIn, StandardCharsets.UTF_8);
		String htmlFile = processor.markdownToHtml(markdownSource);
		PrintWriter writer = new PrintWriter(pathOut, "UTF-8");
		writer.println(htmlFile);
		writer.close();
	}
	
	public String createHTMLStringFromMarkdown(String path) throws IOException {
		String markdownSource = readFile(path, StandardCharsets.UTF_8);
		String htmlFile = processor.markdownToHtml(markdownSource);
		return htmlFile;
	}
	
	public HashMap<String, String> readResultFile(String src) {
		HashMap<String, String> map = new HashMap<>();
		try (Stream<String> lines = Files.lines(Paths.get(src))) {
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
