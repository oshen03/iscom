/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package gui.application.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.MySQL;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import java.sql.ResultSet;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author Oshen Sathsara <oshensathsara2003@gmail.com>
 */
public class Attendance extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private ChartPanel chartPanel;
    private int presentCount = 0;
    private int absentCount = 0;
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");


    /**
     * Creates new form Attendance
     */
    public Attendance() {
        initComponents();
        changeUI();
        setupTableModel();
        loadAttendanceData();
        createPieChart();

    }

    private void changeUI() {
        this.setBackground(UIManager.getColor("Panel.background"));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        sidepanel.setBackground(UIManager.getColor("Panel.background"));
        other.setBackground(UIManager.getColor("Panel.background"));

        chart.setBackground(UIManager.getColor("Panel.background"));
        Color borderColor = UIManager.getColor("Component.borderColor");
        if (borderColor == null) {
            borderColor = new Color(200, 200, 200);
        }
        chart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        chart.setLayout(new BorderLayout());

        jScrollPane1.setBorder(BorderFactory.createEmptyBorder());
        jScrollPane1.getViewport().setBackground(UIManager.getColor("Table.background"));

        jTable1.setRowHeight(35);
        jTable1.setShowGrid(false);
        jTable1.setIntercellSpacing(new Dimension(0, 0));
        jTable1.setSelectionBackground(UIManager.getColor("Table.selectionBackground"));
        jTable1.setSelectionForeground(UIManager.getColor("Table.selectionForeground"));
        jTable1.setFont(UIManager.getFont("Table.font"));

        JTableHeader header = jTable1.getTableHeader();
        header.setBackground(UIManager.getColor("TableHeader.background"));
        header.setForeground(UIManager.getColor("TableHeader.foreground"));
        header.setFont(UIManager.getFont("TableHeader.font"));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        setupTableRenderers();
    }

    private void setupTableModel() {
        tableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Staff ID", "Name", "Phone No", "In Time", "Out Time", "Status"}
        ) {
            boolean[] canEdit = new boolean[]{false, false, false, false, false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
        jTable1.setModel(tableModel);
    }

    private void setupTableRenderers() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 5 && value != null) { // Status column
                    String status = value.toString().toUpperCase();
                    Color accentColor = null;
                    switch (status) {
                        case "PRESENT":
                        case "ACTIVE":
                            accentColor = new Color(34, 197, 94); // Green
                            break;
                        case "ABSENT":
                        case "INACTIVE":
                            accentColor = new Color(239, 68, 68); // Red
                            break;
                        case "LATE":
                            accentColor = new Color(249, 115, 22); // Orange
                            break;
                        case "HALF_DAY":
                            accentColor = new Color(59, 130, 246); // Blue
                            break;
                        default:
                            accentColor = null;
                    }

                    if (accentColor != null && !isSelected) {
                        c.setForeground(accentColor);
                    }
                    setHorizontalAlignment(JLabel.CENTER);
                    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
                return c;
            }
        };

        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            if (i == 5) {
                jTable1.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
            } else {
                jTable1.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

private void loadAttendanceData() {
    try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());

        String query = "SELECT e.employee_id, CONCAT(e.first_name, ' ', e.last_name) as employee_name, e.phone, "
                + "COALESCE(ea.check_in_time, 'Not Checked In') as check_in_time, "
                + "COALESCE(ea.check_out_time, 'Not Checked Out') as check_out_time, "
                + "CASE "
                + "    WHEN e.is_active = 0 THEN 'INACTIVE' "
                + "    WHEN ea.status IS NULL THEN 'ABSENT' "
                + "    ELSE ea.status "
                + "END as status "
                + "FROM employees e "
                + "LEFT JOIN employee_attendance ea ON e.employee_id = ea.employee_id AND ea.attendance_date = '" + today + "' "
                + "ORDER BY e.employee_id";

        ResultSet resultSet = MySQL.executeSearch(query);

        tableModel.setRowCount(0);
        presentCount = 0;
        absentCount = 0;

        while (resultSet.next()) {
            Object[] row = {
                resultSet.getInt("employee_id"),
                resultSet.getString("employee_name"),
                resultSet.getString("phone"),
                resultSet.getString("check_in_time"),
                resultSet.getString("check_out_time"),
                resultSet.getString("status")
            };

            tableModel.addRow(row);

            String status = resultSet.getString("status");
            if ("PRESENT".equals(status) || "LATE".equals(status) || "HALF_DAY".equals(status)) {
                presentCount++;
            } else {
                absentCount++;
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading attendance data: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void createPieChart() {
        try {
            PieDataset dataset = createDataset();
            JFreeChart pieChart = ChartFactory.createPieChart(
                    "Today's Attendance",
                    dataset,
                    true,
                    true,
                    false
            );

            pieChart.setBackgroundPaint(null);
            pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
            pieChart.getTitle().setPaint(new Color(50, 50, 50));
            pieChart.getTitle().setBackgroundPaint(new Color(255, 255, 255, 200));
            pieChart.getTitle().setPadding(new RectangleInsets(5, 135, 5, 135));

            PiePlot plot = (PiePlot) pieChart.getPlot();
            plot.setOutlineVisible(false);
            plot.setSectionPaint("Present", new Color(34, 197, 94)); // Green
            plot.setSectionPaint("Absent", new Color(239, 68, 68)); // Red
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            plot.setShadowPaint(null);
            plot.setBackgroundPaint(null);

            if (chartPanel != null) {
                chart.remove(chartPanel);
            }

            chartPanel = new ChartPanel(pieChart);
            chartPanel.setPreferredSize(new Dimension(300, 200));

            chart.add(chartPanel, BorderLayout.CENTER);
            chart.revalidate();
            chart.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Chart not available", JLabel.CENTER);
            errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            errorLabel.setForeground(UIManager.getColor("Label.foreground"));
            chart.add(errorLabel, BorderLayout.CENTER);
        }
    }

    private PieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Present", presentCount);
        dataset.setValue("Absent", absentCount);
        return dataset;
    }

    private void refreshData() {
        loadAttendanceData();
        createPieChart();
        JOptionPane.showMessageDialog(this, "Data refreshed successfully!",
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Validates time format (HH:MM:SS)
     */
    private boolean isValidTimeFormat(String time) {
        return TIME_PATTERN.matcher(time).matches();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        container = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        sidepanel = new javax.swing.JPanel();
        chart = new javax.swing.JPanel();
        other = new javax.swing.JPanel();
        present = new javax.swing.JButton();
        absent = new javax.swing.JButton();
        checkout = new javax.swing.JButton();
        timeChange = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        container.setLayout(new javax.swing.BoxLayout(container, javax.swing.BoxLayout.LINE_AXIS));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Staff ID", "Name", "Phone No", "In Time", "Out Time", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        container.add(jScrollPane1);

        sidepanel.setLayout(new java.awt.GridLayout(2, 0));

        javax.swing.GroupLayout chartLayout = new javax.swing.GroupLayout(chart);
        chart.setLayout(chartLayout);
        chartLayout.setHorizontalGroup(
            chartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        chartLayout.setVerticalGroup(
            chartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 247, Short.MAX_VALUE)
        );

        sidepanel.add(chart);

        other.setLayout(new java.awt.GridLayout(5, 0));

        present.setText("Mark Attendence");
        present.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presentActionPerformed(evt);
            }
        });
        other.add(present);

        absent.setText("Mark as Absent");
        absent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                absentActionPerformed(evt);
            }
        });
        other.add(absent);

        checkout.setText("Checkout");
        checkout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkoutActionPerformed(evt);
            }
        });
        other.add(checkout);

        timeChange.setText("Make Time changes");
        timeChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeChangeActionPerformed(evt);
            }
        });
        other.add(timeChange);

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        other.add(refreshButton);

        sidepanel.add(other);

        container.add(sidepanel);

        add(container);
    }// </editor-fold>//GEN-END:initComponents

    private void presentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presentActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member from the table.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int staffId = (Integer) tableModel.getValueAt(selectedRow, 0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String today = dateFormat.format(new Date());
            String currentTime = timeFormat.format(new Date());

            // Check if attendance already exists and has check-in time
            String checkQuery = "SELECT attendance_id, check_in_time FROM staff_attendance WHERE staff_id = " + staffId
                    + " AND attendance_date = '" + today + "'";
            ResultSet rs = MySQL.executeSearch(checkQuery);

            String query;
            if (rs.next()) {
                String existingCheckIn = rs.getString("check_in_time");
                if (existingCheckIn != null && !existingCheckIn.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                            "Check-in time is already set for this staff member today.\n" +
                            "Current check-in time: " + existingCheckIn,
                            "Already Checked In", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                // Update existing record (only status, don't change check_in_time if already set)
                query = "UPDATE staff_attendance SET status = 'PRESENT', check_in_time = '" + currentTime
                        + "' WHERE staff_id = " + staffId + " AND attendance_date = '" + today + "' AND check_in_time IS NULL";
            } else {
                // Insert new record
                query = "INSERT INTO staff_attendance (staff_id, attendance_date, check_in_time, status) VALUES ("
                        + staffId + ", '" + today + "', '" + currentTime + "', 'PRESENT')";
            }

            int result = MySQL.executeIUD(query);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Attendance marked as Present successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error marking attendance: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_presentActionPerformed

    private void absentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_absentActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member from the table.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int staffId = (Integer) tableModel.getValueAt(selectedRow, 0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String today = dateFormat.format(new Date());

            // Check if attendance already exists
            String checkQuery = "SELECT attendance_id FROM staff_attendance WHERE staff_id = " + staffId
                    + " AND attendance_date = '" + today + "'";
            ResultSet rs = MySQL.executeSearch(checkQuery);

            String query;
            if (rs.next()) {
                // Update existing record
                query = "UPDATE staff_attendance SET status = 'ABSENT', check_in_time = NULL, check_out_time = NULL "
                        + "WHERE staff_id = " + staffId + " AND attendance_date = '" + today + "'";
            } else {
                // Insert new record
                query = "INSERT INTO staff_attendance (staff_id, attendance_date, status) VALUES ("
                        + staffId + ", '" + today + "', 'ABSENT')";
            }

            int result = MySQL.executeIUD(query);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Attendance marked as Absent successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error marking attendance: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_absentActionPerformed

    private void timeChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeChangeActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member from the table.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a dialog for time changes
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Change Time", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel inLabel = new JLabel("Check In Time (HH:MM:SS):");
        JTextField inField = new JTextField();
        JLabel outLabel = new JLabel("Check Out Time (HH:MM:SS):");
        JTextField outField = new JTextField();

        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");

        panel.add(inLabel);
        panel.add(inField);
        panel.add(outLabel);
        panel.add(outField);
        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);

        saveButton.addActionListener(e -> {
            try {
                int staffId = (Integer) tableModel.getValueAt(selectedRow, 0);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String today = dateFormat.format(new Date());

                String inTime = inField.getText().trim();
                String outTime = outField.getText().trim();

                // Validate input
                if (inTime.isEmpty() && outTime.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter at least one time field!",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Validate time format
                if (!inTime.isEmpty() && !isValidTimeFormat(inTime)) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Invalid check-in time format. Please use HH:MM:SS format (e.g., 09:30:00)",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    inField.requestFocus();
                    return;
                }

                if (!outTime.isEmpty() && !isValidTimeFormat(outTime)) {
                    JOptionPane.showMessageDialog(dialog, 
                            "Invalid check-out time format. Please use HH:MM:SS format (e.g., 17:30:00)",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    outField.requestFocus();
                    return;
                }

                // Check if attendance record exists
                String checkQuery = "SELECT attendance_id FROM staff_attendance WHERE staff_id = " + staffId
                        + " AND attendance_date = '" + today + "'";
                ResultSet rs = MySQL.executeSearch(checkQuery);

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(dialog, 
                            "No attendance record found for today. Please mark attendance first.",
                            "No Record Found", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                StringBuilder query = new StringBuilder("UPDATE staff_attendance SET ");
                boolean hasUpdate = false;

                if (!inTime.isEmpty()) {
                    query.append("check_in_time = '").append(inTime).append("'");
                    hasUpdate = true;
                }

                if (!outTime.isEmpty()) {
                    if (hasUpdate) {
                        query.append(", ");
                    }
                    query.append("check_out_time = '").append(outTime).append("'");
                    hasUpdate = true;
                }

                if (hasUpdate) {
                    query.append(" WHERE staff_id = ").append(staffId)
                            .append(" AND attendance_date = '").append(today).append("'");

                    int result = MySQL.executeIUD(query.toString());
                    if (result > 0) {
                        JOptionPane.showMessageDialog(dialog, "Time updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        refreshData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to update time!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error updating time: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }//GEN-LAST:event_timeChangeActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        refreshData();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void checkoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkoutActionPerformed
                int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member from the table.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int staffId = (Integer) tableModel.getValueAt(selectedRow, 0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String today = dateFormat.format(new Date());
            String currentTime = timeFormat.format(new Date());

            // Check if attendance record exists and staff is checked in
            String checkQuery = "SELECT attendance_id, check_in_time, check_out_time, status FROM staff_attendance WHERE staff_id = " + staffId
                    + " AND attendance_date = '" + today + "'";
            ResultSet rs = MySQL.executeSearch(checkQuery);

            if (rs.next()) {
                String checkInTime = rs.getString("check_in_time");
                String checkOutTime = rs.getString("check_out_time");
                String status = rs.getString("status");

                if (checkInTime == null || "ABSENT".equals(status)) {
                    JOptionPane.showMessageDialog(this, 
                            "Staff member is not checked in today. Cannot check out.",
                            "Not Checked In", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (checkOutTime != null && !checkOutTime.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                            "Staff member is already checked out today.\n" +
                            "Check-out time: " + checkOutTime,
                            "Already Checked Out", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Update check-out time
                String updateQuery = "UPDATE staff_attendance SET check_out_time = '" + currentTime
                        + "' WHERE staff_id = " + staffId + " AND attendance_date = '" + today + "'";

                int result = MySQL.executeIUD(updateQuery);
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Staff member checked out successfully!",
                            "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                        "No attendance record found for today. Please mark attendance first.",
                        "No Attendance Record", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during checkout: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_checkoutActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton absent;
    private javax.swing.JPanel chart;
    private javax.swing.JButton checkout;
    private javax.swing.JPanel container;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel other;
    private javax.swing.JButton present;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel sidepanel;
    private javax.swing.JButton timeChange;
    // End of variables declaration//GEN-END:variables
}
