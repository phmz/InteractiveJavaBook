package fr.upem.jShell.parser;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

import org.pegdown.PegDownProcessor;

/**
 * This class consists exclusively of static methods that allow to parse MarkDown files and
 * Result files the results will be used to create an exercise.
 */

public class Parser {
	
	/**
	 * The processor that will parse the MarkDown files.
	 * The processor is not thread safe.
	 */
	private static final PegDownProcessor processor = new PegDownProcessor();;

	/**
	 * Read the file and put the content in a String.
	 * 
	 * @param path the path of the file.
	 * @param encoding the encoding of the file.
	 * @return the content of the file in a string.
	 * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
	 */
	private static String readFile(Path path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, encoding);
	}
	
	/**
	 * Create a HTML string from a MarkDown file.
	 * 
	 * @param path the path of the source MarkDown file.
	 * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
	 */
	public static String createHTMLStringFromMarkdown(Path path) throws IOException {
		String markdownSource = readFile(path, StandardCharsets.UTF_8);
		String htmlFile = processor.markdownToHtml(markdownSource);
		return htmlFile;
	}
	
	/**
	 * Create a map containing the elements of the source file.
	 * The format is key value
	 * @param src the path of the source file.
	 * @return the map containing the entry.
	 */
	public static HashMap<String, String> readResultFile(Path src) {
		HashMap<String, String> map = new HashMap<>();
		try (Stream<String> lines = Files.lines(src)) {
			lines.forEach(s -> {
				String[] tokens = s.split("%%%");
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