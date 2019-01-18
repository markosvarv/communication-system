package com.example.controlapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*  H klash auth, afora thn emfanish twn entolwn pou lamvanontai apo ton Broker,
 * kathws kai twn diaforwn koumpiwn-settings pou apaitountai. */

public class DisplayMessageActivity extends AppCompatActivity
{
    private boolean doubleBackToExitPressedOnce = false; //gia na ylopoithei to double back button gia epistrofh
    private TextView receivedMessages;
    private boolean soundSelected = false, flashSelected = false;
    /* Ta parapanw boolean lamvanoun true timh, an o hxos kai to flash
    antistoixa leitourgoun, kai false, se antitheth periptwsh. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Toolbar returnToolbar = (Toolbar) findViewById(R.id.return_toolbar);
        setSupportActionBar(returnToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        receivedMessages = findViewById(R.id.receivedMessages);
        receivedMessages.setText("Waiting Command");

        registerReceiver(CommandReceiver,new IntentFilter("GETDATA"));
    }

    private BroadcastReceiver CommandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("DATA");
            receivedMessages.setText(message);
            /* sto shmeio auto tha metavallw to state twn buttons, analoga me to lifthen mhnuma */
            switchButtonState(message);
        }
    };

    public void Sound(View view) {
        switchSoundButton(true);
        sendSetting("SOUND");
        TextView warnings = findViewById(R.id.receivedMessages);
        warnings.setText("Sound Switched ON/OFF");
    }

    public void Flash(View view) {
        switchFlashButton(true);
        sendSetting("FLASH");
        TextView warnings = findViewById(R.id.receivedMessages);
        warnings.setText("Flash Switched ON/OFF");
    }

    public void Logout(View view) {
        sendSetting("LOGOUT");
        Toast.makeText(getApplicationContext(),"Disconnected!",Toast.LENGTH_SHORT).show();
        finish(); //Epistrofh sto mainActivity
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            sendSetting("LOGOUT");
            Toast.makeText(this, "Disconnected!", Toast.LENGTH_SHORT).show();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press Back again to return", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                sendSetting("LOGOUT");
                Toast.makeText(getApplicationContext(),"Disconnected!", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(CommandReceiver);
    }

    /* Voithitikes Synarthseis */

    private void sendSetting(String msg) {
        //apostolh rythmisewn sto MessagingService
        Intent message = new Intent("SETSETTINGS");
        message.putExtra("SETTINGS",msg);
        sendBroadcast(message);
    }

    /* H parakatw sunarthsh metavallei thn timh twn booleans
     * Flash kai Sound, analoga me to state leitourgias tous. */
    private void switchButtonState(String msg)
    {
        if(msg.equals(MessagingService.ALL_ON)) { switchFlashButton(!flashSelected); switchSoundButton(!soundSelected); }
        else if(msg.startsWith(MessagingService.SOUND_ON)) switchSoundButton(!soundSelected);
        else if(msg.equals(MessagingService.SOUND_OFF)) switchSoundButton(soundSelected);
        else if(msg.startsWith(MessagingService.FLASH_ON)) switchFlashButton(!flashSelected);
        else if(msg.equals(MessagingService.FLASH_OFF)) switchFlashButton(flashSelected);
        else if(msg.equals(MessagingService.ALL_OFF)) { switchFlashButton(flashSelected); switchSoundButton(soundSelected); }
    }

    /*  Enallagh tou flashSelected boolean kai tou button
    pou emfanizetai sto DisplayMessageActivity. */
    private void switchFlashButton(boolean _switch)
    {
        if(_switch) flashSelected = !flashSelected;
        Button button = ((Button) findViewById(R.id.flash));
        if(flashSelected == true) button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flash_off, 0, 0, 0);
        else button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flash_on, 0, 0, 0);
    }

    /*  Enallagh tou soundSelected boolean kai tou button
    pou emfanizetai sto DisplayMessageActivity. */
    private void switchSoundButton(boolean _switch)
    {
        if(_switch) soundSelected = !soundSelected;
        Button button = ((Button) findViewById(R.id.sound));
        if(soundSelected == true) button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audio_off, 0, 0, 0);
        else button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audio_on, 0, 0, 0);
    }
}