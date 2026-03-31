package drone_delivery_tests.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import io.vertx.core.json.JsonObject;

public class DeliverySteps {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String lastResponseString;
    private int lastResponseStatusCode;
    private String currentSessionId;
    
    // --- Registering an account ---

    @Given("the account service is running")
    public void the_account_service_is_running() {
        // Assume it's running on localhost:8080
    }

    @When("a user registers with username {string} and password {string}")
    public void a_user_registers_with_username_and_password(String username, String password) throws Exception {
        JsonObject payload = new JsonObject().put("userName", username).put("password", password);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.encode()))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        lastResponseStatusCode = response.statusCode();
        lastResponseString = response.body();
    }

    @Then("the account should be created successfully")
    public void the_account_should_be_created_successfully() {
        Assertions.assertEquals(200, lastResponseStatusCode);
        JsonObject json = new JsonObject(lastResponseString);
        Assertions.assertEquals("ok", json.getString("result"));
    }

    @Then("I can get info for account {string}")
    public void i_can_get_info_for_account(String username) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/accounts/" + username))
                .GET()
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject json = new JsonObject(response.body());
        Assertions.assertEquals("ok", json.getString("result"));
        Assertions.assertNotNull(json.getJsonObject("accountInfo"));
    }

    // --- User logging in ---

    @Given("the dispatch service and account service are running")
    public void the_dispatch_service_and_account_service_are_running() {
        // Assume running on 8080 and 8081
    }

    @Given("an account exists with username {string} and password {string}")
    public void an_account_exists_with_username_and_password(String username, String password) throws Exception {
        // Try to create it, ignore 400 if already exists
        a_user_registers_with_username_and_password(username, password);
    }

    @When("user {string} logs in with password {string}")
    public void user_logs_in_with_password(String username, String password) throws Exception {
        JsonObject payload = new JsonObject().put("userName", username).put("password", password);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/v1/dispatch/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.encode()))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        lastResponseStatusCode = response.statusCode();
        lastResponseString = response.body();
    }

    @Then("the login is successful and a session ID is returned")
    public void the_login_is_successful_and_a_session_id_is_returned() {
        Assertions.assertEquals(200, lastResponseStatusCode);
        JsonObject json = new JsonObject(lastResponseString);
        Assertions.assertEquals("ok", json.getString("result"));
        Assertions.assertNotNull(json.getString("sessionId"));
        currentSessionId = json.getString("sessionId");
    }

    // --- Creating a shipping request ---

    @Given("all services are running")
    public void all_services_are_running() {
        // Assume 8080, 8081, 9001
    }

    @Given("a user {string} is logged in with session ID {string}")
    public void a_user_is_logged_in_with_session_id(String username, String sessionIdAssumed) throws Exception {
        an_account_exists_with_username_and_password(username, "pass");
        user_logs_in_with_password(username, "pass");
        // We use the real dynamically generated currentSessionId over the dummy one in the feature file
    }

    @When("the user requests a shipping from {double}, {double} to {double}, {double}")
    public void the_user_requests_a_shipping_from_to(Double px, Double py, Double dx, Double dy) throws Exception {
        JsonObject payload = new JsonObject()
                .put("pickupX", px).put("pickupY", py)
                .put("deliveryX", dx).put("deliveryY", dy)
                .put("timeLimit", 1000)
                .put("weight", 50);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/v1/dispatch/user-sessions/" + currentSessionId + "/shippings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.encode()))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        lastResponseStatusCode = response.statusCode();
        lastResponseString = response.body();
    }

    @Then("a shipping ID and assigned drone URI should be returned")
    public void a_shipping_id_and_assigned_drone_uri_should_be_returned() {
        Assertions.assertEquals(200, lastResponseStatusCode);
        JsonObject json = new JsonObject(lastResponseString);
        Assertions.assertEquals("ok", json.getString("result"));
        Assertions.assertNotNull(json.getString("shippingId"));
        Assertions.assertNotNull(json.getString("assignedDroneUri"));
    }
}