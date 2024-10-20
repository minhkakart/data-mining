package com.minhkakart.tlu.datamining.clustering;

import java.util.*;

public class KMeans {
    private final int k;
    private final int maxIterations;
    private List<double[]> centroids;

    public KMeans(int k, int maxIterations) {
        this.k = k;
        this.maxIterations = maxIterations;
    }

    public List<Integer> fit(List<double[]> data) {
        int n = data.size();
        int m = data.get(0).length;
        centroids = initializeCentroids(data);

        List<Integer> labels = new ArrayList<>(Collections.nCopies(n, -1));
        boolean converged = false;
        int iterations = 0;

        while (!converged && iterations < maxIterations) {
            converged = true;
            iterations++;

            // Assign clusters
            for (int i = 0; i < n; i++) {
                int newLabel = findClosestCentroid(data.get(i));
                if (labels.get(i) != newLabel) {
                    labels.set(i, newLabel);
                    converged = false;
                }
            }

            // Update centroids
            double[][] newCentroids = new double[k][m];
            int[] counts = new int[k];

            for (int i = 0; i < n; i++) {
                int label = labels.get(i);
                for (int j = 0; j < m; j++) {
                    newCentroids[label][j] += data.get(i)[j];
                }
                counts[label]++;
            }

            for (int i = 0; i < k; i++) {
                for (int j = 0; j < m; j++) {
                    if (counts[i] != 0) {
                        newCentroids[i][j] /= counts[i];
                    }
                }
            }

            centroids = Arrays.asList(newCentroids);
        }

        System.out.println("Converged after " + iterations + " iterations");

        return labels;
    }

    private List<double[]> initializeCentroids(List<double[]> data) {
        List<double[]> centroids = new ArrayList<>();
        Random random = new Random();
        Set<Integer> chosenIndices = new HashSet<>();

        while (centroids.size() < k) {
            int index = random.nextInt(data.size());
            if (!chosenIndices.contains(index)) {
                centroids.add(data.get(index));
                chosenIndices.add(index);
            }
        }

        return centroids;
    }

    private int findClosestCentroid(double[] point) {
        int closest = -1;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < centroids.size(); i++) {
            double distance = euclideanDistance(point, centroids.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                closest = i;
            }
        }

        return closest;
    }

    private double euclideanDistance(double[] point1, double[] point2) {
        double sum = 0.0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public List<double[]> getCentroids() {
        return centroids;
    }
    
    public double getSilhouetteCoefficient(List<double[]> data, List<Integer> labels) {
        int n = data.size();
        double[] a = new double[n];
        double[] b = new double[n];
        
        for (int i = 0; i < n; i++) {
            double sumA = 0.0;
            double sumB = 0.0;
            int countA = 0;
            int countB = 0;
            
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                
                double distance = euclideanDistance(data.get(i), data.get(j));
                if (labels.get(i) == labels.get(j)) {
                    sumA += distance;
                    countA++;
                } else {
                    sumB += distance;
                    countB++;
                }
            }
            
            a[i] = countA == 0 ? 0 : sumA / countA;
            b[i] = countB == 0 ? 0 : sumB / countB;
        }
        
        double[] s = new double[n];
        for (int i = 0; i < n; i++) {
            s[i] = (b[i] - a[i]) / Math.max(a[i], b[i]);
        }
        
        return Arrays.stream(s).average().orElse(0);
    }
    
    public double getDaviesBouldinIndex(List<double[]> data, List<Integer> labels) {
        int n = data.size();
        int m = data.get(0).length;
        
        double[] s = new double[k];
        double[] d = new double[k];
        double[][] dMatrix = new double[k][k];
        
        for (int i = 0; i < k; i++) {
            double sum = 0.0;
            int count = 0;
            
            for (int j = 0; j < n; j++) {
                if (labels.get(j) == i) {
                    sum += euclideanDistance(data.get(j), centroids.get(i));
                    count++;
                }
            }
            
            s[i] = count == 0 ? 0 : sum / count;
        }
        
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (i == j) {
                    continue;
                }
                
                dMatrix[i][j] = (s[i] + s[j]) / euclideanDistance(centroids.get(i), centroids.get(j));
            }
        }
        
        for (int i = 0; i < k; i++) {
            double max = Double.MIN_VALUE;
            for (int j = 0; j < k; j++) {
                if (i == j) {
                    continue;
                }
                
                if (dMatrix[i][j] > max) {
                    max = dMatrix[i][j];
                }
            }
            d[i] = max;
        }
        
        return Arrays.stream(d).average().orElse(0);
    }
}