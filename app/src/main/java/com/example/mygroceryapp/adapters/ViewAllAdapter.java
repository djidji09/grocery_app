package com.example.mygroceryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mygroceryapp.R;
import com.example.mygroceryapp.activities.DetailedActivity;
import com.example.mygroceryapp.models.ViewAllModel;

import java.util.List;

public class ViewAllAdapter extends RecyclerView.Adapter<ViewAllAdapter.ViewHolder> {

    Context context;
    List<ViewAllModel> list;

    public ViewAllAdapter(Context context, List<ViewAllModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_all_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewAllModel item = list.get(position);
        if (item.getImg_url() != null) {
            Glide.with(context).load(item.getImg_url()).into(holder.imageView);
        }
        holder.name.setText(item.getName() != null ? item.getName() : "");
        holder.description.setText(item.getDescription() != null ? item.getDescription() : "");
        holder.rating.setText(item.getRating() != null ? item.getRating() : "0.0");

        String unit = "/kg";
        String type = item.getType();
        if ("eggs".equals(type)) {
            unit = "/dozen";
        } else if ("milk".equals(type)) {
            unit = "/litre";
        }
        holder.price.setText("$" + item.getPrice() + unit);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, DetailedActivity.class);
                intent.putExtra("detail", item);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView name,description,price,rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.view_img);
            name = itemView.findViewById(R.id.view_name);
            description = itemView.findViewById(R.id.view_description);
            price = itemView.findViewById(R.id.view_price);
            rating = itemView.findViewById(R.id.view_rating);
        }
    }
}
