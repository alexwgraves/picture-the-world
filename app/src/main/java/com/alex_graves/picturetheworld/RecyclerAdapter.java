package com.alex_graves.picturetheworld;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        @BindView(R.id.delete_item)
        Button delete;

        UserImageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bindType(ListItem item) {
            final UserImageItem photo = (UserImageItem) item;
            String url = MainActivity.URL + "GET/" + photo.getImageName();
            Picasso.with(image.getContext()).load(url).into(image);
            credit.setText(photo.getCredit());
            location.setText(photo.getPlace());
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmDelete(photo);
                }
            });
        }

        private void confirmDelete(final UserImageItem photo) {
            AlertDialog.Builder sure = new AlertDialog.Builder(image.getContext());
            sure.setTitle(R.string.sure);
            sure.setMessage(R.string.delete_user_photo);

            sure.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deletePhoto(photo);
                }
            });

            sure.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // dismisses automatically
                }
            });

            sure.show();
        }

        private void deletePhoto(final UserImageItem photo) {
            // trim .jpg from the string
            String itemName = photo.getImageName();
            itemName = itemName.substring(0, itemName.length() - 4);

            RedisService.getService().deleteItem(itemName).enqueue(new Callback<RedisService.DelResponse>() {
                @Override
                public void onResponse(Call<RedisService.DelResponse> call, Response<RedisService.DelResponse> response) {
                    // just continue
                }

                @Override
                public void onFailure(Call<RedisService.DelResponse> call, Throwable t) {
                    Toast.makeText(image.getContext(), t.toString(), Toast.LENGTH_LONG).show();
                }
            });

            RedisService.getService().deleteItem(photo.getItemName()).enqueue(new Callback<RedisService.DelResponse>() {
                @Override
                public void onResponse(Call<RedisService.DelResponse> call, Response<RedisService.DelResponse> response) {
                    items.remove(photo);
                    notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<RedisService.DelResponse> call, Throwable t) {
                    Toast.makeText(image.getContext(), t.toString(), Toast.LENGTH_LONG).show();
                }
            });
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
