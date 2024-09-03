package com.arcsoft.arcfacedemo.common;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 一些常量设置
 * <p>
 * 双目偏移可在 [参数设置] -> [识别界面适配] 界面中进行设置
 */
public class Constants {

    /**
     * 方式一： 填好APP_ID等参数，进入激活界面激活
     */
    public static final String APP_ID = "GUhYjMeFLiFARWet58gTGotUpr189H6Ch7QFSSMgMUCZ";
    public static final String SDK_KEY = "4LxEGC47QhYUPdXZxemhKQSEiFh6p9rYUSitfEcAAFFF";
    public static final String ACTIVE_KEY = "ACTIVE_KEY";

    /**
     * 方式二： 在激活界面读取本地配置文件进行激活
     *
     * 配置文件名称，格式如下：
     * APP_ID:XXXXXXXXXXXXX
     * SDK_KEY:XXXXXXXXXXXXXXX
     * ACTIVE_KEY:XXXX-XXXX-XXXX-XXXX
     */
    public static final String ACTIVE_CONFIG_FILE_NAME = "activeConfig.txt";


    /**
     * 注册图所在路径
     */
    public static final String DEFAULT_REGISTER_FACES_DIR = "sdcard/arcfacedemo/register";


    public static boolean activeEngineOffline(Context context){
        boolean result=false;
        String path1=Environment.getExternalStorageDirectory() + "/ArcFacePro32.dat";
        String path2=context.getApplicationContext().getFilesDir() + "/ArcFacePro32.dat";

        String path = Environment.getExternalStorageDirectory().toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }

        File file2=new File(path2);
        if (file2.exists()&& file2.length() > 100){
            result=true;
        }else {

            File file1=new File(path1);
            if (file1.exists()&& file1.length() > 100){
                copyFile(path1,path2);
                if (file2.exists()&& file2.length() > 100){
                    result=true;
                }
            }else {
                Log.e("active_result","false  no .dat file");
            }

        }



        return result;
    }

    public static boolean copyFile(String filePath, String destPath) {
        File originFile = new File(filePath);

        if (!originFile.exists()) {
            Log.e("yw_lisence","lisence not exist");
            return false;
        }
        File destFile = new File(destPath);
        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            reader = new BufferedInputStream(new FileInputStream(originFile));
            writer = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, length);
            }
        } catch (Exception exception) {
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }

    // 获取sn号
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 获取SN号
     *
     * @return
     */
    public static String getSNCode() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 28) {
            return Build.SERIAL;
        } else {
            return getSerialNumber();
        }
    }

}
