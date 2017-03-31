package com.sergey_suslov.tasker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;


public class BriefingService extends Service {

    private Bitmap mLargeIcon;
    private Calendar mCalendar;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private ThreadPoolExecutor threadPoolExecutor;
    private SharedPreferences mSharedPreferences;
    public Integer mId;



    public BriefingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Brief", "onStartCommand");
        mLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_list);
        mId = 1;
        mSharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        startLoop();
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateShownDate(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(getString(R.string.briefing_last_time_shown_date),
                mCalendar.get(Calendar.DAY_OF_YEAR));
        editor.commit();
    }

    private void startLoop(){
        mCalendar = Calendar.getInstance();
        Integer currentTimeInMins;
        Integer briefTime;
        Integer lastTimeShown;
        Integer currentDayOfYear;
        long curT = System.currentTimeMillis();
        long endT = System.currentTimeMillis() + 20000;
        while (curT < endT){
            currentTimeInMins = mCalendar.get(Calendar.HOUR_OF_DAY) * 60 + mCalendar.get(Calendar.MINUTE);
            briefTime = mSharedPreferences.getInt(getString(R.string.preference_saved_briefing_time),
                                Integer.valueOf(getString(R.string.preference_saved_briefing_time_default)));
            lastTimeShown = mSharedPreferences.getInt(getString(R.string.briefing_last_time_shown_date), -1);
            currentDayOfYear = mCalendar.get(Calendar.DAY_OF_YEAR);
            Log.d("MainIf", String.valueOf(currentTimeInMins >= briefTime) +
                    String.valueOf(Math.abs(lastTimeShown - currentDayOfYear) != 0));
            Log.d("MainIf", String.valueOf(lastTimeShown) + " " +
                    String.valueOf(currentDayOfYear));
            if(currentTimeInMins >= briefTime && Math.abs(lastTimeShown - currentDayOfYear) != 0){
                sendNotification();
                updateShownDate();
            }
            try {
                TimeUnit.SECONDS.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification(){

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase readableDatabase = mDbHelper.getReadableDatabase();
        Cursor c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0",
                null,
                null,
                null,
                null);
        int numberOfAll = c.getCount();
        String formatted = format.format(new Date());
        c = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = '" + formatted + "' AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + "=0",
                null,
                null,
                null,
                null);
        int numberOfToday = c.getCount();

        String mainNotifText = "Good day!";
        String subNotifText = "I'va got no tasks, yet";
        if (numberOfAll != 0 || numberOfToday != 0){
            subNotifText = "Today: " + numberOfToday + "  All: " + numberOfAll;
        }

        long[] pattern = {0, 400, 200, 400};
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_checked_done)
                        .setContentTitle(mainNotifText)
                        .setContentText(subNotifText)
                        .setAutoCancel(true)
                        .setVibrate(pattern)
                        .setDefaults(DEFAULT_ALL)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_list_notif))
                        .setTicker(getString(R.string.notification_ticket));
        Intent resultIntent = new Intent(this, TasksActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(TasksActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
