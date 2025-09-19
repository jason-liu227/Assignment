package com.example.ca1bloodcellanalyser;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.*;

public class CellUnionFind {
    private int width, height;
    private Color targetColor;
    private int[] parent;
    private int[] rank;
    private static final double TOLERANCE = 0.1;
    private static final int MIN_SIZE = 100;
    private int minSize = 100;


    public CellUnionFind(int width, int height, Color targetColor, int minSize) {
        this.width = width;
        this.height = height;
        this.targetColor = targetColor;
        this.minSize = minSize;
        int size = width * height;
        parent = new int[size];
        rank = new int[size];

        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    private int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    private void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
        }
    }

    public Map<Integer, List<int[]>> segmentImage(Image image) {
        PixelReader reader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                Color color = reader.getColor(x, y);

                if (isSimilar(color, targetColor, TOLERANCE)) {
                    if (x > 0 && isSimilar(reader.getColor(x - 1, y), targetColor, TOLERANCE)) {
                        union(index, y * width + (x - 1));
                    }
                    if (y > 0 && isSimilar(reader.getColor(x, y - 1), targetColor, TOLERANCE)) {
                        union(index, (y - 1) * width + x);
                    }
                }
            }
        }

        return getConnectedComponents(reader);
    }

    private boolean isSimilar(Color c1, Color c2, double tolerance) {
        return Math.abs(c1.getRed() - c2.getRed()) < tolerance &&
                Math.abs(c1.getGreen() - c2.getGreen()) < tolerance &&
                Math.abs(c1.getBlue() - c2.getBlue()) < tolerance;
    }

    private Map<Integer, List<int[]>> getConnectedComponents(PixelReader reader) {
        Map<Integer, List<int[]>> components = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int root = find(index);

                if (isSimilar(reader.getColor(x, y), targetColor, TOLERANCE)) {
                    components.putIfAbsent(root, new ArrayList<>());
                    components.get(root).add(new int[]{x, y});
                }
            }
        }
        components.entrySet().removeIf(entry -> entry.getValue().size() < minSize);
        return components;
    }

    public Rectangle getBoundingRect(int id) {
        int top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE, left = Integer.MAX_VALUE, right = Integer.MIN_VALUE;

        for (int i = 0; i < parent.length; i++) {
            if (find(i) == id) {
                int x = i % width;
                int y = i / width;
                if (top > y) top = y;
                if (bottom < y) bottom = y;
                if (left > x) left = x;
                if (right < x) right = x;
            }
        }
        return new Rectangle(left, top, right - left, bottom - top);
    }
}