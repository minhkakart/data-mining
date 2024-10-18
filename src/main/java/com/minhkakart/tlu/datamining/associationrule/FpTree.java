package com.minhkakart.tlu.datamining.associationrule;

import java.util.*;

public class FpTree {
    private final double minSupport;
    private final Map<String, Integer> headerTable;
    private final FpNode root;

    public FpTree(double minSupport) {
        this.minSupport = minSupport;
        this.headerTable = new HashMap<>();
        this.root = new FpNode(null, null);
    }

    public void buildTree(List<Set<String>> transactions) {
        // First pass: count item frequencies
        for (Set<String> transaction : transactions) {
            for (String item : transaction) {
                headerTable.put(item, headerTable.getOrDefault(item, 0) + 1);
            }
        }

        // Remove items that do not meet minSupport
        headerTable.entrySet().removeIf(entry -> entry.getValue() < minSupport * transactions.size());

        // Second pass: build the tree
        for (Set<String> transaction : transactions) {
            List<String> sortedItems = new ArrayList<>();
            for (String item : transaction) {
                if (headerTable.containsKey(item)) {
                    sortedItems.add(item);
                }
            }
            sortedItems.sort((a, b) -> headerTable.get(b) - headerTable.get(a));
            insertTransaction(sortedItems, root);
        }
    }

    private void insertTransaction(List<String> transaction, FpNode node) {
        if (transaction.isEmpty()) return;

        String item = transaction.get(0);
        FpNode child = node.getChild(item);
        if (child == null) {
            child = new FpNode(item, node);
            node.addChild(child);
        }
        child.incrementCount();
        insertTransaction(transaction.subList(1, transaction.size()), child);
    }

    public List<Set<String>> mineFrequentItemsets() {
        List<Set<String>> frequentItemsets = new ArrayList<>();
        for (String item : headerTable.keySet()) {
            mineFrequentItemsets(item, new HashSet<>(), frequentItemsets);
        }
        return frequentItemsets;
    }

    private void mineFrequentItemsets(String item, Set<String> suffix, List<Set<String>> frequentItemsets) {
        Set<String> newSuffix = new HashSet<>(suffix);
        newSuffix.add(item);
        frequentItemsets.add(newSuffix);

        List<Set<String>> conditionalPatternBase = new ArrayList<>();
        FpNode node = root.getChild(item);
        while (node != null) {
            List<String> path = new ArrayList<>();
            FpNode parent = node.getParent();
            while (parent != null && parent.getItem() != null) {
                path.add(parent.getItem());
                parent = parent.getParent();
            }
            for (int i = 0; i < node.getCount(); i++) {
                conditionalPatternBase.add(new HashSet<>(path));
            }
            node = node.getNext();
        }

        FpTree conditionalTree = new FpTree(minSupport);
        conditionalTree.buildTree(conditionalPatternBase);
        for (String newItem : conditionalTree.headerTable.keySet()) {
            conditionalTree.mineFrequentItemsets(newItem, newSuffix, frequentItemsets);
        }
    }

    private static class FpNode {
        private final String item;
        private final FpNode parent;
        private final Map<String, FpNode> children;
        private int count;
        private FpNode next;

        public FpNode(String item, FpNode parent) {
            this.item = item;
            this.parent = parent;
            this.children = new HashMap<>();
            this.count = 0;
            this.next = null;
        }

        public String getItem() {
            return item;
        }

        public FpNode getParent() {
            return parent;
        }

        public FpNode getChild(String item) {
            return children.get(item);
        }

        public void addChild(FpNode child) {
            children.put(child.getItem(), child);
        }

        public void incrementCount() {
            count++;
        }

        public int getCount() {
            return count;
        }

        public FpNode getNext() {
            return next;
        }

        public void setNext(FpNode next) {
            this.next = next;
        }
    }
}