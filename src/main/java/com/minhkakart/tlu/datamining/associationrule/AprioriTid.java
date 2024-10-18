package com.minhkakart.tlu.datamining.associationrule;

import java.util.*;

@SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
public class AprioriTid {
    private final double minSupport;

    public AprioriTid(double minSupport) {
        this.minSupport = minSupport;
    }

    public List<Set<String>> findFrequentItemSets(List<Set<String>> transactions) {
        List<Set<String>> frequentItemSets = new ArrayList<>();
        Map<Set<String>, Integer> itemsetCounts = new HashMap<>();
        List<Map<Set<String>, Integer>> transactionItemSets = new ArrayList<>();

        // Initialize transaction itemsets with 1-itemsets
        for (Set<String> transaction : transactions) {
            Map<Set<String>, Integer> itemSets = new HashMap<>();
            for (String item : transaction) {
                Set<String> itemset = new HashSet<>(Collections.singletonList(item));
                itemSets.put(itemset, 1);
                itemsetCounts.put(itemset, itemsetCounts.getOrDefault(itemset, 0) + 1);
            }
            transactionItemSets.add(itemSets);
        }

        // Filter itemsets by minSupport
        itemsetCounts.entrySet().removeIf(entry -> entry.getValue() < minSupport * transactions.size());
        frequentItemSets.addAll(itemsetCounts.keySet());

        // Generate candidate k-itemsets
        int k = 2;
        while (!itemsetCounts.isEmpty()) {
            Map<Set<String>, Integer> candidateItemSets = new HashMap<>();
            List<Set<String>> prevFrequentItemSets = new ArrayList<>(itemsetCounts.keySet());

            for (Map<Set<String>, Integer> transactionItemset : transactionItemSets) {
                Map<Set<String>, Integer> newTransactionItemset = new HashMap<>();
                for (int i = 0; i < prevFrequentItemSets.size(); i++) {
                    for (int j = i + 1; j < prevFrequentItemSets.size(); j++) {
                        Set<String> itemset1 = prevFrequentItemSets.get(i);
                        Set<String> itemset2 = prevFrequentItemSets.get(j);

                        Set<String> candidate = new HashSet<>(itemset1);
                        candidate.addAll(itemset2);

                        if (candidate.size() == k) {
                            int count = 0;
                            for (Set<String> transaction : transactions) {
                                if (transaction.containsAll(candidate)) {
                                    count++;
                                }
                            }
                            if (count >= minSupport * transactions.size()) {
                                candidateItemSets.put(candidate, count);
                                newTransactionItemset.put(candidate, count);
                            }
                        }
                    }
                }
                transactionItemSets.set(transactionItemSets.indexOf(transactionItemset), newTransactionItemset);
            }

            itemsetCounts = candidateItemSets;
            frequentItemSets.addAll(itemsetCounts.keySet());
            k++;
        }

        return frequentItemSets;
    }
}