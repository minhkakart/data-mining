package com.minhkakart.tlu.datamining;

import com.minhkakart.tlu.datamining.clustering.KMeans;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException, CsvException {

        long overallStartTime = System.currentTimeMillis();

        System.out.println("Reading data...");
        List<String[]> rawData = new CSVReader(new FileReader(".\\src\\main\\resources\\data.csv")).readAll();
        rawData = rawData.subList(1, rawData.size());

        int k = 2;
        int maxIterations = 100;
        int numFold = 10;
        double[][] result = new double[numFold][2];

        ExecutorService executor = Executors.newFixedThreadPool(numFold);
        for (int i = 0; i < numFold; i++) {
            int start = i * rawData.size() / numFold;
            int end = (i + 1) * rawData.size() / numFold;
            executor.submit(cluster(rawData.subList(start, end).toArray(new String[rawData.size()/numFold][]), k, maxIterations, result, i));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            System.out.printf("Fold %d: Silhouette: %f, Davies-Bouldin: %f\n", i, result[i][0], result[i][1]);
        }

        long overallEndTime = System.currentTimeMillis();
        System.out.println("Overall finished in: " + (overallEndTime - overallStartTime) + "ms");

    }

    private static double[][] convertToDouble(String[][] rawData, int id) {
        System.out.printf("Fold %d converting data...\n", id);
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
        System.out.printf("Fold %d converted data in: %dms\n", id, endConvertTime - startConvertTime);
        return data;
    }

    private static Runnable cluster(String[][] rawData, int k, int maxIterations, double[][] resultBuffer, int id) {
        return () -> {
            double[][] data = convertToDouble(rawData, id);

            KMeans kMeans = new KMeans(k, maxIterations);
            System.out.printf("Fold %d Clustering...\n", id);
            long startTime = System.currentTimeMillis();
            List<Integer> labels = kMeans.fit(data);

            System.out.printf("Fold %d Calculating metrics...\n", id);
            double silhouette = kMeans.getSilhouetteCoefficient(data, labels);
            double daviesBouldin = kMeans.getDaviesBouldinIndex(data, labels);
            long endTime = System.currentTimeMillis();
            System.out.printf("Fold %d Finished in: %dms\n", id, (endTime - startTime));
            resultBuffer[id][0] = silhouette;
            resultBuffer[id][1] = daviesBouldin;
        };
    }

}