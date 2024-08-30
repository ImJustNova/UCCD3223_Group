package com.example.kachin;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;

public class ExchangeRateService {
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String API_KEY = "f87de2c26e7fc58cbc425fb96bf99e5c";
    private OkHttpClient client;
    private Gson gson;

    public ExchangeRateService() {
        client = new OkHttpClient();
        gson = new Gson();
    }

    public ExchangeRateResponse getExchangeRate(String baseCurrency) throws IOException {
        String url = BASE_URL + API_KEY + "/latest/" + baseCurrency;
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Parse the response to your custom response object
            return gson.fromJson(response.body().string(), ExchangeRateResponse.class);
        }
    }
}

class ExchangeRateResponse {
    public String base_code;
    public String time_last_update_utc;
    public String time_next_update_utc;
    public String time_eol_unix;
    public Map<String, Double> conversion_rates;
}
