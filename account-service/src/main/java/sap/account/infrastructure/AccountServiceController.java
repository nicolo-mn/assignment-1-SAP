package sap.account.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

import sap.account.application.AccountAlreadyPresentException;
import sap.account.application.AccountNotFoundException;
import sap.account.application.AccountService;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.StaticHandler;

/**
 *
 * TicTacToe Account Service controller
 *
 * @author aricci
 *
 */
public class AccountServiceController extends VerticleBase {

	private int port;
	static Logger logger = Logger.getLogger("[AccountServiceController]");

	static final String API_VERSION = "v1";
	static final String ACCOUNTS_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts";
	static final String ACCOUNT_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId";
	static final String CHECK_PWD_RESOURCE_PATH = "/api/" + API_VERSION + "/accounts/:accountId/check-pwd";

	/* Ref. to the application layer */
	private AccountService accountService;

	public AccountServiceController(AccountService service, int port) {
		this.port = port;
		logger.setLevel(Level.INFO);
		this.accountService = service;

	}

	public Future<?> start() {
		logger.log(Level.INFO, "Account Service initializing...");
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, ACCOUNTS_RESOURCE_PATH).handler(this::createAccount);
		router.route(HttpMethod.GET, ACCOUNT_RESOURCE_PATH).handler(this::getAccountInfo);
		router.route(HttpMethod.POST, CHECK_PWD_RESOURCE_PATH).handler(this::checkAccountPassword);

		/* static files */

		router.route("/public/*").handler(StaticHandler.create());

		/* start the server */

		var fut = server.requestHandler(router).listen(port);

		fut.onSuccess(res -> {
			logger.log(Level.INFO, "Account Service ready - port: " + port);
		});

		return fut;
	}

	/* List of handlers mapping the API */

	/**
	 * 
	 * Register a new user
	 * 
	 * @param context
	 */
	protected void createAccount(RoutingContext context) {
		logger.log(Level.INFO, "create a new account");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + userInfo);
			var userName = userInfo.getString("userName");
			var password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				accountService.registerUser(userName, password);
				reply.put("result", "ok");
				var accountPath = ACCOUNT_RESOURCE_PATH.replace(":accountId", userName);
				reply.put("accountLink", accountPath);
				sendReply(context.response(), reply);
			} catch (AccountAlreadyPresentException ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
		});
	}

	/**
	 * 
	 * Get account info
	 * 
	 * @param context
	 */
	protected void getAccountInfo(RoutingContext context) {
		logger.log(Level.INFO, "get account info");
		var userName = context.pathParam("accountId");
		var reply = new JsonObject();
		try {
			var acc = accountService.getAccountInfo(userName);
			reply.put("result", "ok");
			var accJson = new JsonObject();
			accJson.put("userName", acc.getUserName());
			accJson.put("password", acc.getPassword());
			accJson.put("whenCreated", acc.getWhenCreated());
			reply.put("accountInfo", accJson);
			sendReply(context.response(), reply);
		} catch (AccountNotFoundException ex) {
			reply.put("result", "error");
			reply.put("error", "account-not-present");
			sendReply(context.response(), reply);
		} catch (Exception ex1) {
			sendError(context.response());
		}
	}

	/**
	 * 
	 * Get account info
	 * 
	 * @param context
	 */
	protected void checkAccountPassword(RoutingContext context) {
		logger.log(Level.INFO, "check account password");
		context.request().handler(buf -> {
			var userName = context.pathParam("accountId");
			JsonObject userInfo = buf.toJsonObject();
			var password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				var res = accountService.isValidPassword(userName, password);
				if (res) {
					reply.put("result", "valid-password");
				} else {
					reply.put("result", "invalid-password");
				}
				sendReply(context.response(), reply);
			} catch (AccountNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "account-not-present");
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
		});
	}

	/* Aux methods */

	private void sendReply(HttpServerResponse response, JsonObject reply) {
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}

	private void sendError(HttpServerResponse response) {
		response.setStatusCode(500);
		response.putHeader("content-type", "application/json");
		response.end();
	}

}
