package com.sergey_suslov.tasker;

import android.app.Activity;
import android.graphics.Color;
import java.util.Calendar;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.github.florent37.singledateandtimepicker.widget.WheelDayPicker;

import java.util.Date;

import mehdi.sakout.fancybuttons.FancyButton;

public class AddTaskActivity extends AppCompatActivity {

    private FancyButton mUrgentBtn;
    private FancyButton mImportantBtn;

    private static final int TODAY_DATE = 0;
    private static final int TOMORROW_DATE = 1;
    private static final int CHOSEN_DATE = 2;
    private int mCurrentDateState = TODAY_DATE;

    private Date mTaskDate;
    private FancyButton mTodayDateBtn;
    private FancyButton mTomorrowDateBtn;
    private FancyButton mChosenDateBtn;

    private EditText mNewTaskEditText;

    private String mActiveColor = "#50FFFFFF";
//    private String mActiveColor = "#80C5725A";
    private String mPassiveColor = "#80C5725A";

    private Boolean mIsUrgent;
    private Boolean mIsImportant;


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        mNewTaskEditText.clearFocus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        mIsUrgent = false;
        mIsImportant = false;

        mCurrentDateState = TODAY_DATE;

        Calendar todayDate = Calendar.getInstance();
        Calendar tomorrowDate = Calendar.getInstance();
        tomorrowDate.add(Calendar.DAY_OF_MONTH, 1);
        Calendar chosenDate;

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.new_task_actionbar);
        getSupportActionBar().setElevation(0);

        FancyButton newTask_floating_btn = (FancyButton) findViewById(R.id.new_task_back_btn);
        newTask_floating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "BackPressed", Toast.LENGTH_SHORT).show();
            }
        });

        // New task etid field inimation
        mNewTaskEditText = (EditText) findViewById(R.id.new_task_edit_text) ;

        mNewTaskEditText.setSelected(false);
        mNewTaskEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            Animation animation;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("Animation edittext", String.valueOf(hasFocus));
                EditText et = (EditText) v;
                float alpha;
                if(hasFocus){
                    alpha = 0.9f;
                }
                else{
                    alpha = 0.5f;
                    hideKeyboard(v);
                }
                et.animate()
                        .setDuration(400)
                        .alpha(alpha)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                            }
                        })
                        .start();
            }
        });
        ///////////



        // Uregent and Important button dealing
        mUrgentBtn = (FancyButton) findViewById(R.id.urgent_checkbox_btn);
        mImportantBtn = (FancyButton) findViewById(R.id.important_checkbox_btn);

        mUrgentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsUrgent = !mIsUrgent;
                if (mIsUrgent)
                    mUrgentBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                else
                    mUrgentBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
            }
        });
        mImportantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsImportant = !mIsImportant;
                if (mIsImportant)
                    mImportantBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                else
                    mImportantBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
            }
        });
        ////////////


        // Dating
        WheelDayPicker wheelDayPicker = (WheelDayPicker) findViewById(R.id.single_day_picker);
        wheelDayPicker.setCurved(true);

        mTodayDateBtn = (FancyButton) findViewById(R.id.today_date_btn);
        mTomorrowDateBtn = (FancyButton) findViewById(R.id.tomorrow_date_btn);
        mChosenDateBtn = (FancyButton) findViewById(R.id.another_date_btn);
        mTodayDateBtn.setBackgroundColor(Color.parseColor(mActiveColor));

        mTodayDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentDateState = TODAY_DATE;
                mTodayDateBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                mTomorrowDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mChosenDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
            }
        });
        mTomorrowDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentDateState = TOMORROW_DATE;
                mTodayDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mTomorrowDateBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                mChosenDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
            }
        });
        mChosenDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentDateState = CHOSEN_DATE;
                mTodayDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mTomorrowDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mChosenDateBtn.setBackgroundColor(Color.parseColor(mActiveColor));
            }
        });
        ///////////
    }


}
