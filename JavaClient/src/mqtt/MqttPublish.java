package mqtt;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublish {
    private String broker;
    private String clientId;
    private String topic;
    private String open_content;
    private String close_content;
    private int qos;
    private String username;
    private char[] password;
    MqttClient sampleClient;

    public void setSettings(String b, String cid, String t, String oc, String cc, int q, String u, char[] p) {
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
        open_content = oc;
        close_content = cc;
        qos = q;
    }

    //publishes the message and returns true on success and false on failure
    public boolean PublishMessage (boolean isOpenContent) {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            if (username!=null && password!=null) {
                connOpts.setUserName(username);
                connOpts.setPassword(password);
            }
            //connecting to broker
            sampleClient.connect(connOpts);

            MqttMessage message;
            //Publishing message
            if (isOpenContent) message = new MqttMessage(open_content.getBytes());
            else message = new MqttMessage(close_content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            sampleClient.disconnect();
            return true;
        } catch (MqttException me) {
            System.err.println("reason " + me.getReasonCode());
            System.err.println("msg " + me.getMessage());
            System.err.println("loc " + me.getLocalizedMessage());
            System.err.println("cause " + me.getCause());
            System.err.println("excep " + me);
            //me.printStackTrace();
            return false;
        }
    }
}
