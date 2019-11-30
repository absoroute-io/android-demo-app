package com.absoroute.io.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;

import com.absoroute.io.visionai.FaceRecognizer;
import com.absoroute.io.visionai.FaceRecognizer.FaceRecognizerConfig;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static int ML_THREADS = 4;
    static boolean USE_GPU = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // Initialize FaceRecognizer here, so that it'll be ready to use for face registration
        // in FrConfigActivity.
        if (!FaceRecognizer.isInitialized()) {
            // Construct a config object. Usually you only need to set the context and lensFacing
            // for image processing use case, and the callback and lifecycleOwner in addition for
            // video processing use case. Other parameters have sensible default values already.
            FaceRecognizerConfig config = new FaceRecognizerConfig();
            config.context = getApplicationContext();
            config.lensFacing = CameraX.LensFacing.FRONT;
            config.totalThreads = ML_THREADS;
            config.useGpu = USE_GPU;
            FaceRecognizer.initialize(config);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Free up memory used for internal ML models.
        FaceRecognizer.getInstance().stop();
    }

    public void onButtonFrConfigClick(View view) {
        Log.d(TAG, "onButtonFrConfigClick");
        startActivity(new Intent(this, FrConfigActivity.class));
    }

    public void onButtonFrDemoVideoClick(View view) {
        Log.d(TAG, "onButtonFrDemoClick");
        startActivity(new Intent(this, FrDemoActivity.class));
    }

    public void onButtonFrDemoImageClick(View view) {
        Log.d(TAG, "onButtonFrDemoClick");
        // TODO
    }

    public void onButtonFdDemoClick(View view) {
        Log.d(TAG, "onButtonFrDemoClick");
        // TODO
    }

}
