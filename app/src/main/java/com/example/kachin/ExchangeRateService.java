package com.example.kachin;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;

public class ExchangeRateService {
    private static final String BASE_URL = "https://api.freecurrencyapi.com/v1/";
    private static final String API_KEY = "fca_live_VZtc7lMAyv3LUM1z0Yuj6A3FXXLJeKrd8tQ1kIUB";
    private OkHttpClient client;
    private Gson gson;

    public ExchangeRateService() {
        client = new OkHttpClient();
        gson = new Gson();
    }

    public ExchangeRateResponse getExchangeRate(String baseCurrency) throws IOException {
        String url = BASE_URL + "latest?apikey=" + API_KEY + "&base_currency=" + baseCurrency;
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            return gson.fromJson(response.body().string(), ExchangeRateResponse.class);
        }
    }
}

class ExchangeRateResponse {
    public Map<String, Double> data;
}