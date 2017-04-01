package com.sergey_suslov.tasker;

import android.app.Notification;
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
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;


public class BriefingService extends Service {

    private Bitmap mLargeIcon;
    private Calendar mCalendar;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
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
            Log.d("LoopeThred", "");
            currentTimeInMins = mCalendar.get(Calendar.HOUR_OF_DAY) * 60 + mCalendar.get(Calendar.MINUTE);
            briefTime = mSharedPreferences.getInt(getString(R.string.preference_saved_briefing_time),
                                Integer.valueOf(getString(R.string.preference_saved_briefing_time_default)));
            lastTimeShown = mSharedPreferences.getInt(getString(R.string.briefing_last_time_shown_date), -1);
            currentDayOfYear = mCalendar.get(Calendar.DAY_OF_YEAR);
            Log.d("LoopeThred", String.valueOf(currentTimeInMins >= briefTime) +
                    String.valueOf(Math.abs(lastTimeShown - currentDayOfYear) != 0));
            Log.d("LoopeThred", String.valueOf(lastTimeShown) + " " +
                    String.valueOf(currentDayOfYear));
            Log.d("LoopeThred", String.valueOf(currentTimeInMins) + " " +
                    String.valueOf(briefTime));
            if(currentTimeInMins >= briefTime && Math.abs(lastTimeShown - currentDayOfYear) != 0){
                sendNotification();
                updateShownDate();
            }
            try {
                TimeUnit.SECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification(){
//TODO: add the number of overdue tasks
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase readableDatabase = mDbHelper.getReadableDatabase();
        Cursor cAll;
        Cursor cToday;
        cAll = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = 0",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
        int numberOfAll = cAll.getCount();
        String formatted = format.format(new Date());
        cToday = readableDatabase.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = '" + formatted + "' AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + "=0",
                null,
                null,
                null,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PRIORITY);
        int numberOfToday = cToday.getCount();
        LinkedList<String> bigMessage = new LinkedList<>();
        if (cToday.moveToFirst()){
            boolean b = true;
            Log.d("LoopeThred", numberOfToday + "");
            do{
                Log.d("LoopeThred", cToday.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE));
                String taskTitle = cToday.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE).length() > 50 ?
                        cToday.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE).substring(0, 40) + "...\n":
                        cToday.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE) + "\n";
                bigMessage.add(taskTitle);
            }while (cToday.moveToNext());
        }else
            if (cAll.moveToFirst()){
                boolean b = true;
                Log.d("LoopeThred", numberOfToday + "");
                do{
                    String taskTitle = cAll.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE).length() > 50 ?
                            cAll.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE).substring(0, 40) + "...\n":
                            cAll.getString(FeedReaderContract.FeedEntry.COLUMN_NUMBER_TITLE) + "\n";
                    bigMessage.add(taskTitle);
                }while (cAll.moveToNext());
            }else {
                bigMessage.add(getString(R.string.no_tasks_notification_text));
            }

        String mainNotifText = getString(R.string.notification_compressed_title_text);
        String subNotifText = "Today: " + numberOfToday + "  All: " + numberOfAll;

        Intent resultIntent = new Intent(this, TasksActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TasksActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .setBigContentTitle(getString(R.string.notification_expanded_title_text))
                .setSummaryText(subNotifText);
        ListIterator<String> itr = bigMessage.listIterator();
        while (itr.hasNext()){
            inboxStyle.addLine(itr.next());
        }
        mNotification = new Notification.Builder(getApplicationContext())
                .setContentTitle(mainNotifText)
                .setContentText(subNotifText)
                .setContentIntent(resultPendingIntent)
                .setDefaults(DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_checked_done)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_list_notif))
                .setStyle(inboxStyle)
                .build();

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mNotification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
