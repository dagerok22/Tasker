package com.sergey_suslov.tasker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TasksActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private FeedReaderDbHelper mDbHelper;

    private Context mContext;

    private ArrayList<TaskItem> mDataSet;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyTasksAdapter mAdapter;
    private SQLiteDatabase readableDatabase;

    TextView TodayTasksCountValueTextView;

    private TextView headerTitle;
    private View headerView;

    private Color mColorPrimary;

    private static final int ALL_TASKS_FILTER_STATE = 1;
    private static final int IMPORTANT_TASKS_FILTER_STATE = 2;
    private static final int URGENT_TASKS_FILTER_STATE = 3;
    private static final int NUNI_TASKS_FILTER_STATE = 4;
    private static final int TODAY_TASKS_FILTER_STATE = 5;
    private static final int TOMORROW_TASKS_FILTER_STATE = 6;
    private static final int IU_TASKS_FILTER_STATE = 7;
    private int CURRENT_FILTER_STATE = TODAY_TASKS_FILTER_STATE;

    private static final int NEW_TASK_ACTIVITY_REQUEST_CODE = 1;
    private static final int EDIT_TASK_ACTIVITY_REQUEST_CODE = 2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setElevation(0);

        SharedPreferences mSharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(getString(R.string.briefing_last_time_shown_date),
                89);
        editor.commit();

        startService(new Intent(getApplicationContext(), BriefingService.class));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        FloatingActionButton newTask_floating_btn = (FloatingActionButton) findViewById(R.id.newTask_floating_btn);
        newTask_floating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddTaskActivity.class);
                startActivityForResult(intent, NEW_TASK_ACTIVITY_REQUEST_CODE);
            }
        });

//
//        PackageManager pm = getPackageManager();
//        pm.setComponentEnabledSetting(new ComponentName("com.sergey_suslov.tasker", "com.sergey_suslov.tasker.TasksActivityAlias"),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//        pm.setComponentEnabledSetting(new ComponentName("com.sergey_suslov.tasker", "com.sergey_suslov.tasker.IntroActivity"),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        headerTitle = (TextView) findViewById(R.id.header_title);
        headerTitle.setText(R.string.today_tasks_title);
        CURRENT_FILTER_STATE = TODAY_TASKS_FILTER_STATE;
        // SQLite
        mDataSet = doQueryForToday();


        ////



        // Adapter things
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
//        mRecyclerView.setItemAnimator(new ScaleInAnimator());

        SnapHelper snapHelperTop = new GravitySnapHelper(Gravity.TOP);
        snapHelperTop.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getApplicationContext(), EditTaskActivity.class);
                        intent.putExtra(TaskFieldsContract.ID_NAME, String.valueOf(mDataSet.get(position).mId));
                        intent.putExtra(TaskFieldsContract.PRIORITY_NAME, mDataSet.get(position).mPriority);
                        intent.putExtra(TaskFieldsContract.TASK_TEXT_NAME, String.valueOf(mDataSet.get(position).mTitle));
                        startActivityForResult(intent, EDIT_TASK_ACTIVITY_REQUEST_CODE);
                    }
                })
        );
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyTasksAdapter(mDataSet, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        // Swipe stuff
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);
        ///////
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerElevation(0f);
        drawer.setScrimColor(ContextCompat.getColor(this, R.color.colorDrawerScrim));
//        drawer.setDrawerShadow(R.mipmap.transparent_shadow, GravityCompat.START);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){



            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                FrameLayout containerFrame  = (FrameLayout) findViewById(R.id.activity_tasks_frame_layout);
                containerFrame.setTranslationX(slideOffset * drawerView.getWidth());
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
                //below line used to remove shadow of drawer
                drawer.setScrimColor(Color.TRANSPARENT);
            }//this method helps you to aside menu drawer
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);


        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


//    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
//
//    private ArrayList<TaskItem> doQueryForAll() {
//        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
//        readableDatabase = mDbHelper.getReadableDatabase();
//        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0",
//                null,
//                null,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " , " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
//
//        ArrayList DataSet = new ArrayList<>();
//        if (c.moveToFirst()) {
//            do {
//                TaskItem taskItem = new TaskItem();
//                taskItem.setmId(c.getInt(0));
//                taskItem.setmTitle(c.getString(1));
//                try {
//                    taskItem.setmDateFormat(format.parse(c.getString(2)));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                taskItem.setmDate(c.getString(2));
//                if (c.getInt(3) == 0)
//                    taskItem.setmStatus(false);
//                else
//                    taskItem.setmStatus(true);
//                taskItem.setmPriority(c.getInt(4));
//                DataSet.add(taskItem);
//            } while (c.moveToNext());
//        }
//        c.close();
//        Collections.sort(DataSet);
//        return DataSet;
//    }
//
//    private ArrayList<TaskItem> doQueryForIU() {
//        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
//        readableDatabase = mDbHelper.getReadableDatabase();
//        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0 AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=1",
//                null,
//                null,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " , " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
//
//        ArrayList DataSet = new ArrayList<>();
//        if (c.moveToFirst()) {
//            do {
//                TaskItem taskItem = new TaskItem();
//                taskItem.setmId(c.getInt(0));
//                taskItem.setmTitle(c.getString(1));
//                try {
//                    taskItem.setmDateFormat(format.parse(c.getString(2)));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                taskItem.setmDate(c.getString(2));
//                if (c.getInt(3) == 0)
//                    taskItem.setmStatus(false);
//                else
//                    taskItem.setmStatus(true);
//                taskItem.setmPriority(c.getInt(4));
//                DataSet.add(taskItem);
//            } while (c.moveToNext());
//        }
//        c.close();
//        Collections.sort(DataSet);
//        return DataSet;
//    }
//
//    //
//    private ArrayList<TaskItem> doQueryForImportant() {
//        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
//        readableDatabase = mDbHelper.getReadableDatabase();
//        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=2",
//                null,
//                null,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);
//
//        ArrayList DataSet = new ArrayList<>();
//        if (c.moveToFirst()) {
//            do {
//                TaskItem taskItem = new TaskItem();
//                taskItem.setmId(c.getInt(0));
//                taskItem.setmTitle(c.getString(1));
//                try {
//                    taskItem.setmDateFormat(format.parse(c.getString(2)));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                taskItem.setmDate(c.getString(2));
//                if (c.getInt(3) == 0)
//                    taskItem.setmStatus(false);
//                else
//                    taskItem.setmStatus(true);
//                taskItem.setmPriority(c.getInt(4));
//                DataSet.add(taskItem);
//            } while (c.moveToNext());
//        }
//        c.close();
//        Collections.sort(DataSet);
//        return DataSet;
//    }
//
//    private ArrayList<TaskItem> doQueryForUrgent() {
//        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
//        readableDatabase = mDbHelper.getReadableDatabase();
//        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=3",
//                null,
//                null,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);
//
//        ArrayList DataSet = new ArrayList<>();
//        if (c.moveToFirst()) {
//            do {
//                TaskItem taskItem = new TaskItem();
//                taskItem.setmId(c.getInt(0));
//                taskItem.setmTitle(c.getString(1));
//                try {
//                    taskItem.setmDateFormat(format.parse(c.getString(2)));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                taskItem.setmDate(c.getString(2));
//                if (c.getInt(3) == 0)
//                    taskItem.setmStatus(false);
//                else
//                    taskItem.setmStatus(true);
//                taskItem.setmPriority(c.getInt(4));
//                DataSet.add(taskItem);
//            } while (c.moveToNext());
//        }
//        c.close();
//        Collections.sort(DataSet);
//        return DataSet;
//    }
//
//    private ArrayList<TaskItem> doQueryForNotImportantNotUrgant() {
//        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
//        readableDatabase = mDbHelper.getReadableDatabase();
//        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=4",
//                null,
//                null,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);
//
//        ArrayList DataSet = new ArrayList<>();
//        if (c.moveToFirst()) {
//            do {
//                TaskItem taskItem = new TaskItem();
//                taskItem.setmId(c.getInt(0));
//                taskItem.setmTitle(c.getString(1));
//                try {
//                    taskItem.setmDateFormat(format.parse(c.getString(2)));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                taskItem.setmDate(c.getString(2));
//                if (c.getInt(3) == 0)
//                    taskItem.setmStatus(false);
//                else
//                    taskItem.setmStatus(true);
//                taskItem.setmPriority(c.getInt(4));
//                DataSet.add(taskItem);
//            } while (c.moveToNext());
//        }
//        c.close();
//        Collections.sort(DataSet);
//        return DataSet;
//    }
//
    private ArrayList<TaskItem> doQueryForToday() {
        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        readableDatabase = mDbHelper.getReadableDatabase();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String formatted = format.format(new Date());
        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = '" + formatted + "' AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + "=0",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);

        ArrayList DataSet = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                TaskItem taskItem = new TaskItem();
                taskItem.setmId(c.getInt(0));
                taskItem.setmTitle(c.getString(1));
                try {
                    taskItem.setmDateFormat(format.parse(c.getString(2)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                taskItem.setmDate(c.getString(2));
                if (c.getInt(3) == 0)
                    taskItem.setmStatus(false);
                else
                    taskItem.setmStatus(true);
                taskItem.setmPriority(c.getInt(4));
                DataSet.add(taskItem);
            } while (c.moveToNext());
        }
        c.close();
        Collections.sort(DataSet);
        return DataSet;
    }
//
//    private ArrayList<TaskItem> doQueryForTomorrow() {
//        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
//        readableDatabase = mDbHelper.getReadableDatabase();
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DAY_OF_YEAR, 1);
//        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
//        String formatted = format.format(calendar.getTime());
//        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = '" + formatted + "' AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + "=0",
//                null,
//                null,
//                null,
//                FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
//
//        ArrayList DataSet = new ArrayList<>();
//        if (c.moveToFirst()) {
//            do {
//                TaskItem taskItem = new TaskItem();
//                taskItem.setmId(c.getInt(0));
//                taskItem.setmTitle(c.getString(1));
//                try {
//                    taskItem.setmDateFormat(format.parse(c.getString(2)));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                taskItem.setmDate(c.getString(2));
//                if (c.getInt(3) == 0)
//                    taskItem.setmStatus(false);
//                else
//                    taskItem.setmStatus(true);
//                taskItem.setmPriority(c.getInt(4));
//                DataSet.add(taskItem);
//            } while (c.moveToNext());
//        }
//        c.close();
//        Collections.sort(DataSet);
//        return DataSet;
//    }

//
//    public void updateCounter(){
//        TodayTasksCountValueTextView.setText(String.valueOf(mDataSet.size()));
//    }

    private void updateAdapter() {
        ArrayList<TaskItem> newDataset;
//        switch (CURRENT_FILTER_STATE) {
//            case ALL_TASKS_FILTER_STATE:
//                newDataset = doQueryForAll();
//                break;
//            case IU_TASKS_FILTER_STATE:
//                newDataset = doQueryForIU();
//                break;
//            case IMPORTANT_TASKS_FILTER_STATE:
//                newDataset = doQueryForImportant();
//                break;
//            case URGENT_TASKS_FILTER_STATE:
//                newDataset = doQueryForUrgent();
//                break;
//            case NUNI_TASKS_FILTER_STATE:
//                newDataset = doQueryForNotImportantNotUrgant();
//                break;
//            case TODAY_TASKS_FILTER_STATE:
//                newDataset = doQueryForToday();
//                break;
//            case TOMORROW_TASKS_FILTER_STATE:
//                newDataset = doQueryForTomorrow();
//                break;
//            default:
//                newDataset = doQueryForAll();
//                break;
//        }
        QueryForTasks queryForTasks = new QueryForTasks();
        queryForTasks.execute(CURRENT_FILTER_STATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_TASK_ACTIVITY_REQUEST_CODE || requestCode == NEW_TASK_ACTIVITY_REQUEST_CODE){
                updateAdapter();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_all) {
            CURRENT_FILTER_STATE = ALL_TASKS_FILTER_STATE;
            headerTitle.setText(R.string.all_tasks_title);
            updateAdapter();
        } else if (id == R.id.nav_iu) {
            CURRENT_FILTER_STATE = IU_TASKS_FILTER_STATE;
            headerTitle.setText(R.string.all_tasks_title);
            updateAdapter();
        } else if (id == R.id.nav_i) {
            CURRENT_FILTER_STATE = IMPORTANT_TASKS_FILTER_STATE;
            headerTitle.setText(R.string.important_tasks_title);
            updateAdapter();
        } else if (id == R.id.nav_u) {
            CURRENT_FILTER_STATE = URGENT_TASKS_FILTER_STATE;
            headerTitle.setText(R.string.urgent_tasks_title);
            updateAdapter();
        } else if (id == R.id.nav_today) {
            CURRENT_FILTER_STATE = TODAY_TASKS_FILTER_STATE;
            headerTitle.setText(R.string.today_tasks_title);
            updateAdapter();
        } else if (id == R.id.nav_tomorrow) {
            CURRENT_FILTER_STATE = TOMORROW_TASKS_FILTER_STATE;
            headerTitle.setText(R.string.tomorrow_tasks_title);
            updateAdapter();
        } else if (id == R.id.nav_ninu) {
            CURRENT_FILTER_STATE = NUNI_TASKS_FILTER_STATE;
            headerTitle.setText(R.string.not_i_not_u_tasks_title);
            updateAdapter();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerShadow(R.color.colorTransparent, GravityCompat.START);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Tasks Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public class QueryForTasks extends AsyncTask<Integer, Integer, ArrayList<TaskItem>> {

        @Override
        protected ArrayList<TaskItem> doInBackground(Integer... params) {
            mDbHelper = new FeedReaderDbHelper(getApplicationContext());
            readableDatabase = mDbHelper.getReadableDatabase();
            Cursor c;
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            switch (CURRENT_FILTER_STATE)
            {
                case ALL_TASKS_FILTER_STATE:
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " , " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
                    break;
                case IU_TASKS_FILTER_STATE:
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0 AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=1",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " , " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
                    break;
                case IMPORTANT_TASKS_FILTER_STATE:
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=2",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);
                    break;
                case URGENT_TASKS_FILTER_STATE:
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=3",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);
                    break;
                case NUNI_TASKS_FILTER_STATE:
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=4",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);
                    break;
                case TODAY_TASKS_FILTER_STATE:
                    String formatted = format.format(new Date());
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = '" + formatted + "' AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + "=0",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
                    break;
                case TOMORROW_TASKS_FILTER_STATE:
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                    String formattedDate = dateFormat.format(calendar.getTime());
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = '" + formattedDate + "' AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + "=0",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
                    break;
                default:
                    c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0",
                            null,
                            null,
                            null,
                            FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " , " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
                    break;
            }

            ArrayList DataSet = new ArrayList<>();
            if (c.moveToFirst()) {
                do {
                    TaskItem taskItem = new TaskItem();
                    taskItem.setmId(c.getInt(FeedReaderContract.FeedEntry.COLUMN_NUMBER_ID));
                    taskItem.setmTitle(c.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE));
                    try {
                        taskItem.setmDateFormat(format.parse(c.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_DATE)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    taskItem.setmDate(c.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_DATE));
                    if (c.getInt(FeedReaderContract.FeedEntry.COLUMN_NUMBER_STATUS) == 0)
                        taskItem.setmStatus(false);
                    else
                        taskItem.setmStatus(true);
                    taskItem.setmPriority(c.getInt(FeedReaderContract.FeedEntry.COLUMN_NUMBER_PRIORITY));
                    DataSet.add(taskItem);
                } while (c.moveToNext());
            }
            c.close();
            Collections.sort(DataSet);
            return DataSet;
        }

        @Override
        protected void onPostExecute(ArrayList<TaskItem> taskItems) {
            super.onPostExecute(taskItems);
            mDataSet = taskItems;
            mAdapter.setNewDate(mDataSet);
        }
    }

}
