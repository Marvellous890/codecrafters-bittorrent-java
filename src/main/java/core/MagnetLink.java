package core;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MagnetLink {
    private String infoHash;
    private String trackerUrl;

    public MagnetLink(String magnetLink) {
        parseMagnetLink(magnetLink);
    }

    private void parseMagnetLink(String magnetLink) {
        if (!magnetLink.startsWith("magnet:?")) {
            throw new IllegalArgumentException("Invalid magnet link format");
        }
        String[] params = magnetLink.substring(8).split("&");
        Map<String, String> queryParams = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                queryParams.put(key, value);
            }
        }
        this.infoHash = queryParams.getOrDefault("xt", "").replace("urn:btih:", "");
        this.trackerUrl = queryParams.get("tr");
    }

    public String getInfoHash() {
        return infoHash;
    }

    public String getTrackerUrl() {
        return trackerUrl;
    }
}
