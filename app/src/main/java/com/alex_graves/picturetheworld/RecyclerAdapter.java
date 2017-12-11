package com.alex_graves.picturetheworld;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by agraves on 12/11/17.
 *
 * Custom recycler adapter for displays places in list form.
 */

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    private ArrayList<PlaceListItem> items;

    RecyclerAdapter(ArrayList<PlaceListItem> items) {
        this.items = items;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_image)
        ImageView image;
        @BindView(R.id.item_place_name)
        TextView name;
        @BindView(R.id.item_description)
        TextView description;

        ItemViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_list_item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final PlaceListItem item = items.get(position);

        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription());
        holder.image.setImageBitmap(item.getImage());
    }
}
