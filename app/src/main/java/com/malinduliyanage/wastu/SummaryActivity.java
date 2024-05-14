package com.malinduliyanage.wastu;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    String basePath = null, detectionPath = null, timeStamp = null;
    ImageView imageView;
    Button toggleBtn;
    SQLiteHelper dbHelper;
    LinearLayout camera, location, time, storage;
    TextView locTxt, timeTxt, intText;
    private RecyclerView detectionsContainer;
    private ObjectAdapter objectAdapter;
    private List<Object> objectList;

    boolean baseImage = false;
    private MapView mapView;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_summary);

        basePath = getIntent().getStringExtra("base_path");
        detectionPath = getIntent().getStringExtra("detection_path");
        timeStamp = getIntent().getStringExtra("timestamp");

        backBtn = findViewById(R.id.return_summary);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageView = findViewById(R.id.summaryView);
        toggleBtn = findViewById(R.id.toggle_Btn);

        camera = findViewById(R.id.cameraLayout);
        location = findViewById(R.id.locLayout);
        time = findViewById(R.id.timeLayout);
        storage = findViewById(R.id.storageLayout);

        locTxt = findViewById(R.id.loc_text);
        timeTxt = findViewById(R.id.time_text);
        intText = findViewById(R.id.internet_text);

        mapView = findViewById(R.id.map_view);

        camera.setVisibility(View.GONE);
        location.setVisibility(View.GONE);
        time.setVisibility(View.GONE);
        storage.setVisibility(View.GONE);
        mapView.setVisibility(View.GONE);

        loadDetectedImage();

        loadDetections();

        loadBaseImagedata();


        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!baseImage){
                    loadBaseImage();
                    baseImage = true;
                    toggleBtn.setText("See Detected Object");
                } else {
                    loadDetectedImage();
                    baseImage = false;
                    toggleBtn.setText("See Base Image");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void loadDetectedImage() {
        File imageFile = new File(detectionPath);
        Picasso.get()
                .load(imageFile)
                .placeholder(R.drawable.bg_placeholder)
                .into(imageView);
    }

    private void loadBaseImage() {
        if (basePath.contains("/raw/")){
            basePath = basePath.replace("/raw/", "");
        }
        File imageFile = new File(basePath);
        Picasso.get()
                .load(imageFile)
                .placeholder(R.drawable.bg_placeholder)
                .into(imageView);
    }

    private void loadDetections() {

        detectionsContainer = findViewById(R.id.detections_container);
        dbHelper = new SQLiteHelper(this);
        objectList = new ArrayList<>();
        objectAdapter = new ObjectAdapter(objectList, this,true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        detectionsContainer.setLayoutManager(layoutManager);
        detectionsContainer.setAdapter(objectAdapter);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"img_path", "path", "timestamp"};
        String selection = "img_path = ?";
        String[] selectionArgs = {basePath};
        Cursor cursor = db.query("recognitions", projection, selection, selectionArgs, null, null, "timestamp DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String img_path = cursor.getString(cursor.getColumnIndexOrThrow("img_path"));
                String path = cursor.getString(cursor.getColumnIndexOrThrow("path"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                Object object = new Object(img_path, path, timestamp);
                objectList.add(object);

            } while (cursor.moveToNext());
            cursor.close();
            objectAdapter.notifyDataSetChanged();

        }else{
        }

        db.close();
    }

    private void loadBaseImagedata() {

        String timestamp, lat, lon;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"timestamp", "lat", "lon"};
        String selection = "path = ?";
        String[] selectionArgs = {basePath};
        Cursor cursor = db.query("captures", projection, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"));
                lon = cursor.getString(cursor.getColumnIndexOrThrow("lon"));

            } while (cursor.moveToNext());
            cursor.close();
            camera.setVisibility(View.VISIBLE);
            if(!lat.contains("No Data") || !lon.contains("No Data")){
                mapView.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
                intText.setVisibility(View.VISIBLE);
                double latitude = Double.parseDouble(lat);
                double longitude = Double.parseDouble(lon);
                locTxt.setText("Location Data : " + lat + ", " + lon);

                Configuration.getInstance().setUserAgentValue("com.example.openstreet");
                mapView.setTileSource(TileSourceFactory.MAPNIK); // Set the tile source
                mapView.setMultiTouchControls(false);
                GeoPoint startPoint = new GeoPoint(latitude, longitude);
                mapView.getController().setCenter(startPoint);
                mapView.getController().setZoom(17.0);
                Marker marker = new Marker(mapView); // Replace "this" with context if needed
                marker.setPosition(startPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
                mapView.invalidate();
            }
            time.setVisibility(View.VISIBLE);
            timeTxt.setText("Timestamp : " + timeStamp);
        }else{
            storage.setVisibility(View.VISIBLE);
        }
        db.close();

    }


}