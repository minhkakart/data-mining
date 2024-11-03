package com.minhkakart.tlu.datamining.gui;

import com.minhkakart.tlu.datamining.clustering.KMeans;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PredictPanel extends JDialog {

    public PredictPanel(Component owner, KMeans model, String[] columnNames, Map<String, String> discretizeData) {
        super((Frame) SwingUtilities.getWindowAncestor(owner), "Predict", true);
        JPanel contentPanel = initContent(model, columnNames, discretizeData);
        this.setContentPane(contentPanel);

        this.pack();
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JPanel initContent(KMeans model, String[] columnNames, Map<String, String> discretizeData) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(800, 600));

        JPanel inputPanel = new JPanel();
        inputPanel.setBounds(50, 10, 700, 30*columnNames.length);
        inputPanel.setLayout(new GridLayout(0, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));

        for (String columnName : columnNames) {
            inputPanel.add(new JLabel(columnName + discretizeData.getOrDefault(columnName, "") + ": "));
            JTextField textField = new JTextField();
            textField.setVerifyInputWhenFocusTarget(true);
            inputPanel.add(textField);
        }

        panel.add(inputPanel);

        JButton predictButton = new JButton("Predict");
        predictButton.setBounds(50, inputPanel.getHeight() + 10, 100, 30);
        panel.add(predictButton);
        
        predictButton.addActionListener(e -> {
            double[] data = new double[columnNames.length];
            for (int i = 0; i < columnNames.length; i++) {
                JTextField textField = (JTextField) inputPanel.getComponent(i*2 + 1);
                String value = textField.getText();
                data[i] = Double.parseDouble(value);
            }

            int label = model.predict(data);
            JOptionPane.showMessageDialog(panel, "Predicted label: " + label);
        });

        return panel;
    }
}
