package com.sergey_suslov.tasker;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by PMI51 on 03.02.2017.
 */

public class MyTasksAdapter extends RecyclerView.Adapter<MyTasksAdapter.ViewHolder> {
    private ArrayList<TaskItem> mDataset;
    private SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    private Date todayDate = new Date();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTitleTextView;
        public TextView mDateTextView;
        public ImageView mUrgentImageView;
        public ImageView mImportantImageView;
        public RelativeLayout mTaskItemContainer;

        public ViewHolder(View v) {
            super(v);
            mTitleTextView = (TextView) v.findViewById(R.id.task_title_textview);
            mDateTextView = (TextView) v.findViewById(R.id.task_date_textview);
            mUrgentImageView = (ImageView) v.findViewById(R.id.urgent_imageView);
            mImportantImageView = (ImageView) v.findViewById(R.id.important_imageView);
            mTaskItemContainer = (RelativeLayout) v.findViewById(R.id.task_item_container);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyTasksAdapter(ArrayList<TaskItem> myDataset) {
        mDataset = myDataset;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyTasksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item_view, parent, false);

        // Customize View

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (mDataset.get(position).mStatus)
            holder.mTaskItemContainer.setAlpha(0.5f);
        else
            holder.mTaskItemContainer.setAlpha(1.0f);

        Date date;
        holder.mTitleTextView.setText(mDataset.get(position).mTitle);
        try {
            date = formater.parse(mDataset.get(position).mDate.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date(0);
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        cal2.setTime(todayDate);
        if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)&&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
            holder.mDateTextView.setText("Today");
        else
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)&&
                    cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR) == 1)
            holder.mDateTextView.setText("Tomorrow");
        else
            holder.mDateTextView.setText(mDataset.get(position).mDate);
        int priority = mDataset.get(position).mPriority;
        switch (priority){
            case 1:
                break;
            case 2:
                holder.mUrgentImageView.setAlpha(0.3f);
                break;
            case 3:
                holder.mImportantImageView.setAlpha(0.3f);
                break;
            case 4:
                holder.mUrgentImageView.setAlpha(0.3f);
                holder.mImportantImageView.setAlpha(0.3f);
                break;
            default:break;
        }
        Log.d("Adapter check", "\nPosition: " + position + "\nPriority: " + priority + "\nStatus: " +  mDataset.get(position).mStatus);
    }

    public void refreshBlockOverlay(int position) {
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setNewDate(ArrayList<TaskItem> newData){
        mDataset.clear();
        mDataset.addAll(newData);
        notifyDataSetChanged();
    }
}
