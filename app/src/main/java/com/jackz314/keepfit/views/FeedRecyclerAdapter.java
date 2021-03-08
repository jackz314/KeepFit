package com.jackz314.keepfit.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKot;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;

import java.util.List;

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    private static final String TAG = "FeedRecyclerAdapter";

    private List<Media> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;


    // data is passed into the constructor
    public FeedRecyclerAdapter(Context context, List<Media> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
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
            holder.durationText.setText(UtilsKot.formatDurationString(media.getDuration()));
        }

        Glide.with(holder.image)
                .load(media.getThumbnail())
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

        Glide.with(holder.profilePic)
                .load(creator.getProfilePic())
                .fitCenter()
                .placeholder(R.drawable.ic_account_circle_24)
                .into(holder.profilePic);

        holder.profilePic.setOnClickListener(v -> Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show());
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
        ImageView profilePic;
        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.feed_title_text);
            detailText = itemView.findViewById(R.id.feed_detail_text);
            durationText = itemView.findViewById(R.id.feed_duration_text);
            profilePic = itemView.findViewById(R.id.feed_profile_pic);
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
