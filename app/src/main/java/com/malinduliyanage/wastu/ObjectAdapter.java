package com.malinduliyanage.wastu;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.ViewHolder>{

    private List<Object> objectList;
    private Context context;
    private Boolean isSummary;

    public ObjectAdapter(List<Object> objectList, Context context, Boolean isSummary) {
        this.objectList = objectList;
        this.context = context;
        this.isSummary = isSummary;
    }

    @NonNull
    @Override
    public ObjectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (isSummary){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_object_small, parent, false);
            return new ViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_object, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ObjectAdapter.ViewHolder holder, int position) {

        Object object = objectList.get(position);
        File imageFile = new File(object.getPath());
        Picasso.get()
                .load(imageFile)
                .placeholder(R.drawable.vector)
                .into(holder.objImg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, SummaryActivity.class);
                intent.putExtra("base_path", object.getImgPath());
                intent.putExtra("detection_path", object.getPath());
                intent.putExtra("timestamp", object.getTimestamp());
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView objImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            objImg = itemView.findViewById(R.id.obj_img);
        }
    }
}
