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
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.controllers.VideoController;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.SearchActivity;
import com.jackz314.keepfit.views.UserProfileActivity;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder> {

    private static final String TAG = "HistoryRecyclerAdapter";

    private final List<Media> mData;
    private final LayoutInflater mInflater;
    private final HashSet<String> likedVideos = new HashSet<>();
    private final HashSet<String> dislikedVideos = new HashSet<>();
    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
    private final Context con;
    private ItemClickListener mClickListener;


    // data is passed into the constructor
    public HistoryRecyclerAdapter(Context context, List<Media> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.con = context;
        UserControllerKt.getCurrentUserDoc().collection("liked_videos").addSnapshotListener(((value, e) -> {
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            likedVideos.clear();
            for (QueryDocumentSnapshot doc : value) {
                likedVideos.add(doc.getId());
            }

            updateMediaListLikeStatus(true);
        }));
        UserControllerKt.getCurrentUserDoc().collection("disliked_videos").addSnapshotListener(((value, e) -> {
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            dislikedVideos.clear();
            for (QueryDocumentSnapshot doc : value) {
                dislikedVideos.add(doc.getId());
            }

            updateMediaListLikeStatus(false);
        }));
    }

    private void updateMediaListLikeStatus(boolean liked) {
        if (liked) {
            for (int i = 0, mDataSize = mData.size(); i < mDataSize; i++) {
                Media media = mData.get(i);
                boolean isLiked = likedVideos.contains(media.getUid());

                if (media.getLiked() != isLiked) {
                    media.setLiked(isLiked);
                }
            }
        } else {
            for (int i = 0, mDataSize = mData.size(); i < mDataSize; i++) {
                Media media = mData.get(i);

                boolean isDisliked = dislikedVideos.contains(media.getUid());

                if (media.getDisliked() != isDisliked) {
                    media.setDisliked(isDisliked);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void notifyDataChanged() {
        updateMediaListLikeStatus(true);
        updateMediaListLikeStatus(false);
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

        if (media.isLivestream()) {
            holder.durationText.setText("LIVE");
            holder.durationText.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0xB8, 0x03, 0x06)));
        } else {
            holder.durationText.setText(UtilsKt.formatDurationString(media.getDuration()));
            holder.durationText.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        }

        String thumbnail;
        if (media.isLivestream() || !"".equals(media.getThumbnail()))
            thumbnail = media.getThumbnail();
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
                    if (user != null && user.getUid() != null) {
                        long duration = System.currentTimeMillis() - start;
                        Log.d(TAG, "onBindViewHolder: duration: " + duration);
                        media.getCreator().removeObserver(this);
                        populateCreatorInfo(holder, media, user);
                    }
                }
            });
        } else {
            populateCreatorInfo(holder, media, creator);
        }


        holder.likeButton.setLiked(media.getLiked());
        holder.dislikeButton.setLiked(media.getDisliked());

        holder.likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                if (holder.dislikeButton.isLiked()) {
                    holder.dislikeButton.callOnClick();
                }
                UserControllerKt.likeVideo(media.getUid());
                VideoController.likeVideo(media.getUid());
                media.setLikes(media.getLikes() + 1); // no listener, so manually update
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                UserControllerKt.unlikeVideo(media.getUid());
                VideoController.unlikeVideo(media.getUid());
                media.setLikes(media.getLikes() - 1);
            }
        });
        holder.dislikeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                if (holder.likeButton.isLiked()) {
                    holder.likeButton.callOnClick();
                }
                UserControllerKt.dislikeVideo(media.getUid());
                VideoController.dislikeVideo(media.getUid());
                media.setDislikes(media.getDislikes() + 1); // no listener, so manually update
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                UserControllerKt.undislikeVideo(media.getUid());
                VideoController.undislikeVideo(media.getUid());
                media.setDislikes(media.getDislikes() - 1); // no listener, so manually update
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(con)
                    .setMessage(R.string.delete_history_confirm)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> UserControllerKt.deleteFromHistory(media.getUid()))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
//            Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show()
        });
        holder.deleteButton.setImageResource(R.drawable.ic_delete_hist);
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

//        String categoryTextString = "";
//        for(int i = 0; i < categories.size() ; ++i){
//            categoryTextString += categories.get(i);
//            if(i < categories.size()-1){
//                categoryTextString += ", ";
//            }
//        }
//
//        holder.categoryText.setText(categoryTextString);

        holder.categoryText.setTags(categories);

        holder.categoryText.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
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

        Glide.with(mInflater.getContext().getApplicationContext())
                .load(creator.getProfilePic())
                .fitCenter()
                .placeholder(R.drawable.ic_account_circle_24)
                .into(holder.profilePic);

        holder.profilePic.setOnClickListener(v -> {
            Intent in = new Intent(v.getContext(), UserProfileActivity.class);
            in.putExtra(GlobalConstants.USER_PROFILE, creator);
            v.getContext().startActivity(in);
//            Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show()
        });
    }


    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
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

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleText;
        TextView detailText;
        TextView durationText;
        TagContainerLayout categoryText;
        ImageView profilePic;
        ImageView image;
        LikeButton likeButton;
        LikeButton dislikeButton;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            detailText = itemView.findViewById(R.id.detail_text);
            durationText = itemView.findViewById(R.id.duration_text);
            categoryText = itemView.findViewById(R.id.category_text);
            profilePic = itemView.findViewById(R.id.profile_pic);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
            image = itemView.findViewById(R.id.thumbnail_image);
            deleteButton = itemView.findViewById(R.id.delete_video);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
