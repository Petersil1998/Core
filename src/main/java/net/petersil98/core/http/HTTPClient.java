package net.petersil98.core.http;

import net.petersil98.core.Core;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * @param params The Parameters. They get included in the Url as GET Parameters
     * @return The Response if the request was successful, {@code null} otherwise
     */
    public HttpResponse<String> get(String url, Map<String, String> params) {
        String fullUrl = url+"?"+buildParameters(params);
        try {
            URI uri = URI.create(fullUrl);
            return request(HttpRequest.newBuilder(uri).GET().build());
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

    /**
     * Utility Method to create the HTTP <b>GET</b> parameters as used in the URL from a given Map
     * @see URLEncodedUtils#format(List, String)
     * @param params The Map of Keys and Values that should get converted
     * @return HTTP <b>GET</b> parameters
     */
    private String buildParameters(Map<String, String> params) {
        List<BasicNameValuePair> list = new ArrayList<>();
        params.forEach((k, v) -> list.add(new BasicNameValuePair(k, v)));
        return URLEncodedUtils.format(list, StandardCharsets.UTF_8);
    }
}
