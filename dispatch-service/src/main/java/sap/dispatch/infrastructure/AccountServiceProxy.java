package sap.dispatch.infrastructure;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import io.vertx.core.json.JsonObject;
import sap.common.exagonal.Adapter;
import sap.dispatch.application.AccountService;
import sap.dispatch.application.ServiceNotAvailableException;

@Adapter
public class AccountServiceProxy implements AccountService {

    private final String serviceURI;

    public AccountServiceProxy(String serviceURI) {
        this.serviceURI = serviceURI;
    }

    @Override
    public boolean isValidPassword(String userName, String password) throws ServiceNotAvailableException {
        try {
            HttpClient client = HttpClient.newHttpClient();

            JsonObject body = new JsonObject();
            body.put("password", password);

            String url = serviceURI + "/api/v1/accounts/" + userName + "/check-pwd";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = new JsonObject(response.body());
                var res = json.getString("result");
                return "valid-password".equals(res);
            } else {
                return false;
            }
        } catch (Exception ex) {
            throw new ServiceNotAvailableException();
        }
    }
}
