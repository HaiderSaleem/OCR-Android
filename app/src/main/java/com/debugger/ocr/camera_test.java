package com.debugger.ocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import androidx.annotation.NonNull;

public class camera_test extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor {

    private SurfaceView cameraView;
    private TextView txtView;
    private CameraSource cameraSource;
    private EditText txt1;
    Button clear;
    Button send;
    String numbers;
    StringBuilder strBuilder1;
    Button dial;
    Boolean check= false;
    Button regex;
    ScrollView scrollView;
    Canvas canvasDrawingPane;
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (Exception e) {

                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_ocr);
        cameraView = findViewById(R.id.surface_view);
        txtView = findViewById(R.id.txtview);
        txt1 = findViewById(R.id.txt1);
        clear = findViewById(R.id.clear);
        send = findViewById(R.id.send);
        dial = findViewById(R.id.dial);
        regex = findViewById(R.id.regex);
        scrollView = findViewById(R.id.scroll_view);
        Bitmap bit = Bitmap.createBitmap(408, 636, Bitmap.Config.ARGB_8888);
        canvasDrawingPane = new Canvas(bit);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt1.setText("");
            }
        });
        dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallToMetallurgGates();
            }
        });
        regex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!check)
                    check = true;
                else
                    check = false;
            }
        });


        TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!txtRecognizer.isOperational()) {
            Log.e("Main Activity", "Detector dependencies are not yet available");
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), txtRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(this);
            txtRecognizer.setProcessor(this);

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
                return;
            }
            cameraSource.start(cameraView.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraSource.stop();
    }

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections detections) {

        SparseArray items = detections.getDetectedItems();
        Rect[] rec = new Rect[items.size()];

        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < items.size(); i++)
        {
            TextBlock item = (TextBlock)items.valueAt(i);
            canvasDrawingPane.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
             rec[i] = item.getBoundingBox();
             Log.d("rectangles", rec[i].toString());
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(5);
            canvasDrawingPane.drawRect(rec[i], paint);

            strBuilder.append(item.getValue());
            strBuilder.append("/");

        }
        final String str= strBuilder.toString();
        String str2 = str.replace("/","\n");
        String str1[]= str2.split("\n");
        Log.d("numbersss",str);
        strBuilder1 = new StringBuilder();
        for(int i=0;i<str1.length;i++)
        {
            if(check) {
                if (str1[i].replace("-", "").matches("^((\\+92)|(0092))-{0,1}\\d{3}-{0,1}\\d{7}$|^\\d{11}$|^\\d{4}-\\d{7}$")) {
                    strBuilder1.append(str1[i]);
                    strBuilder1.append("/");
                    Log.d("adad", str1[i]);
                }
            }
            else
            {
                strBuilder1.append(str1[i]);
                strBuilder1.append("/");
                Log.d("adad", str1[i]);
            }

        }


        Log.v("strBuilder.toString()", strBuilder.toString());
        Log.d("beeer", strBuilder1.toString());

        txtView.post(new Runnable() {
            @Override
            public void run() {
                txtView.setText(strBuilder1.toString());
                txtView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(txt1.getText().toString().contains(""))
                        {

                        }
                        cameraView.setVisibility(View.GONE);
                        txtView.setVisibility(View.GONE);
                        regex.setVisibility(View.GONE);
                        txt1.setVisibility(View.VISIBLE);
                        clear.setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);
                        dial.setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.VISIBLE);

                        String all_number=txtView.getText().toString();
                        all_number = all_number.replace("/","\n");
                        //String all[] = all_number.split("\n");

                        /*for(int i=0 ; i<all.length;i++)
                        {
                            if(all[i].length()<11 || all[i].length()>16 )
                                all[i]="";

                        }*/
                        //String n = Arrays.toString(all);

                        //n = n.replace("0092", "0");


                        txt1.setText(all_number);

                    }


                });
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        numbers = txt1.getText().toString();

                        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + numbers));
                        sendIntent.putExtra("address", numbers);
                        sendIntent.putExtra("sms_body", "");
                        startActivity(sendIntent);

                    }
                });

            }
        });

    }
    public void CallToMetallurgGates()
    {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},1);
                numbers = txt1.getText().toString();
                numbers = numbers.replace("-", "");
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + numbers));
                startActivity(dialIntent);
            }
            else
            {
                numbers = txt1.getText().toString();
                numbers = numbers.replace("-", "");
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + numbers));
                startActivity(dialIntent);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if(txt1.getVisibility()==View.VISIBLE)
        {
            cameraView.setVisibility(View.VISIBLE);
            txtView.setVisibility(View.VISIBLE);
            regex.setVisibility(View.VISIBLE);
            txt1.setVisibility(View.GONE);
            clear.setVisibility(View.GONE);
            send.setVisibility(View.GONE);
            dial.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
        }
        else
            finish();

    }
}

