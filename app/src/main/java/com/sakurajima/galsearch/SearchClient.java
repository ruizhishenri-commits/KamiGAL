package com.sakurajima.galsearch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchClient {
    private static final String API = "https://cf.api.searchgal.top/gal";
    private static final String VNDB = "https://api.vndb.org/kana/vn";
    private static final String UA = "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36";

    private final Gson gson = new Gson();

    // 搜索结果模型
    public static class Item {
        public String name;
        public String url;
        public Item() {}
        public Item(String name, String url) { this.name = name; this.url = url; }
    }

    public static class PlatformResult {
        public String platform;
        public List<String> tags;
        public List<Item> items;
        public PlatformResult() {}
        public PlatformResult(String platform, List<String> tags, List<Item> items) {
            this.platform = platform; this.tags = tags; this.items = items;
        }
    }

    public static class VndbInfo {
        public String title;
        public String imageUrl;
        public Double rating;
        public String released;
        public String description;
        public String id;
        public VndbInfo() {}
        public VndbInfo(String title, String imageUrl, Double rating, String released, String description, String id) {
            this.title = title; this.imageUrl = imageUrl; this.rating = rating;
            this.released = released; this.description = description; this.id = id;
        }
    }

    // 搜索SearchGal
    public List<PlatformResult> searchGal(String query, boolean isPatch) throws Exception {
        String mode = isPatch ? "patch" : "game";
        String encoded = URLEncoder.encode(query, "UTF-8");
        String body = "game=" + encoded + "&mode=" + mode;

        HttpURLConnection conn = (HttpURLConnection) new URL(API).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", UA);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Origin", "https://searchgal.top");
        conn.setRequestProperty("Referer", "https://searchgal.top/");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(body);
        writer.flush();
        writer.close();

        int code = conn.getResponseCode();
        if (code != 200) throw new Exception("HTTP " + code);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        reader.close();
        conn.disconnect();

        return parseGalResults(sb.toString());
    }

    private List<PlatformResult> parseGalResults(String json) {
        List<PlatformResult> results = new ArrayList<>();
        java.util.Set<String> seenPlatforms = new java.util.HashSet<>();
        String[] lines = json.trim().split("\n");
        for (String line : lines) {
            try {
                Map<String, Object> obj = gson.fromJson(line, Map.class);
                if (obj == null || !obj.containsKey("result")) continue;
                Map<String, Object> result = (Map<String, Object>) obj.get("result");
                if (result == null) continue;
                if (result.containsKey("error") && result.get("error") instanceof String && !((String) result.get("error")).isEmpty()) continue;
                List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) result.get("items");
                if (itemsRaw == null || itemsRaw.isEmpty()) continue;
                String platName = (String) result.get("name");
                if (platName == null || platName.isEmpty()) continue;
                // 平台名去重（合并items）
                if (seenPlatforms.contains(platName)) continue;
                seenPlatforms.add(platName);

                List<String> tags = (List<String>) result.get("tags");
                if (tags == null) tags = new ArrayList<>();
                List<Item> items = new ArrayList<>();
                java.util.Set<String> seenUrls = new java.util.HashSet<>();
                for (Map<String, Object> item : itemsRaw) {
                    String n = (String) item.get("name");
                    String u = (String) item.get("url");
                    if (n != null && u != null && seenUrls.add(u)) items.add(new Item(n, u));
                }
                results.add(new PlatformResult(platName, tags, items));
            } catch (Exception ignored) {}
        }
        return results;
    }

    // 搜索VNDB
    public VndbInfo searchVndb(String query) throws Exception {
        String safeQuery = query.replace("\"", "\\\"");
        String jsonBody = "{\"filters\":[\"search\",\"=\",\"" + safeQuery + "\"],\"fields\":\"id,title,image.url,rating,released,description\",\"results\":1}";

        HttpURLConnection conn = (HttpURLConnection) new URL(VNDB).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", UA);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.getOutputStream().write(jsonBody.getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();

        Map<String, Object> resp = gson.fromJson(sb.toString(), Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
        if (results == null || results.isEmpty()) return null;

        Map<String, Object> vn = results.get(0);
        String title = (String) vn.get("title");
        String id = (String) vn.get("id");
        String released = (String) vn.get("released");
        String description = (String) vn.get("description");
        Double rating = vn.get("rating") instanceof Double ? (Double) vn.get("rating") : null;
        String imgUrl = null;
        Map<String, Object> img = (Map<String, Object>) vn.get("image");
        if (img != null) imgUrl = (String) img.get("url");

        return new VndbInfo(title, imgUrl, rating, released, description, id);
    }
}
