package com.example.controlapp;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* H klash auth, diathrei oles tis methodous gia thn egkathydrish
kai to xeirismo ths sundeshs kai ths epikoinwnias me ton MQTT Broker.  */

public class MQTT {

    public MqttAndroidClient MQTTAndroidClient;
    private String ServerURI, ClientID, Username, Password, Topic;
    private int QOS;

    /* Constructor ths klashs, lamvanei kai diathrei sthn klash, ta stoixeia sundeshs me ton Broker! */
    public MQTT(Context context, String ReceivedServer, String ReceivedPort, String ReceivedClientID,
                String ReceivedUsername, String ReceivedPassword, String ReceivedTopic, String ReceivedQOS){

        this.ServerURI = "tcp://" + ReceivedServer + ":" + ReceivedPort; this.ClientID = ReceivedClientID;
        this.Username = ReceivedUsername; this.Password = ReceivedPassword;
        this.Topic = ReceivedTopic; this.QOS = Integer.parseInt(ReceivedQOS);

        MQTTAndroidClient = new MqttAndroidClient(context, ServerURI, ClientID);
        MQTTAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    public void setCallback(MqttCallbackExtended callback) {
        MQTTAndroidClient.setCallback(callback);
    }

    /* Sunarthsh sundeshs ston Broker */
    public void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        /* O parakatw elegxos, afora th diakrish periptwsewn, opou o Broker apaitei,
        h mh, thn eisagwgh katallhlou Username kai Password. */
        if((Username != null && !Username.trim().isEmpty()) && (Password != null && !Password.trim().isEmpty()))
        {
            mqttConnectOptions.setUserName(Username);
            mqttConnectOptions.setPassword(Password.toCharArray());
        }

        try {
            MQTTAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    MQTTAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {}
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    /* Sunarthsh eggrafhs (subscribe) sto aitoumeno topic */
    private void subscribeToTopic() {
        try {
            MQTTAndroidClient.subscribe(Topic, QOS, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });

        } catch (MqttException ex) {
            System.err.println("Cannot Subscribe To Topic!");
            ex.printStackTrace();
        }
    }

    /* Epestrepse thn katastash sundeshs tou Paho MQTT Service */
    public boolean isConnected() { return MQTTAndroidClient.isConnected(); }

    /* H parakatw sunarthsh, kanei unsubscribe ton Client apo to topic eggrafhs
     * kai kalei thn antistoixh sunarthsh aposundeshs apo ton broker. */
    public void closeConnection() {
        try {
            MQTTAndroidClient.unsubscribe(this.Topic);
        } catch (MqttException ex) {
            System.err.println("Cannot Unsubscribe From Topic!");
            ex.printStackTrace();
        }
        try {
            MQTTAndroidClient.disconnect();
        } catch (MqttException ex) {
            System.err.println("Cannot Disconnect From Server!");
            ex.printStackTrace();
        }
    }

    /* Sunarthsh dhmosieushs mhnumatos sto antistoixo topic. */
    public void publishMessage(String msg) {
        if(this.isConnected()) //elegxos sundeshs me ton broker!
        {
            MqttMessage message = new MqttMessage(msg.getBytes());
            message.setQos(this.QOS);
            try {
                MQTTAndroidClient.publish(this.Topic+"_Answer",message);
            } catch (MqttException ex) {
                System.err.println("Cannot Publish Message!");
                ex.printStackTrace();
            }
        }
    }
}