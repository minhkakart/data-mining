package com.minhkakart.tlu.datamining.gui;

import com.minhkakart.tlu.datamining.utils.HtmlText;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minhkakart.tlu.datamining.gui.Application.DEFAULT_DIRECTORY;

@SuppressWarnings({"CallToPrintStackTrace", "MagicConstant"})
public class MainPanel extends JPanel {
    JDialog loadingDialog = new JDialog();

    private final JLabel chosenFileLabel = new JLabel("No file chosen");
    private final JFileChooser fileChooser = new JFileChooser();
    private final JButton openFileButton = new JButton("Choose file");
    private String chosenFilePath;

    private final JTable dataTable = new JTable();

    private final JPanel labelSelectorPanel = new JPanel();
    private final ButtonGroup labelSelectorGroup = new ButtonGroup();

    private final JPanel columnAnalysisPanel = new JPanel();
    private final JLabel totalCountLabel = new JLabel(ColumnAnalysis.TOTAL_COUNT + "N/A");
    private final JLabel distinctLabel = new JLabel(ColumnAnalysis.DISTINCT + "N/A");
    private final JLabel missingCountLabel = new JLabel(ColumnAnalysis.MISSING_COUNT + "N/A");
    private final JLabel minLabel = new JLabel(ColumnAnalysis.MIN + "N/A");
    private final JLabel maxLabel = new JLabel(ColumnAnalysis.MAX + "N/A");
    /**
     * Nút điền dữ liệu thiếu
     */
    private final JButton fillMissingButton = new JButton(HtmlText.centerText("Fill missing"));
    /**
     * Nút xóa hàng chứa dữ liệu thiếu
     */
    private final JButton removeMissingButton = new JButton(HtmlText.centerText("Remove missing"));
    /**
     * Nút chuẩn hóa dữ liệu
     */
    private final JButton normalizeButton = new JButton(HtmlText.centerText("Normalize date-time"));
    /**
     * Nút rời rạc hóa dữ liệu
     */
    private final JButton discretizeButton = new JButton(HtmlText.centerText("Discretize data"));
    /**
     * Nút xóa cột
     */
    private final JButton dropColumnButton = new JButton(HtmlText.centerText("Drop column"));
    /**
     * Nút lưu dữ liệu
     */
    private final JButton saveButton = new JButton(HtmlText.centerText("Save file"));

    private final JButton clusteringButton = new JButton("Clustering");

    private String[][] fileData = null;
    private String[] columnNames = null;
    private String[] columnData = null;
    private int selectedColumnIndex = -1;

    private final Map<String, String> discretizeData = new HashMap<>();

    public MainPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(Application.WIDTH, Application.HEIGHT));

        setupLoadingDialog();
        setupFileChooser();
        setupTable();
        initLabelSelectorPanel();
        initColumnAnalysisPanel();
        initClusteringButton();
    }

    private void initClusteringButton() {
        this.clusteringButton.setBounds(998, 58, 190, 190);
        this.clusteringButton.addActionListener(e -> {
            ClusteringPanel clusteringPanel = new ClusteringPanel((JFrame) SwingUtilities.getWindowAncestor(this), fileData, columnNames, discretizeData);
            clusteringPanel.setVisible(true);
        });

        add(this.clusteringButton);
    }

    private void setupLoadingDialog() {
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        loadingDialog.setResizable(false);
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setAlwaysOnTop(true);

        JLabel loadingLabel = new JLabel("Loading...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingDialog.add(loadingLabel);
    }

    private void setupFileChooser() {
        this.chosenFileLabel.setBounds(120, 10, 300, 30);
        add(this.chosenFileLabel);

        File currDir;
        try {
            currDir = new File(DEFAULT_DIRECTORY);
        } catch (Exception e) {
//            e.printStackTrace();
            currDir = new File(".");
        }

        this.fileChooser.setCurrentDirectory(currDir);
        this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.fileChooser.setMultiSelectionEnabled(false);
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        this.openFileButton.setFocusable(false);
        this.openFileButton.setBounds(10, 10, 100, 30);
        this.openFileButton.addActionListener(e -> new Thread(() -> {
            int result = this.fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                this.chosenFilePath = this.fileChooser.getSelectedFile().getAbsolutePath();
                this.chosenFileLabel.setText(this.fileChooser.getSelectedFile().getName());

                try {
                    readCsvFile(this.chosenFilePath);
                } catch (IOException | CsvException ex) {
                    ex.printStackTrace();
                }
            }
        }).start());
        add(this.openFileButton);
    }

    private void setupTable() {
        JScrollPane scrollPane = new JScrollPane(this.dataTable);
        scrollPane.setBounds(10, 290, Application.WIDTH - 20, Application.HEIGHT - 300);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Data table"));
        add(scrollPane);
    }

    private void readCsvFile(String filePath) throws IOException, CsvException {
        new Thread(() -> loadingDialog.setVisible(true)).start();
        new Thread(() -> {
            try {
                List<String[]> data = new CSVReader(new FileReader(filePath)).readAll();

                populateTable(data);
            } catch (IOException | CsvException e) {
                e.printStackTrace();
            } finally {
                loadingDialog.setVisible(false);
            }
        }).start();
    }

    private void populateTable(List<String[]> data) {
        if (data.isEmpty()) return;

        this.columnNames = data.get(0);
        fileData = data.subList(1, data.size()).toArray(new String[0][]);

        setupLabelSelectorPanel(this.columnNames);

        this.dataTable.setModel(new DataTableModel(fileData, this.columnNames));
    }

    private void updateTable() {
        this.dataTable.setModel(new DataTableModel(fileData, this.columnNames));
    }

    private void initLabelSelectorPanel() {
        this.labelSelectorPanel.setBounds(10, 50, 400, 200);
        this.labelSelectorPanel.setBorder(BorderFactory.createTitledBorder("Data label"));
        add(this.labelSelectorPanel);
    }

    private void setupLabelSelectorPanel(String[] columnNames) {
        this.labelSelectorPanel.removeAll();
        int rows = Math.min(columnNames.length, 13);
        int cols = (int) (columnNames.length > 13 ? Math.ceil(columnNames.length / 13.0) : 1);
        this.labelSelectorPanel.setLayout(new GridLayout(rows, cols));

        for (String columnName : columnNames) {
            JRadioButton radio = createLabelSelector(columnName);

            labelSelectorGroup.add(radio);
            this.labelSelectorPanel.add(radio);
        }
        this.labelSelectorPanel.revalidate();
    }

    private void initColumnAnalysisPanel() {
        checkAnalyzeButton();

        this.columnAnalysisPanel.setBounds(420, 50, 500, 200);
        this.columnAnalysisPanel.setBorder(BorderFactory.createTitledBorder("Column analysis"));
        this.columnAnalysisPanel.setLayout(null);

        JPanel labelArea = new JPanel(new GridLayout(5, 1));
        labelArea.setBounds(10, 20, 150, 170);
        labelArea.add(this.totalCountLabel);
        labelArea.add(this.distinctLabel);
        labelArea.add(this.missingCountLabel);
        labelArea.add(this.minLabel);
        labelArea.add(this.maxLabel);

        JPanel buttonArea = new JPanel(new GridLayout(3, 2, 10, 10));
        buttonArea.setBounds(200, 20, 290, 170);
        this.fillMissingButton.setFocusable(false);
        this.removeMissingButton.setFocusable(false);
        this.normalizeButton.setFocusable(false);
        this.discretizeButton.setFocusable(false);
        this.dropColumnButton.setFocusable(false);
        this.saveButton.setFocusable(false);
        buttonArea.add(this.fillMissingButton);
        buttonArea.add(this.removeMissingButton);
        buttonArea.add(this.normalizeButton);
        buttonArea.add(this.discretizeButton);
        buttonArea.add(this.dropColumnButton);
        buttonArea.add(this.saveButton);

        this.fillMissingButton.addActionListener(e -> {
            if (selectedColumnIndex == -1) return;

            // Request user to get new value to fill
            String fillValue = JOptionPane.showInputDialog(this, "Value");
            if (fillValue == null) return;

            // Fill new value to column
            for (int i = 0; i < this.columnData.length; i++) {
                this.columnData[i] = this.columnData[i].isEmpty() ? fillValue : this.columnData[i];
            }

            // Update data and table
            fillNewDataToSelectedColumn(this.columnData);
        });

        this.removeMissingButton.addActionListener(e -> {
            if (selectedColumnIndex == -1) return;

            // Ask user to confirm remove record
            int confirm = JOptionPane.showOptionDialog(this, "Confirm remove record", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (confirm != 0) return;

            // 
            List<String[]> newData = new ArrayList<>();
            for (String[] row : fileData) {
                if (!row[selectedColumnIndex].isEmpty()) {
                    newData.add(row);
                }
            }
            fileData = newData.toArray(new String[0][]);
            updateTable();
            this.columnData = getColumnData(selectedColumnIndex);
            analyzeColumn(this.columnData);
            checkAnalyzeButton();
        });

        this.normalizeButton.addActionListener(e -> {
            // Transform date-time to timestamp
            try {
                for (int i = 0; i < columnData.length; i++) {
                    /* Old code
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(columnData[i]));*/
                    columnData[i] = columnData[i].substring(5, 7);
                }
                fillNewDataToSelectedColumn(this.columnData);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        this.discretizeButton.addActionListener(e -> {
            // Discretize data
            List<String> distinct = Arrays.stream(columnData).distinct().collect(Collectors.toList());
            String showMessage = distinct.stream().reduce("", (acc, elem) -> acc + elem + ": " + distinct.indexOf(elem) + ", ");
            showMessage = showMessage.substring(0, showMessage.length() - 2);

            if (!discretizeData.containsKey(columnNames[selectedColumnIndex])) {
                discretizeData.put(columnNames[selectedColumnIndex], showMessage);
                for (int i = 0; i < columnData.length; i++) {
                    columnData[i] = distinct.indexOf(columnData[i]) + "";
                }
                fillNewDataToSelectedColumn(this.columnData);

                JOptionPane.showMessageDialog(this, showMessage);
            }
        });

        this.dropColumnButton.addActionListener(e -> {
            // Drop column
            if (selectedColumnIndex == -1) return;

            // Ask user to confirm drop column
            int confirm = JOptionPane.showOptionDialog(this, "Confirm drop column " + columnNames[selectedColumnIndex], "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (confirm != 0) return;

            // Drop column
            String[][] newData = new String[fileData.length][fileData[0].length - 1];
            for (int row = 0; row < fileData.length; row++) {
                for (int col = 0, newCol = 0; col < fileData[0].length; col++) {
                    if (col != selectedColumnIndex) {
                        newData[row][newCol++] = fileData[row][col];
                    }
                }
            }
            fileData = newData;
            columnNames = Arrays.stream(columnNames).filter(s -> !s.equals(columnNames[selectedColumnIndex])).toArray(String[]::new);
            setupLabelSelectorPanel(columnNames);
            updateTable();
            selectedColumnIndex = -1;
            this.columnData = null;
            checkAnalyzeButton();
        });

        // Save button
        this.saveButton.addActionListener(e -> {
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            saveChooser.setMultiSelectionEnabled(false);
            saveChooser.setAcceptAllFileFilterUsed(false);
            saveChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

            int res = saveChooser.showSaveDialog(this);

            if (res == JFileChooser.APPROVE_OPTION) {
                String fileName = saveChooser.getSelectedFile().getName() + ".csv";
                System.out.println(fileName);
                FileWriter fileWriter = null;
                CSVWriter csvWriter = null;
                try {
                    fileWriter = new FileWriter(saveChooser.getSelectedFile().getAbsolutePath() + ".csv");
                    csvWriter = new CSVWriter(fileWriter);
                    List<String[]> data = new ArrayList<>();
                    data.add(columnNames);
                    data.addAll(Arrays.stream(fileData).collect(Collectors.toList()));
                    csvWriter.writeAll(data);
                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
                    ex.printStackTrace();
                } finally {
                    if (csvWriter != null) {
                        try {
                            csvWriter.close();
                            fileWriter.close();
                        } catch (IOException ex) {
//                            throw new RuntimeException(ex);
                            ex.printStackTrace();
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Save file successfully");
                }
            }

        });

        this.columnAnalysisPanel.add(labelArea);
        this.columnAnalysisPanel.add(buttonArea);
        add(this.columnAnalysisPanel);
    }

    private void fillNewDataToSelectedColumn(String[] data) {
        if (selectedColumnIndex == -1) return;
        for (int i = 0; i < fileData.length; i++) {
            fileData[i][selectedColumnIndex] = data[i];
        }
        updateTable();
        checkAnalyzeButton();
    }

    private void checkAnalyzeButton() {
        this.saveButton.setEnabled(fileData != null);
        this.dropColumnButton.setEnabled(true);

        if (columnData == null || selectedColumnIndex == -1) {
            this.fillMissingButton.setEnabled(false);
            this.removeMissingButton.setEnabled(false);
            this.normalizeButton.setEnabled(false);
            this.discretizeButton.setEnabled(false);
            this.dropColumnButton.setEnabled(false);
            return;
        }

        if (Arrays.stream(columnData).noneMatch(String::isEmpty)) {
            this.fillMissingButton.setEnabled(false);
            this.removeMissingButton.setEnabled(false);

            this.normalizeButton.setEnabled(true);
            this.discretizeButton.setEnabled(true);
        } else {
            this.fillMissingButton.setEnabled(true);
            this.removeMissingButton.setEnabled(true);

            this.normalizeButton.setEnabled(false);
            this.discretizeButton.setEnabled(false);
        }


    }

    private JRadioButton createLabelSelector(String columnName) {
        JRadioButton radio = new JRadioButton(columnName);
        radio.setEnabled(true);
        radio.setFocusable(false);
        radio.addActionListener(e -> {
            JRadioButton source = (JRadioButton) e.getSource();
            for (int i = 0; i < this.labelSelectorPanel.getComponents().length; i++) {
                JRadioButton button = (JRadioButton) this.labelSelectorPanel.getComponent(i);
                if (button == source) {
                    selectedColumnIndex = i;
                    this.columnData = getColumnData(selectedColumnIndex);
                    checkAnalyzeButton();
                    analyzeColumn(this.columnData);
                }
            }
        });
        return radio;
    }

    private void analyzeColumn(String[] columnData) {
        // Total count
        this.totalCountLabel.setText(ColumnAnalysis.TOTAL_COUNT + String.valueOf(columnData.length));

        // Unique count
        long uniqueCount = Arrays.stream(columnData).distinct().count();
        this.distinctLabel.setText(ColumnAnalysis.DISTINCT + String.valueOf(uniqueCount));

        // Missing count
        long missingCount = Arrays.stream(columnData).filter(String::isEmpty).count();
        this.missingCountLabel.setText(ColumnAnalysis.MISSING_COUNT + String.valueOf(missingCount));

        String[] nonEmptyData = Arrays.stream(columnData).filter(s -> !s.isEmpty()).toArray(String[]::new);
        // For numeric columns
        try {
            String minValue = Arrays.stream(nonEmptyData).mapToDouble(Double::parseDouble).min().orElse(0) + "";
            this.minLabel.setText(ColumnAnalysis.MIN + minValue);

            String maxValue = Arrays.stream(nonEmptyData).mapToDouble(Double::parseDouble).max().orElse(0) + "";
            this.maxLabel.setText(ColumnAnalysis.MAX + maxValue);

        } catch (NumberFormatException e) {
            // For date-time columns
            try {
                Date minDate = convertToDateStream(nonEmptyData).min(Date::compareTo).orElse(null);
                String minValue = minDate != null ? new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(minDate) : "N/A";
                this.minLabel.setText(ColumnAnalysis.MIN + minValue);

                Date maxDate = convertToDateStream(nonEmptyData).max(Date::compareTo).orElse(null);
                String maxValue = maxDate != null ? new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(maxDate) : "N/A";
                this.maxLabel.setText(ColumnAnalysis.MAX + maxValue);

            } catch (Exception ex) {
                this.minLabel.setText(ColumnAnalysis.MIN + "N/A");
                this.maxLabel.setText(ColumnAnalysis.MAX + "N/A");

//                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            this.minLabel.setText(ColumnAnalysis.MIN + "N/A");
            this.maxLabel.setText(ColumnAnalysis.MAX + "N/A");

//            throw new RuntimeException(e);
        }

//        columnAnalysisPanel.revalidate();
    }

    private static Stream<Date> convertToDateStream(String[] nonEmptyData) {
        return Arrays.stream(nonEmptyData)
                .map(datetime -> {
                    String[] split = datetime.split(" ");
                    String[] date = split[0].split("-");
                    String[] time = split[1].split(":");

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]),
                            Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
                    return calendar.getTime();
                });
    }

    private String[] getColumnData(int columnIndex) {
        String[] columnData = new String[fileData.length];
        for (int i = 0; i < fileData.length; i++) {
            columnData[i] = fileData[i][columnIndex];
        }
        return columnData;
    }

    private enum ColumnAnalysis {
        TOTAL_COUNT, DISTINCT, MISSING_COUNT, MIN, MAX;

        @Override
        public String toString() {
            String name = this.name().replace("_", " ").toLowerCase();
            String capitalize = name.substring(0, 1).toUpperCase() + name.substring(1);
            return capitalize + ": ";
        }
    }

    private static class DataTableModel extends DefaultTableModel {
        public DataTableModel(Object[][] fileData, Object[] columnNames) {
            super(fileData, columnNames);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
