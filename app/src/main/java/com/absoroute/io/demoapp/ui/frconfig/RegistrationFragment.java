package com.absoroute.io.demoapp.ui.frconfig;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.absoroute.io.demoapp.R;
import com.absoroute.io.visionai.FaceRecognizer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class RegistrationFragment extends Fragment {

    private static final String TAG = "RegistrationFragment";
    private static final int REQUEST_CHOOSE_IMAGE = 1;
    private static final int THUMBNAIL_HEIGHT = 300;
    private List<Bitmap> images;
    private View root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        images = new ArrayList<>();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        root = inflater.inflate(R.layout.fragment_fr_config_registration, container, false);

        // set the click callbacks
        Button buttonChooseImg = root.findViewById(R.id.button_choose_img);
        buttonChooseImg.setOnClickListener(v -> {
            Log.d(TAG, "Prompting user to choose images");
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_CHOOSE_IMAGE);
        });

        Button buttonClearSelection = root.findViewById(R.id.button_clear_selection);
        buttonClearSelection.setOnClickListener(v -> {
            Log.d(TAG, String.format("Removing all %d images", images.size()));
            images.clear();
            displayImages(images);
        });

        Button buttonRegisterFace = root.findViewById(R.id.button_register);
        buttonRegisterFace.setOnClickListener(v -> {
            Log.d(TAG, String.format("Registering a face from %d images", images.size()));
            FaceRecognizer.getInstance().registerFace(images);
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();          // will be null if user chose multiple images
            ClipData clipData = data.getClipData(); // will be null if user chose a single image

            if ((imageUri == null) && (clipData != null)) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();
                    images.add(readImageFromUri(uri));
                }
            } else if ((imageUri != null) && (clipData == null)) {
                images.add(readImageFromUri(imageUri));
            } else {
                Log.w(TAG, "User didn't choose any image");
                return;
            }

            displayImages(images);
        }
    }

    Bitmap readImageFromUri(Uri imageUri) {
        Log.d(TAG, "Reading image " + imageUri);
        InputStream imageStream;
        try {
            imageStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap image = BitmapFactory.decodeStream(imageStream);

            // rotate the image if needed.
            ExifInterface exif = new ExifInterface(imageStream);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0) {
                Log.d(TAG, String.format("Rotate image by %d degrees", rotationInDegrees));
                matrix.preRotate(rotationInDegrees);
                image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
            }

            return image;
        } catch (Exception e) {
            Log.w(TAG, "Got " + e.toString());
        }
        return null;
    }

    private int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    private void displayImages(List<Bitmap> images) {
        Log.d(TAG, "Clearing existing thumbnails if any");
        LinearLayout thumbnailLayout = root.findViewById(R.id.layout_registration_thumbnail);
        thumbnailLayout.removeAllViews();

        Log.d(TAG, String.format("Displaying %d images", images.size()));

        for (Bitmap image: images) {
            ImageView imgView = new ImageView(this.getContext());
            imgView.setImageBitmap(image);
            imgView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    THUMBNAIL_HEIGHT
            ));
            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            thumbnailLayout.addView(imgView);
        }
    }

}
