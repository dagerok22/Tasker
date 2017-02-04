package com.sergey_suslov.tasker;

import java.util.Date;

/**
 * Created by PMI51 on 03.02.2017.
 */

public class TaskItem {


    public Integer mId;
    public String mTitle;
    public Boolean mStatus;
    public Integer mPriority;
    public String mDate;

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
}
