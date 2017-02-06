package com.sergey_suslov.tasker;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import java.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private ArrayList<TaskItem> mDataSet;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyTasksAdapter mAdapter;
    private SQLiteDatabase readableDatabase;

    private TextView headerTitle;

    private static final int ALL_TASKS_FILTER_STATE = 1;
    private static final int IMPORTANT_TASKS_FILTER_STATE = 2;
    private static final int URGENT_TASKS_FILTER_STATE = 3;
    private static final int NUNI_TASKS_FILTER_STATE = 4;
    private static final int TODAY_TASKS_FILTER_STATE = 5;
    private static final int TOMORROW_TASKS_FILTER_STATE = 6;
    private int CURRENT_FILTER_STATE = TODAY_TASKS_FILTER_STATE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setElevation(0);

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
                startActivityForResult(intent, 1);
            }
        });

//
//        PackageManager pm = getPackageManager();
//        pm.setComponentEnabledSetting(new ComponentName(this, com.sergey_suslov.tasker.TasksActivity.class),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//        pm.setComponentEnabledSetting(new ComponentName(this, com.sergey_suslov.tasker.IntroActivity.class),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        headerTitle = (TextView) findViewById(R.id.header_title);
        headerTitle.setText(R.string.today_tasks);
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
                    @Override public void onItemClick(View view, int position) {
                        doQueryForDone(mDataSet.get(position));
                        mDataSet.remove(position);
                        mAdapter.notifyItemRemoved(position);
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
        mAdapter = new MyTasksAdapter(mDataSet);
        mRecyclerView.setAdapter(mAdapter);
        ///////
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setScrimColor(ContextCompat.getColor(this, R.color.colorDrawerScrim));
        drawer.setDrawerShadow(R.mipmap.transparent_shadow, GravityCompat.START);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

    private ArrayList<TaskItem> doQueryForAll(){
        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        readableDatabase = mDbHelper.getReadableDatabase();
        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " , " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);

        ArrayList DataSet = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                TaskItem taskItem = new TaskItem();
                taskItem.setmId(c.getInt(0));
                taskItem.setmTitle(c.getString(1));
                taskItem.setmDate(c.getString(2));
                if(c.getInt(3) == 0)
                    taskItem.setmStatus(false);
                else
                    taskItem.setmStatus(true);
                taskItem.setmPriority(c.getInt(4));
                DataSet.add(taskItem);
            }while (c.moveToNext());
        }
        c.close();
        return DataSet;
    }
//
    private ArrayList<TaskItem> doQueryForImportant(){
        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        readableDatabase = mDbHelper.getReadableDatabase();
        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=2",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);

        ArrayList DataSet = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                TaskItem taskItem = new TaskItem();
                taskItem.setmId(c.getInt(0));
                taskItem.setmTitle(c.getString(1));
                taskItem.setmDate(c.getString(2));
                if(c.getInt(3) == 0)
                    taskItem.setmStatus(false);
                else
                    taskItem.setmStatus(true);
                taskItem.setmPriority(c.getInt(4));
                DataSet.add(taskItem);
            }while (c.moveToNext());
        }
        c.close();
        return DataSet;
    }
    private ArrayList<TaskItem> doQueryForUrgent(){
        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        readableDatabase = mDbHelper.getReadableDatabase();
        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=3",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);

        ArrayList DataSet = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                TaskItem taskItem = new TaskItem();
                taskItem.setmId(c.getInt(0));
                taskItem.setmTitle(c.getString(1));
                taskItem.setmDate(c.getString(2));
                if(c.getInt(3) == 0)
                    taskItem.setmStatus(false);
                else
                    taskItem.setmStatus(true);
                taskItem.setmPriority(c.getInt(4));
                DataSet.add(taskItem);
            }while (c.moveToNext());
        }
        c.close();
        return DataSet;
    }
    private ArrayList<TaskItem> doQueryForNotImportantNotUrgant(){
        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        readableDatabase = mDbHelper.getReadableDatabase();
        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0" + " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY + "=4",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE);

        ArrayList DataSet = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                TaskItem taskItem = new TaskItem();
                taskItem.setmId(c.getInt(0));
                taskItem.setmTitle(c.getString(1));
                taskItem.setmDate(c.getString(2));
                if(c.getInt(3) == 0)
                    taskItem.setmStatus(false);
                else
                    taskItem.setmStatus(true);
                taskItem.setmPriority(c.getInt(4));
                DataSet.add(taskItem);
            }while (c.moveToNext());
        }
        c.close();
        return DataSet;
    }
    private ArrayList<TaskItem> doQueryForToday(){
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
        if(c.moveToFirst()){
            do{
                TaskItem taskItem = new TaskItem();
                taskItem.setmId(c.getInt(0));
                taskItem.setmTitle(c.getString(1));
                taskItem.setmDate(c.getString(2));
                if(c.getInt(3) == 0)
                    taskItem.setmStatus(false);
                else
                    taskItem.setmStatus(true);
                taskItem.setmPriority(c.getInt(4));
                DataSet.add(taskItem);
            }while (c.moveToNext());
        }
        c.close();
        return DataSet;
    }
    private ArrayList<TaskItem> doQueryForTomorrow(){
        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        readableDatabase = mDbHelper.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String formatted = format.format(calendar.getTime());
        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = '" + formatted + "' AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + "=0",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);

        ArrayList DataSet = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                TaskItem taskItem = new TaskItem();
                taskItem.setmId(c.getInt(0));
                taskItem.setmTitle(c.getString(1));
                taskItem.setmDate(c.getString(2));
                if(c.getInt(3) == 0)
                    taskItem.setmStatus(false);
                else
                    taskItem.setmStatus(true);
                taskItem.setmPriority(c.getInt(4));
                DataSet.add(taskItem);
            }while (c.moveToNext());
        }
        c.close();
        return DataSet;
    }

    private void doQueryForDone(TaskItem item){
        mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        readableDatabase = mDbHelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, 1);
        readableDatabase.update(FeedReaderContract.FeedEntry.TABLE_NAME, contentValues, FeedReaderContract.FeedEntry._ID + "=" + item.mId, null);
    }

    private void updateAdapter(){
        ArrayList<TaskItem> newDataset;
        switch (CURRENT_FILTER_STATE){
            case ALL_TASKS_FILTER_STATE:
                newDataset = doQueryForAll();
                break;
            case IMPORTANT_TASKS_FILTER_STATE:
                newDataset = doQueryForImportant();
                break;
            case URGENT_TASKS_FILTER_STATE:
                newDataset = doQueryForUrgent();
                    break;
            case NUNI_TASKS_FILTER_STATE:
                newDataset = doQueryForNotImportantNotUrgant();
                break;
            case TODAY_TASKS_FILTER_STATE:
                newDataset = doQueryForToday();
                break;
            case TOMORROW_TASKS_FILTER_STATE:
                newDataset = doQueryForTomorrow();
                break;
            default:
                newDataset = doQueryForAll();
                break;
        }
        mDataSet = newDataset;
        mAdapter.setNewDate(mDataSet);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
            // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            updateAdapter();
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
        if (id == R.id.nav_iu) {
            CURRENT_FILTER_STATE = ALL_TASKS_FILTER_STATE;
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
        }else if (id == R.id.nav_ninu) {
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
}
