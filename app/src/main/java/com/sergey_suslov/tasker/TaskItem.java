package com.sergey_suslov.tasker;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by PMI51 on 03.02.2017.
 */

public class TaskItem implements Comparable<TaskItem>{


    public Integer mId;
    public String mTitle;
    public Boolean mStatus;
    public Integer mPriority;

    public String getmDate() {
        return mDate;
    }

    public String mDate;

    public void setmDateFormat(Calendar mDateFormat) {
        this.mDateFormat = mDateFormat;
    }
    public void setmDateFormat(Date mDateFormat) {
        Calendar c = Calendar.getInstance();
        c.setTime(mDateFormat);
        this.mDateFormat = c;
    }

    public Calendar mDateFormat;

    public TaskItem() {
    }

    public void setmTitle(String mTitle) {

        this.mTitle = mTitle;
    }

    public void setmId(Integer mId) {
        this.mId = mId;
    }

    public void setmStatus(Boolean mStatus) {
        this.mStatus = mStatus;
    }

    public void setmPriority(Integer mPriority) {
        this.mPriority = mPriority;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    //Fix this later
    @Override
    public int compareTo(@NonNull TaskItem o) {
        if (o.mDateFormat.get(Calendar.DAY_OF_YEAR) == mDateFormat.get(Calendar.DAY_OF_YEAR))
        {
            return mPriority.compareTo(o.mPriority);
        }

        return mDateFormat.compareTo(o.mDateFormat);
    }
}
