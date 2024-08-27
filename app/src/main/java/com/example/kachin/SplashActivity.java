package com.example.kachin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SplashActivity extends AppCompatActivity {

    private ImageView coinImage;
    private TextView kachinText;
    private ConstraintLayout splashLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        coinImage = findViewById(R.id.coinImage);
        kachinText = findViewById(R.id.kachinText);
        splashLayout = findViewById(R.id.splashLayout);

        // Ensure the background color is set immediately
        splashLayout.setBackgroundColor(Color.parseColor("#00FF00"));

        // Optional: Fade in the green background if needed
        AlphaAnimation fadeInBackground = new AlphaAnimation(0.0f, 1.0f);
        fadeInBackground.setDuration(500); // 0.5 second
        splashLayout.startAnimation(fadeInBackground);

        // Get screen width for translation
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Translate animation: Move coin from left to center
        TranslateAnimation translate = new TranslateAnimation(
                -screenWidth, 0,  // Move from off-screen left to center
                0, 0);            // Vertical position remains the same
        translate.setDuration(3000);    // 3 seconds duration
        translate.setFillAfter(true);   // Keep the coin in its final position

        // Rotate animation: Simulate rolling effect
        RotateAnimation rotate = new RotateAnimation(
                0, 1080,                       // 3 full rotations
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(3000);       // Match duration with translation
        rotate.setFillAfter(true);      // Keep the rotation angle

        // Combine animations in a set
        AnimationSet coinAnimationSet = new AnimationSet(true);
        coinAnimationSet.addAnimation(translate);
        coinAnimationSet.addAnimation(rotate);

        // Start coin animation
        coinImage.startAnimation(coinAnimationSet);

        // Listen for when the coin animation ends
        coinAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                kachinText.setVisibility(View.GONE);  // Hide the text initially
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                kachinText.setVisibility(View.VISIBLE);  // Show the text

                // Create a fade-in and scale-up animation for the text
                AnimationSet kachinAnimationSet = new AnimationSet(true);

                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(1000); // 1 second duration

                ScaleAnimation scaleUp = new ScaleAnimation(
                        0.5f, 1.0f,  // Scale from 50% to 100%
                        0.5f, 1.0f,  // Scale from 50% to 100%
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                scaleUp.setDuration(1000); // 1 second duration

                kachinAnimationSet.addAnimation(fadeIn);
                kachinAnimationSet.addAnimation(scaleUp);

                kachinText.startAnimation(kachinAnimationSet);  // Start text animation
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not used in this context
            }
        });
    }
}
