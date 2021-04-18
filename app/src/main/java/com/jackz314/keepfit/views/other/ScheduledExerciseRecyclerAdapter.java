package com.jackz314.keepfit.views.other;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.SchedulingController;
import com.jackz314.keepfit.models.ScheduledExercise;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScheduledExerciseRecyclerAdapter extends RecyclerView.Adapter<ScheduledExerciseRecyclerAdapter.ViewHolder> {

    private static final String TAG = "ExerciseRecyclerAdapter";

    private final List<ScheduledExercise> mData;
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
    private ItemClickListener mClickListener;


    // data is passed into the constructor
    public ScheduledExerciseRecyclerAdapter(Context context, List<ScheduledExercise> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.exercise_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the View in each row
    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        ScheduledExercise exercise = mData.get(position);
        holder.categoryText.setText(exercise.getCategory());
        holder.detailText.setText(DateUtils.formatDateTime(mContext, exercise.getTime().getTime(),
                DateUtils.FORMAT_NO_NOON | DateUtils.FORMAT_NO_MIDNIGHT |
                        DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_NO_YEAR |
                        DateUtils.FORMAT_SHOW_TIME));
        holder.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(mContext)
                    .setMessage(R.string.delete_scheduled_exercise_confirm_msg)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                            SchedulingController.deleteScheduledExercise(exercise.uid))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // convenience method for getting data at click position
    public ScheduledExercise getItem(int id) {
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
        TextView categoryText;
        TextView detailText;
        Button deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.scheduled_category);
            detailText = itemView.findViewById(R.id.scheduled_detail_text);
            deleteBtn = itemView.findViewById(R.id.scheduled_delete_btn);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
