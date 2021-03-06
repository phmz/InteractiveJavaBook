package fr.upem.jShell.JShellServer;

import java.util.Map;
import java.util.List;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

	public static final String appUrl = "localhost";
	private EventBus eb;

	/**
	 * Load the callback functions for listening to the eventbus and respond to REST requests.
	 * Automatically called at deploying time by vertx.
	 */
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		final int port = config().getInteger("http.port");
		Router router = Router.router(vertx);
		eb = vertx.eventBus();

		// Create the server
		vertx.createHttpServer().requestHandler(request -> {
			// Check if remote client is in the same machine
			if (accessControl(request)) {
				// Enables read body for all routes under "/api/exercises
				router.route("/api/exercises*").handler(BodyHandler.create());

				// Add request for the index
				router.get("/api/exercises").handler(this::readIndex);
				// Add request for the exercises
				router.get("/api/exercises/:id").handler(this::readOne);

				router.post("/api/register/:id").handler(this::registerConsumer);
				router.post("/api/solution/:id").handler(this::sendSolutionToExercise);

				// exercises
				router.route().handler(StaticHandler.create());
				router.accept(request);
			} else {
				// If not, it won't receive our pages
				router.route().handler(this::forbiddenAccess);
				router.accept(request);
			}
		}).listen(
				// Port 8989
				port, appUrl, result -> {
					if (result.succeeded()) {
						System.out.println("Server listening in: " + appUrl + ":" + port + "/");
					} else {
						startFuture.fail(result.cause());
					}
				});
	}

	// Get the JSon file which contains the exercises list
	private void readIndex(RoutingContext routingContext) {
		MessageConsumer<String> consumer = eb.consumer(IndexVerticle.RESPONSE_INDEX);
		consumer.handler(message -> {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(message.body().toString());
			consumer.unregister();
		});
		eb.send(IndexVerticle.GET_INDEX, "give me the index");
	}

	// Handler get for one exercice
	private void readOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		MessageConsumer<String> consumer = eb.consumer(ExerciseManagerVerticle.RETURN_EXERCISE + ":" + id);
		consumer.handler(message -> {
			routingContext.response().putHeader("content-type", "text/html; charset=utf-8")
					.end(message.body().toString());
			consumer.unregister();
		});
		eb.send(ExerciseManagerVerticle.GET_EXERCISE, id);
	}

	// Check if the client is in the same machine
	private boolean accessControl(HttpServerRequest request) {
		String local = request.localAddress().host();
		String remote = request.remoteAddress().host();
		if (!local.equals(remote)) {
			return false;
		}
		return true;
	}

	// Route for the case in which the client is not in the same machine
	// (Access forbidden)
	private void forbiddenAccess(RoutingContext context) {
		context.response().setStatusCode(403).setStatusMessage("Access Forbidden").end();
	}

	private void registerConsumer(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		eb.send(WatchDirVerticle.WATCH_DIR, "exercises/"+id);
		MessageConsumer<String> consumer = eb.consumer(ExerciseManagerVerticle.RETURN_EXERCISE_QUESTION + ":" + id);
		consumer.handler(message -> {
			routingContext.response().putHeader("content-type", "text/html; charset=utf-8")
					.end(message.body().toString());
			consumer.unregister();
		});
	}

	private void sendSolutionToExercise(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		MessageConsumer<String> consumer = eb.consumer(ExerciseManagerVerticle.EVALUATED+":"+id);
		consumer.handler(message -> {
				String response = message.body().toString();
				System.out.println("sendSolutionToExercise: " + response);
				routingContext.response().end(response);
				consumer.unregister();
		});
		routingContext.request().bodyHandler(buff -> {
			QueryStringDecoder qsd = new QueryStringDecoder(buff.toString(), false);
			Map<String, List<String>> params = qsd.parameters();
			eb.send(ExerciseManagerVerticle.EVAL_EXERCISE, id + ":" + params.get("code").get(0));
		});
	}

	// Deploy the verticles
	public static void main(String[] args) throws Exception {
		int port = 8989;

		DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));

		Vertx vertx = Vertx.vertx();
		DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
		vertx.deployVerticle(MainVerticle.class.getName(), options);
		vertx.deployVerticle(WatchDirVerticle.class.getName(), workerOptions);
		vertx.deployVerticle(ExerciseManagerVerticle.class.getName(), workerOptions);
		vertx.deployVerticle(IndexVerticle.class.getName(), workerOptions);
	}

}
