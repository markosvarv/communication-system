package com.example.controlapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/* H klash SettingsActivity, ylopoiei to skelos ths diepafhs tou xrhsth
 * me tis parametrous sundeshs me ton MQTT broker kai ton orismo autwn. */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        android.support.v7.widget.Toolbar returnToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.return_toolbar);
        setSupportActionBar(returnToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        /*Profortwsh twn ruthmisewn, an yfistantai apo proigoumeno session leitourgias.*/
        SharedPreferences prefs = getSharedPreferences("MQTT_prefs",MODE_PRIVATE);
        if(prefs.contains("SERVER") && prefs.contains("PORT") && prefs.contains("USERNAME")
        && prefs.contains("PASSWORD") && prefs.contains("TOPIC") && prefs.contains("FREQUENCY")
        && prefs.contains("QOS"))
        {
            EditText editText = (EditText) findViewById(R.id.Server);
            editText.setText(prefs.getString("SERVER", ""));

            editText = findViewById(R.id.Port);
            editText.setText(prefs.getString("PORT", ""));

            editText = findViewById(R.id.username);
            editText.setText(prefs.getString("USERNAME", ""));

            editText = findViewById(R.id.password);
            editText.setText(prefs.getString("PASSWORD", ""));

            editText = findViewById(R.id.topic);
            editText.setText(prefs.getString("TOPIC", ""));

            editText = findViewById(R.id.receivedFrequency);
            editText.setText(prefs.getString("FREQUENCY", ""));

            RadioGroup QOSGroup = (RadioGroup) findViewById(R.id.radioGroup);
            int QOS = Integer.parseInt(prefs.getString("QOS", ""));
            if(QOS == 0) QOSGroup.check(R.id.radio0);
            else if(QOS == 1) QOSGroup.check(R.id.radio1);
            else QOSGroup.check(R.id.radio2);
        }
    }

    /* Called when the user taps the SAVE button. */
    public void saveCredentials(View view)
    {
        //Apothikeush twn rythmisewn se SharedPreferences arxeio
        RadioGroup QOSGroup; RadioButton QOSButton; boolean valuesFilled = true;
        SharedPreferences prefs = getSharedPreferences("MQTT_prefs", MODE_PRIVATE);
        SharedPreferences.Editor credentials = prefs.edit();

        EditText editText = (EditText) findViewById(R.id.Server);
        if(TextUtils.isEmpty(editText.getText())) valuesFilled = false;
        else credentials.putString("SERVER", editText.getText().toString());

        editText = findViewById(R.id.Port);
        if(TextUtils.isEmpty(editText.getText())) valuesFilled = false;
        else credentials.putString("PORT", editText.getText().toString());

        editText = findViewById(R.id.username);
        credentials.putString("USERNAME", editText.getText().toString());

        editText = findViewById(R.id.password);
        credentials.putString("PASSWORD", editText.getText().toString());

        editText = findViewById(R.id.topic);
        if(TextUtils.isEmpty(editText.getText())) valuesFilled = false;
        else credentials.putString("TOPIC", editText.getText().toString());

        editText = findViewById(R.id.receivedFrequency);
        if(TextUtils.isEmpty(editText.getText())) valuesFilled = false;
        else credentials.putString("FREQUENCY", editText.getText().toString());

        QOSGroup = (RadioGroup) findViewById(R.id.radioGroup);
        QOSButton = (RadioButton) findViewById(QOSGroup.getCheckedRadioButtonId());
        credentials.putString("QOS", QOSButton.getText().toString());

        if(valuesFilled) //einai aparaithto o xrhsths na symplirwsei ola ta pedia, gia na ginei h apothikeush
        {
            credentials.commit();
            Toast.makeText(getApplicationContext(),"Settings Saved",Toast.LENGTH_SHORT).show();
            finish();
        }
        else Toast.makeText(getApplicationContext(),"Please fill out all required fields!",Toast.LENGTH_SHORT).show();
    }

    /* Diagrafh twn paliwn rythmisewn, mesw ths diagrafhs tou SharedPreference arxeiou. */
    public void deleteSettings(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you confirm deleting old settings?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //Ekkatharish twn paliwn rythmisewn apo ta ekastote pedia!
                SharedPreferences prefs = getSharedPreferences("MQTT_prefs",MODE_PRIVATE);
                if(prefs.contains("SERVER") && prefs.contains("PORT") && prefs.contains("USERNAME")
                && prefs.contains("PASSWORD") && prefs.contains("TOPIC") && prefs.contains("FREQUENCY")
                && prefs.contains("QOS"))
                {
                    SharedPreferences.Editor credentials = prefs.edit();
                    credentials.clear();
                    credentials.commit();
                    dialog.dismiss();

                    EditText editText = (EditText) findViewById(R.id.Server);
                    editText.getText().clear();
                    editText = findViewById(R.id.Port);
                    editText.getText().clear();
                    editText = findViewById(R.id.username);
                    editText.getText().clear();
                    editText = findViewById(R.id.password);
                    editText.getText().clear();
                    editText = findViewById(R.id.topic);
                    editText.getText().clear();
                    editText = findViewById(R.id.receivedFrequency);
                    editText.getText().clear();
                    RadioGroup QOSGroup = (RadioGroup) findViewById(R.id.radioGroup);
                    QOSGroup.check(R.id.radio0);

                    Toast.makeText(getApplicationContext(), "Old Settings Deleted", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(getApplicationContext(), "No Old Settings!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert=builder.create(); //alert epivevaiwshs oti o xrhsths epithymei thn diagrafh
        alert.show();
    }
}