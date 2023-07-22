package net.petersil98.core.util;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Util {

    /**
     * Utility Method to create the HTTP <b>GET</b> parameters as used in the URL from a given Map
     * @see URLEncodedUtils#format(List, String)
     * @param params The Map of Keys and Values that should get converted
     * @return HTTP <b>GET</b> parameters
     */
    public static String buildParameters(Map<String, String> params) {
        List<BasicNameValuePair> list = new ArrayList<>();
        params.forEach((k, v) -> list.add(new BasicNameValuePair(k, v)));
        return URLEncodedUtils.format(list, StandardCharsets.UTF_8);
    }
}
