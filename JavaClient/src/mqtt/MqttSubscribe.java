package mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscribe implements MqttCallback{
        private String broker;
        private String clientId;
        private String topic;
        private int qos;
        private String username;
        private char[] password;
        private long frequency;
        private MqttClient sampleClient;

        public void setSettings(String b, String cid, String t, int q, String u, char[] p) {
            //username and password are optional
            if ((!u.equals("")) && p.length != 0) {
                username = u;
                password = p;
            } else {
                username = null;
                password = null;
            }
            broker = b;
            clientId = cid;
            topic = t;
            frequency = -1;
            qos = q;
        }

        public void Subscribe () {
            MemoryPersistence persistence = new MemoryPersistence();
            try {
                sampleClient = new MqttClient(broker, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);

                if (username!=null && password!=null) {
                    connOpts.setUserName(username);
                    connOpts.setPassword(password);
                }

                sampleClient.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean b, String s) {
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    }

                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection lost!" + throwable);
                        System.exit(1);
                    }

                    @Override
                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                        String[] command = mqttMessage.toString().split(" ");
                        setFrequency(Long.parseLong(command[1])*1000);
                        System.out.println("(messageArrived:) frequency = " + getFrequency());
                    }
                });

                //connecting to broker
                sampleClient.connect(connOpts);

                //subscribe to a topic
                sampleClient.subscribe(topic, qos);
            } catch (MqttException me) {
                System.err.println("reason " + me.getReasonCode());
                System.err.println("msg " + me.getMessage());
                System.err.println("loc " + me.getLocalizedMessage());
                System.err.println("cause " + me.getCause());
                System.err.println("excep " + me);
                //me.printStackTrace();
            }
        }

        public long getFrequency () {
            return this.frequency;
        }

        public void setFrequency (long f) {
            this.frequency = f;
        }

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
