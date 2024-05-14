package com.malinduliyanage.wastu;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class PredictActivity extends AppCompatActivity {

    private final int IMAGE_PICK = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123; // You can choose any unique number

    ImageView imageView;
    Bitmap bitmap;
    Yolov5TFLiteDetector yolov5TFLiteDetector;
    Paint boxPaint = new Paint();
    Paint textPain = new Paint();
    ArrayList<Recognition> recognitions;
    Button galBtn,camBtn;
    RecyclerView recyclerView;
    ConstraintLayout layout;
    PreviewView previewView;
    SQLiteHelper dbHandler;

    private static final String TAG = "PredictActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Camera camera;
    private ImageCapture imageCapture;
    ProcessCameraProvider cameraProvider;
    String capturePath = null, latitudeString = null, longitudeString = null, timeStamp = null;
    boolean isPreviewBound = false;

    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_predict);

        if (!allPermissionsGranted()) {
            Intent intent = new Intent(PredictActivity.this, PermissionActivity.class);
            startActivity(intent);
            finish();
        }

        backBtn = findViewById(R.id.return_button);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layout = findViewById(R.id.conslayout);

        dbHandler = new SQLiteHelper(PredictActivity.this);

        SharedPreferences sharedPreferences = getSharedPreferences("ModelParameters", MODE_PRIVATE);

        Float DETECT_THRESHOLD = sharedPreferences.getFloat("DETECT_THRESHOLD",0);
        Float IOU_THRESHOLD = sharedPreferences.getFloat("DETECT_THRESHOLD",0);
        Float IOU_CLASS_DUPLICATED_THRESHOLD = sharedPreferences.getFloat("DETECT_THRESHOLD",0);

        ActivityCompat.requestPermissions(this,
                new String[]{READ_MEDIA_IMAGES, WRITE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        imageView = findViewById(R.id.imageView);
        galBtn = findViewById(R.id.galleryBtn);
        camBtn = findViewById(R.id.captureBtn);
        previewView = findViewById(R.id.previewView);

        yolov5TFLiteDetector = new Yolov5TFLiteDetector();
        yolov5TFLiteDetector.setModelFile("yolov5s-fp16.tflite");
        yolov5TFLiteDetector.initialModel(this, DETECT_THRESHOLD, IOU_THRESHOLD, IOU_CLASS_DUPLICATED_THRESHOLD);

        boxPaint.setStrokeWidth(5);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.RED);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        startCamera();

        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPreviewBound){
                    bindPreview(cameraProvider);
                    imageView.setVisibility(View.GONE);
                    previewView.setVisibility(View.VISIBLE);

                }else{
                    captureAndSaveImage(PredictActivity.this);

                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_PICK && data != null){
            Uri uri = data.getData();
            capturePath = uri.getPath();
            try {
                isPreviewBound = false;
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                previewView.setVisibility(View.GONE);
                predict();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public void selectImage(View view){
        isPreviewBound = false;
        camBtn.setText("Open Camera");
        if (cameraProvider != null){
            cameraProvider.unbindAll();
        }
        if (previewView.getVisibility() == View.VISIBLE){
            previewView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK);

    }
    public void predict(){
        Toast.makeText(PredictActivity.this, "Processing", Toast.LENGTH_SHORT).show();
        recognitions =  yolov5TFLiteDetector.detect(bitmap);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        for(Recognition recognition: recognitions){
            if(recognition.getConfidence() > 0.4){
                RectF location = recognition.getLocation();
                canvas.drawRect(location, boxPaint);

            }
        }
        imageView.setImageBitmap(mutableBitmap);

        if(!recognitions.isEmpty()){
            createPopup();

        }else{
            Toast.makeText(PredictActivity.this, "No Predictions!", Toast.LENGTH_LONG).show();
        }
    }
    private void createPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpview = inflater.inflate(R.layout.popup_detections, null);

        recyclerView = popUpview.findViewById(R.id.container_views);

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popUpview, width, height, focusable);

        int spanCount = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(PredictActivity.this, spanCount);
        recyclerView.setLayoutManager(layoutManager);
        RecognitionAdapter adapter = new RecognitionAdapter(PredictActivity.this, recognitions, bitmap, capturePath);
        recyclerView.setAdapter(adapter);
        layout.post(() -> popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0));

    }
    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                if (cameraProvider != null) {
                    imageCapture = new ImageCapture.Builder()
                            .build();

                } else {
                    Log.e(TAG, "Unable to retrieve camera provider.");
                    Toast.makeText(PredictActivity.this, "Unable to start camera.", Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
                Toast.makeText(PredictActivity.this, "Error starting camera.", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        isPreviewBound = true;
        camBtn.setText("Click to Capture");
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }
    private void captureAndSaveImage(Context context) {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if (cameraProvider != null) {
                    int locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (locationPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PredictActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        return;
                    }

                    timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String fileName = "IMG_" + timeStamp; // Generate a unique file name
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

                    File outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File outputFile = new File(outputDirectory, fileName + ".jpg");

                    Location location = getLocation(context); // Implement getLocation() method below
                    if (location != null) {
                        contentValues.put(MediaStore.Images.Media.LATITUDE, location.getLatitude());
                        contentValues.put(MediaStore.Images.Media.LONGITUDE, location.getLongitude());

                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        latitudeString = String.valueOf(latitude);
                        longitudeString = String.valueOf(longitude);

                    }else{
                        latitudeString = "No Data";
                        longitudeString = "No Data";
                    }

                    ImageCapture.OutputFileOptions outputOptions = new ImageCapture
                            .OutputFileOptions
                            .Builder(outputFile)
                            .build();

                    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            capturePath = outputFile.getAbsolutePath();
                            Log.d(TAG, "Image saved successfully: " + outputFile.getAbsolutePath());
                            dbHandler.addCapture(capturePath, timeStamp, latitudeString, longitudeString);
                            processCapturePath(context);
                        }

                        @Override
                        public  void onError(ImageCaptureException exc) {
                            Log.e(TAG, "Error capturing image: " + exc.getMessage(), exc);
                            Toast.makeText(PredictActivity.this, "Error capturing image.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error capturing image: " + e.getMessage(), e);
            }
        }, ContextCompat.getMainExecutor(context));
    }
    private Location getLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                return location;
            } else {
                return null;
            }
        }
        return null;
    }
    private void processCapturePath(Context context) {
        if (capturePath != null) {
            Log.d(TAG, "Capture path: " + capturePath);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Uri uri = Uri.fromFile(new File(capturePath));

                try {
                    bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));

                    cameraProvider.unbindAll();
                    isPreviewBound = false;
                    camBtn.setText("Open Camera");
                    imageView.setImageBitmap(bitmap);
                    previewView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    predict();
                    latitudeString = null;
                    longitudeString = null;
                    timeStamp = null;

                } catch (IOException e) {
                    Log.e(TAG, "Error loading captured image: " + e.getMessage());
                    Toast.makeText(PredictActivity.this, "Error loading image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PredictActivity.this, "Permission Problem", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PredictActivity.this, "Capture Path null", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean allPermissionsGranted() {
        String[] REQUIRED_PERMISSIONS = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}