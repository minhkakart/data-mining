package com.minhkakart.tlu.datamining;

import com.minhkakart.tlu.datamining.associationrule.AprioriTid;
import com.minhkakart.tlu.datamining.clustering.KMeans;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ExtractMethodRecommender")
public class Main {
    public static void main(String[] args) {
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
        List<double[]> data = Arrays.asList(
                new double[]{1, 8},
                new double[]{0, 1},
                new double[]{2, 0},
                new double[]{0, 3},
                new double[]{2, 7}
        );

        int k = 2;
        int maxIterations = 100;

        KMeans kMeans = new KMeans(k, maxIterations);
        List<Integer> labels = kMeans.fit(data);
        List<double[]> centroids = kMeans.getCentroids();

        System.out.println("Cluster assignments:");
        for (int i = 0; i < labels.size(); i++) {
            System.out.println("Data point " + Arrays.toString(data.get(i)) + " is in cluster " + labels.get(i));
        }

        System.out.println("\nCentroids:");
        for (double[] centroid : centroids) {
            System.out.println(Arrays.toString(centroid));
        }
    }
}