package com.jackz314.keepfit.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
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
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchRecyclerAdapter extends RecyclerView.Adapter {

    private static final String TAG = "SearchRecyclerAdapter";

    private List<Object> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
    // TODO: Alter file to add profile functionality


    ArrayList<Object> models;
    final static int PROFILE=1;
    final static int MEDIA=2;



    // data is passed into the constructor
    public SearchRecyclerAdapter(Context context, List<Object> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewTy) {
        switch (viewTy)
        {
            case PROFILE:return new ViewHolder2(LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item,parent,false));
            default:return new ViewHolder1(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item,parent,false));
        }
    }
    @Override
    public int getItemViewType(int position) {
        if(mData.get(position)instanceof User)
            return PROFILE;
        else if(mData.get(position)instanceof Media)
            return MEDIA;
        else
            return MEDIA;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if(mData.get(position) instanceof User)
            ((ViewHolder2)holder).Bind((User)mData.get(position));
        else if (mData.get(position) instanceof Media)
            ((ViewHolder1)holder).Bind((Media) mData.get(position));



        //use ref directly, similar speed
//        media.getCreatorRef().get().addOnSuccessListener(snapshot -> {
//            long duration = System.currentTimeMillis() - start;
//            Log.d(TAG, "onBindViewHolder: duration: " + duration);
//            User creator = new User(snapshot);
//            populateCreatorInfo(holder, media, creator);
//        });
    }

//    private void populateCreatorInfo(RecyclerView.ViewHolder holder, Media media, User creator) {
//        holder.media.detailText.setText(media.getDetailString());
//
//        Glide.with(holder.profilePic)
//                .load(creator.getProfilePic())
//                .fitCenter()
//                .placeholder(R.drawable.ic_account_circle_24)
//                .into(holder.profilePic);
//
//        holder.profilePic.setOnClickListener(v -> Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show());
//    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder1 extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleText;
        TextView detailText;
        TextView durationText;
        ImageView profilePic;
        ImageView image;
        boolean isMedia = true;
        Media media = null;

        ViewHolder1(View itemView) {
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

        private void populateCreatorInfo(User creator) {
            this.detailText.setText(media.getDetailString());

            Glide.with(this.profilePic)
                    .load(creator.getProfilePic())
                    .fitCenter()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .into(this.profilePic);

            this.profilePic.setOnClickListener(v -> Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show());
        }

        public void Bind(Media media) {
            this.media = media;
            titleText.setText(media.getTitle());

            if (media.isLivestream()) {
                durationText.setText("LIVE");
                durationText.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0xB8, 0x03, 0x06)));
            } else {
                durationText.setText(UtilsKt.formatDurationString(media.getDuration()));
            }

            Glide.with(image)
                    .load(media.getThumbnail())
                    .fitCenter()
                    .placeholder(R.drawable.ic_thumb_placeholder)
                    .into(image);

            long start = System.currentTimeMillis();
            User creator = media.getCreator().getValue();
            if (creator == null || creator.getUid() == null) {
                media.getCreator().observeForever(new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        if (user != null && user.getUid() != null) {
                            long duration = System.currentTimeMillis() - start;
                            Log.d(TAG, "onBindViewHolder: duration: " + duration);
                            media.getCreator().removeObserver(this);
                            populateCreatorInfo(user);
                        }
                    }
                });
            } else {
                populateCreatorInfo(creator);
            }
        }

    }
    public class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView userName;
        ImageView profilePic;
        boolean isMedia = false;
        User user = null;

        ViewHolder2(View itemView){
            super(itemView);
            userName = itemView.findViewById(R.id.user_name_text);
            profilePic = itemView.findViewById(R.id.search_profile_pic);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        public void Bind(User user) {
            this.user = user;
            Glide.with(profilePic)
                    .load(user.getProfilePic())
                    .fitCenter()
                    .placeholder(R.drawable.ic_thumb_placeholder)
                    .into(profilePic);
            userName.setText(user.getName());
        }
    }

    // convenience method for getting data at click position
    public Object getItem(int id) {
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
