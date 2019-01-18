package com.example.controlapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* H klash Messaging Service ylopoiiei to service pou kanei connect kai subscribe sto antistoixo
* requested topic. O logos pou epilexthike service gia th leitourgia auth, einai h diathrhsh twn
* dunatothtwn ths efarmoghs enw auth vrisketai sto paraskhnio.*/

public class MessagingService extends Service {

    /* Statheres */
    private final static String ClientID = "AndroidClient";
    private  final static int MIN_DURATION = 1, MAX_DURATION = 60, DEFAULT_DURATION = 5;
    /* Oi statheres DURATION aforoun th megisth, thn elaxisth kai thn
    proepilegmenh diarkeia optikis h hxhtikhs eidopoihshs antistoixa. */
    private  final static int NETWORK_CHECK_FREQUENCY = 10; //Syxnothta elegxou uparkshs sundeshs sto diadiktyo
    public final static String FLASH_ON = "Turn on flashlight for ", SOUND_ON = "Play sound for ",
    UNKNOWN = "Unknown Command", FLASH_OFF = "Flashlight Closed!", SOUND_OFF = "Sound Stopped!",
    ALL_ON = "Turn on Flashlight and Sound for " + DEFAULT_DURATION + " seconds!",
    ALL_OFF = "Flash and Sound have been switched off!", FREQUENCY = "Frequency Confirmation";
    /* Ta parapanw strings apostellontai sto displaymessageactivity, kai emfanizontai
     * sthn othonh ths efarmoghs kata thn ektelesh twn lifthentwn entolwn! */

    private MQTT Handler; private Sound sound; private Flash flash;
    private NetworkThread networkCheck; private int receivingFrequency; //suxnothta apostolhs munhmatwn apo to broker

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        /* Arxikopoiish xeiristwn twn diaforwn voithitikwn klasewn */
        sound = null; flash = null; Handler = null; receivingFrequency = 0;
        registerReceiver(SettingsReceiver,new IntentFilter("SETSETTINGS"));

        networkCheck = new NetworkThread(getApplicationContext(),NETWORK_CHECK_FREQUENCY);
        networkCheck.start(); //ekkinhsh thread elegxou uparkshs sundeshs sto internet
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle credentials = intent.getExtras(); /* lipsi twn login credentials apo to SettingsActivity */
        receivingFrequency = Integer.parseInt(credentials.getString("FREQUENCY")); /* lipsi suxnothtas apo to SettingsActivity  */
        MQTTClient(credentials.getString("SERVER"),credentials.getString("PORT"),ClientID,credentials.getString("USERNAME"),
                credentials.getString("PASSWORD"),credentials.getString("TOPIC"),credentials.getString("QOS"));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(SettingsReceiver); //kleisimo tou BroadcastReceiver
        logout(); //aposundesh apo ton MQTT Broker
        networkCheck.stopRunning(); //apenergopoihsh tou thread elegxou sundesimothtas
        Toast.makeText(getApplicationContext(),"MQTT Client Shutdown",Toast.LENGTH_LONG).show();
    }

    /* O BroadcastReceiver, einai anagkaios prokeimenou na lamvanei to service entoles gia th
     * diaxeirish tou flash, tou hxou kai ths sundeshs se opoiadhpote anagkaia stigmi. */
    private BroadcastReceiver SettingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("SETTINGS");
            if(message.equals("SOUND")) switchSound(-1); //koumpi energopoishs/apenergopoihshs hxou
            else if(message.equals("FLASH")) switchFlash(-1); //koumpi energopoihshs/apenergopoihshs flash
            else if(message.equals("LOGOUT")) logout(); //koumpi logout/aposundeshs apo ton broker
            else if(message.equals("CHECKCONNECTION")) sendConnectionStatus(checkStatus());
            //elegxos an einai sundedemeno to service ston broker
        }
    };

    /* H parakatw sunarthsh, einai ypeuthini gia th sundesh tou service me to MQTT broker. */
    private void MQTTClient(String Server, String Port, String ClientID,
                            String Username, String Password, String Topic, String QOS){

        Handler = new MQTT(getApplicationContext(),Server,Port,ClientID,Username,Password,Topic,QOS);
        Handler.connect();
        Handler.setCallback(new MqttCallbackExtended() {

            /* Parakatw, kaleitai me intent to activity pou emfanizei tis entoles pou lifthikan
             * apo to topic. Epilexthike auth h sunarthsh, prokeimenou to DisplayMessageActivity, na
             * "anoigei" mono otan exoume epityxws syndethei me ton broker. Se periptwsh malista
             * epityxous syndeshs, emfanizetai kai antistoixo mhnuma. Antitheta, an h sundesh den
             * epitygxanetai, tote aplws den kaleitai pote, to antistoixo activity. Dystyxws, de
             * vrika dunatothta mesw twn libraries tou MQTT, na enimerwnw to xrhsth gia tin aitia tis
             * anepityxous sundeshs, gi auto kai den emfanizetai kapoio mhnuma sthn periptwsh ths.*/
            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(getApplicationContext(),"Connection Successful!", Toast.LENGTH_SHORT).show();
                frequencyConfirm(null); //Steile frequency ston Java client
                Intent intent = new Intent(getApplicationContext(),DisplayMessageActivity.class);
                startActivity(intent); //ekkinhsh tou activity emfanishs entolwn!
            }

            /* Sthn periptwsh opou xathei h sundesh, emfanizetai katallhlo mhnuma kai to service
            * aposyndeetai apo ton broker. Sunepws prepei na metavoume sto MainActivity kai na
            * epanasyndethoume. */
            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Connection Lost!", Toast.LENGTH_SHORT).show();
                logout(); //aposundesh apo ton MQTT broker
            }

            /* Otan lifthei mhnuma sto service, apo ton MQTT Broker, auto ermhneuetai kai energopoieitai
             * h antistoixh optiki h hxhtikh eidopoiish. Parallhla, me intent, enhmerwnetai katallhla,
             * kai to DisplayMessageActivity. */
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String[] Command = mqttMessage.toString().split(" ");
                Intent message = new Intent("GETDATA");
                parseCommand(Command,message); //ermhneia ths liftheisas entolhs
                sendBroadcast(message); //apostolh entolhs sto DisplayMessageActivity
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    /* Voithitikes Sunarthseis */

    /* H parakatw sunarthsh, diavazei to mhnuma pou elave to service apo ton broker
     * kai pragmatopoiei thn antistoixh energeia. To egkyro mhnuma einai ths morfhs,
     * ENTOLH XX, opou XX einai pedio proairetiko, kai afora th diarkeia anaparagwghs
     * ths eidopoihshs, h ENTOLH H/W, opou h/w afora to flash, ton hxo h kai ta dyo.*/
    private void parseCommand(String[] Command, Intent Preview)
    {
        if ((Command[0].equals("Sound") || Command[0].equals("Flash") || Command[0].equals("Start")) && (Command.length == 1 || Command.length == 2)) switchOnHardware(Command,Preview);
        else if (Command[0].equals("Frequency") && Command.length == 1) frequencyConfirm(Preview); //apostolh frequency ston JavaClient
        else if (Command[0].equals("Stop") && (Command.length == 2) && (Command[1].equals("Sound") || Command[1].equals("Flash") || Command[1].equals("All"))) switchOffHardware(Command,Preview);
        else Preview.putExtra("DATA",UNKNOWN); //lanthasmenh entolh
    }

    /* H mikrh auth sunarthsh, elegxei to orisma "Diarkeia" pou proairetika sunodeuei kapoies entoles
    * kai to metatrepei apo string se int, h to aporriptei an einai mh egkyrhs morfhs. */
    private int parseSeconds(String[] Command) {
        int Seconds;
        if(Command.length >= 2)
        {
            try{
                Seconds = Integer.parseInt(Command[1]);
                if(Seconds < MIN_DURATION || Seconds > MAX_DURATION) Seconds = DEFAULT_DURATION;
            }catch(NumberFormatException e){
                Seconds = DEFAULT_DURATION;
            }
        }
        else Seconds = DEFAULT_DURATION;
        return Seconds;
    }

    /* Sunarthsh aposundeshs */
    private void logout() {
        switchOffHardware(new String[] {" ","All"},null); //apenergopoihsh flash kai hxou
        if(Handler != null) {
            Handler.closeConnection(); //aposundesh apo ton MQTT broker
            Handler = null; }
    }

    /* H parakatw sunarthsh einai upeuthini gia tin energopoihsh tou flash h/kai ths hxhtikhs eidopoihshs.
    Parallhla, apostellei kai to katallhlo mhnuma pou tha emfanistei sthn othonh tou kinhtou, mesw ths
    DisplayMessageActivity. */
    private void switchOnHardware(String[] Command, Intent Preview)
    {
        switch(Command[0])
        {
            case "Sound":
                playSound(Command,Preview);
                break;
            case "Flash":
                openFlash(Command,Preview);
                break;
            default:
                playSound(Command,null);
                openFlash(Command,null);
                Preview.putExtra("DATA",ALL_ON);
        }
    }

    /* H parakatw sunarthsh einai upeuthini gia tin apenergopoihsh tou flash h/kai ths hxhtikhs eidopoihshs.
    Parallhla, apostellei kai to katallhlo mhnuma pou tha emfanistei sthn othonh tou kinhtou, mesw ths
    DisplayMessageActivity. */
    private void switchOffHardware(String[] Command, Intent Preview)
    {
        switch(Command[1])
        {
            case "Sound":
                if(sound != null) //an o hxos "einai anoixtos"
                {
                    Preview.putExtra("DATA",SOUND_OFF);
                    switchSound(-1);
                }
                break;
            case "Flash":
                if(flash != null) //an to flash einai anoixto
                {
                    Preview.putExtra("DATA",FLASH_OFF);
                    switchFlash(-1);
                }
                break;
            default:
                if(sound != null) { switchSound(-1); }
                if(flash != null) { switchFlash(-1); }
                if(Preview != null) Preview.putExtra("DATA",ALL_OFF);
        }
    }

    /* H parakatw sunarthsh energopoiei thn hxhtikh eidopoihsh an einai
     * apenergopoihmenh, kai antistrofa thn apenergopoiei an einai energopoihmenh! */
    private boolean switchSound(int seconds) //h energopoihsh afora diarkeia 'Seconds'
    {
        if(sound == null) {
            sound = new Sound(getApplicationContext());
            sound.playSound(seconds);
            return true;
        } else {
            sound.stopSound();
            sound = null;
            return false; }
    }

    /* H parakatw sunarthsh energopoiei to flash an einai apenergopoihmeno,
     * kai antistrofa to apenergopoiei an einai energopoihmeno! */
    private boolean switchFlash(int seconds) //h energopoihsh afora diarkeia 'Seconds'
    {
        if(flash == null) {
            flash = new Flash(getApplicationContext());
            flash.openFlash(seconds);
            return true;
        } else {
            flash.closeFlash();
            flash = null;
            return false; }
    }

    /* Anaparagwgh hxhtikhs eidopoihshs gia sugkekrimenh xronikh diarkeia. */
    private void playSound(String[] seconds, Intent Preview)
    {
        int sec = parseSeconds(seconds);
        if(Preview != null) Preview.putExtra("DATA", SOUND_ON + sec + " seconds!");
        if(!switchSound(sec)) switchSound(sec); //prwta kleinei prohgoumeno hxo kai meta paizei ap' thn arxh
    }

    /* Anoigma optikhs eidopoihshs gia sugkekrimenh xronikh diarkeia. */
    private void openFlash(String[] seconds, Intent Preview)
    {
        int sec = parseSeconds(seconds);
        if(Preview != null) Preview.putExtra("DATA", FLASH_ON + sec + " seconds!");
        if(!switchFlash(sec)) switchFlash(sec);
        //prwta kleinei to flash an einai hdh anoixto, kai meta to ksana-anoigei
    }

    /* H parakatw sunarthsh apostellei ston JavaClient,
    thn epithymith suxnothta apostolhs mhnumatwn sto Broker. */
    private void frequencyConfirm(Intent Preview)
    {
        if(Preview != null) Preview.putExtra("DATA",FREQUENCY);
        Handler.publishMessage("Frequency_Answer " + receivingFrequency);
    }

    /* Sunarthsh elegxou ths katastashs sundeshs */
    private String checkStatus() {
        if(Handler != null && Handler.isConnected()) return "CONNECTED";
        else return "DISCONNECTED";
    }

    /* Apostolh, sto mainActivity, ths katastashs sundeshs, prokeimenou
    * na gnorizei an xreiazetai na epanalifthei i diadikasia tou login,
    * i na metavoume kateutheian sto DisplayMessageActivity. */
    private  void sendConnectionStatus(String connection) {
        Intent status = new Intent("GETCONNECTION");
        status.putExtra("CONNECTION",connection);
        sendBroadcast(status);
    }
}