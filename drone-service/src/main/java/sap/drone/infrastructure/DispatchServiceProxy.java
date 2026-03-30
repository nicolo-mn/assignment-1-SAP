package sap.drone.infrastructure;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import sap.common.exagonal.Adapter;
import sap.drone.application.DispatchService;

import java.util.logging.Level;
import java.util.logging.Logger;

@Adapter
public class DispatchServiceProxy implements DispatchService {

    static Logger logger = Logger.getLogger("[DispatchServiceProxy]");

    private final String dispatchURI;

    public DispatchServiceProxy(String dispatchURI) {
        // e.g. "http://localhost:8081"
        this.dispatchURI = dispatchURI;
    }

    @Override
    public void notifyShippingCompleted(String shippingId) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = dispatchURI + "/api/v1/dispatch/shippings/" + shippingId + "/completed";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(Level.INFO, "notifyShippingCompleted " + shippingId
                    + " -> " + response.statusCode() + " " + response.body());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to notify dispatch service for shipping " + shippingId, ex);
        }
    }
}
