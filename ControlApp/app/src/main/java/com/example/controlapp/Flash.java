package com.example.controlapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import java.util.Timer;
import java.util.TimerTask;

/* H klash auth, periexei oles tis aparaithtes sunarthseis gia ton xeirismo
 * tou flash ths kameras, ths suskeuhs. Oi parakatw methodoi, merimnoun kai
 * gia thn periptwsh opou h suskeuh de diathetei katholou flashlight. */

public class Flash {

    private Context context;
    private CameraManager camManager;
    private String cameraId;
    private boolean flashExists;
    private Timer timer;
    private TimerTask task;

    public Flash(Context context) {
        this.context = context; timer = null; task = null;
        camManager = (CameraManager) this.context.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = camManager.getCameraIdList()[0];
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        CameraCharacteristics chars = null;
        try {
            chars = camManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        flashExists = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
    }

    /* Energopoihsh tou flashlight */
    public void openFlash(int seconds) {
        if(flashExists) {
            try {
                camManager.setTorchMode(cameraId, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            if(seconds >= 0) {
                task = new TimerTask() {
                    @Override
                    public void run() {
                        closeFlash();
                        //apostolh mhnumatos apenergopoihshs sto displaymessageactivity
                        Intent message = new Intent("GETDATA");
                        message.putExtra("DATA",MessagingService.FLASH_OFF);
                        context.sendBroadcast(message);
                        //apostolh mhnumatos apenergopoihshs sto messagingservice
                        message = new Intent("SETSETTINGS");
                        message.putExtra("SETTINGS","FLASH");
                        context.sendBroadcast(message);
                    }
                };
                timer = new Timer();
                timer.schedule(task, 1000 * seconds);
            }
        }
    }

    /* Apenergopoihsh tou flashlight */
    public void closeFlash() {
        stopFlashTimer(); //akyrwsh tou scheduled event gia apenergopoihsh tou flash
        if(flashExists) {
            try {
                camManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /* Reset timer, Akyrwsh tou scheduled event */
    private void stopFlashTimer() {
        if(timer != null && task != null) {
            task.cancel();
            timer.cancel();
        }
    }
}