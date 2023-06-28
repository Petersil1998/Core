package net.petersil98.core.http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.petersil98.core.Core;
import net.petersil98.core.util.settings.Settings;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPClient {

    private final HttpClient client;
    private static final Cache<String, HttpResponse<String>> CACHE = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(Duration.ofMinutes(10)).build();
    private static final HTTPClient INSTANCE = new HTTPClient();
    private static final Marker MARKER = MarkerManager.getMarker(HTTPClient.class.getSimpleName());

    public static HTTPClient getInstance() {
        return INSTANCE;
    }

    private HTTPClient() {
        this.client = HttpClient.newHttpClient();
    }

    public HttpResponse<String> get(String url, Map<String, String> params) {
        String fullUrl = url+"?"+buildParameters(params);
        if(Settings.useCache()) {
            HttpResponse<String> cachedResponse = CACHE.getIfPresent(fullUrl);
            if (cachedResponse != null) return cachedResponse;
        }
        try {
            URI uri = URI.create(fullUrl);
            HttpResponse<String> response = request(HttpRequest.newBuilder(uri).GET().build());
            if(Settings.useCache() && response.statusCode() >= 200 && response.statusCode() < 300) CACHE.put(fullUrl, response);
            return response;
        } catch (Exception e) {
            Core.LOGGER.error(MARKER, "Failed to perform GET request", e);
        }
        return null;
    }

    private HttpResponse<String> request(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String buildParameters(Map<String, String> params) {
        List<BasicNameValuePair> list = new ArrayList<>();
        params.forEach((k, v) -> list.add(new BasicNameValuePair(k, v)));
        return URLEncodedUtils.format(list, StandardCharsets.UTF_8);
    }
}
