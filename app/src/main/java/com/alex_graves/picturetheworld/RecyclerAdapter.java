package com.alex_graves.picturetheworld;

import android.support.v7.widget.RecyclerView;
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
 * Custom recycler adapter for displaying places and images in list form.
 */

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private ArrayList<ListItem> items;

    RecyclerAdapter(ArrayList<ListItem> items) {
        this.items = items;
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bindType(ListItem item);
    }

    class PlaceViewHolder extends ViewHolder {
        @BindView(R.id.item_image)
        ImageView image;
        @BindView(R.id.item_credit)
        TextView credit;
        @BindView(R.id.item_place_name)
        TextView name;
        @BindView(R.id.item_description)
        TextView description;

        PlaceViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bindType(ListItem item) {
            PlaceListItem place = (PlaceListItem) item;
            name.setText(place.getName());
            description.setText(place.getDescription());
            image.setImageBitmap(place.getImage());
            credit.setText(place.getCredit());
        }
    }

    class ImageViewHolder extends ViewHolder {
        @BindView(R.id.item_image)
        ImageView image;
        @BindView(R.id.item_credit)
        TextView credit;
        @BindView(R.id.item_description)
        TextView description;

        ImageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bindType(ListItem item) {
            ImageItem photo = (ImageItem) item;
            Picasso.with(image.getContext()).load(photo.getURL());
            credit.setText(photo.getCredit());
            description.setText(photo.getDescription());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getListItemType();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ListItem.PLACE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.place_list_item_layout, parent, false);
                return new PlaceViewHolder(view);
            case ListItem.IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.image_list_item_layout, parent, false);
                return new ImageViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindType(items.get(position));
    }
}
