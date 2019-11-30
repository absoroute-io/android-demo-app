package com.absoroute.io.demoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.absoroute.io.visionai.FaceRecognizer;
import com.absoroute.io.visionai.RecognizedFace;


public class FrDemoActivity extends AppCompatActivity {

    private static final String TAG = "FrDemoActivity";
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private static final int REQUEST_CODE_PERMISSIONS = 42;
    private TextureView textureView;
    private TextView textView;
    private Long lastTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_fr_demo);
        textureView = findViewById(R.id.view_preview);
        textView = findViewById(R.id.text_fr_label);
        lastTimestamp = System.currentTimeMillis();

        if (allPermissionGranted()) {
            initialize();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Free up memory used for internal ML models.
        FaceRecognizer.getInstance().stop();
    }

    private boolean allPermissionGranted() {
        for (String permission: REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            initialize();
        } else {
            Log.w(TAG, "User denied permissions.");
            finish();
        }
    }

    private void initialize() {
        // Start CameraX for preview
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(1, 1))
                .setLensFacing(CameraX.LensFacing.FRONT)
                .build();
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(output -> {
            ConstraintLayout constraintLayout = (ConstraintLayout) textureView.getParent();
            constraintLayout.removeView(textureView);
            textureView.setSurfaceTexture(output.getSurfaceTexture());
            constraintLayout.addView(textureView, 0);
            updateTransform();
        });
        CameraX.bindToLifecycle(this, preview);

        // (Re)initialize FaceRecognizer for video processing
        FaceRecognizer.FaceRecognizerConfig config = new FaceRecognizer.FaceRecognizerConfig();
        config.context = getApplicationContext();
        config.lensFacing = CameraX.LensFacing.FRONT;
        config.totalThreads = MainActivity.ML_THREADS;
        config.useGpu = MainActivity.USE_GPU;
        config.lifecycleOwner = this;
        config.callback = faces -> {
            for (RecognizedFace face: faces) {
                Long latency = System.currentTimeMillis() - lastTimestamp;
                lastTimestamp = System.currentTimeMillis();
                String msg = String.format("Detected %s, latency %d ms", face.id, latency);
                Log.d(TAG, msg);
                textView.setText(msg);
            }
        };
        FaceRecognizer.initialize(config);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();

        // find the center
        float centerX = textureView.getWidth() / 2f;
        float centerY = textureView.getHeight() / 2f;

        // rotate screen
        int rotation = 0;
        switch ((int) textureView.getRotation()) {
            case Surface.ROTATION_0:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
                break;
            default:
                rotation = 0;
                break;
        }

        matrix.postRotate(-rotation, centerX, centerY);
        textureView.setTransform(matrix);
    }

}
