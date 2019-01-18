package com.example.controlapp;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.Timer;
import java.util.TimerTask;

/* H klash auth xeirizetai to kommati hardware ths
suskeuhs, pou afora thn hxhtikh eidopoihsh. */

public class Sound {

    private Context context;
    private Ringtone Sound;
    private Timer timer;
    private TimerTask task;

    public Sound(Context context) {
        this.context = context; timer = null; task = null;
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Sound = RingtoneManager.getRingtone(this.context,notification);
    }

    /* Apenergopoihsh hxhtikhs eidopoihshs kai akyrwsh
    tou scheduled event gia mellontikh apenergopoihsh tou. */
    public void stopSound() { stopSoundTimer(); Sound.stop(); }

    public void playSound(int seconds) {
        Sound.play();
        if(seconds >= 0) {
            task = new TimerTask() {
                @Override
                public void run() {
                    stopSound();
                    //apostolh mhnumatos apenergopoihshs sto displaymessageactivity
                    Intent message = new Intent("GETDATA");
                    message.putExtra("DATA",MessagingService.SOUND_OFF);
                    context.sendBroadcast(message);
                    //apostolh mhnumatos apenergopoihshs sto messagingservice
                    message = new Intent("SETSETTINGS");
                    message.putExtra("SETTINGS","SOUND");
                    context.sendBroadcast(message);
                }
            };
            timer = new Timer();
            timer.schedule(task, 1000 * seconds);
        }
    }

    /* Akyrwsh tou scheduled event, Epanafora timer */
    private void stopSoundTimer() {
        if(timer != null && task != null) {
            task.cancel();
            timer.cancel();
        }
    }
}