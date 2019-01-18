package com.example.controlapp;

import android.content.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

/* H klash auth afora thn ylopoihsh ths kyrias "othonis",
* dhladh ths formas eisodou kai sundeshs ston MQTT Broker. */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        registerReceiver(ConnectionReceiver,new IntentFilter("GETCONNECTION"));
    }

    /* Called when the user taps the LOGIN button */
    public void sendCredentials(View view)
    {
        SharedPreferences prefs = getSharedPreferences("MQTT_prefs",MODE_PRIVATE);
        if(NetworkThread.checkConnectivity(getApplicationContext()))
        {
            if (prefs.contains("SERVER") && prefs.contains("PORT") && prefs.contains("USERNAME")
            && prefs.contains("PASSWORD") && prefs.contains("TOPIC") && prefs.contains("FREQUENCY")
            && prefs.contains("QOS"))
            {
                Intent intent = new Intent(this, MessagingService.class);
                Bundle credentials = new Bundle();

                credentials.putString("SERVER", prefs.getString("SERVER", ""));
                credentials.putString("PORT", prefs.getString("PORT", ""));
                credentials.putString("USERNAME", prefs.getString("USERNAME", ""));
                credentials.putString("PASSWORD", prefs.getString("PASSWORD", ""));
                credentials.putString("TOPIC", prefs.getString("TOPIC", ""));
                credentials.putString("FREQUENCY", prefs.getString("FREQUENCY", ""));
                credentials.putString("QOS", prefs.getString("QOS", ""));

                intent.putExtras(credentials);
                startService(intent);
            }
            else Toast.makeText(getApplicationContext(), "Insert Connection Settings First!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Check Internet Connection!", Toast.LENGTH_SHORT).show();
            NetworkThread.showNotification(getApplicationContext());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options,menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //if user pressed "yes", then he is allowed to exit from application
                dialog.dismiss();
                onFinish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.app_settings:
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.option_exit:
                onFinish();

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void onFinish() {
        unregisterReceiver(ConnectionReceiver);
        stopService(new Intent(getBaseContext(),MessagingService.class));
        finish();
    }

    private BroadcastReceiver ConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("CONNECTION"); //to string message periexei to status sundeshs
            if (message.equals("CONNECTED")) {
                //an einai true, phgaine me sto displaymessageactivity
                Intent display = new Intent(getApplicationContext(),DisplayMessageActivity.class);
                startActivity(display);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sendSetting("CHECKCONNECTION"); //send to service
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        sendSetting("CHECKCONNECTION"); //send to service
    }

    /* Voithitikes Synarthseis */

    private void sendSetting(String msg) {
        //apostolh rythmisewn sto MessagingService
        Intent message = new Intent("SETSETTINGS");
        message.putExtra("SETTINGS",msg);
        sendBroadcast(message);
    }
}