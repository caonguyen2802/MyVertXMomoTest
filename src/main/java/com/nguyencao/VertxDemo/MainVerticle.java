package com.nguyencao.VertxDemo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MainVerticle extends AbstractVerticle {
	private static JWTAuth authProvider;
	private static Vertx vertx;

	public static void main(String[] args) {
		vertx = Vertx.vertx();
		authProvider = JwtGen.getInstance(vertx);
		vertx.deployVerticle(MainVerticle.class.getName());
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());
		router.get("/login").handler(this::getLoginPage);
		router.post("/login").handler(this::loginHandler);
		router.get("/product").handler(this::getProductPage);
		router.post("/product").handler(JWTAuthHandler.create(authProvider)).handler(this::productHandler);

		vertx.createHttpServer().requestHandler(router).listen(8080);
	}

	private void getLoginPage(RoutingContext ctx) {
		HttpServerResponse response = ctx.response();
		response.putHeader("content-type", "text/html");
		response.sendFile("webroot/login.html");
	}

	private void loginHandler(RoutingContext ctx) {
		String username = ctx.request().getParam("username");
		String password = ctx.request().getParam("password");

		MySqlConfig.getInstance(vertx).preparedQuery("SELECT * FROM users WHERE username=? and password=?")
				.execute(Tuple.of(username, password), ar -> {
					if (ar.succeeded()) {
						RowSet<Row> result = ar.result();
						if (result.size() == 0) {
							failResponse(ctx, 401);
						} else {
							System.out.println("------Gen token------");
							// Generate token
							String token = JwtGen.getInstance(vertx).generateToken(new JsonObject(),
									new JWTOptions().setAlgorithm("RS256"));
							System.out.println(token);
							successResponseWithToken(ctx, token);
						}
					} else {
						System.out.println("Failure: " + ar.cause().getMessage());
						failResponse(ctx, 500);
					}
				});
	}

	private void getProductPage(RoutingContext ctx) {
		HttpServerResponse response = ctx.response();
		response.putHeader("content-type", "text/html");
		response.sendFile("webroot/product.html");
	}

	private void productHandler(RoutingContext ctx) {
		String productName = ctx.request().getParam("productName");
		String productDescription = ctx.request().getParam("productDescription");

		MySqlConfig.getInstance(vertx).preparedQuery("INSERT INTO products (name, description) VALUES (?, ?)")
				.execute(Tuple.of(productName, productDescription), ar -> {
					if (ar.succeeded()) {
						successResponse(ctx);
					} else {
						System.out.println("Failure: " + ar.cause().getMessage());
						failResponse(ctx, 500);
					}
				});
	}

	private void successResponseWithToken(RoutingContext ctx, String token) {
		HttpServerResponse response = ctx.response();
		response.setChunked(true);
		response.write(Buffer.buffer(token, "UTF-8"));
		response.setStatusCode(200);
		response.end();
		response.close();
	}

	private void successResponse(RoutingContext ctx) {
		HttpServerResponse response = ctx.response();
		response.setChunked(true);
		response.setStatusCode(200);
		response.end();
		response.close();
	}

	private void failResponse(RoutingContext ctx, int status) {
		HttpServerResponse response = ctx.response();
		response.setChunked(true);
		response.setStatusCode(status);
		response.end();
		response.close();
	}
}
