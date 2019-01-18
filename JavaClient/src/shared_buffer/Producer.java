package shared_buffer;

import entropy.Experiment;

import java.text.DecimalFormat;
import java.util.LinkedList;

public class Producer implements Runnable {
    private SharedBuffer shared;
    private LinkedList<Experiment> X;
    private LinkedList<Experiment> testSet;
    private int checkedExperiments;
    private int countSuccess;
    private int k;

    public Producer (SharedBuffer shared, LinkedList X_list, LinkedList testSet_list, int k) {
        this.X = X_list;
        this.shared =shared;
        this.testSet = testSet_list;
        this.checkedExperiments=0;
        this.countSuccess = 0;
        this.k=k;

        new Thread(this, "Producer").start();
    }

    public void run() {
        int counter=0;
        while(testSet.size()!=0){
            this.checkedExperiments++;
            String command = kNN(testSet.poll(), X, k);
            if (command!=null) shared.put("Execute " + command);
            if (counter % 5==0) printPercent(getPercent());
            counter++;
        }

        shared.put("end");
        printPercent(getPercent());
    }

    private String kNN (Experiment x, LinkedList X, int k) {
        int m = X.size(), counter_eyesOp = 0, counter_eyesCl=0;
        double w_eyesOp=0.0, w_eyesCl=0.0;
        double[] distances = new double[m];
        Experiment[] I = new Experiment[k];

        //initialize I
        for (int i=0; i<k; i++) I[i]=(Experiment)X.get(i);

        int maxIndex;
        for (int i=0; i<m; i++) {
            Experiment currentX = (Experiment) X.get(i);
            if (currentX==null) {
                System.err.println("null currentX");
                continue;
            }

            distances[i] = EuclideanDistance(x.getVector(), currentX.getVector());
            if (distances[i]==-1) return null;

            maxIndex = 0;

            //find the max element to be replaced
            for (int j=0; j<k; j++){
                if(distances[j] > distances[maxIndex]){
                    maxIndex = j;
                    distances[maxIndex] = distances[j];
                }
            }
            //Swap items if distances[i] < max
            if (distances[i] < distances[maxIndex]){
                double temp = distances[maxIndex];
                distances[maxIndex] = distances[i];
                distances[i] = temp;
                //keep current experiment in I
                I[maxIndex] = currentX;
            }

        }

        for (int i=0; i<k; i++) {
            if (I[i].getName().equals("EyesOpened")) {
                counter_eyesOp++;
                w_eyesOp += 1.0/distances[i];
            }
            else if (I[i].getName().equals("EyesClosed")) {
                counter_eyesCl++;
                w_eyesCl += 1.0/distances[i];
            }
            else {
                System.err.println("Unknown label");
                return null;
            }
        }

        String class_category;
        if (counter_eyesCl*w_eyesCl > counter_eyesOp*w_eyesOp) class_category = "EyesClosed";
        else class_category = "EyesOpened";

        checkCategory(x, class_category);

        X.add(x); //add the classified experiment in X

        return class_category;
    }

    private void checkCategory (Experiment x, String class_category) {
        if (class_category.equals(x.getName())) {
            System.out.println("Success");
            countSuccess++;
        }
        else System.out.println("Not Success");
    }

    private double EuclideanDistance (double[] x, double[] y) {
        if (x.length != y.length) {
            System.err.println("Error in EuclideanDistance. Vectors must have the same size");
            return -1.0;
        }
        double diff_square_sum = 0.0;
        for (int i=0; i<x.length; i++) {
            diff_square_sum += (x[i] - y[i]) * (x[i] - y[i]);
        }
        return Math.sqrt(diff_square_sum);
    }

    public double getPercent () {
        return ((double) countSuccess / checkedExperiments * 100);
    }

    public void printPercent (double percent) {
        System.out.println("countSuccess = " + countSuccess);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        System.out.println("success Percent = " + df.format(percent));
    }
}