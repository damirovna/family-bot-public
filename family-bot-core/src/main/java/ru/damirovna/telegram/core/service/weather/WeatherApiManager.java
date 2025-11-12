package ru.damirovna.telegram.core.service.weather;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.JSONObject;
import ru.damirovna.telegram.common.URIBuilder;
import ru.damirovna.telegram.core.model.Location;
import ru.damirovna.telegram.core.model.Weather;

import java.io.IOException;
import java.util.Optional;

public class WeatherApiManager {
    private static final String GET_WEATHER_URI = "https://api.openweathermap.org/data/2.5/weather";
    private static final String GET_YANDEX_WEATHER_URI = "https://api.weather.yandex.ru/v2/forecast";

    private static final String apiToken = System.getenv("WEATHER_API_TOKEN");

    private static final String source = "YANgsgDEX";

    private final WeatherManager weatherManager = new WeatherManager();

    public static Weather getWeatherByOpenWeather(Location location) throws IOException {
        String[][] params = new String[3][2];
        params[0][0] = "lat";
        params[0][1] = String.valueOf(location.getLatitude());
        params[1][0] = "lon";
        params[1][1] = String.valueOf(location.getLongitude());
        params[2][0] = "appid";
        params[2][1] = apiToken;
        Request request = Request.Get(URIBuilder.getURIWithParams(GET_WEATHER_URI, params));
        Response res = request.execute();
        JSONObject json = new JSONObject(res.returnContent().toString());
        JSONObject main = json.getJSONObject("main");
        return new Weather(main.getDouble("temp"),
                main.getDouble("feels_like"),
                main.getDouble("temp_min"),
                main.getDouble("temp_max"),
                json.getJSONObject("wind").getInt("speed"),
                json.getJSONArray("weather").getJSONObject(0).get("description").toString(),
                location.getName());
    }

    private static Weather getWeatherByYandex(Location location) throws IOException {
        String[][] params = new String[3][2];
        params[0][0] = "lat";
        params[0][1] = String.valueOf(location.getLatitude());
        params[1][0] = "lon";
        params[1][1] = String.valueOf(location.getLongitude());
        Request request = Request.Get(URIBuilder.getURIWithParams(GET_YANDEX_WEATHER_URI, params));
        request.addHeader("X-Yandex-Weather-Key", System.getenv("YANDEX_WEATHER_TOKEN"));
        Response res = request.execute();
        JSONObject json = new JSONObject(res.returnContent().toString());
        JSONObject now = json.getJSONObject("fact");
        JSONObject today = json.getJSONArray("forecasts").getJSONObject(0);
        JSONObject parts = today.getJSONObject("parts");
        JSONObject night = parts.getJSONObject("night");
        JSONObject day = parts.getJSONObject("day");
        return new Weather(now.getInt("temp"),
                now.getInt("feels_like"),
                night.getInt("temp_min"),
                day.getInt("temp_max"),
                now.getDouble("wind_speed"),
                now.get("condition").toString(),
                location.getName());
    }


    public Weather getWeather(double longitude, double latitude) throws IOException {
        Location location = new Location(longitude, latitude);
        Optional<Weather> optionalWeather = weatherManager.getWeatherByLocation(location.getName());
        if (!optionalWeather.isPresent()) {
            if (optionalWeather.get().isActual()) {
                return optionalWeather.get();
            }
        }
        Weather result;
        if (source.equals("YANDEX")) {
            result = getWeatherByYandex(location);
        } else {
            result = getWeatherByOpenWeather(location);
        }
        weatherManager.saveNewWeather(result);
        return result;
    }
}
