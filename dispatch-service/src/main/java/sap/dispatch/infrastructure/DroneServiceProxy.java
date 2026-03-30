package sap.dispatch.infrastructure;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import io.vertx.core.json.JsonObject;
import sap.common.exagonal.Adapter;
import sap.dispatch.application.DroneService;
import sap.dispatch.application.ShippingAlreadyPresentException;
import sap.dispatch.application.ShippingNotFoundException;
import sap.dispatch.domain.Position;

import java.util.logging.Level;
import java.util.logging.Logger;

@Adapter
public class DroneServiceProxy implements DroneService {

    static Logger logger = Logger.getLogger("[DroneServiceProxy]");

    private final String droneURI;

    public DroneServiceProxy(String droneURI) {
        // e.g. "http://localhost:9001"
        this.droneURI = droneURI;
    }

    @Override
    public void createNewShipping(String shippingId, Position pickupPosition, Position deliveryPosition)
            throws ShippingAlreadyPresentException {
        try {
            HttpClient client = HttpClient.newHttpClient();

            JsonObject body = new JsonObject();
            body.put("shippingId", shippingId);
            body.put("pickupX", pickupPosition.x());
            body.put("pickupY", pickupPosition.y());
            body.put("deliveryX", deliveryPosition.x());
            body.put("deliveryY", deliveryPosition.y());

            String url = droneURI + "/api/shippings";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(Level.INFO, "createNewShipping response: " + response.statusCode() + " " + response.body());

            if (response.statusCode() == 400) {
                throw new ShippingAlreadyPresentException();
            }
        } catch (ShippingAlreadyPresentException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error reaching drone at " + droneURI, ex);
        }
    }

    @Override
    public void startShipping(String shippingId) throws ShippingNotFoundException {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = droneURI + "/api/shippings/" + shippingId + "/start";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(""))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(Level.INFO, "startShipping response: " + response.statusCode() + " " + response.body());

            if (response.statusCode() == 404) {
                throw new ShippingNotFoundException();
            }
        } catch (ShippingNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error reaching drone at " + droneURI, ex);
        }
    }
}
