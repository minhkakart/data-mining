package com.minhkakart.tlu.datamining.gui;

import com.minhkakart.tlu.datamining.clustering.KMeans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("CallToPrintStackTrace")
public class ClusteringPanel extends JDialog {
    private int k;
    private int maxIterations;
    private int numFold;
    private final Vector<KMeans> result = new Vector<>();
    private JTextField clustersFields;
    private JTextField maxIterationsField;
    private JTextField numFoldField;

    private final String[][] rawData;
    private final String[] columnNames;
    private final Map<String, String> discretizeData;
    
    private JPanel bestClusterPanelCentroidPanel;

    public ClusteringPanel(Frame owner, String[][] rawData, String[] columnNames, Map<String, String> discretizeData) {
        super(owner, "Clustering", true);
        this.rawData = rawData;
        this.columnNames = columnNames;
        this.discretizeData = discretizeData;

        JPanel contentPanel = initContent();
        this.setContentPane(contentPanel);

        this.pack();
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JPanel initContent() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(800, 600));

        JPanel inputPanel = new JPanel();
        inputPanel.setBounds(50, 10, 400, 130);
        inputPanel.setLayout(new GridLayout(0, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));

        inputPanel.add(new JLabel("Clusters:"));
        clustersFields = new JTextField();
        clustersFields.setVerifyInputWhenFocusTarget(true);
        inputPanel.add(clustersFields);

        inputPanel.add(new JLabel("Max iterations:"));
        maxIterationsField = new JTextField();
        inputPanel.add(maxIterationsField);

        inputPanel.add(new JLabel("Number of folds:"));
        numFoldField = new JTextField();
        inputPanel.add(numFoldField);

        // Add empty label to fill the grid
        inputPanel.add(new JLabel());

        JButton startButton = new JButton("Start");
        startButton.setFocusable(false);
        startButton.addActionListener(clustering());
        inputPanel.add(startButton);

        panel.add(inputPanel);

        bestClusterPanelCentroidPanel = new JPanel();
        bestClusterPanelCentroidPanel.setBounds(10, 150, 780, 380);
        bestClusterPanelCentroidPanel.setLayout(new GridLayout(0, 1, 5, 5));
        bestClusterPanelCentroidPanel.setBorder(BorderFactory.createTitledBorder("Best Cluster Centroid"));
        panel.add(bestClusterPanelCentroidPanel);


        JButton predictButton = new JButton("Predict");
        predictButton.setBounds(600, 12, 150, 70);
        predictButton.setFocusable(false);
        predictButton.addActionListener(e -> new PredictPanel(this, getBestModel(), columnNames, discretizeData).setVisible(true));
        panel.add(predictButton);
                
        return panel;
    }

    private ActionListener clustering() {
        return (e) -> {
            long overallStartTime = System.currentTimeMillis();

            getParameters();
            
            ExecutorService executor = Executors.newFixedThreadPool(numFold);
            for (int i = 0; i < numFold; i++) {
                int start = i * rawData.length / numFold;
                int end = (i + 1) * rawData.length / numFold;
                
                String[][] fold = splitData(rawData, start, end);
                executor.submit(cluster(fold, k, maxIterations, i));
            }

            executor.shutdown();
            try {
                boolean isFinished = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                if (isFinished) {
                    long overallEndTime = System.currentTimeMillis();
                    System.out.println("Overall finished in: " + (overallEndTime - overallStartTime) + "ms");

                    double[][] bestClusterCentroid = getBestModel().getCentroids();
                    bestClusterPanelCentroidPanel.removeAll();
                    for (int i = 0; i < bestClusterCentroid.length; i++) {
                        StringBuilder label = new StringBuilder("Cluster ").append(i + 1).append(": ");
                        StringBuilder value = new StringBuilder("[ ");
                        for (int j = 0; j < bestClusterCentroid[i].length; j++) {
                            label.append(columnNames[j]).append(", ");
                            value.append(String.format("%.5f", bestClusterCentroid[i][j])).append(", ");
                        }
                                
                        bestClusterPanelCentroidPanel.add(new JLabel(label.substring(0, label.length() - 2)));
                        bestClusterPanelCentroidPanel.add(new JLabel(value.substring(0, value.length() - 2) + " ]"));
                        bestClusterPanelCentroidPanel.add(new JLabel());
                    }
                    bestClusterPanelCentroidPanel.revalidate();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
        };
    }
    
    private void getParameters() {
        try {
            k = Integer.parseInt(clustersFields.getText());
            maxIterations = Integer.parseInt(maxIterationsField.getText());
            numFold = Integer.parseInt(numFoldField.getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input");
        }
    }

    private String[][] splitData(String[][] rawData, int start, int end) {
        String[][] fold = new String[end - start][rawData[0].length];
        if (end - start >= 0) System.arraycopy(rawData, start, fold, 0, end - start);
        return fold;
    }

    private double[][] convertToDouble(String[][] rawData, int id) {
        System.out.printf("Fold %d converting data...\n", id + 1);
        long startConvertTime = System.currentTimeMillis();
        double[][] data = new double[rawData.length][rawData[0].length];
        try {
            for (int i = 0; i < rawData.length; i++) {
                String[] row = rawData[i];
                double[] convertedRow = new double[row.length];
                for (int j = 0; j < row.length; j++) {
                    if (row[j].equals("?")) {
                        convertedRow[j] = 0;
                        continue;
                    }
                    convertedRow[j] = Double.parseDouble(row[j]);
                }
                data[i] = convertedRow;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Data must be numeric");
        }
        long endConvertTime = System.currentTimeMillis();
        System.out.printf("Fold %d converted data in: %dms\n", id + 1, endConvertTime - startConvertTime);
        return data;
    }

    private Runnable cluster(String[][] rawData, int k, int maxIterations, int id) {
        return () -> {
            double[][] data = convertToDouble(rawData, id);
            KMeans kMeans = new KMeans(k, maxIterations);
            long startTime = System.currentTimeMillis();
            kMeans.fit(data);
            long endTime = System.currentTimeMillis();
            System.out.printf("Fold %d Finished in: %dms\n", id + 1, (endTime - startTime));
            result.add(kMeans);
        };
    }

    private KMeans getBestModel() {
        if (result.isEmpty()) {
            throw new RuntimeException("No result");
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return result.stream()
                .min(Comparator.comparingDouble(KMeans::getDaviesBouldinIndex))
                .orElseThrow(() -> new RuntimeException("No result"));
    }

    public static void main(String[] args) {
        new ClusteringPanel(null, null, null, null).setVisible(true);
    }
}
