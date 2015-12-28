package fr.upem.jShell.JShellServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class IndexVerticle extends AbstractVerticle{
	
	private static final String INDEX_PATH = "webroot/exercises/index/index.js";
	private static final String INDEX_DIR = "exercises/index";
	public static final String GET_INDEX = "fr.upem.jShell.index.get";
	public static final String RESPONSE_INDEX = "fr.upem.jShell.index.response";
	private String index;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		EventBus eb = vertx.eventBus();
		eb.consumer(WatchDirVerticle.DIR_EDITED + ":" + INDEX_PATH, message -> {
			try {
				loadIndex(Paths.get(INDEX_PATH));
			} catch (IOException e) {
				// Index file is corrupted, our response becomes null
				index = null;
			}
		});
		eb.consumer(GET_INDEX, message -> eb.send(RESPONSE_INDEX, index));
		loadIndex(Paths.get(INDEX_PATH));
		eb.send(WatchDirVerticle.WATCH_DIR, Paths.get(INDEX_DIR).toString());
	}

	private void loadIndex(Path indexPath) throws IOException {
		index = Files.readAllLines(indexPath)
				.stream()
				.collect(Collectors.joining("\n"));
	}
}
