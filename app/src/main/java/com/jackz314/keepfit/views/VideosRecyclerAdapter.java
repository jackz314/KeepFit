package com.jackz314.keepfit.views;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class VideosRecyclerAdapter extends RecyclerView.Adapter<VideosRecyclerAdapter.ViewHolder> {

    private static final String TAG = "VideosRecyclerAdapter";

    private List<Media> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private HashSet<String> ownVideos = new HashSet<>();
    private VideosFragment frag;

    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;


    // data is passed into the constructor
    public VideosRecyclerAdapter(Context context, List<Media> data, VideosFragment fragment) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.frag = fragment;
        UserControllerKt.getCurrentUserDoc().collection("videos").addSnapshotListener(((value, e) -> {
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            ownVideos.clear();
            for (QueryDocumentSnapshot doc : value) {
                ownVideos.add(doc.getId());
            }

        }));
    }



    public void notifyDataChanged(){

        super.notifyDataSetChanged();
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Media media = mData.get(position);
        holder.titleText.setText(media.getTitle());

        if(media.isLivestream()){
            holder.durationText.setText("LIVE");
            holder.durationText.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0xB8,0x03, 0x06)));
        }else{
            holder.durationText.setText(UtilsKt.formatDurationString(media.getDuration()));
            holder.durationText.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        }

        String thumbnail;
        if (media.isLivestream() || !"".equals(media.getThumbnail())) thumbnail = media.getThumbnail();
        else thumbnail = media.getLink();
        Glide.with(holder.image)
                .load(thumbnail)
                .fitCenter()
                .placeholder(R.drawable.ic_thumb_placeholder)
                .into(holder.image);

        long start = System.currentTimeMillis();
        User creator = media.getCreator().getValue();
        if (creator == null || creator.getUid() == null) {
            media.getCreator().observeForever(new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if(user != null && user.getUid() != null){
                        long duration = System.currentTimeMillis() - start;
                        Log.d(TAG, "onBindViewHolder: duration: " + duration);
                        media.getCreator().removeObserver(this);
                        populateCreatorInfo(holder, media, user);
                    }
                }
            });
        }else{
            populateCreatorInfo(holder, media, creator);
        }
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(frag.getContext())
                    .setMessage(R.string.delete_video_confirm)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> frag.deleteVideo(media.getUid(),media.getCreatorRef().getId(),media.getLink()))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
//            Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show()
        });




        //use ref directly, similar speed
//        media.getCreatorRef().get().addOnSuccessListener(snapshot -> {
//            long duration = System.currentTimeMillis() - start;
//            Log.d(TAG, "onBindViewHolder: duration: " + duration);
//            User creator = new User(snapshot);
//            populateCreatorInfo(holder, media, creator);
//        });
    }

    private void populateCreatorInfo(ViewHolder holder, Media media, User creator) {
        holder.detailText.setText(media.getProfileString());

        List<String> categories = media.getCategories();

        String categoryTextString = "";
        for(int i = 0; i < categories.size() ; ++i){
            categoryTextString += categories.get(i);
            if(i < categories.size()-1){
                categoryTextString += ", ";
            }
        }

        holder.categoryText.setText(categoryTextString);


    }



    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleText;
        TextView detailText;
        TextView durationText;
        TextView categoryText;
        ImageView image;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.feed_title_text);
            detailText = itemView.findViewById(R.id.feed_detail_text);
            durationText = itemView.findViewById(R.id.feed_duration_text);
            categoryText = itemView.findViewById(R.id.feed_category_text);
            image = itemView.findViewById(R.id.feed_image);
            deleteButton = itemView.findViewById(R.id.delete_video);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public Media getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    @FunctionalInterface
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
