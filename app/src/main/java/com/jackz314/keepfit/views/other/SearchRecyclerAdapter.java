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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.controllers.VideoController;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.SearchActivity;
import com.jackz314.keepfit.views.UserProfileActivity;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

public class SearchRecyclerAdapter extends RecyclerView.Adapter {

    final static int USER = 1;
    final static int MEDIA = 2;
    private static final String TAG = "SearchRecyclerAdapter";
    private final List<String> greetings = Arrays.asList("Hello", "Hi!", "Let's Be Friends");
    private final List<SearchResult> mData;
    private final LayoutInflater mInflater;
    // TODO: Alter file to add profile functionality
    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
    private final HashSet<String> likedVideos = new HashSet<>();
    private final HashSet<String> dislikedVideos = new HashSet<>();
    ArrayList<Object> models;
    private ItemClickListener mClickListener;


    // data is passed into the constructor
    public SearchRecyclerAdapter(Context context, List<SearchResult> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
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
                SearchResult res = mData.get(i);

                if (!res.isUser()) {
                    boolean isLiked = likedVideos.contains(res.getMedia().getUid());

                    if (res.getMedia().getLiked() != isLiked) {
                        res.getMedia().setLiked(isLiked);
                    }
                }
            }
        } else {
            for (int i = 0, mDataSize = mData.size(); i < mDataSize; i++) {
                SearchResult res = mData.get(i);

                if (!res.isUser()) {
                    boolean isDisliked = dislikedVideos.contains(res.getMedia().getUid());

                    if (res.getMedia().getDisliked() != isDisliked) {
                        res.getMedia().setDisliked(isDisliked);
                    }
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewTy) {
        switch (viewTy) {
            case USER:
                return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false));
            default:
                return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.condensed_video_item, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).isUser())
            return USER;
        else
            return MEDIA;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if (mData.get(position).isUser())
            ((UserViewHolder) holder).Bind(mData.get(position).getUser());
        else
            ((MediaViewHolder) holder).Bind(mData.get(position).getMedia());


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

    // stores and recycles views as they are scrolled off screen
    public class MediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleText;
        TextView detailText;
        TextView durationText;
        ImageView profilePic;
        ImageView image;
        TagContainerLayout categoryText;
        boolean isMedia = true;
        Media media = null;
        LikeButton likeButton;
        LikeButton dislikeButton;
        ImageButton deleteButton;
        ImageButton options;


        MediaViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            detailText = itemView.findViewById(R.id.detail_text);
            durationText = itemView.findViewById(R.id.duration_text);
            profilePic = itemView.findViewById(R.id.profile_pic);
            categoryText = itemView.findViewById(R.id.category_text);
            image = itemView.findViewById(R.id.thumbnail_image);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
            deleteButton = itemView.findViewById((R.id.delete_video));
            options = itemView.findViewById(R.id.options_button);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        private void populateCreatorInfo(User creator) {
            this.detailText.setText(media.getProfileString());

            List<String> categories = media.getCategories().stream().map(String::trim).collect(Collectors.toList());


//            String categoryTextString = "";
//            int s = categories.size();
//            for(int i = 0; i<s ; ++i){
//                categoryTextString +=categories.get(i);
//                if(i < s-1){
//                    categoryTextString += ", ";
//                }
//            }

            categoryText.setTags(categories);

            categoryText.setOnTagClickListener(new TagView.OnTagClickListener() {
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

            Glide.with(this.profilePic)
                    .load(creator.getProfilePic())
                    .fitCenter()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .into(this.profilePic);

            profilePic.setOnClickListener(v -> {
                Intent in = new Intent(v.getContext(), UserProfileActivity.class);
                in.putExtra(GlobalConstants.USER_PROFILE, creator);
                v.getContext().startActivity(in);
//            Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show()
            });
        }

        public void Bind(Media media) {
            this.media = media;
            titleText.setText(media.getTitle());

            if (media.isLivestream()) {
                durationText.setText("LIVE");
                durationText.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0xB8, 0x03, 0x06)));
            } else {
                durationText.setText(UtilsKt.formatDurationString(media.getDuration()));
                durationText.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }

            String thumbnail;
            if (media.isLivestream() || !"".equals(media.getThumbnail()))
                thumbnail = media.getThumbnail();
            else thumbnail = media.getLink();
            Glide.with(image)
                    .load(thumbnail)
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

            if (likeButton != null) {
                likeButton.setLiked(media.getLiked());

                likeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        if (dislikeButton.isLiked()) {
                            dislikeButton.callOnClick();
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
            }
            if (dislikeButton != null) {
                dislikeButton.setLiked(media.getDisliked());

                dislikeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton dislikeButton) {
                        if (likeButton.isLiked()) {
                            likeButton.callOnClick();
                        }
                        UserControllerKt.dislikeVideo(media.getUid());
                        VideoController.dislikeVideo(media.getUid());
                        media.setDislikes(media.getDislikes() + 1); // no listener, so manually update
                    }

                    @Override
                    public void unLiked(LikeButton dislikeButton) {
                        UserControllerKt.undislikeVideo(media.getUid());
                        VideoController.undislikeVideo(media.getUid());
                        media.setDislikes(media.getDislikes() - 1); // no listener, so manually update
                    }
                });
            }
            deleteButton.setVisibility(View.GONE);
            options.setVisibility(View.GONE);
        }

    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView userName;
        TextView userEmail;
        TextView bio;
        ImageView profilePic;
        boolean isMedia = false;
        User user = null;


        UserViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name_text);
            userEmail = itemView.findViewById(R.id.user_email_text);
            profilePic = itemView.findViewById(R.id.search_profile_pic);
            bio = itemView.findViewById(R.id.biography);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        public void Bind(User user) {
            this.user = user;
            Glide.with(mInflater.getContext().getApplicationContext())
                    .load(user.getProfilePic())
                    .fitCenter()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .into(profilePic);

            profilePic.setOnClickListener(v -> {
                Intent in = new Intent(v.getContext(), UserProfileActivity.class);
                in.putExtra(GlobalConstants.USER_PROFILE, user);
                v.getContext().startActivity(in);
//            Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show()
            });
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());
            if (user.getBiography().isEmpty()) {
                Random rand = new Random();
                String randGreeting = greetings.get(rand.nextInt(greetings.size()));
                bio.setText(randGreeting);
            } else
                bio.setText("About Me: " + user.getBiography());
        }
    }
}
