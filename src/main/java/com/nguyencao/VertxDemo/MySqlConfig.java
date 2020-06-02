package com.nguyencao.VertxDemo;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class MySqlConfig {
	private static MySQLPool client;
	static Vertx vertx = Vertx.vertx();
	public static MySQLPool getInstance(Vertx vertx) {
		return getMySQLPool(vertx);
	}

	private static MySQLPool getMySQLPool(Vertx vertx) {
		if (client == null) {
			MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
					.setDatabase("vertxdb").setUser("root").setPassword("caonguyen280293");

			// Pool options
			PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

			// Create the client pool
			client = MySQLPool.pool(vertx, connectOptions, poolOptions);
		}
		return client;
	}
	public static MySQLPool connection() {
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("vertxdb").setUser("root").setPassword("caonguyen280293");

		// Pool options
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
		MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);
		return client;
	}
}
