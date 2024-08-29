package com.example.kachin;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView tvLanguage = findViewById(R.id.tv_language);
        tvLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

        TextView tvHelp = findViewById(R.id.tv_help);
        tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Chinese", "Malay"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");
        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        setLocale("en");
                        break;
                    case 1:
                        setLocale("zh");
                        break;
                    case 2:
                        setLocale("ms");
                        break;
                }
            }
        });
        builder.show();
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        Log.d("Locale", "Setting locale to: " + langCode);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        resources.updateConfiguration(config, dm);
        getSharedPreferences("Settings", MODE_PRIVATE).edit().putString("My_Lang", langCode).apply();
        recreate();
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help");
        builder.setMessage("Welcome to Kachin! Here's how you can use the app. \n\n" +
                "1.Use the bottom navigation bar to quickly access different sections like Home, Add expense/income, History, Report and Profile. \n\n" +
                "2.On the Home screen, you can get a quick overview of your financial status, including a summary of recent transactions and an overview of your budget.\n\n" +
                "3.To add a new expense, tap on the '+' button located at the bottom of the screen. Enter the amount, category, date, and any additional notes, then tap 'Save'.\n\n"+
                "4.You can view all your past expenses by navigating to the 'History' section. \n\n"+
                "5.In the 'Report' section, you can generate detailed financial reports, including expense breakdowns by category, monthly summaries, and more. This helps you analyze your spending patterns and make informed financial decisions.\n\n" +
                "6.In the Profile section, you can manage your settings, logout and edit your profile information.\n\n" +
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
