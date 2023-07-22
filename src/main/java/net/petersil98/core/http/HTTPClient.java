package net.petersil98.core.http;

import net.petersil98.core.Core;
import net.petersil98.core.util.settings.Settings;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This class represents an HTTP Client and is used by {@link RiotAPI}. It uses the {@link HttpClient} internally to make the Requests.
 */
public class HTTPClient {

    private final HttpClient client;
    private static final HTTPClient INSTANCE = new HTTPClient();
    private static final Marker MARKER = MarkerManager.getMarker(HTTPClient.class.getSimpleName());

    /**
     * Gets an Instance of the HTTP Client using the <b>Singleton</b> Pattern.
     * @return Instance of the HTTP Client
     */
    public static HTTPClient getInstance() {
        return INSTANCE;
    }

    /**
     * Private Constructor
     */
    private HTTPClient() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Method used to perform a <b>GET</b> Request
     * @param url The Url
     * @return The Response if the request was successful, {@code null} otherwise
     */
    public HttpResponse<String> get(String url) {
        try {
            URI uri = URI.create(url);
            return request(HttpRequest.newBuilder(uri)
                    .header("X-Riot-Token", Settings.getAPIKey())
                    .GET()
                    .build());
        } catch (Exception e) {
            Core.LOGGER.error(MARKER, "Failed to perform GET request", e);
        }
        return null;
    }

    /**
     * Utility Method to make the actual Request
     * @see HttpClient#send(HttpRequest, HttpResponse.BodyHandler)
     * @param request The Request that should be sent
     * @return The Response as a String
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    private HttpResponse<String> request(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
