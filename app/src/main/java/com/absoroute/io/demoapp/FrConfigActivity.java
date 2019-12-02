package com.absoroute.io.demoapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.absoroute.io.demoapp.ui.frconfig.SectionsPagerAdapter;
import com.absoroute.io.visionai.FaceRecognizer;
import com.google.android.material.tabs.TabLayout;

public class FrConfigActivity extends AppCompatActivity {

    private static final String TAG = "FrConfigActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Initialize FaceRecognizer here, so that it'll be ready to use for face registration
        if (!FaceRecognizer.isInitialized()) {
            // Construct a config object. Usually you only need to set the context to use it for
            // face registration.
            FaceRecognizer.FaceRecognizerConfig config = new FaceRecognizer.FaceRecognizerConfig();
            config.context = getApplicationContext();
            config.totalThreads = MainActivity.ML_THREADS;
            config.useGpu = MainActivity.USE_GPU;
            FaceRecognizer.initialize(config);
        }

        setContentView(R.layout.activity_fr_config);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }
}