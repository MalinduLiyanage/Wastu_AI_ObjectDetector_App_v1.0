package com.malinduliyanage.wastu;

import static java.lang.Integer.parseInt;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ParametersActivity extends AppCompatActivity {

    private SeekBar thresholdSlider, iouSlider, classSlider;
    private TextView thresholdValueText, iouValueText, classValueText;
    private Button saveBtn, resetBtn;
    private float DETECT_THRESHOLD, IOU_THRESHOLD, IOU_CLASS_DUPLICATED_THRESHOLD;
    final private float RESET_DETECT_THRESHOLD = 0.6f, RESET_IOU_THRESHOLD = 0.45f, RESET_IOU_CLASS_DUPLICATED_THRESHOLD = 0.7f;
    SharedPreferences sharedPreferences;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parameters);

        sharedPreferences = getSharedPreferences("ModelParameters", MODE_PRIVATE);

        DETECT_THRESHOLD = sharedPreferences.getFloat("DETECT_THRESHOLD",0);
        IOU_THRESHOLD = sharedPreferences.getFloat("IOU_THRESHOLD",0);
        IOU_CLASS_DUPLICATED_THRESHOLD = sharedPreferences.getFloat("IOU_CLASS_DUPLICATED_THRESHOLD",0);

        thresholdSlider = findViewById(R.id.thresholdbar);
        thresholdValueText = findViewById(R.id.thresholdval);

        iouSlider = findViewById(R.id.ioudbar);
        iouValueText = findViewById(R.id.iouval);

        classSlider = findViewById(R.id.classbar);
        classValueText = findViewById(R.id.classval);

        saveBtn = findViewById(R.id.save_btn);
        resetBtn = findViewById(R.id.reset_btn);

        backBtn = findViewById(R.id.return_parameters);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("DETECT_THRESHOLD", DETECT_THRESHOLD);
                editor.putFloat("IOU_THRESHOLD", IOU_THRESHOLD);
                editor.putFloat("IOU_CLASS_DUPLICATED_THRESHOLD", IOU_CLASS_DUPLICATED_THRESHOLD);
                editor.apply();
                Toast.makeText(ParametersActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();

            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DETECT_THRESHOLD = RESET_DETECT_THRESHOLD;
                IOU_THRESHOLD = RESET_IOU_THRESHOLD;
                IOU_CLASS_DUPLICATED_THRESHOLD = RESET_IOU_CLASS_DUPLICATED_THRESHOLD;
                changeParameters();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeParameters();

    }

    private void changeParameters() {

        float initialThreshold = DETECT_THRESHOLD * 100;
        thresholdSlider.setProgress((int) initialThreshold);
        thresholdValueText.setText(String.valueOf(initialThreshold/100));

        thresholdSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float currentProgress = (float) progress / seekBar.getMax(); // Convert progress to float
                thresholdValueText.setText(String.valueOf(currentProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: handle touch start event (e.g., show a toast)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DETECT_THRESHOLD = (float) seekBar.getProgress() / seekBar.getMax();
            }
        });

        float initialIou = IOU_THRESHOLD * 100;
        iouSlider.setProgress((int) initialIou);
        iouValueText.setText(String.valueOf(initialIou/100));

        iouSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float currentProgress = (float) progress / seekBar.getMax(); // Convert progress to float
                iouValueText.setText(String.valueOf(currentProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: handle touch start event (e.g., show a toast)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                IOU_THRESHOLD = (float) seekBar.getProgress() / seekBar.getMax();
            }
        });

        float initialclass = IOU_CLASS_DUPLICATED_THRESHOLD * 100;
        classSlider.setProgress((int) initialclass);
        classValueText.setText(String.valueOf(initialclass/100));

        classSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float currentProgress = (float) progress / seekBar.getMax(); // Convert progress to float
                classValueText.setText(String.valueOf(currentProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: handle touch start event (e.g., show a toast)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                IOU_CLASS_DUPLICATED_THRESHOLD = (float) seekBar.getProgress() / seekBar.getMax();
            }
        });

    }

}