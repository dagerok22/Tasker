package com.sergey_suslov.tasker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
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
import java.util.Locale;

/**
 * Created by PMI51 on 03.02.2017.
 */

public class MyTasksAdapter extends RecyclerView.Adapter<MyTasksAdapter.ViewHolder>{
    private ArrayList<TaskItem> mDataset;
    private SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    private Date todayDate = new Date();
    public Context mContext;
    private SQLiteDatabase readableDatabase;

    public void onItemDismiss(int position) {
        if(mContext instanceof ICounterUpdateMethod)
            ((ICounterUpdateMethod) mContext).updateCounter();
        doQueryForDone(mDataset.get(position).mId);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    private void doQueryForDone(int id){
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(mContext);
        readableDatabase = mDbHelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, 1);
        readableDatabase.update(FeedReaderContract.FeedEntry.TABLE_NAME, contentValues, FeedReaderContract.FeedEntry._ID + "=" + id, null);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
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

        public void bindView(TaskItem item){
            int priority = item.mPriority;
            switch (priority){
                case 1:
                    mImportantImageView.setAlpha(1.0f);
                    mUrgentImageView.setAlpha(1.0f);
                    break;
                case 2:
                    mImportantImageView.setAlpha(1.0f);
                    mUrgentImageView.setAlpha(0.3f);
                    break;
                case 3:
                    mUrgentImageView.setAlpha(1.0f);
                    mImportantImageView.setAlpha(0.3f);
                    break;
                case 4:
                    mUrgentImageView.setAlpha(0.3f);
                    mImportantImageView.setAlpha(0.3f);
                    break;
                default:break;
            }
            Date date;
            mTitleTextView.setText(item.mTitle);
            try {
                date = formater.parse(item.mDate.toString());
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
                mDateTextView.setText("Today");
            else
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)&&
                    cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR) == 1)
                mDateTextView.setText("Tomorrow");
            else
                mDateTextView.setText(item.mDate);

        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyTasksAdapter(ArrayList<TaskItem> myDataset, Context ctx) {
        mContext = ctx;
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TaskItem currentItem = mDataset.get(position);
        holder.bindView(currentItem);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setNewDate(ArrayList<TaskItem> newData){
        mDataset = newData;
        notifyDataSetChanged();
    }
}
