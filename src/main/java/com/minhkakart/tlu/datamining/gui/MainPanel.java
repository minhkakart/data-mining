package com.minhkakart.tlu.datamining.gui;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static com.minhkakart.tlu.datamining.gui.Application.DEFAULT_DIRECTORY;

@SuppressWarnings({"CallToPrintStackTrace", "MagicConstant"})
public class MainPanel extends JPanel {
    private static final Log log = LogFactory.getLog(MainPanel.class);
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
    private final JLabel uniqueCountLabel = new JLabel(ColumnAnalysis.UNIQUE_COUNT + "N/A");
    private final JLabel missingCountLabel = new JLabel(ColumnAnalysis.MISSING_COUNT + "N/A");
    private final JLabel minLabel = new JLabel(ColumnAnalysis.MIN + "N/A");
    private final JLabel maxLabel = new JLabel(ColumnAnalysis.MAX + "N/A");
    private final JButton fillMissingButton = new JButton("Fill missing");
    private final JButton removeMissingButton = new JButton("Remove missing");
    private final JButton normalizeButton = new JButton("Normalize data");
    private final JButton discretizeButton = new JButton("Discretize data");
    private final JButton dropColumnButton = new JButton("Discretize data");

    private String[][] fileData;
    private String[] columnNames;
    private String[] columnData;
    private int selectedColumnIndex = -1;

    public MainPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(Application.WIDTH, Application.HEIGHT));

        setUpLoadingDialog();
        setupFileChooser();
        setupTable();
        initLabelSelectorPanel();
        initColumnAnalysisPanel();
    }

    private void setUpLoadingDialog() {
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        loadingDialog.setResizable(false);
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(this);

        JLabel loadingLabel = new JLabel("Loading...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingDialog.add(loadingLabel);
    }

    private void setupFileChooser() {
        this.chosenFileLabel.setBounds(120, 10, 300, 30);
        add(this.chosenFileLabel);

        this.fileChooser.setCurrentDirectory(new File(DEFAULT_DIRECTORY));
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

        this.columnAnalysisPanel.setBounds(420, 50, 400, 200);
        this.columnAnalysisPanel.setBorder(BorderFactory.createTitledBorder("Column analysis"));
        this.columnAnalysisPanel.setLayout(null);

        JPanel labelArea = new JPanel(new GridLayout(5, 1));
        labelArea.setBounds(10, 20, 180, 170);
        labelArea.add(this.totalCountLabel);
        labelArea.add(this.uniqueCountLabel);
        labelArea.add(this.missingCountLabel);
        labelArea.add(this.minLabel);
        labelArea.add(this.maxLabel);

        JPanel buttonArea = new JPanel(new GridLayout(5, 1, 0, 10));
        buttonArea.setBounds(230, 20, 160, 170);
        this.fillMissingButton.setFocusable(false);
        this.removeMissingButton.setFocusable(false);
        this.normalizeButton.setFocusable(false);
        this.discretizeButton.setFocusable(false);
        this.dropColumnButton.setFocusable(false);
        buttonArea.add(this.fillMissingButton);
        buttonArea.add(this.removeMissingButton);
        buttonArea.add(this.normalizeButton);
        buttonArea.add(this.discretizeButton);
        buttonArea.add(this.dropColumnButton);

        this.fillMissingButton.addActionListener(e -> {
            if (selectedColumnIndex == -1) return;

            // Request user to get new value to fill
            String fillValue = JOptionPane.showInputDialog(this, "Value");
            if (fillValue == null) return;

            // Fill new value to column
            for (int i = 0; i < columnData.length; i++) {
                columnData[i] = columnData[i].isEmpty() ? fillValue : columnData[i];
            }

            // Update data and table
            for (int i = 0; i < fileData.length; i++) {
                fileData[i][selectedColumnIndex] = columnData[i];
            }
            updateTable();

        });

        this.columnAnalysisPanel.add(labelArea);
        this.columnAnalysisPanel.add(buttonArea);
        add(this.columnAnalysisPanel);
    }

    private void checkAnalyzeButton() {
        if (columnData == null) {
            this.fillMissingButton.setEnabled(false);
            this.removeMissingButton.setEnabled(false);

            this.normalizeButton.setEnabled(false);
            this.discretizeButton.setEnabled(false);
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
                    this.columnData = getColumnData(i);
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
        this.uniqueCountLabel.setText(ColumnAnalysis.UNIQUE_COUNT + String.valueOf(uniqueCount));

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
        TOTAL_COUNT, UNIQUE_COUNT, MISSING_COUNT, MIN, MAX;

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
