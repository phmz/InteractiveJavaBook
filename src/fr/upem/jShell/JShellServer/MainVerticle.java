package fr.upem.jShell.JShellServer;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#start(io.vertx.core.Future)
	 */
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		Router router = Router.router(vertx);

		//router.route("/exercises/*").handler(StaticHandler.create("exercises"));
		// Enables read body for all routes under "/api/whiskies
		router.route("/api/exercises*").handler(BodyHandler.create());

		// Ajoute appel REST get pour l'index
		router.get("/api/exercises").handler(this::readIndex);
		router.get("/api/exercises/:id").handler(this::readOne);
		
		// Ajoute route aux resources statiques dans le dossier exercises
		router.route().handler(StaticHandler.create());
		
		/*Files.walk(new File(".").toPath())
	     .filter(p -> !p.toString()
	                    .contains(File.separator + "."))
	     .forEach(System.out::println);*/
		
		// Crée le serveur
		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Récuper la port dans la configuration
				// défaut 8080
				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(result.cause());
					}
				});
	}

	private void readIndex(RoutingContext routingContext) {
		routingContext.response()
			.putHeader("content-type", "application/json; charset=utf-8")
			.sendFile("webroot/exercises.txt");
	}
	
	private void readOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		routingContext.response()
			.putHeader("content-type", "text/html; charset=utf-8")
			.sendFile("webroot/exercises/" + id + ".txt");
	}
	
	public static void main(String[] args) throws Exception {
		int port = 8080;
		
		DeploymentOptions options = new DeploymentOptions()
				.setConfig(new JsonObject().put("http.port", port));
		
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(MainVerticle.class.getName(), options);
	}
}
