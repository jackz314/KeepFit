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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

public class SearchRecyclerAdapter extends RecyclerView.Adapter {

    private static final String TAG = "SearchRecyclerAdapter";

    private final List<String> greetings = Arrays.asList("Hello", "Hi!", "Let's Be Friends");
    private List<SearchResult> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
    // TODO: Alter file to add profile functionality


    ArrayList<Object> models;
    final static int USER =1;
    final static int MEDIA=2;



    // data is passed into the constructor
    public SearchRecyclerAdapter(Context context, List<SearchResult> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewTy) {
        switch (viewTy)
        {
            case USER:return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item,parent,false));
            default:return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item,parent,false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mData.get(position).isUser())
            return USER;
        else
            return MEDIA;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if(mData.get(position).isUser())
            ((UserViewHolder)holder).Bind(mData.get(position).getUser());
        else
            ((MediaViewHolder)holder).Bind(mData.get(position).getMedia());



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
    public class MediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleText;
        TextView detailText;
        TextView durationText;
        ImageView profilePic;
        ImageView image;
        TagContainerLayout categoryText;
        boolean isMedia = true;
        Media media = null;

        MediaViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.feed_title_text);
            detailText = itemView.findViewById(R.id.feed_detail_text);
            durationText = itemView.findViewById(R.id.feed_duration_text);
            profilePic = itemView.findViewById(R.id.feed_profile_pic);
            categoryText = itemView.findViewById(R.id.feed_category_text);
            image = itemView.findViewById(R.id.feed_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        private void populateCreatorInfo(User creator) {
            this.detailText.setText(media.getDetailString());

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
            });        }

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
            if (media.isLivestream() || !"".equals(media.getThumbnail())) thumbnail = media.getThumbnail();
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
        }

    }
    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView userName;
        TextView userEmail;
        TextView bio;
        ImageView profilePic;
        boolean isMedia = false;
        User user = null;

        UserViewHolder(View itemView){
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
            Glide.with(profilePic)
                    .load(user.getProfilePic())
                    .fitCenter()
                    .placeholder(R.drawable.ic_thumb_placeholder)
                    .into(profilePic);
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());
            if(user.getBiography().isEmpty()){
                Random rand = new Random();
                String randGreeting = greetings.get(rand.nextInt(greetings.size()));
                bio.setText(randGreeting);
            }
            else
                bio.setText("About Me: "+user.getBiography());
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
