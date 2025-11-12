package ru.damirovna.telegram.core.service.location;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.JSONObject;
import ru.damirovna.telegram.common.URIBuilder;
import ru.damirovna.telegram.core.model.Location;

import java.io.IOException;

public class LocationApiManager {
    private static final String apiToken = System.getenv("YANDEX_MAP_TOKEN");
    private static final String GEOCODE_YANDEX_MAP_URI = "https://geocode-maps.yandex.ru/v1";

    public static String getLocationName(Location location) throws IOException {
        String[][] params = new String[4][2];
        params[0][0] = "geocode";
        params[0][1] = String.valueOf(location.getLongitude()) + ",%20" + String.valueOf(location.getLatitude());
        params[1][0] = "format";
        params[1][1] = "json";
        params[2][0] = "apikey";
        params[2][1] = apiToken;
        params[3][0] = "results";
        params[3][1] = "1";
        Request request = Request.Get(URIBuilder.getURIWithParams(GEOCODE_YANDEX_MAP_URI, params));
        Response res = request.execute();
        JSONObject json = new JSONObject(res.returnContent().toString());
        JSONObject geoObject = new JSONObject(json.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").get(0).toString());
        return geoObject.getJSONObject("GeoObject").get("description").toString();
    }
}
