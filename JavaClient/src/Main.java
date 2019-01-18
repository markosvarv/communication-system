import entropy.Experiment;
import mqtt.MqttPublish;
import mqtt.MqttSubscribe;
import shared_buffer.Consumer;
import shared_buffer.Producer;
import shared_buffer.SharedBuffer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Scanner;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) throws InterruptedException{
        graphic();
    }

    private static void runExperiment (File test_set_folder, File training_set_file, int k, long f, MqttPublish mqttPub){
        LinkedList<Experiment> testSet = new LinkedList<>();
        LinkedList<Experiment> X = new LinkedList<>();
        getTestSet(test_set_folder, testSet);
        getTrainingSet(training_set_file, X);
        SharedBuffer shared = new SharedBuffer(4);

        new Producer(shared, X, testSet, k);
        new Consumer(shared, f, mqttPub);
    }

    //add experiment in list for every CSV file in the given folder
    private static void getTestSet(final File folder, LinkedList experiments) {
        String filename;
        double[] array;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getTestSet(fileEntry, experiments);
            } else {
                filename = fileEntry.getName();
                Experiment exp;
                if (filename.contains("EyesClosed")) exp = new Experiment("EyesClosed");
                else if (filename.contains("EyesOpened")) exp = new Experiment("EyesOpened");
                else {
                    System.err.println("Cannot resolve file name " + filename);
                    continue;
                }
                array = exp.getCSVEntropy(fileEntry.getAbsolutePath());
                if (array!= null) {
                    exp.setVector (array);
                    experiments.add(exp);
                }
            }
        }
    }

    //add all experiments of training set in linked list X
    private static void getTrainingSet (File training_set_file, LinkedList X) {
        String s;
        Scanner scanner = null;
        try {
            scanner = new Scanner(training_set_file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (scanner==null) return;
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            s = scanner.nextLine();
            String[] strArray = s.split(",");

            if (strArray.length != 15) {
                System.err.println("Wrong file format");
                return;
            }
            double[] entropyArray = new double[14];

            for (int i = 0; i < 14; i++) {
                //the first element of strArray is the experiment name
                entropyArray[i] = Double.parseDouble(strArray[i + 1]);
            }
            Experiment exp = new Experiment(strArray[0], entropyArray);
            X.add(exp);
        }
        scanner.close();
    }
    //wait to receive frequency from the other client
    private static long waitFrequency (MqttSubscribe mqttSub) {
        long f;
        do {
            f=mqttSub.getFrequency();
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (f<0);
        return f;
    }

    private static void graphic() {
        JTextField TFbroker, TFport, TFtopic, TFusername, TFopen_content, TFclose_content, TF_k;
        JPasswordField password_field;
        ButtonGroup group;

        final JFrame f = new JFrame("Publish Settings");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //java program will end after closing the frame
        f.setSize(350, 500);
        f.setTitle("Publish Settings");

        JLabel lbroker = new JLabel("server");
        lbroker.setText("Server URI:");
        lbroker.setBounds(20, 50, 80, 30);
        TFbroker = new JTextField("localhost");
        TFbroker.setBounds(110, 50, 130, 30);
        JLabel lport = new JLabel("port");
        lport.setText("Port:");
        lport.setBounds(250, 50, 40, 30);
        TFport = new JTextField("1883");
        TFport.setBounds(290, 50, 40, 30);

        JLabel lusername = new JLabel("username");
        lusername.setText("Username:");
        lusername.setBounds(20, 100, 80, 30);
        TFusername = new JTextField();
        TFusername.setBounds(110, 100, 220, 30);

        JLabel lpassword = new JLabel("password");
        lpassword.setText("Password:");
        lpassword.setBounds(20, 150, 80, 30);
        password_field = new JPasswordField();
        password_field.setBounds(110, 150, 220, 30);

        JLabel ltopic = new JLabel("topic");
        ltopic.setText("Topic:");
        ltopic.setBounds(20, 200, 80, 30);
        TFtopic = new JTextField();
        TFtopic.setBounds(110, 200, 220, 30);

        JLabel lopen_content = new JLabel("open_content");
        lopen_content.setText("OpenEyes:");
        lopen_content.setBounds(20, 250, 80, 30);
        TFopen_content = new JTextField();
        TFopen_content.setBounds(110, 250, 220, 30);

        JLabel lclose_content = new JLabel("close_content");
        lclose_content.setText("CloseEyes:");
        lclose_content.setBounds(20, 300, 80, 30);
        TFclose_content = new JTextField();
        TFclose_content.setBounds(110, 300, 220, 30);

        JLabel lqoc = new JLabel("qos");
        lqoc.setText("QoS:");
        lqoc.setBounds(20, 400, 40, 30);

        //Add radio buttons
        JRadioButton Button0 = new JRadioButton("0");
        Button0.setBounds(60, 400, 40, 30);
        Button0.setSelected(true);
        JRadioButton Button1 = new JRadioButton("1");
        Button1.setBounds(100, 400, 40, 30);
        JRadioButton Button2 = new JRadioButton("2");
        Button2.setBounds(140, 400, 40, 30);
        //Group the radio buttons.
        group = new ButtonGroup();
        group.add(Button0);group.add(Button1);group.add(Button2);

        JButton training_set_button = new JButton("Training Set");
        training_set_button.setBounds(20, 350, 120, 25);

        JButton test_set_button = new JButton("Test Set");
        test_set_button.setBounds(150, 350, 120, 25);

        JLabel l_k = new JLabel("k");
        l_k.setText("k:");
        l_k.setBounds(285, 350, 25, 30);
        TF_k = new JTextField();
        TF_k.setBounds(300, 350, 30, 30);

        JButton button_send = new JButton("Save");
        button_send.setBounds(210, 400, 120, 40);

        //add components to frame
        f.add(lbroker);f.add(TFbroker);
        f.add(lport);f.add(TFport);
        f.add(ltopic);f.add(TFtopic);
        f.add(lusername);f.add(TFusername);
        f.add(lpassword);f.add(password_field);
        f.add(lopen_content);f.add(TFopen_content);
        f.add(lclose_content);f.add(TFclose_content);
        f.add(lqoc);f.add(Button0);f.add(Button1);f.add(Button2);
        f.add(test_set_button);f.add(training_set_button);f.add(l_k);f.add(TF_k);
        f.add(button_send);

        f.setLayout(null);
        f.setVisible(true);

        MqttPublish mqttPub = new MqttPublish();

        JFileChooser training_set_chooser = new JFileChooser();
        JFileChooser test_set_chooser = new JFileChooser();

        ActionListener ac = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                File training_set_file = training_set_chooser.getSelectedFile();
                File test_set_folder = test_set_chooser.getSelectedFile();

                String port_string = TFport.getText();
                String qos_string = getSelectedButtonText(group);
                String broker = "tcp://" + TFbroker.getText() + ':' + TFport.getText();
                String topic = TFtopic.getText();
                String username = TFusername.getText();
                String open_content = TFopen_content.getText();
                String close_content = TFclose_content.getText();
                String k_string = TF_k.getText();
                char[] password = password_field.getPassword();
                if (qos_string==null) return;
                if (broker.equals("") || topic.equals("") || qos_string.equals("") || port_string.equals("") || (k_string.equals(""))) {
                    JOptionPane.showMessageDialog(f, "Empty fields!");
                    return;
                }

                if (training_set_file==null){
                    JOptionPane.showMessageDialog(f, "Please select Training Set file!");
                    return;
                }
                if (test_set_folder==null){
                    JOptionPane.showMessageDialog(f, "Please select Test Set folder!");
                    return;
                }

                int k;
                try {
                    k = parseInt(k_string);
                }catch (NumberFormatException e){
                    System.out.println("k must be an integer");
                    return;
                }
                int qos = parseInt(qos_string);

                //create a new subscriber to get frequency
                String subTopic = topic+"_Answer";
                MqttSubscribe mqttSub = new MqttSubscribe();
                mqttSub.setSettings(broker, "JavaSub", subTopic, qos, username, password);
                mqttSub.Subscribe();

                //publish a message to get frequency
                mqttPub.setSettings(broker, "JavaClient", topic, "Frequency", close_content, qos, username, password);
                mqttPub.PublishMessage(true);

                long fr = waitFrequency(mqttSub);

                //set the right mqtt settings and run experiment
                mqttPub.setSettings(broker, "JavaClient", topic, open_content, close_content, qos, username, password);
                runExperiment(test_set_folder, training_set_file, k, fr, mqttPub);
            }
        };
        button_send.addActionListener(ac);

        ActionListener training_set_listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String choosertitle="Select Training Set";

                training_set_chooser.setCurrentDirectory(new java.io.File(".."));
                training_set_chooser.setDialogTitle(choosertitle);

                FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
                training_set_chooser.setFileFilter(filter);
                training_set_chooser.showOpenDialog(f);
            }
        };
        training_set_button.addActionListener(training_set_listener);
        ActionListener test_set_listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String choosertitle="Select Test Set";

                test_set_chooser.setCurrentDirectory(new java.io.File(".."));
                test_set_chooser.setDialogTitle(choosertitle);
                test_set_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                // disable the "All files" option.
                test_set_chooser.setAcceptAllFileFilterUsed(false);
                test_set_chooser.showOpenDialog(f);
            }
        };
        test_set_button.addActionListener(test_set_listener);
    }

    //We use this function to get the selected number from the radio buttons in the GUI
    private static String getSelectedButtonText(ButtonGroup ButtonGroup) {
        for (Enumeration<AbstractButton> buttons = ButtonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }
}