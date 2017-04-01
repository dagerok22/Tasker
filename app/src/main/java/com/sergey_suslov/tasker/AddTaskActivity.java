package com.sergey_suslov.tasker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mehdi.sakout.fancybuttons.FancyButton;

public class AddTaskActivity extends AppCompatActivity
        implements CalendarDatePickerDialogFragment.OnDateSetListener{

    private FancyButton mUrgentBtn;
    private FancyButton mImportantBtn;
    private FloatingActionButton mCreateTaskBtn;

    private static final int TODAY_DATE = 0;
    private static final int TOMORROW_DATE = 1;
    private static final int CHOSEN_DATE = 2;
    private int mCurrentDateState = TODAY_DATE;

    private Calendar mTaskDate;
//    private Calendar mTodayDate;
//    private Calendar mTomorrowDate;
//    private Calendar mChosenDate;
    private FancyButton mTodayDateBtn;
    private FancyButton mTomorrowDateBtn;
    private FancyButton mChosenDateBtn;

    private EditText mNewTaskEditText;

    private Integer mActiveColor;
    //    private String mActiveColor = "#80C5725A";
    private Integer mPassiveColor;

    private Boolean mIsUrgent;
    private Boolean mIsImportant;

    private SQLiteDatabase db;

    private static final String FRAG_TAG_DATE_PICKER = "fragment_date_picker";


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
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        mIsUrgent = false;
        mIsImportant = false;

        setResult(RESULT_CANCELED);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        mActiveColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryActive);
        mPassiveColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryPassive);

//        mTodayDate = new Date();
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DAY_OF_MONTH, 1);
//        mTomorrowDate = cal.getTime();
//        mTaskDate = mTodayDate;
        mTaskDate = Calendar.getInstance();


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
        mNewTaskEditText = (EditText) findViewById(R.id.new_task_edit_text);

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
        mUrgentBtn = (FancyButton) findViewById(R.id.urgent_checkbox_btn);
        mUrgentBtn.setBackgroundColor(mPassiveColor);
        mImportantBtn = (FancyButton) findViewById(R.id.important_checkbox_btn);
        mImportantBtn.setBackgroundColor(mPassiveColor);

        mUrgentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsUrgent = !mIsUrgent;
                if (mIsUrgent)
                    mUrgentBtn.setBackgroundColor(mActiveColor);
                else
                    mUrgentBtn.setBackgroundColor(mPassiveColor);
            }
        });
        mImportantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsImportant = !mIsImportant;
                if (mIsImportant)
                    mImportantBtn.setBackgroundColor(mActiveColor);
                else
                    mImportantBtn.setBackgroundColor(mPassiveColor);
            }
        });
        ////////////


        // Dating
//        final WheelDayPicker wheelDayPicker = (WheelDayPicker) findViewById(R.id.single_day_picker);
//        wheelDayPicker.setCurved(true);
//        wheelDayPicker.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                mCurrentDateState = CHOSEN_DATE;
//                mTodayDateBtn.setBackgroundColor(mPassiveColor);
//                mTomorrowDateBtn.setBackgroundColor(mPassiveColor);
//                mChosenDateBtn.setBackgroundColor(mActiveColor);
//                return false;
//            }
//        });

        mTodayDateBtn = (FancyButton) findViewById(R.id.today_date_btn);
        mTomorrowDateBtn = (FancyButton) findViewById(R.id.tomorrow_date_btn);
        mChosenDateBtn = (FancyButton) findViewById(R.id.another_date_btn);
        mTodayDateBtn.setBackgroundColor(mActiveColor);
        mTomorrowDateBtn.setBackgroundColor(mPassiveColor);
        mChosenDateBtn.setBackgroundColor(mPassiveColor);

        mTodayDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentDateState = TODAY_DATE;
                mTaskDate = Calendar.getInstance();
                mTodayDateBtn.setBackgroundColor(mActiveColor);
                mTomorrowDateBtn.setBackgroundColor(mPassiveColor);
                mChosenDateBtn.setBackgroundColor(mPassiveColor);
            }
        });
        mTomorrowDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentDateState = TOMORROW_DATE;
                mTaskDate = Calendar.getInstance();
                mTaskDate.add(Calendar.DAY_OF_YEAR, 1);
                mTodayDateBtn.setBackgroundColor(mPassiveColor);
                mTomorrowDateBtn.setBackgroundColor(mActiveColor);
                mChosenDateBtn.setBackgroundColor(mPassiveColor);
            }
        });
        mChosenDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentDateState = CHOSEN_DATE;
                mTodayDateBtn.setBackgroundColor(mPassiveColor);
                mTomorrowDateBtn.setBackgroundColor(mPassiveColor);
                mChosenDateBtn.setBackgroundColor(mActiveColor);
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(AddTaskActivity.this)
                        .setThemeCustom(R.style.CustomDateTimePickerThemeStyle);
                cdp.show(getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);

            }
        });
        ///////////
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        // Create task
        mCreateTaskBtn = (FloatingActionButton) findViewById(R.id.finish_add_task_floating_btn);

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

//                switch (mCurrentDateState) {
//                    case TODAY_DATE:
//                        mTaskDate = mTodayDate;
//                        break;
//                    case TOMORROW_DATE:
//                        mTaskDate = mTomorrowDate;
//                        break;
////                    case CHOSEN_DATE:
////                        mTaskDate = wheelDayPicker.getCurrentDate();
////                        break;
//                    default:
//                        mTaskDate = mTodayDate;
//                        break;
//                }

                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                String formatted = format.format(mTaskDate.getTime());

                ContentValues values = new ContentValues();
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY, priority);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, formatted);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, 0);
                long newRowId = db.insert(
                        FeedReaderContract.FeedEntry.TABLE_NAME,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,
                        values);
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

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        mTaskDate.set(year, monthOfYear, dayOfMonth);
    }
}
