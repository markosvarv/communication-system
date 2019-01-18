package shared_buffer;

import mqtt.MqttPublish;

import static java.lang.Thread.sleep;

public class Consumer implements Runnable {
    private SharedBuffer shared;
    private long f;
    private MqttPublish mqttPub;

    public Consumer(SharedBuffer shared, long frequency, MqttPublish mqttPub) {
        this.shared =shared;
        this.f = frequency;
        this.mqttPub = mqttPub;
        new Thread(this, "Consumer").start();
    }

    public void run() {
        while (true) {
            String command = shared.get();
            if (command == "end") break;

            if (command.equals("Execute EyesOpened")) mqttPub.PublishMessage(true);
            else if (command.equals("Execute EyesClosed")) mqttPub.PublishMessage(false);
            else System.err.println("Consumer: Unknown command");

            //sleep for f seconds before sending the next command
            try {
                sleep(f);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}