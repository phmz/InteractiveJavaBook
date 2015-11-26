package fr.upem.test.JShellServerTest;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.upem.jShell.JShellServer.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

	private Vertx vertx;
	private int port;
	
	@Before
	public void setup(TestContext context) throws IOException{
		ServerSocket socket = new ServerSocket(0);
		port = socket.getLocalPort();
		socket.close();
		
		DeploymentOptions options = new DeploymentOptions()
				.setConfig(new JsonObject().put("http.port", port));
		
		vertx = Vertx.vertx();
		vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());
	}
	
	@After
	public void tearDown(TestContext context){
		vertx.close(context.asyncAssertSuccess());
	}
	
	@Test
	public void testMyApplication(TestContext context){
		final Async async = context.async();
		
		vertx.createHttpClient().getNow(port, "localhost", "/", 
				response -> {
					response.handler(body -> {
						context.assertTrue(body.toString().contains("JShellBook"));
						async.complete();
					});
				});
	}
	
	@Test
	public void testAllExercises(TestContext context){
		Async async = context.async();
		vertx.createHttpClient().getNow(port, "localhost", "/api/exercises", response -> {
		    context.assertEquals(response.statusCode(), 200);
		    context.assertEquals(response.headers().get("content-type"), "application/json; charset=utf-8");
		    response.bodyHandler(body -> {
		      context.assertTrue(body.toString().contains("title"));
		      async.complete();
		    });
		  });
	}
}
