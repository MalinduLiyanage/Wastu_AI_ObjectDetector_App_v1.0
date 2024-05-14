package com.malinduliyanage.wastu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class RecognitionAdapter extends RecyclerView.Adapter<RecognitionAdapter.RecognitionViewHolder> {

    private ArrayList<Recognition> recognitions;
    private Bitmap bitmap;
    Context context;
    SQLiteHelper dbHandler;
    String capturePath;
    private String timeStamp;

    public RecognitionAdapter(Context context, ArrayList<Recognition> recognitions, Bitmap bitmap, String capturePath) {
        this.context = context;
        this.recognitions = recognitions;
        this.bitmap = bitmap;
        this.capturePath = capturePath;
    }

    @NonNull
    @Override
    public RecognitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        dbHandler = new SQLiteHelper(context);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_recognition, parent, false);
        return new RecognitionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecognitionViewHolder holder, int position) {
        Recognition recognition = recognitions.get(position);
        RectF location = recognition.getLocation();
        Bitmap croppedBitmap = cropObject(location); // Assuming cropObject() crops the region from the original bitmap
        Glide.with(holder.itemView.getContext())
                .load(croppedBitmap)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToStorage(croppedBitmap);
            }
        });
    }

    private void saveImageToStorage(Bitmap bitmap) {
        String filename = UUID.randomUUID().toString() + ".jpg";
        File file = new File(context.getFilesDir(), filename);

        try {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            String path = file.getAbsolutePath();
            dbHandler.addRecognition(capturePath, path, timeStamp);
            Toast.makeText(context, "Detection Saved!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private Bitmap cropObject(RectF location) {
        int left = (int) location.left;
        int top = (int) location.top;
        int right = (int) location.right;
        int bottom = (int) location.bottom;
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
    }


    @Override
    public int getItemCount() {
        return recognitions.size();
    }

    static class RecognitionViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        RecognitionViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.obj_img);
        }
    }
}

