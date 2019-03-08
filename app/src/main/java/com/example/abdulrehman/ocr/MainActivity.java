package com.example.abdulrehman.ocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Bitmap bitmap;
    EditText textView;
    Button get;
    Button image;
    ImageView img ;
    Button clean;
    Button send_msg;
    String numbers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView= findViewById(R.id.txt);
        get = findViewById(R.id.get);
        image = findViewById(R.id.image);
        img= findViewById(R.id.img1);
        clean = findViewById(R.id.clean);
        send_msg = findViewById(R.id.Send);
        FirebaseApp.initializeApp(this);

        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numbers = numbers.replace("\n",";");
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + numbers));
                sendIntent.putExtra("address", numbers);
                sendIntent.putExtra("sms_body", "");
                startActivity(sendIntent);
            }
        });


    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void detect (View v)
    {
        if(bitmap ==  null)
        {
            Toast.makeText(getApplicationContext(),"Nothing to show", Toast.LENGTH_SHORT).show();
        }
        else
        {
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseApp.initializeApp(this);
            FirebaseVisionTextRecognizer firebaseVisionTextDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            firebaseVisionTextDetector.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    process_text(firebaseVisionText);

                }
            });
        }
    }
    public void process_text(FirebaseVisionText firebaseVisionText)
    {
        List<FirebaseVisionText.TextBlock> blocks= firebaseVisionText.getTextBlocks();
        if(blocks.size()==0)
        {
            Toast.makeText(getApplicationContext(),"Nothing detected", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks())
            {
                numbers = block.getText();
                textView.setText(numbers);
            }
        }
    }
    public void pick_image(View v)
    {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==1 && resultCode== RESULT_OK)
        {
            Uri uri = data.getData();
            try {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                Matrix matrix = new Matrix();
                matrix.postRotate(90);

               bitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                img.setImageBitmap(bitmap);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }


}
