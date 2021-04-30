package com.jackz314.keepfit.views.other;

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
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.SearchActivity;
import com.like.LikeButton;

import java.util.List;
import java.util.stream.Collectors;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

public class FollowRecyclerAdapter extends RecyclerView.Adapter<FollowRecyclerAdapter.ViewHolder> {

    private static final String TAG = "FeedRecyclerAdapter";

    private final List<Media> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;


    // data is passed into the constructor
    public FollowRecyclerAdapter(Context context, List<Media> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.condensed_video_item, parent, false);
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
        holder.profilePic.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.likeButton.setVisibility(View.GONE);
        holder.dislikeButton.setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.options_button).setVisibility(View.GONE);

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

        List<String> categories = media.getCategories().stream().map(String::trim).collect(Collectors.toList());


//            String categoryTextString = "";
//            int s = categories.size();
//            for(int i = 0; i<s ; ++i){
//                categoryTextString +=categories.get(i);
//                if(i < s-1){
//                    categoryTextString += ", ";
//                }
//            }

        holder.categoryText.setTags(categories);

        holder.categoryText.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                Log.d(TAG, "onTagClick: clicked");
                Intent intent = new Intent(mInflater.getContext(), SearchActivity.class);
                intent.putExtra(GlobalConstants.SEARCH_QUERY, text);
                mInflater.getContext().startActivity(intent);
            }

            @Override
            public void onTagLongClick(int position, String text) {

            }

            @Override
            public void onSelectedTagDrag(int position, String text) {

            }

            @Override
            public void onTagCrossClick(int position) {

            }
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
        TagContainerLayout categoryText;
        ImageView image;
        ImageView profilePic;
        ImageButton deleteButton;
        LikeButton likeButton;
        LikeButton dislikeButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            detailText = itemView.findViewById(R.id.detail_text);
            durationText = itemView.findViewById(R.id.duration_text);
            categoryText = itemView.findViewById(R.id.category_text);
            image = itemView.findViewById(R.id.thumbnail_image);
            profilePic = itemView.findViewById(R.id.profile_pic);
            deleteButton = itemView.findViewById(R.id.delete_video);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
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
