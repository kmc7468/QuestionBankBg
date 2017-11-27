package com.staticom.friend.questionbank;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class QuestionBank extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        toast = new Handler();

        Intent questionBankOrg = getPackageManager().getLaunchIntentForPackage("appinventor.ai_youngjaewon2017.Question_Bank_A_");
        startActivity(questionBankOrg);

        thread = new Thread(task);
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        thread.stop();
        super.onDestroy();
    }

    private class ToastRunnable implements Runnable {
        private String message;

        public ToastRunnable(String text) {
            message = text;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private Handler toast;
    private Context c = this;
    private Thread thread = null;
    private Runnable task = new Runnable() {

        private static final String pack = "appinventor.ai_youngjaewon2017.Question_Bank_A_";
        private char[] buf = new char[20];
        private long i = 0;

        private File f_off = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/잠금해제.txt");
        private File f_real_off = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/영구해제.txt");

        @Override
        public void run() {
            boolean _run = true;

            while (_run) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
                    long endTime = System.currentTimeMillis();
                    long beginTime = endTime - 1000*1000;

                    String running_pack = null;
                    List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
                    String topActivity = null;

                    if (stats != null)
                    {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                        for (UsageStats usageStats : stats)
                        {
                            mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
                        }

                        if (!mySortedMap.isEmpty())
                        {
                            topActivity =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        }
                    }

                    if(topActivity != null && !topActivity.equals(pack)) {
                        Intent questionBankOrg = getPackageManager().getLaunchIntentForPackage("appinventor.ai_youngjaewon2017.Question_Bank_A_");
                        startActivity(questionBankOrg);
                    }
                } else {
                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

                    List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

                    for (ActivityManager.RunningAppProcessInfo process : processes) {
                        if (process == null) continue;

                        if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            if (!pack.equals(process.processName)) {
                                Intent questionBankOrg = getPackageManager().getLaunchIntentForPackage("appinventor.ai_youngjaewon2017.Question_Bank_A_");
                                startActivity(questionBankOrg);
                            }
                            break;
                        }
                    }
                }

                boolean stop_thread = false;

                try {
                    ++i;

                    if (f_off.exists()) {
                        FileReader reader = new FileReader(f_off);
                        reader.read(buf);

                        String str_tmp = new String(buf);

                        if (str_tmp.contains("off")) {
                            stop_thread = true;

                            reader.close();
                            f_off.delete();
                        } else {
                            reader.close();
                        }
                    }

                    if (f_real_off.exists()) {
                        Arrays.fill(buf, '\0');

                        FileReader reader = new FileReader(f_real_off);
                        reader.read(buf);

                        String str_tmp = new String(buf);
                        if (str_tmp.contains("off")) {
                            _run = false;
                        } else {
                            Arrays.fill(buf, '\0');
                        }

                        reader.close();
                    }

                    if (i == Long.MAX_VALUE) {
                        i = 0;
                    }

                    if (stop_thread) {
                        stop_thread = false;
                        Thread.sleep(1800000);
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {

                }
            }

            File new_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/작동중.txt");
            new_file.delete();

            Intent intent = new Intent(c, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
}
