package com.minhkakart.tlu.datamining.associationrule;

import java.util.*;

@SuppressWarnings({"CollectionAddAllCanBeReplacedWithConstructor", "ExtractMethodRecommender"})
public class Apriori {
    private final double minSupport;
    private final double minConfidence;

    public Apriori(double minSupport, double minConfidence) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
    }

    public List<Set<String>> findFrequentItemSets(List<Set<String>> transactions) {
        List<Set<String>> frequentItemSets = new ArrayList<>();
        Map<Set<String>, Integer> itemsetCounts = new HashMap<>();

        // Generate candidate 1-itemSets
        for (Set<String> transaction : transactions) {
            for (String item : transaction) {
                Set<String> itemset = new HashSet<>(Collections.singletonList(item));
                itemsetCounts.put(itemset, itemsetCounts.getOrDefault(itemset, 0) + 1);
            }
        }

        // Filter itemSets by minSupport
        itemsetCounts.entrySet().removeIf(entry -> entry.getValue() < minSupport * transactions.size());
        frequentItemSets.addAll(itemsetCounts.keySet());

        // Generate candidate k-itemSets
        int k = 2;
        while (!itemsetCounts.isEmpty()) {
            Map<Set<String>, Integer> candidateItemSets = new HashMap<>();
            List<Set<String>> prevFrequentItemSets = new ArrayList<>(itemsetCounts.keySet());

            for (int i = 0; i < prevFrequentItemSets.size(); i++) {
                for (int j = i + 1; j < prevFrequentItemSets.size(); j++) {
                    Set<String> itemSet1 = prevFrequentItemSets.get(i);
                    Set<String> itemSet2 = prevFrequentItemSets.get(j);

                    Set<String> candidate = new HashSet<>(itemSet1);
                    candidate.addAll(itemSet2);

                    if (candidate.size() == k) {
                        int count = 0;
                        for (Set<String> transaction : transactions) {
                            if (transaction.containsAll(candidate)) {
                                count++;
                            }
                        }
                        if (count >= minSupport * transactions.size()) {
                            candidateItemSets.put(candidate, count);
                        }
                    }
                }
            }

            itemsetCounts = candidateItemSets;
            frequentItemSets.addAll(itemsetCounts.keySet());
            k++;
        }

        return frequentItemSets;
    }

    public List<String> generateAssociationRules(List<Set<String>> frequentItemSets) {
        List<String> rules = new ArrayList<>();

        for (Set<String> itemset : frequentItemSets) {
            if (itemset.size() > 1) {
                List<String> items = new ArrayList<>(itemset);
                int n = items.size();

                for (int i = 1; i < (1 << n); i++) {
                    Set<String> antecedent = new HashSet<>();
                    Set<String> consequent = new HashSet<>(itemset);

                    for (int j = 0; j < n; j++) {
                        if ((i & (1 << j)) > 0) {
                            antecedent.add(items.get(j));
                        }
                    }

                    consequent.removeAll(antecedent);

                    if (!antecedent.isEmpty() && !consequent.isEmpty()) {
                        double supportAntecedent = calculateSupport(antecedent, frequentItemSets);
                        double supportItemSet = calculateSupport(itemset, frequentItemSets);
                        double confidence = supportItemSet / supportAntecedent;

                        if (confidence >= minConfidence) {
                            rules.add(antecedent + " => " + consequent + " (confidence: " + confidence + ")");
                        }
                    }
                }
            }
        }

        return rules;
    }

    private double calculateSupport(Set<String> itemset, List<Set<String>> frequentItemSets) {
        for (Set<String> frequentItemset : frequentItemSets) {
            if (frequentItemset.equals(itemset)) {
                return (double) Collections.frequency(frequentItemSets, frequentItemset) / frequentItemSets.size();
            }
        }
        return 0.0;
    }
}