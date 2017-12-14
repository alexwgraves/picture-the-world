package com.alex_graves.picturetheworld;

import android.content.Context;
import android.content.Intent;
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
    private Context listContext;

    RecyclerAdapter(ArrayList<ListItem> items) {
        this.items = items;
        this.listContext = null;
    }

    RecyclerAdapter(ArrayList<ListItem> items, Context listContext) {
        this.items = items;
        this.listContext = listContext;
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bindType(ListItem item);
    }

    class PlaceViewHolder extends ViewHolder {
        @BindView(R.id.item_place_name)
        TextView name;
        @BindView(R.id.item_description)
        TextView description;

        PlaceViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    ListItem item = items.get(position);
                    if (item.getListItemType() == ListItem.PLACE) {
                        PlaceListItem place = (PlaceListItem) item;
                        Intent intent = new Intent(view.getContext(), PlaceActivity.class);
                        intent.putExtra(view.getContext().getString(R.string.place_id), place.getID());
                        intent.putExtra(view.getContext().getString(R.string.place_item_name), place.getName());
                        if (listContext != null) {
                            ListActivity list = (ListActivity) listContext;
                            intent.putParcelableArrayListExtra(list.getString(R.string.place_list_item), list.getItems());
                            intent.putExtra(list.getString(R.string.current_lat), list.getCurrentLat());
                            intent.putExtra(list.getString(R.string.current_lng), list.getCurrentLng());
                        }
                        view.getContext().startActivity(intent);
                    }
                }
            });
        }

        public void bindType(ListItem item) {
            final PlaceListItem place = (PlaceListItem) item;
            name.setText(place.getName());
            description.setText(place.getDescription());
        }
    }

    class ImageViewHolder extends ViewHolder {
        @BindView(R.id.item_image)
        ImageView image;
        @BindView(R.id.item_credit)
        TextView credit;

        ImageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bindType(ListItem item) {
            ImageItem photo = (ImageItem) item;
            image.setImageBitmap(photo.getImage());
            credit.setText(photo.getCredit());
        }
    }

    class UserImageViewHolder extends ViewHolder {
        @BindView(R.id.item_user_image)
        ImageView image;
        @BindView(R.id.item_user_credit)
        TextView credit;
        @BindView(R.id.item_user_location)
        TextView location;

        UserImageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bindType(ListItem item) {
            UserImageItem photo = (UserImageItem) item;
            String url = MainActivity.URL + "GET/" + photo.getImageName();
            Picasso.with(image.getContext()).load(url).into(image);
            credit.setText(photo.getCredit());
            location.setText(photo.getLocation().toString());
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
            case ListItem.USER_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_image_list_item_layout, parent, false);
                return new UserImageViewHolder(view);
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
