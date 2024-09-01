package com.example.kachin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class SettingActivity extends BaseActivity {

    private TextView tvLanguage;
    private TextView tvHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_setting);

        tvLanguage = findViewById(R.id.tv_language);
        tvHelp = findViewById(R.id.tv_help);
        TextView tvCurrency = findViewById(R.id.tv_currency);

        tvCurrency.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, CurrencyConverter.class);
            startActivity(intent);
        });
        tvLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

        tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });

        // Update texts after setting up the views
        updateTexts();
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Chinese", "Malay"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");
        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String langCode = "";
                switch (which) {
                    case 0:
                        langCode = "en";
                        break;
                    case 1:
                        langCode = "zh";
                        break;
                    case 2:
                        langCode = "ms";
                        break;
                }
                confirmLanguageChange(langCode);
            }
        });
        builder.show();
    }

    private void confirmLanguageChange(final String langCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Language");
        builder.setMessage("Do you want to change the language?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setLocale(langCode);
                Intent intent = new Intent(SettingActivity.this, HomePageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        builder.show();
    }


    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        Context context = createConfigurationContext(config);
        resources.updateConfiguration(config, context.getResources().getDisplayMetrics());

        getSharedPreferences("Settings", MODE_PRIVATE).edit().putString("My_Lang", langCode).apply();

        updateTexts();
    }

    private void loadLocale() {
        String langCode = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "en");
        setLocale(langCode);
    }

    private void updateTexts() {
        if (tvLanguage != null) {
            tvLanguage.setText(R.string.language);
        }

        if (tvHelp != null) {
            tvHelp.setText(R.string.help);
        }
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help");
        builder.setMessage("Welcome to Kachin! Here's how you can use the app. \n\n" +
                "1. Use the bottom navigation bar to quickly access different sections like Home, Add expense/income, History, Report and Profile. \n\n" +
                "2. On the Home screen, you can get a quick overview of your financial status, including a summary of recent transactions and an overview of your budget.\n\n" +
                "3. To add a new expense, tap on the '+' button located at the bottom of the screen. Enter the amount, category, date, and any additional notes, then tap 'Save'.\n\n" +
                "4. You can view all your past expenses by navigating to the 'History' section. \n\n" +
                "5. In the 'Report' section, you can generate detailed financial reports, including expense breakdowns by category, monthly summaries, and more. This helps you analyze your spending patterns and make informed financial decisions.\n\n" +
                "6. In the Profile section, you can manage your settings, logout and edit your profile information.\n\n" +
                "Thank you for using Kachin! We hope you have a great experience.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
