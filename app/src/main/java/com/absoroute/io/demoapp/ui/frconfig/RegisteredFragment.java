package com.absoroute.io.demoapp.ui.frconfig;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.absoroute.io.demoapp.R;
import com.absoroute.io.visionai.FaceRecognizer;

import java.util.List;

public class RegisteredFragment extends Fragment {

    private static final String TAG = "RegisteredFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_fr_config_registered, container, false);
        final LinearLayout linearLayoutScrollView = root.findViewById(R.id.layout_registered_thumbnail);
        List<String> ids = FaceRecognizer.getInstance().getRegisteredFaceIds();

        for (final String id: ids) {
            Log.d(TAG, "Adding a View for id " + id);
            String buttonStr = "FaceId: " + id + " (Click to delete)";
            Button button = new Button(this.getContext());
            button.setText(buttonStr);
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            button.setGravity(1); // center_horizontal
            button.setOnClickListener(
                    v -> {
                        Log.d(TAG, "Deleting faceId: " + id);
                        FaceRecognizer.getInstance().deleteRegisteredFace(id);
                        linearLayoutScrollView.removeView(v);
                    }
            );
            linearLayoutScrollView.addView(button);
        }

        return root;
    }

}
