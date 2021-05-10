package com.jackz314.keepfit.views.other;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jackz314.keepfit.R;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.models.Exercise;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class ExerciseRecyclerAdapter extends RecyclerView.Adapter<ExerciseRecyclerAdapter.ViewHolder> {

    private static final String TAG = "ExerciseRecyclerAdapter";

    private final List<Exercise> mData;
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final int widthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
    private ItemClickListener mClickListener;


    // data is passed into the constructor
    public ExerciseRecyclerAdapter(Context context, List<Exercise> data) {
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
        Exercise exercise = mData.get(position);
        holder.categoryText.setText(exercise.getCategory());
        holder.calText.setText(String.format(Locale.getDefault(), "%.3f Cal", exercise.getCalories()));
        holder.dateText.setText(DateUtils.getRelativeDateTimeString(mContext, exercise.getStartingTime().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_MONTH));
        holder.durationText.setText(UtilsKt.formatDurationTextString((exercise.getElapsedTime() / DateUtils.SECOND_IN_MILLIS)));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // convenience method for getting data at click position
    public Exercise getItem(int id) {
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
        TextView calText;
        TextView durationText;
        TextView dateText;

        ViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.exercise_category);
            calText = itemView.findViewById(R.id.exercise_cals);
            durationText = itemView.findViewById(R.id.exercise_duration);
            dateText = itemView.findViewById(R.id.exercise_date);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
