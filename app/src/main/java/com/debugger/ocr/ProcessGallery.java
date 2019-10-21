package com.debugger.ocr;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessGallery extends AppCompatActivity {


    Bitmap bitmap, singleBitmap, bitmapArray[];
    EditText etText;
    Button btnMultipleImages, btnSingleImage, btnCopy, btnClear, btnDetect;
    ImageView ivScreenShot;
    String numbers;
    Switch scNumber;
    List<Task<FirebaseVisionText>> results;
    Boolean check = false;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        FirebaseApp.initializeApp(this);

        initViews();
        setListeners();


    }

    private void initViews() {

        etText = findViewById(R.id.etText);
        etText.setScroller(new Scroller(getApplicationContext()));
        etText.setVerticalScrollBarEnabled(true);
        etText.setMovementMethod(new ScrollingMovementMethod());
        btnMultipleImages = findViewById(R.id.btnMultipleImages);
        btnDetect = findViewById(R.id.btnDetect);
        ivScreenShot = findViewById(R.id.ivScreenShot);
        btnClear = findViewById(R.id.btnClear);
        btnCopy = findViewById(R.id.btnCopy);
        btnSingleImage = findViewById(R.id.btnSingleImage);
        scNumber = findViewById(R.id.scNumber);
        progressDialog = new ProgressDialog(this);
    }

    private void setListeners() {
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etText.setText("");
                results = null;

            }
        });
        btnSingleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etText.setText("");
                single_pic();
            }
        });
        btnMultipleImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etText.setText("");

                pick_image();
            }
        });
        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                results = new ArrayList<>();
                detectText();
            }
        });
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* numbers = numbers.replace("\n",";");
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + numbers));
                sendIntent.putExtra("address", numbers);
                sendIntent.putExtra("sms_body", "");
                startActivity(sendIntent);*/
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied", etText.getText().toString());
                clipboard.setPrimaryClip(clip);
            }
        });

        scNumber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    check = true;
                else
                    check = false;

            }
        });
    }

    //detect image
    public void detectText() {

        if (singleBitmap == null && bitmap == null) {
            Toast.makeText(getApplicationContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
        } else {


            //img
            if (singleBitmap != null) {

                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(singleBitmap);

                FirebaseApp.initializeApp(this);
                FirebaseVisionTextRecognizer firebaseVisionTextDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                results.add(firebaseVisionTextDetector.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processText(results);
                    }
                }));

            }
        /*   else if(bitmap!=null)
            {
                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

                FirebaseApp.initializeApp(this);
                FirebaseVisionTextRecognizer firebaseVisionTextDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                results = firebaseVisionTextDetector.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processText(results);
                    }
                });
            }*/
            else {

                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                FirebaseApp.initializeApp(this);
                FirebaseVisionTextRecognizer fireBaseVisionTextDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                for (Bitmap bit : bitmapArray) {

                    if (bit != null) {

                        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bit);

                        results.add(fireBaseVisionTextDetector.processImage(firebaseVisionImage).addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>() {
                            @Override
                            public void onComplete(@NonNull Task<FirebaseVisionText> task) {
                                //complete
                                if (task.isSuccessful()) {

                                    if (processText(results)) {
                                        results = null;
                                        return;

                                    }
                                }
                            }
                        }));
                    }

                }
            }

        }
    }

    //detect numbers from image
    public void process_text(FirebaseVisionText firebaseVisionText) {

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();

        if (blocks.size() == 0) {
            Toast.makeText(getApplicationContext(), "Nothing detected", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                numbers = block.getText();
                final Rect boundingBox = block.getBoundingBox();
                etText.setText(numbers);
            }
        }
    }

    public boolean processText(List<Task<FirebaseVisionText>> results) {

        try {
            if (results.size() == bitmapArray.length) {

                List<String> allText = new ArrayList<>();
                for (Task<FirebaseVisionText> result : results) {

                    String resultText = result.getResult().getText();
                    //resultText = resultText.replace(" ", "");
                    resultText = resultText.replace("-", "");
                    resultText = resultText.replace("(", "");
                    resultText = resultText.replace(")", "");
                    resultText = resultText.replace("]", "");
                    String str[] = resultText.split("\n");

                    for (String i : str) {
                        boolean stringCheck = false;
                        for (String j : allText) {


                            if (i.equals(j))
                                stringCheck = true;

                        }
                        if (!stringCheck)
                            if (check && i.matches("^(\\+\\d{1,9}[- ]?)?\\d{3}[ ]?\\d{7}$")) {
                                allText.add(i);
                                allText.add("\n");
                               // Toast.makeText(getApplicationContext(), "Done: matching numbers", Toast.LENGTH_SHORT).show();
                            } else if (!check) {
                                allText.add(i);
                                allText.add("\n");
                            }


                    }


                }
                String finalText = allText.toString().replace("[","");
                finalText = finalText.replace(",","");
                finalText = finalText.replace("]","");
                etText.append(finalText);
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
            }
            return true;
        } catch (Exception ex) {
            Log.d("Issue", ex.getMessage());
            return false;
        }


    }

    public void single_pic() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(i, 2);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(i, 2);
        }
    }

    public void pick_image() {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
                //**These following line is the important one!
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
            /*else
            {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i,1);
            }*/


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==1 && resultCode== RESULT_OK)
        {
            Uri uri = data.getData();
            try {
                //reading image from uri

               final InputStream imageStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(imageStream);
                bitmap = get_rotated(uri,bitmap);


                if(bitmap!=null) {
                    Log.d("displaying","Done");
                    img.setImageBitmap(bitmap);
                    //Toast.makeText(getApplicationContext(),"Image", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d("displaying","none");
                    //Toast.makeText(getApplicationContext(), "No image", Toast.LENGTH_SHORT).show();
                }

            }
            catch (Exception ex)
            {
                Log.d("displaying",ex.getMessage());
            }
        }
    }*/

    private Bitmap mergeMultiple(Bitmap[] parts) {

        Bitmap result = Bitmap.createBitmap(parts[0].getWidth() * 2, parts[0].getHeight() * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] != null)
                canvas.drawBitmap(parts[i], parts[i].getWidth() * (i % 2), parts[i].getHeight() * (i / 2), paint);
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri;
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    bitmapArray = new Bitmap[count];
                    for (int i = 0; i < count; i++) {
                        imageUri = data.getClipData().getItemAt(i).getUri();
                        final InputStream imageStream;
                        try {
                            imageStream = getContentResolver().openInputStream(imageUri);
                            bitmap = BitmapFactory.decodeStream(imageStream);
                            bitmap = get_rotated(imageUri, bitmap);
                            if (bitmap != null && count < 100)
                                bitmapArray[i] = bitmap;


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }


                        //do something with the image (save it to some directory or whatever you need to do with it here)
                    }
                    try {
                        bitmap = mergeMultiple(bitmapArray);
                        singleBitmap = null;
                        ivScreenShot.setImageBitmap(bitmap);
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), "Select max 4 pictures", Toast.LENGTH_SHORT).show();
                        Log.d("issue", ex.getMessage());
                    }

                } else if (data.getData() != null) {
                    String imagePath = data.getData().getPath();
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                }
            }
        } else if (requestCode == 2) {

            try {
                //reading image from uri
                Uri uri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(uri);
                singleBitmap = BitmapFactory.decodeStream(imageStream);
                singleBitmap = get_rotated(uri, singleBitmap);


                if (singleBitmap != null) {
                    Log.d("displaying", "Done");
                    ivScreenShot.setImageBitmap(singleBitmap);
                    bitmap = null;
                    //Toast.makeText(getApplicationContext(),"Image", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("displaying", "none");
                    //Toast.makeText(getApplicationContext(), "No image", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception ex) {
                Log.d("displaying", ex.getMessage());
            }
        }
    }

    public Bitmap get_rotated(Uri uri, Bitmap bitmap1) {
        ExifInterface ei = null;
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            ei = new ExifInterface(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap1, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap1, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap1, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap1;
        }
        return rotatedBitmap;
    }

    public Bitmap rotateImage(Bitmap bit, int angle) {
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(angle);
        Bitmap bmp = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
        return bmp;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bitmapArray = null;
        results = null;
    }
}
