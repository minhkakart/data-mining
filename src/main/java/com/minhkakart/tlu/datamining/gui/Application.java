package com.minhkakart.tlu.datamining.gui;

import javax.swing.*;

public class Application extends JFrame {
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800;
    public static final String DEFAULT_DIRECTORY = "E:\\TLU_Subject\\Ki_5\\Hoc_may\\BaiTap";
    
    public Application() {
        setContentPane(new MainPanel());
        
        pack();
        setTitle("Data Mining");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        setResizable(false);
        setAlwaysOnTop(true);
    }

    public static void main(String[] args) {
        new Application();
    }
}
