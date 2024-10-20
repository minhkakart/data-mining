package com.minhkakart.tlu.datamining;

import com.minhkakart.tlu.datamining.associationrule.AprioriTid;
import com.minhkakart.tlu.datamining.clustering.KMeans;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ExtractMethodRecommender")
public class Main {
    public static void main(String[] args) throws IOException, CsvException {
        /* Data
        List<Set<String>> transactions = Arrays.asList(
                new HashSet<>(Arrays.asList("b e f g h".split(" "))),
                new HashSet<>(Arrays.asList("d f k m".split(" "))),
                new HashSet<>(Arrays.asList("a c d f h m n".split(" "))),
                new HashSet<>(Arrays.asList("a b h k m n".split(" "))),
                new HashSet<>(Arrays.asList("a b e g h m n".split(" ")))
        );

        double minSupport = 0.6;
        double minConfidence = 0.8;*/

        /* Apriori
        Apriori apriori = new Apriori(minSupport, minConfidence);
        List<Set<String>> frequentItemSets = apriori.findFrequentItemSets(transactions);
        List<String> associationRules = apriori.generateAssociationRules(frequentItemSets);

        System.out.println("Frequent ItemSets:");
        for (Set<String> itemset : frequentItemSets) {
            System.out.println(itemset);
        }

        System.out.println("\nAssociation Rules:");
        for (String rule : associationRules) {
            System.out.println(rule);
        }*/

        /* AprioriTid
        AprioriTid aprioriTid = new AprioriTid(minSupport);
        List<Set<String>> frequentItemSets = aprioriTid.findFrequentItemSets(transactions);

        System.out.println("Frequent ItemSets:");
        for (Set<String> itemset : frequentItemSets) {
            System.out.println(itemset);
        }*/

        /* FpTree
        FpTree fpTree = new FpTree(minSupport);
        fpTree.buildTree(transactions);
        List<Set<String>> frequentItemsets = fpTree.mineFrequentItemsets();

        System.out.println("Frequent Itemsets:");
        for (Set<String> itemset : frequentItemsets) {
            System.out.println(itemset);
        }*/

        // Clustering
        long overallStartTime = System.currentTimeMillis();

        System.out.println("Reading data...");
        List<String[]> rawData = new CSVReader(new FileReader("F:\\linh_tinh\\tesst123.csv")).readAll();
        rawData = rawData.subList(1, rawData.size());

        int k = 2;
        int maxIterations = 100;
        int numFold = 10;
        double[][] result = new double[numFold][2];
        
        ExecutorService executor = Executors.newFixedThreadPool(numFold);
        for (int i = 0; i < numFold; i++) {
            int start = i * rawData.size() / numFold;
            int end = (i + 1) * rawData.size() / numFold;
            executor.submit(cluster(rawData.subList(start, end), k, maxIterations, result, i));
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

    private static List<double[]> convertToDouble(List<String[]> rawData, int id) {
        System.out.printf("Fold %d converting data...\n", id);
        long startConvertTime = System.currentTimeMillis();
        List<double[]> data = new ArrayList<>();
        try {
            for (int i = 1; i < rawData.size(); i++) {
                String[] row = rawData.get(i);
                double[] convertedRow = new double[row.length];
                for (int j = 0; j < row.length; j++) {
                    if (row[j].equals("?")) {
                        convertedRow[j] = 0;
                        continue;
                    }
                    convertedRow[j] = Double.parseDouble(row[j]);
                }
                data.add(convertedRow);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Data must be numeric");
        }
        long endConvertTime = System.currentTimeMillis();
        System.out.printf("Fold %d finished in: %dms\n", id, endConvertTime - startConvertTime);
        return data;
    }

    private static Runnable cluster(List<String[]> rawData, int k, int maxIterations, double[][] resultBuffer, int id) {
        return () -> {
            List<double[]> data = convertToDouble(rawData, id);

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