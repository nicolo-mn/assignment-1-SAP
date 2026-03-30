package sap.account.infrastructure;

import sap.account.application.AccountServiceImpl;
import io.vertx.core.Vertx;

public class AccountServiceMain {

	static final int ACCOUNT_SERVICE_PORT = 9000;

	public static void main(String[] args) {
		
		var service = new AccountServiceImpl();
		
		service.bindAccountRepository(new InMemoryAccountRepository());
		
		var vertx = Vertx.vertx();
		var server = new AccountServiceController(service, ACCOUNT_SERVICE_PORT);
		vertx.deployVerticle(server);	
	}

}

