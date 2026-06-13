package com.shuqiang.captain.timer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 计时器频道天气数据源：优先当前位置，定位不可用时用默认坐标保证页面仍有温度信息。
public final class TimerWeatherRepository {
    public interface Callback {
        void onWeatherLoaded(@NonNull TimerWeatherInfo weatherInfo);

        void onWeatherFailed();
    }

    private static final Coordinate DEFAULT_COORDINATE = new Coordinate(31.2304, 121.4737, true);
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(12, TimeUnit.SECONDS)
            .build();

    public void load(@NonNull Context context, @NonNull Callback callback) {
        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                TimerWeatherInfo weatherInfo = requestWeather(resolveCoordinate(appContext));
                callback.onWeatherLoaded(weatherInfo);
            } catch (Exception ignored) {
                callback.onWeatherFailed();
            }
        }, "timer-weather").start();
    }

    private Coordinate resolveCoordinate(Context context) {
        Location location = getLastKnownLocation(context);
        if (location == null) {
            return DEFAULT_COORDINATE;
        }
        return new Coordinate(location.getLatitude(), location.getLongitude(), false);
    }

    @Nullable
    private Location getLastKnownLocation(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return null;
            }
            Location networkLocation = null;
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            Location gpsLocation = null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            return pickNewerLocation(networkLocation, gpsLocation);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Nullable
    private Location pickNewerLocation(@Nullable Location first, @Nullable Location second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.getTime() >= second.getTime() ? first : second;
    }

    private TimerWeatherInfo requestWeather(Coordinate coordinate) throws IOException {
        String url = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=%.4f&longitude=%.4f"
                        + "&current=temperature_2m,weather_code,is_day"
                        + "&timezone=auto&forecast_days=1",
                coordinate.latitude,
                coordinate.longitude);
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "CaptainTimer/1.0")
                .get()
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("weather request failed: " + response.code());
            }
            JSONObject current = new JSONObject(response.body().string()).getJSONObject("current");
            int temperature = (int) Math.round(current.optDouble("temperature_2m"));
            int weatherCode = current.optInt("weather_code", -1);
            Boolean day = current.has("is_day") ? current.optInt("is_day") == 1 : null;
            return new TimerWeatherInfo(TimerWeatherCodeMapper.map(weatherCode),
                    temperature,
                    day,
                    coordinate.defaultLocation);
        } catch (Exception exception) {
            if (exception instanceof IOException) {
                throw (IOException) exception;
            }
            throw new IOException(exception);
        }
    }

    private static final class Coordinate {
        private final double latitude;
        private final double longitude;
        private final boolean defaultLocation;

        private Coordinate(double latitude, double longitude, boolean defaultLocation) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.defaultLocation = defaultLocation;
        }
    }
}
