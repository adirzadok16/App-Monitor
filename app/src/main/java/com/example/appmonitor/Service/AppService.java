package com.example.appmonitor.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.provider.Settings;
import android.util.Log;
import android.os.Build.VERSION_CODES;
import android.net.Uri;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;

import com.example.appmonitor.MainActivity;
import com.example.appmonitor.R;
import com.example.appmonitor.Utilities.SharedPreferencesManager;
import com.example.appmonitor.Utilities.appInformation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppService extends Service {
    private static final String TAG = "CHECK!!!!";
    private static final String FOREGROUND_CHANNEL_ID = "ForegroundServiceChannel";
    private static final String ALERTS_CHANNEL_ID = "UsageAlertsChannel";
    private Handler handler;
    private Runnable checkAppRunnable;
    private List<appInformation> appInfoList = new ArrayList<>();
    private int timeUse;
    private int limitTime;
    private String username;
    private DatabaseReference databaseRef;


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        SharedPreferencesManager.init(this);
        username = SharedPreferencesManager.getInstance().getString("username", "");
        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("Apps");
        handler = new Handler();
        checkAppRunnable = new Runnable() {
            @Override
            public void run() {

                if (isSupportedDevice()) {
                    if(appInfoList.isEmpty()){
                        Log.d("SERVICE!!!!","List is empty ");
                    }
                    String foregroundApp = getForegroundApp();
                    Log.d("SERVICE!!!!","The app in the foreground is : " + foregroundApp);

                    for( appInformation app : appInfoList){
                        if(foregroundApp.equals(app.getPackageName())){

                            DatabaseReference  timeUseRef = databaseRef.child(app.getAppName()).child("timeUse");
                            DatabaseReference  limitTimeRef = databaseRef.child(app.getAppName()).child("timeInSec");
                            DatabaseReference  notification75Ref = databaseRef.child(app.getAppName()).child("notification75");
                            DatabaseReference  notification90Ref = databaseRef.child(app.getAppName()).child("notification90");

                            timeUseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        timeUse = dataSnapshot.getValue(Integer.class);
                                        Log.d("Firebase", "Time Use Value: " + timeUse);

                                        // עכשיו נמשוך את limitTime ורק אז נבצע את החישוב
                                        limitTimeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    limitTime = dataSnapshot.getValue(Integer.class);
                                                    Log.d("Firebase", "Limit Time Value: " + limitTime);

                                                    notification75Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            boolean notification75 = dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class);

                                                            notification90Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    boolean notification90 = dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class);

                                                                    int totalTime = timeUse + 1;
                                                                    databaseRef.child(app.getAppName()).child("timeUse").setValue(totalTime);

                                                                    if (totalTime >= 0.75 * limitTime && !notification75) {
                                                                        Log.d("Firebase", "Time Use: " + timeUse + ", Limit Time: " + limitTime);
                                                                        Log.d("Firebase", "Notification 75: " + notification75 + ", Notification 90: " + notification90);
                                                                        databaseRef.child(app.getAppName()).child("notification75").setValue(true);
                                                                        sendNotification(75, app.getAppName());
                                                                    }

                                                                    if (totalTime >= 0.9 * limitTime && !notification90) {
                                                                        Log.d("Firebase", "Time Use: " + timeUse + ", Limit Time: " + limitTime);
                                                                        Log.d("Firebase", "Notification 75: " + notification75 + ", Notification 90: " + notification90);
                                                                        databaseRef.child(app.getAppName()).child("notification90").setValue(true);
                                                                        sendNotification(90, app.getAppName());
                                                                    }

                                                                    // בדיקה האם לעמעם את המסך
                                                                    if (totalTime >= limitTime) {
                                                                        setScreenBrightness(0);
                                                                    } else {
                                                                        setScreenBrightness(255);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                    Log.e("Firebase", "Failed to read notification90.", databaseError.toException());
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            Log.e("Firebase", "Failed to read notification75.", databaseError.toException());
                                                        }
                                                    });
                                                } else {
                                                    Log.d("Firebase", "limitTime does not exist.");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.e("Firebase", "Failed to read limitTime.", databaseError.toException());
                                            }
                                        });

                                    } else {
                                        Log.d("Firebase", "timeUse does not exist.");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("Firebase", "Failed to read timeUse.", databaseError.toException());
                                }
                            });


                        }
                    }
                    Log.d(TAG, "Foreground App: " + foregroundApp);



                    // Retry if no app is detected
                    if (foregroundApp.contains("No App Detected")) {
                        Log.d(TAG, "Retrying to get foreground app...");
                    }
                } else {
                    Log.d(TAG, "Device not supported for brightness adjustment.");
                }

                resetTimeUseAtMidnight();

                // Keep checking every second
                handler.postDelayed(this, 1000);
            }
        };
        fetchAppInfoListAndStartHandler();
        listenForAppInfoChanges();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    "Foreground App Detection",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Service notification channel created.");
            } else {
                Log.e(TAG, "NotificationManager is null. Cannot create service notification channel.");
            }

            NotificationChannel alertsChannel = new NotificationChannel(
                    ALERTS_CHANNEL_ID,
                    "Usage Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertsChannel.setDescription("Used for usage limit alerts");
            alertsChannel.enableVibration(true);
            alertsChannel.setVibrationPattern(new long[]{0, 500, 200, 500});

            if (manager != null) {
                manager.createNotificationChannel(alertsChannel);
                Log.d(TAG, "Alerts notification channel created.");
            } else {
                Log.e(TAG, "NotificationManager is null. Cannot create alerts notification channel.");
            }
        }
    }

    private void sendNotification(int percentage, String appName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("TEST!!!!", "sendNotification called with percentage: " + percentage + " and appName: " + appName);

        if (notificationManager == null) {
            Log.e("TEST!!!!", "NotificationManager is null. Cannot send notification.");
            return;
        }

        // הסר את יצירת הערוץ מכאן - היא כבר נעשית ב-createNotificationChannel()
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);  // הוסף FLAG_IMMUTABLE

        String title = "Usage Alert";
        String message = "You have used " + percentage + "% of your limit time for " + appName + ".";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERTS_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        try {
            int notificationId = (int) System.currentTimeMillis();  // ייצר מזהה ייחודי
            notificationManager.notify(notificationId, builder.build());
            Log.d("TEST!!!!", "Notification sent successfully with ID: " + notificationId);
        } catch (Exception e) {
            Log.e("TEST!!!!", "Failed to send notification.", e);
            e.printStackTrace();  // הוסף מידע נוסף על השגיאה
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this, FOREGROUND_CHANNEL_ID)
                .setContentTitle("Foreground App Service")
                .setContentText("Monitoring foreground app...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(checkAppRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private String getForegroundApp() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 5000, time);

        if (usageStatsList == null || usageStatsList.isEmpty()) {
            Log.d(TAG, "No usage stats available. Ensure usage access is granted.");
            return "No App Detected (Grant Usage Access)";
        }

        UsageStats lastUsedApp = null;
        for (UsageStats usageStats : usageStatsList) {
            if (lastUsedApp == null || usageStats.getLastTimeUsed() > lastUsedApp.getLastTimeUsed()) {
                lastUsedApp = usageStats;
            }
        }

        if (lastUsedApp != null) {
            Log.d(TAG, "Foreground app detected: " + lastUsedApp.getPackageName());
            return lastUsedApp.getPackageName();
        }

        Log.d(TAG, "No app detected in the foreground.");
        return "No App Detected (Grant Usage Access)";
    }


    private void setScreenBrightness(int brightnessValue) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessValue);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set screen brightness", e);
        }
    }

    private boolean isSupportedDevice() {
        return Build.MODEL.equals("SM-A715F") || Build.VERSION.SDK_INT >= VERSION_CODES.Q;
    }

    // Fetch the app information list from Firebase and start the handler
    private void fetchAppInfoListAndStartHandler() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                appInfoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("LIST CREATION!", "Child key: " + snapshot.getKey());
                    appInformation app = snapshot.getValue(appInformation.class);


                    if (app != null) {
                        Log.d("LIST CREATION!", "package Name: " + app.getPackageName());
                        appInfoList.add(app);
                    }
                }
                // Start the handler after the list is populated
                handler.post(checkAppRunnable);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch app info list.", databaseError.toException());
            }
        });
    }

    // Listen for changes in the app information list from Firebase
    private void listenForAppInfoChanges() {
        databaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                appInformation app = dataSnapshot.getValue(appInformation.class);
                if (app != null) {
                    appInfoList.add(app);
                    Log.d(TAG, "App added: " + app.getAppName());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                appInformation updatedApp = dataSnapshot.getValue(appInformation.class);
                if (updatedApp != null) {
                    for (int i = 0; i < appInfoList.size(); i++) {
                        if (appInfoList.get(i).getAppName().equals(updatedApp.getAppName())) {
                            appInfoList.set(i, updatedApp);
                            Log.d(TAG, "App updated: " + updatedApp.getAppName());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                appInformation removedApp = dataSnapshot.getValue(appInformation.class);
                if (removedApp != null) {
                    appInfoList.removeIf(app -> app.getAppName().equals(removedApp.getAppName()));
                    Log.d(TAG, "App removed: " + removedApp.getAppName());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Not needed for this use case
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to listen for app info changes.", databaseError.toException());
            }
        });
    }

    private void resetTimeUseAtMidnight() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (hour == 23 && minute == 59) {
            for (appInformation app : appInfoList) {
                databaseRef.child(app.getAppName()).child("timeUse").setValue(0);
                databaseRef.child(app.getAppName()).child("notification75").setValue(false);
                databaseRef.child(app.getAppName()).child("notification90").setValue(false);
            }
            Log.d(TAG, "Reset timeUse for all apps at midnight.");
        }
    }


}
