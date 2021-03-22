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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    private static final String TAG = "FeedRecyclerAdapter";

    private List<Media> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private HashSet<String> likedVideos = new HashSet<>();

    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;


    // data is passed into the constructor
    public FeedRecyclerAdapter(Context context, List<Media> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        UserControllerKt.getCurrentUserDoc().collection("liked_videos").addSnapshotListener(((value, e) -> {
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            likedVideos.clear();
            for (QueryDocumentSnapshot doc : value) {
                likedVideos.add(doc.getDocumentReference("ref").getId());
            }

            for (Media media : mData) {
                if (likedVideos.contains(media.getUid())) {
                    media.setLiked(true);
                } else {
                    media.setLiked(false);
                }
            }
        }));
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.feed_item, parent, false);
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


        holder.likeButton.setLiked(media.getLiked());


        holder.likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                UserControllerKt.likeVideo(media.getUid());
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                UserControllerKt.unlikeVideo(media.getUid());
            }
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
        holder.detailText.setText(media.getDetailString());

        List<String> categories = media.getCategories();

        String categoryTextString = "";
        for(int i = 0; i < categories.size() ; ++i){
            categoryTextString += categories.get(i);
            if(i < categories.size()-1){
                categoryTextString += ", ";
            }
        }

        holder.categoryText.setText(categoryTextString);

        Glide.with(holder.profilePic)
                .load(creator.getProfilePic())
                .fitCenter()
                .placeholder(R.drawable.ic_account_circle_24)
                .into(holder.profilePic);

        holder.profilePic.setOnClickListener(v -> {
            Intent in = new Intent(v.getContext(), UserProfileActivity.class);
            in.putExtra("other", creator);
            v.getContext().startActivity(in);
//            Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show()
        });
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
        ImageView profilePic;
        ImageView image;
        LikeButton likeButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.feed_title_text);
            detailText = itemView.findViewById(R.id.feed_detail_text);
            durationText = itemView.findViewById(R.id.feed_duration_text);
            categoryText = itemView.findViewById(R.id.feed_category_text);
            profilePic = itemView.findViewById(R.id.feed_profile_pic);
            likeButton = itemView.findViewById(R.id.feed_like_button);
            image = itemView.findViewById(R.id.feed_image);
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
