package com.staticom.friend.questionbank;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import javax.xml.transform.OutputKeys;

public class MainActivity extends AppCompatActivity {

    private static Intent service = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int READ_WRITE_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (READ_WRITE_permission != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "권한을 부여해 주세요. 권한이 없으면 앱은 종료됩니다. (문제은행 앱의 잠금 해제를 위해 필요한 권한입니다.)", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
            case 2:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                break;

            default:
                break;
        }
    }

    public void end(View view) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/영구해제.txt");

            if (file.exists()) {
                FileReader reader = new FileReader(file);

                char[] buf = new char[20];
                reader.read(buf);

                String str = new String(buf);

                if (str.contains("off")) {
                    reader.close();
                    file.delete();

                    if (service != null) {
                        stopService(service);
                    }

                    android.os.Process.killProcess(android.os.Process.myPid());
                    return;
                }
            }

            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/작동중.txt");

            if(!file.exists()) {
                android.os.Process.killProcess(android.os.Process.myPid());
                return;
            }
        } catch (Exception e) {

        } finally {
            Toast.makeText(getApplicationContext(), "종료에 실패했습니다. (오류가 발생했거나, 학부모 권한 없이 문제은행 앱이 종료되었습니다.)", Toast.LENGTH_SHORT).show();
        }
    }

    public void shell(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean granted = false;

            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), getPackageName());

            if (mode == AppOpsManager.MODE_DEFAULT) {
                granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
            } else {
                granted = (mode == AppOpsManager.MODE_ALLOWED);
            }

            if (!granted)
            {
                Toast.makeText(getApplicationContext(), "QuestionBank에 권한을 부여해 주세요. 그 후 버튼을 다시 눌러주세요.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
                return;
            }

            int READ_WRITE_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (!granted || READ_WRITE_permission != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "필요한 권한이 없어 문제은행을 실행할 수 없습니다. 이 앱을 껐다가 다시 켜주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/작동중.txt");
            file.createNewFile();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 이 앱을 껐다가 다시 켜신 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        service = new Intent(this, QuestionBank.class);

        startService(service);
        moveTaskToBack(true);
        finish();
    }
}
