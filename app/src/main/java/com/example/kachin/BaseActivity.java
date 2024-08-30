package com.example.kachin;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(applyLocale(newBase));
    }

    private Context applyLocale(Context context) {
        String langCode = context.getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "en");
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}

