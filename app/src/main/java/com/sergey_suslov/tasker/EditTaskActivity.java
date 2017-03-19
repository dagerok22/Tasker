package com.sergey_suslov.tasker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.github.florent37.singledateandtimepicker.widget.WheelDayPicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mehdi.sakout.fancybuttons.FancyButton;

public class EditTaskActivity extends AppCompatActivity {

    private FancyButton mUrgentBtn;
    private FancyButton mImportantBtn;
    private FancyButton mCreateTaskBtn;

    private static final int TODAY_DATE = 0;
    private static final int TOMORROW_DATE = 1;
    private static final int CHOSEN_DATE = 2;
    private int mCurrentDateState = TODAY_DATE;

    private Date mTaskDate;
    private Date mTodayDate;
    private Date mTomorrowDate;
    private Date mChosenDate;
    private FancyButton mTodayDateBtn;
    private FancyButton mTomorrowDateBtn;
    private FancyButton mChosenDateBtn;

    private EditText mNewTaskEditText;

    private String mActiveColor = "#50FFFFFF";
    //    private String mActiveColor = "#80C5725A";
    private String mPassiveColor = "#80C5725A";

    private Boolean mIsUrgent;
    private Boolean mIsImportant;

    private SQLiteDatabase db;




    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        mNewTaskEditText.clearFocus();
    }

    public static Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        mIsUrgent = false;
        mIsImportant = false;
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        String taskTitleExtra = intent.getStringExtra(TaskFieldsContract.TASK_TEXT_NAME);
        int taskPriorityExtra = intent.getIntExtra(TaskFieldsContract.PRIORITY_NAME, 4);
        final String taskIdExtra = intent.getStringExtra(TaskFieldsContract.ID_NAME);
        mNewTaskEditText = (EditText) findViewById(R.id.new_task_edit_text);
        mNewTaskEditText.setText(taskTitleExtra);
        mTodayDateBtn = (FancyButton) findViewById(R.id.today_date_btn);
        mTomorrowDateBtn = (FancyButton) findViewById(R.id.tomorrow_date_btn);
        mChosenDateBtn = (FancyButton) findViewById(R.id.another_date_btn);

        mUrgentBtn = (FancyButton) findViewById(R.id.urgent_checkbox_btn);
        mImportantBtn = (FancyButton) findViewById(R.id.important_checkbox_btn);


        switch (taskPriorityExtra){
            case 1:
                mIsUrgent = true;
                mIsImportant = true;
                mUrgentBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                mImportantBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                break;
            case 2:
                mIsUrgent = false;
                mIsImportant = true;
                mUrgentBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mImportantBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                break;
            case 3:
                mIsUrgent = true;
                mIsImportant = false;
                mUrgentBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                mImportantBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                break;
            case 4:
                mIsUrgent = false;
                mIsImportant = false;
                mUrgentBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mImportantBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                break;
        }



        mTodayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        mTomorrowDate = cal.getTime();
        mTaskDate = mTodayDate;


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
                setResult(RESULT_CANCELED);
                finish();
            }
        });


        // New task etid field inimation


        mNewTaskEditText.setSelected(false);
        mNewTaskEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("Animation edittext", String.valueOf(hasFocus));
                EditText et = (EditText) v;
                float alpha;
                if (hasFocus) {
                    alpha = 0.9f;
                } else {
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
        final WheelDayPicker wheelDayPicker = (WheelDayPicker) findViewById(R.id.single_day_picker);
        wheelDayPicker.setCurved(true);
        wheelDayPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCurrentDateState = CHOSEN_DATE;
                mTodayDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mTomorrowDateBtn.setBackgroundColor(Color.parseColor(mPassiveColor));
                mChosenDateBtn.setBackgroundColor(Color.parseColor(mActiveColor));
                return false;
            }
        });

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

        db = mDbHelper.getWritableDatabase();

        // Create task
        mCreateTaskBtn = (FancyButton) findViewById(R.id.finish_add_task_btn);
        mCreateTaskBtn.setIconResource(R.drawable.ic_checked_done);

        mCreateTaskBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String title = mNewTaskEditText.getText().toString();
                Integer priority = 4;
                if (mIsUrgent && mIsImportant)
                    priority = 1;
                else if (mIsUrgent)
                    priority = 3;
                else if (mIsImportant)
                    priority = 2;

                switch (mCurrentDateState) {
                    case TODAY_DATE:
                        mTaskDate = mTodayDate;
                        break;
                    case TOMORROW_DATE:
                        mTaskDate = mTomorrowDate;
                        break;
                    case CHOSEN_DATE:
                        mTaskDate = wheelDayPicker.getCurrentDate();
                        break;
                    default:
                        mTaskDate = mTodayDate;
                        break;
                }

                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                String formatted = format.format(mTaskDate);

                ContentValues values = new ContentValues();
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY, priority);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, formatted);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, 0);
                long newRowId = db.update(FeedReaderContract.FeedEntry.TABLE_NAME,
                        values,
                        FeedReaderContract.FeedEntry._ID + "=" + taskIdExtra,
                        null);
                Log.d("mTaskDate newRowId", String.valueOf(newRowId));
                setResult(RESULT_OK);
                finish();
            }
        });

        ///////////
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
//        super.onBackPressed();
    }
}
