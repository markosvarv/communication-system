package entropy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Experiment {
    private double[] vector;
    private String exp_name;

    public Experiment (String name) {
        exp_name = name;
    }

    public Experiment (String name, double[] entropyArray) {
        exp_name = name;
        vector = entropyArray;
    }

    //returns a 14-value vector with the entropy for every channel in a CSV file
    public double[] getCSVEntropy (String path) {
        String s;
        int col=-1;
        int count = countLines(path)-1;
        double[][] doubleArray = new double[14][count];
        double[] vector = new double[14];
        //System.out.println(count);
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (scanner==null) return null;
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        int actual_columns = count;
        for (int c=0; c<count; c++) {
            col++;
            s = scanner.nextLine();
            String[] strArray = s.split(",");
            boolean skipline = false;
            if (strArray.length<28) {
                System.err.println("Wrong file format");
                return null;
            }
            double[] tempArray = new double[strArray.length];

            for (int i=0; i<strArray.length; i++)
                tempArray[i] = Double.parseDouble(strArray[i]);

            for (int i=0; i<14; i++) {
                //check the sensor's connection quality
                if (tempArray[i+14] >= 2.0) doubleArray[i][col] = tempArray[i];
                else skipline = true;
            }
            if (skipline) {
                actual_columns--;
                col--;
            }
        }
        scanner.close();

        for (int i=0; i<14 ; i++) {
            //calculate entropy for every sensor
            double dataArray[]= new double[actual_columns];
            for (int j=0; j<actual_columns; j++) dataArray[j]=doubleArray[i][j];
            vector[i] = CalcEntropy.calculateEntropy(dataArray);
        }
        return vector;
    }

    public void setVector (double[] array_vector) {
        vector = array_vector;
    }

    public double[] getVector () {
        return vector;
    }

    public String getName () {
        return exp_name;
    }

    private int countLines(String path) {
        int count_rows = 0;
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (scanner==null) return -1;
        while (scanner.hasNextLine()) {
            scanner.nextLine();
            count_rows++;
        }
        return count_rows;
    }
}


