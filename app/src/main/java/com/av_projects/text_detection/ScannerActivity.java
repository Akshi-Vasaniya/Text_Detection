package com.av_projects.text_detection;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.security.Permission;

public class ScannerActivity extends AppCompatActivity {

    private ImageView capturedImg;
    private TextView text1;
    private Button snapBtn, detectBtn;
    // Used for when user has captured the image.
    private Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        capturedImg = findViewById(R.id.capturedImg);
        text1 = findViewById(R.id.scannerTxt1);
        snapBtn = findViewById(R.id.snapBtn);
        detectBtn = findViewById(R.id.detectBtn);

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission()){
                    capturedImage();
                } else{
                    requestPermission();
                }
            }
        });

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTxt();
            }
        });
    }

    private boolean checkPermission(){
        //ContextCompat class is used when you would like to retrieve resources, such as drawable or color without bother about theme.
        // ContextCompat.checkSelfPermission() - is used to check the dangerous permission.
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        // if permission is already granted, then its 0, if not then its -1. So if cameraPermission is 0 then it's true, other wise its false.
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        int PERMISSION_CODE = 200;
        // It is used to request the dangerous permission.
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    private void detectTxt(){
        // This are the class of ML kit.
        // .fromBitmap = Creates InputImage obj using Bitmap.(rotationDegrees - the image's counter-clockwise orientation degrees.Only 0, 90, 180, 270 are supported. IllegalArgumentException will be thrown if the input degree is not in the list.)
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder result = new StringBuilder();
                for (Text.TextBlock block:text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line:block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for (Text.Element element: line.getElements()) {
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        text1.setText(blockText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Fail to detect text from image = "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void capturedImage(){
//        MediaStore.ACTION_IMAGE_CAPTURE - It captures the img and return it.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // If you try starting an activity, and there is no match, you get an ActivityNotFoundException, so this check is trying to avoid such an exception.
        //So for that resolveActivity() is used.
        if(intent.resolveActivity(getPackageManager())!=null){
            // it start activity and ask for result also.
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURED);
        }
    }

    @Override
    public void onRequestPermissionsResult(int  requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                capturedImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // When we get the results from another activity, then the first function which get the results is this.
    // Another activity is started has use clicks on snap btn, so basically it goes to captured activity.
    // And has result come from that activity, it will directly calls this method.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // requestCode comes its value is 1 or else 0
        if(requestCode ==  REQUEST_IMAGE_CAPTURED && resultCode == RESULT_OK){
            // Bundle is used to pass data b/w two activities, and its totally up-to you can pass any type of data. Bundle accepts all type of data.
            // .getExtras() - return all the data, which placed it array.
            assert data != null;
            Bundle extras = data.getExtras();
            // Bitmap is used to captured any image and return it.
            imageBitmap = (Bitmap) extras.get("data");
            capturedImg.setImageBitmap(imageBitmap);
        }
    }
}