package fr.upem.jShell.JShellServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


import fr.upem.jshell.watching.DirObserverFactory;
import fr.upem.jshell.watching.DirObserverFactory.ExerciseWatcher;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle implements ExerciseWatcher{

	public static final String appUrl = "localhost";
	private boolean alerted = false;
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#start(io.vertx.core.Future)
	 */
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		
		final int port = config().getInteger("http.port");
		Router router = Router.router(vertx);
		
		/*
		 * Files.walk(new File(".").toPath()) .filter(p -> !p.toString()
		 * .contains(File.separator + ".")) .forEach(System.out::println);
		 */
		
		// Crée le serveur
		vertx.createHttpServer()
			.requestHandler(request -> {
				//Check if remote client is in the same machine
				if(accessControl(request)){
					// Enables read body for all routes under "/api/exercises
					router.route("/api/exercises*").handler(BodyHandler.create());

					// Ajoute appel REST get pour l'index
					router.get("/api/exercises").handler(this::readIndex);
					// Route REST pour les requetes des exercises
					router.get("/api/exercises/:id").handler(this::readOne);
					//
					router.get("/api/exercises/update/:id").handler(this::waitForUpdate);

					// Ajoute route aux resources statiques dans le dossier exercises
					router.route().handler(StaticHandler.create());
					router.accept(request);
				} else {
					//If not, it won't receive our pages
					router.route().handler(this::forbiddenAccess);
					router.accept(request);
				}
			})			
			.listen(
				// Port  8989
				port, appUrl, result -> {
					if (result.succeeded()) {
						System.out.println("Server listening in: " + appUrl + ":" + port +"/");
					} else {
						startFuture.fail(result.cause());
					}
				});
	}

	// Récuper le fichier Json exercises.txt qui contient la liste des exercises
	private void readIndex(RoutingContext routingContext) {
		routingContext.response()
			.putHeader("content-type", "application/json; charset=utf-8")
			.sendFile("webroot/exercises.txt");
	}

	//Handler get pour recuperer un exercise 
	private void readOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		try {
			Path filePath = Paths.get("webroot/exercises/" + id + ".md");
			String html = new Parser().createHTMLStringFromMarkdown("webroot/exercises/" + id + ".md");
			DirObserverFactory.getFactory().getDirObserver(Paths.get("webroot", "exercises"))
				.register(filePath, this);
			routingContext.response()
			.putHeader("content-type", "text/html; charset=utf-8")
			.end(html);
		} catch (IOException e) {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void waitForUpdate(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		String html;
		synchronized (appUrl) {
			while(!alerted){
				try {
					appUrl.wait();
				} catch (InterruptedException e) {
					//Do not send anything if interrupted
					return;
				}
			}
		}
		try {
			html = new Parser().createHTMLStringFromMarkdown("webroot/exercises/" + id + ".md");
			routingContext.response()
			.putHeader("content-type", "text/html; charset=utf-8")
			.end(html);
		} catch (IOException e) {
			routingContext.response()
			.setStatusCode(500)
			.end();
		}
	}
	
	//Check if the client is in the same machine
	private boolean accessControl(HttpServerRequest request){
		String local = request.localAddress().host();
		String remote = request.remoteAddress().host();
		if(!local.equals(remote)){
			return false;
		} 
		return true;
	}
	
	//Route for the case in which the client is not in the same machine
	//(Access forbidden)
	private void forbiddenAccess(RoutingContext context){
		context.response().setStatusCode(403).setStatusMessage("Access Forbidden").end();
	}
	
	public void alert(){
		synchronized (appUrl) {
			alerted = true;
			appUrl.notify();
		}
	}
	

	//Run and deploy our verticle
	public static void main(String[] args) throws Exception {
		int port = 8989;

		DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));

		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(MainVerticle.class.getName(), options);
	}
	
}
