package com.jackz314.keepfit.views.other;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.models.Comment;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.views.VideoActivity;
import com.jackz314.keepfit.views.VideosFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.CommentViewHolder>{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "CommentRecyclerAdapter";

    private final List<Comment> mData;
    private final LayoutInflater mInflater;
    private VideosRecyclerAdapter.ItemClickListener mClickListener;

    private VideoActivity videoActivity;

    public CommentRecyclerAdapter(Context context, List<Comment> data, VideoActivity videoActivity){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.videoActivity = videoActivity;
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = mData.get(position);
        String username = "";
        DocumentReference docRef = db.collection("users").document(comment.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        holder.mUsername.setText(document.getString("name"));
                        Glide.with(mInflater.getContext().getApplicationContext())
                                .load(document.getString("profile_pic"))
                                .fitCenter()
                                .placeholder(R.drawable.ic_account_circle_24)
                                .into(holder.mProfilePic);
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        holder.uid = document.getString("user");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        holder.mText.setText(comment.getText());
        holder.mUploadTime.setText(convertStringToDate(comment.getUploadTime()));

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(videoActivity)
                    .setMessage("Do you want to delete the comment?")
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> videoActivity.deleteComment(comment.getUid(),comment.getCid(), comment.getMid()))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
//            Toast.makeText(v.getContext(), "Go to " + creator.getName() + "'s profile page", Toast.LENGTH_SHORT).show()
        });

    }

    public String convertStringToDate(Date indate)
    {
        String dateString = null;
        SimpleDateFormat sdfr = new SimpleDateFormat(" hh:mm:ss dd/MMM/yy");
        /*you can also use DateFormat reference instead of SimpleDateFormat
         * like this: DateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
         */
        try{
            dateString = sdfr.format( indate );
        }catch (Exception ex ){
            System.out.println(ex);
        }
        return dateString;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mUsername;
        private TextView mText;
        private TextView mUploadTime;
        private ImageView mProfilePic;
        private ImageButton deleteButton;
        String uid;

        public CommentViewHolder(View itemView) {
            super(itemView);

            mUsername = itemView.findViewById(R.id.comment_item_username);
            mText = itemView.findViewById(R.id.comment_item_text);
            mUploadTime = itemView.findViewById(R.id.comment_item_date);
            mProfilePic = itemView.findViewById(R.id.comment_item_profile_pic);
            deleteButton = itemView.findViewById(R.id.comment_item_delete);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setClickListener(VideosRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    @FunctionalInterface
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}


