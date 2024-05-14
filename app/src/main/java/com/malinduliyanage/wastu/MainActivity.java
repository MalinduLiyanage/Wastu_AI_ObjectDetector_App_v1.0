package com.malinduliyanage.wastu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private FrameLayout contentViewContainer;
    private LinearLayout bottomNavigationBar;
    private ImageButton homeButton;
    private ImageButton profileButton;
    private RecyclerView objectContainer;
    private CardView predBtn;
    private Button settingsBtn;
    TextView brandTxt, gpuTxt, profilebrandTxt, detectThresholdTxt, iouThresholdTxt, iouClassDuplicatedThresholdTxt, detectTxt;
    SQLiteHelper dbHelper;
    private List<Object> objectList;
    private ObjectAdapter objectAdapter;

    Boolean isHome = true;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!allPermissionsGranted()) {
            Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
            startActivity(intent);
            finish();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("ModelParameters", MODE_PRIVATE);

        if (!sharedPreferences.contains("DETECT_THRESHOLD") && !sharedPreferences.contains("IOU_THRESHOLD") && !sharedPreferences.contains("IOU_CLASS_DUPLICATED_THRESHOLD")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("DETECT_THRESHOLD", 0.6f);
            editor.putFloat("IOU_THRESHOLD", 0.45f);
            editor.putFloat("IOU_CLASS_DUPLICATED_THRESHOLD", 0.7f);
            editor.apply();
        }

        dbHelper = new SQLiteHelper(MainActivity.this);

        contentViewContainer = findViewById(R.id.content_view_container);
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        homeButton = findViewById(R.id.home_button);
        profileButton = findViewById(R.id.profile_button);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomeView();
                homeButton.setImageResource(R.drawable.btn_home_round);
                profileButton.setImageResource(R.drawable.btn_user);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileView();
                homeButton.setImageResource(R.drawable.btn_home);
                profileButton.setImageResource(R.drawable.btn_user_round);
            }
        });
        showHomeView();
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isHome){
            showHomeView();
        }else{
            showProfileView();
        }

    }
    private void showHomeView() {

        isHome = true;

        contentViewContainer.removeAllViews();
        View homeView = getLayoutInflater().inflate(R.layout.view_home, null);
        contentViewContainer.addView(homeView);

        predBtn = findViewById(R.id.predict_btn);
        brandTxt = findViewById(R.id.brand_text);
        gpuTxt = findViewById(R.id.gpu_text);
        detectTxt = findViewById(R.id.detect_Txt);

        String phoneModel = Build.MODEL;
        String phoneProduct = Build.BRAND;
        String phoneVersion = Build.CPU_ABI;

        brandTxt.setText(phoneProduct + " " + phoneModel);
        gpuTxt.setText(phoneVersion);

        predBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PredictActivity.class);
                startActivity(intent);
            }
        });

        detectedObjects();

    }
    private void showProfileView() {

        isHome = false;

        contentViewContainer.removeAllViews();
        View profileView = getLayoutInflater().inflate(R.layout.view_profile, null);
        contentViewContainer.addView(profileView);

        profilebrandTxt = findViewById(R.id.brand_text_profile);
        detectThresholdTxt = findViewById(R.id.detect_text);
        iouThresholdTxt = findViewById(R.id.iou_text);
        iouClassDuplicatedThresholdTxt = findViewById(R.id.class_text);

        settingsBtn = findViewById(R.id.settings_Btn);
        backBtn = findViewById(R.id.return_profile);

        String phoneModel = Build.MODEL;
        String phoneProduct = Build.BRAND;
        SharedPreferences sharedPreferences = getSharedPreferences("ModelParameters", MODE_PRIVATE);

        profilebrandTxt.setText(phoneProduct + " " + phoneModel);

        Float DETECT_THRESHOLD = sharedPreferences.getFloat("DETECT_THRESHOLD",0);
        Float IOU_THRESHOLD = sharedPreferences.getFloat("IOU_THRESHOLD",0);
        Float IOU_CLASS_DUPLICATED_THRESHOLD = sharedPreferences.getFloat("IOU_CLASS_DUPLICATED_THRESHOLD",0);

        detectThresholdTxt.setText(DETECT_THRESHOLD.toString());
        iouThresholdTxt.setText(IOU_THRESHOLD.toString());
        iouClassDuplicatedThresholdTxt.setText(IOU_CLASS_DUPLICATED_THRESHOLD.toString());

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ParametersActivity.class);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHome = true;
                showHomeView();
                homeButton.setImageResource(R.drawable.btn_home_round);
                profileButton.setImageResource(R.drawable.btn_user);

            }
        });

    }
    private void detectedObjects() {

        objectContainer = findViewById(R.id.image_container);
        dbHelper = new SQLiteHelper(this);
        objectList = new ArrayList<>();
        objectAdapter = new ObjectAdapter(objectList, this,false);

        int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, spanCount);
        objectContainer.setLayoutManager(layoutManager);

        objectContainer.setAdapter(objectAdapter);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"img_path", "path", "timestamp"};
        Cursor cursor = db.query("recognitions", projection, null, null, null, null, "timestamp DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {

                String img_path = cursor.getString(cursor.getColumnIndexOrThrow("img_path"));
                String path = cursor.getString(cursor.getColumnIndexOrThrow("path"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                Object object = new Object(img_path, path, timestamp);
                objectList.add(object);
            } while (cursor.moveToNext());
            detectTxt.setVisibility(View.GONE);
            cursor.close();
            objectAdapter.notifyDataSetChanged();
        }else{
            detectTxt.setVisibility(View.VISIBLE);
        }

        db.close();

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