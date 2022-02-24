package com.debugger.ocr.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.debugger.ocr.R;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private EditText etText;
    private Button btnMultipleImages, btnCopy, btnClear;
    private TextRecognizer recognizer;
    private static final String TAG = GalleryActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initViews();
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        setListeners();
    }

    private void initViews() {
        etText = findViewById(R.id.etText);
        etText.setScroller(new Scroller(getApplicationContext()));
        etText.setVerticalScrollBarEnabled(true);
        etText.setMovementMethod(new ScrollingMovementMethod());
        btnMultipleImages = findViewById(R.id.btnMultipleImages);
        btnClear = findViewById(R.id.btnClear);
        btnCopy = findViewById(R.id.btnCopy);
    }

    private void setListeners() {
        btnClear.setOnClickListener(v -> etText.setText(""));
        btnMultipleImages.setOnClickListener(v -> {
            etText.setText("");
            selectImages();
        });

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied", etText.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(),"Copied Successfully", Toast.LENGTH_SHORT).show();
        });
    }

    public void selectImages() {
        try {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
            //**These following line is the important one!
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Images"), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            List<InputImage> imageList = new ArrayList<>();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                Log.d(TAG, "onActivityResult: " + count);
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    addImages(imageUri, imageList);
                }
                processImages(imageList);
            } else if (data.getData() != null) {
                Uri imagePath = data.getData();
                Log.d(TAG, "onActivityResult: " + imagePath);
                addImages(imagePath, imageList);
                processImages(imageList);
            }
        }
    }

    private void addImages(Uri imageUri, List<InputImage> imageList) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

            InputImage inputImage = InputImage.fromBitmap(bitmap, fixRotation(imageUri));
            imageList.add(inputImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processImages(List<InputImage> imageList) {
        if (imageList != null && !imageList.isEmpty())
            for (InputImage inputImage : imageList) {
                recognizer.process(inputImage)
                        .addOnSuccessListener(visionText -> {
                            Log.d("processImages", "onSuccess: " + visionText.getText());
                            etText.setText(etText.getText().toString().concat(visionText.getText()));
                        })
                        .addOnFailureListener(e -> Log.d("processImages", "onFailure: " + e));
            }
    }

    public int fixRotation(@NonNull Uri uri) {
        ExifInterface ei;
        int fixOrientation = 0;
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            ei = new ExifInterface(input);

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    fixOrientation = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    fixOrientation = 80;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    fixOrientation = 270;
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    fixOrientation = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fixOrientation;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
