package com.example.controlapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

/* H klash NetworkThread, periexei methodous gia ton elegxo ths sundesimothtas ths
* efarmoghs me to diadiktyo. Sthn periptwsh opou xathei i sundesh, emfanizetai katallhlo notification.*/

public class NetworkThread extends Thread {

    private Context context;
    private int mInterval; private Handler mHandler;

    NetworkThread(Context appContext, int networkCheckFrequency)
    {
        context = appContext;
        mInterval = networkCheckFrequency*1000; //periodos elegxou
        mHandler = new Handler();
    }

    public void run() { startRepeatingTask(); } //ekkinhsh tou thread

    public void stopRunning() { stopRepeatingTask(); } //termatismos tou thread

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //elegxos sundeshs kai emfanish antistoixhs eidopoihshs
                if(!checkConnectivity(context)) showNotification(context);
            } finally { mHandler.postDelayed(mStatusChecker, mInterval); }
        }
    };

    private void startRepeatingTask() { mStatusChecker.run(); }

    private void stopRepeatingTask() { mHandler.removeCallbacks(mStatusChecker); }

    /* H parakatw sunarthsh, dhmiourgei to periexomeno tou Notification pou
    * eprokeito na emfanistei sth suskeuh, an xathei h sundesh me to diadiktyo. */
    public static void showNotification(Context context)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true) //an pathsoume panw sthn eidopoihsh, auth eksafanizetai
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Internet Connection Lost!")
                .setContentText("Press to reestablish connectivity.");

        //an pathsoume panw sthn eidopoihsh, metavainoume stis rythmiseis suskeuhs kai h eidopoihsh eksafanizetai
        Intent notificationIntent = new Intent(Settings.ACTION_SETTINGS);
        PendingIntent contentIntent = PendingIntent.getActivity(context,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    /* H parakatw sunarthsh elegxei ean h suskeuh einai sundedemenh
     * se WiFi diktyo, h sto diktyo dedomenwn kinhths thlefwnias. */
    public static boolean checkConnectivity(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}