/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package gui.application.form;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.MySQL;
import java.sql.ResultSet;

/**
 *
 * @author Oshen Sathsara <oshensathsara2003@gmail.com>
 */
public class StockManagement extends javax.swing.JPanel {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Creates new form StockManagement
     */
    public StockManagement() {
        initComponents();
        initCustom();

    }

    private void initDateField() {
        Date.setText(dateFormat.format(new Date()));
    }

    private void initCustom() {
        // Modern UI enhancements
        enhanceUI();

        // Initialize tables
        initMaterialsTable();
        initStockTable();

        // Load initial data
        refreshMaterialsTable();
        refreshStockTable();

        search.addActionListener(e -> searchMaterials(searchInput.getText()));
        Date.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                searchStockByDate(Date.getText());
            }
        });
    }

    private void enhanceUI() {
        
        for (Component comp : jPanel2.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
        }

       
        jTabbedPane3.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

       
        materialsTable.setRowHeight(25);
        materialsTable.setShowGrid(true);
        materialsTable.setGridColor(new Color(240, 240, 240));
        materialsTable.setIntercellSpacing(new Dimension(0, 0));

        stockTable.setRowHeight(25);
        stockTable.setShowGrid(true);
        stockTable.setGridColor(new Color(240, 240, 240));
        stockTable.setIntercellSpacing(new Dimension(0, 0));
    }

    private void initMaterialsTable() {
        String[] columns = {"ID", "Name", "Code", "Category", "Unit", "Current Stock", "Min Stock", "Unit Cost"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        materialsTable.setModel(model);
    }

    private void initStockTable() {
        String[] columns = {"ID", "Material", "Type", "Quantity", "Unit Cost", "Total Cost", "Reference", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable.setModel(model);
    }

    private void refreshMaterialsTable() {
        DefaultTableModel model = (DefaultTableModel) materialsTable.getModel();
        model.setRowCount(0);

        try {
            String query = "SELECT m.material_id, m.material_name, m.material_code, "
                    + "c.category_name, u.unit_name, m.current_stock, "
                    + "m.minimum_stock, m.unit_cost "
                    + "FROM materials m "
                    + "JOIN material_categories c ON m.category_id = c.category_id "
                    + "JOIN units u ON m.unit_id = u.unit_id "
                    + "WHERE m.is_active = 1";

            ResultSet rs = MySQL.executeSearch(query);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("material_id"),
                    rs.getString("material_name"),
                    rs.getString("material_code"),
                    rs.getString("category_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("current_stock"),
                    rs.getDouble("minimum_stock"),
                    rs.getDouble("unit_cost")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading materials: " + e.getMessage());
        }
    }

    private void searchMaterials(String searchText) {
        DefaultTableModel model = (DefaultTableModel) materialsTable.getModel();
        model.setRowCount(0);

        try {
            String query = "SELECT m.material_id, m.material_name, m.material_code, "
                    + "c.category_name, u.unit_name, m.current_stock, "
                    + "m.minimum_stock, m.unit_cost "
                    + "FROM materials m "
                    + "JOIN material_categories c ON m.category_id = c.category_id "
                    + "JOIN units u ON m.unit_id = u.unit_id "
                    + "WHERE m.is_active = 1 AND (m.material_name LIKE '%" + searchText + "%' "
                    + "OR m.material_code LIKE '%" + searchText + "%')";

            ResultSet rs = MySQL.executeSearch(query);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("material_id"),
                    rs.getString("material_name"),
                    rs.getString("material_code"),
                    rs.getString("category_name"),
                    rs.getString("unit_name"),
                    rs.getDouble("current_stock"),
                    rs.getDouble("minimum_stock"),
                    rs.getDouble("unit_cost")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching materials: " + e.getMessage());
        }
    }

    private void refreshStockTable() {
        DefaultTableModel model = (DefaultTableModel) stockTable.getModel();
        model.setRowCount(0);

        try {
            String query = "SELECT s.transaction_id, m.material_name, s.transaction_type, "
                    + "s.quantity, s.unit_cost, s.total_cost, "
                    + "CONCAT(s.reference_type, '-', s.reference_id) as reference, "
                    + "s.transaction_date "
                    + "FROM stock_transactions s "
                    + "JOIN materials m ON s.material_id = m.material_id "
                    + "ORDER BY s.transaction_date DESC";

            ResultSet rs = MySQL.executeSearch(query);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getString("material_name"),
                    rs.getString("transaction_type"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("total_cost"),
                    rs.getString("reference"),
                    rs.getTimestamp("transaction_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading stock transactions: " + e.getMessage());
        }
    }

    private void searchStockByDate(String date) {
        DefaultTableModel model = (DefaultTableModel) stockTable.getModel();
        model.setRowCount(0);

        try {
            String query = "SELECT s.transaction_id, m.material_name, s.transaction_type, "
                    + "s.quantity, s.unit_cost, s.total_cost, "
                    + "CONCAT(s.reference_type, '-', s.reference_id) as reference, "
                    + "s.transaction_date "
                    + "FROM stock_transactions s "
                    + "JOIN materials m ON s.material_id = m.material_id ";

            if (date != null && !date.trim().isEmpty()) {
                query += "WHERE DATE(s.transaction_date) = '" + date + "' ";
            }

            query += "ORDER BY s.transaction_date DESC";

            ResultSet rs = MySQL.executeSearch(query);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getString("material_name"),
                    rs.getString("transaction_type"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("total_cost"),
                    rs.getString("reference"),
                    rs.getTimestamp("transaction_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching stock transactions: " + e.getMessage());
        }
    }

    // Inner class for Material Dialog
    class MaterialDialog extends JDialog {

        private JTextField txtName, txtCode, txtStock, txtMinStock, txtUnitCost;
        private JComboBox<String> cmbCategory, cmbUnit;
        private JButton btnSave;
        private Integer materialId;

        public MaterialDialog(Frame parent, Integer materialId) {
            super(parent, true);
            this.materialId = materialId;
            initComponents();
            loadCategoriesAndUnits();
            if (materialId != null) {
                loadMaterialData();
            }
        }

        private void initComponents() {
            setLayout(new GridLayout(0, 2, 10, 10));

            add(new JLabel("Name:"));
            txtName = new JTextField();
            add(txtName);

            add(new JLabel("Code:"));
            txtCode = new JTextField();
            add(txtCode);

            add(new JLabel("Category:"));
            cmbCategory = new JComboBox<>();
            add(cmbCategory);

            add(new JLabel("Unit:"));
            cmbUnit = new JComboBox<>();
            add(cmbUnit);

            add(new JLabel("Current Stock:"));
            txtStock = new JTextField();
            add(txtStock);

            add(new JLabel("Minimum Stock:"));
            txtMinStock = new JTextField();
            add(txtMinStock);

            add(new JLabel("Unit Cost:"));
            txtUnitCost = new JTextField();
            add(txtUnitCost);

            btnSave = new JButton("Save");
            btnSave.addActionListener(e -> saveMaterial());
            add(btnSave);

            pack();
            setLocationRelativeTo(null);
        }

        private void loadCategoriesAndUnits() {
            try {
                ResultSet rs = MySQL.executeSearch("SELECT category_id, category_name FROM material_categories WHERE is_active = 1");
                while (rs.next()) {
                    cmbCategory.addItem(rs.getString("category_name"));
                }

                rs = MySQL.executeSearch("SELECT unit_id, unit_name FROM units WHERE is_active = 1");
                while (rs.next()) {
                    cmbUnit.addItem(rs.getString("unit_name"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading categories/units: " + e.getMessage());
            }
        }

        private void loadMaterialData() {
            try {
                String query = "SELECT * FROM materials WHERE material_id = " + materialId;
                ResultSet rs = MySQL.executeSearch(query);
                if (rs.next()) {
                    txtName.setText(rs.getString("material_name"));
                    txtCode.setText(rs.getString("material_code"));
                    txtStock.setText(rs.getString("current_stock"));
                    txtMinStock.setText(rs.getString("minimum_stock"));
                    txtUnitCost.setText(rs.getString("unit_cost"));

                    // Select the appropriate category and unit in comboboxes
                    // This would require additional queries to get the names from IDs
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading material: " + e.getMessage());
            }
        }

        private void saveMaterial() {
            // Implementation for saving material
            // This would include validation and database insert/update
        }
    }

    // Implement the Stock Transaction Dialog
    class StockTransactionDialog extends JDialog {

        private JComboBox<String> cmbMaterial, cmbType;
        private JTextField txtQuantity, txtUnitCost, txtReferenceType, txtReferenceId;
        private JTextArea txtNotes;
        private JButton btnSave;

        public StockTransactionDialog(Frame parent) {
            super(parent, "Add Stock Transaction", true);
            initComponents();
            loadMaterials();
        }

        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            // Material selection
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(new JLabel("Material:"), gbc);
            gbc.gridx = 1;
            cmbMaterial = new JComboBox<>();
            add(cmbMaterial, gbc);

            // Transaction type
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(new JLabel("Type:"), gbc);
            gbc.gridx = 1;
            cmbType = new JComboBox<>(new String[]{"IN", "OUT", "ADJUSTMENT"});
            add(cmbType, gbc);

            // Quantity
            gbc.gridx = 0;
            gbc.gridy = 2;
            add(new JLabel("Quantity:"), gbc);
            gbc.gridx = 1;
            txtQuantity = new JTextField();
            add(txtQuantity, gbc);

            // Unit Cost
            gbc.gridx = 0;
            gbc.gridy = 3;
            add(new JLabel("Unit Cost:"), gbc);
            gbc.gridx = 1;
            txtUnitCost = new JTextField();
            add(txtUnitCost, gbc);

            // Reference Type
            gbc.gridx = 0;
            gbc.gridy = 4;
            add(new JLabel("Reference Type:"), gbc);
            gbc.gridx = 1;
            txtReferenceType = new JTextField();
            add(txtReferenceType, gbc);

            // Reference ID
            gbc.gridx = 0;
            gbc.gridy = 5;
            add(new JLabel("Reference ID:"), gbc);
            gbc.gridx = 1;
            txtReferenceId = new JTextField();
            add(txtReferenceId, gbc);

            // Notes
            gbc.gridx = 0;
            gbc.gridy = 6;
            add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1;
            txtNotes = new JTextArea(3, 20);
            add(new JScrollPane(txtNotes), gbc);

            // Save button
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            btnSave = new JButton("Save Transaction");
            btnSave.addActionListener(e -> saveTransaction());
            add(btnSave, gbc);

            pack();
            setLocationRelativeTo(null);
        }

        private void loadMaterials() {
            try {
                String query = "SELECT material_id, material_name FROM materials WHERE is_active = 1";
                ResultSet rs = MySQL.executeSearch(query);
                while (rs.next()) {
                    cmbMaterial.addItem(rs.getString("material_name") + " (" + rs.getInt("material_id") + ")");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading materials: " + e.getMessage());
            }
        }

        private void saveTransaction() {
            try {
                // Get selected material ID
                String materialSelection = (String) cmbMaterial.getSelectedItem();
                int materialId = Integer.parseInt(materialSelection.substring(materialSelection.lastIndexOf("(") + 1, materialSelection.lastIndexOf(")")));

                // Get other values
                String type = (String) cmbType.getSelectedItem();
                double quantity = Double.parseDouble(txtQuantity.getText());
                double unitCost = Double.parseDouble(txtUnitCost.getText());
                double totalCost = quantity * unitCost;
                String referenceType = txtReferenceType.getText();
                int referenceId = txtReferenceId.getText().isEmpty() ? 0 : Integer.parseInt(txtReferenceId.getText());
                String notes = txtNotes.getText();

                // Get current stock for this material
                double currentStock = 0;
                ResultSet rs = MySQL.executeSearch("SELECT current_stock FROM materials WHERE material_id = " + materialId);
                if (rs.next()) {
                    currentStock = rs.getDouble("current_stock");
                }

                // Calculate new balance
                double newBalance;
                if (type.equals("IN")) {
                    newBalance = currentStock + quantity;
                } else if (type.equals("OUT")) {
                    newBalance = currentStock - quantity;
                } else { // ADJUSTMENT
                    newBalance = quantity; // For adjustment, we set directly to the quantity
                }

                // Insert transaction
                String query = "INSERT INTO stock_transactions (material_id, transaction_type, quantity, "
                        + "unit_cost, total_cost, reference_type, reference_id, balance_after, notes, created_by) "
                        + "VALUES (" + materialId + ", '" + type + "', " + quantity + ", " + unitCost + ", "
                        + totalCost + ", '" + referenceType + "', " + referenceId + ", " + newBalance
                        + ", '" + notes + "', 1)";

                MySQL.executeIUD(query);

                // Update material stock
                String updateQuery = "UPDATE materials SET current_stock = " + newBalance
                        + " WHERE material_id = " + materialId;
                MySQL.executeIUD(updateQuery);

                JOptionPane.showMessageDialog(this, "Transaction saved successfully!");
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        title = new javax.swing.JLabel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        searchInput = new javax.swing.JTextField();
        search = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        materialsTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        addMaterial = new javax.swing.JButton();
        editMaterial = new javax.swing.JButton();
        deleteMaterial = new javax.swing.JButton();
        refresh = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        Date = new javax.swing.JFormattedTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        stockTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        addTransaction = new javax.swing.JButton();
        deleteStock = new javax.swing.JButton();

        title.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("Stock Management");

        search.setText("Search");
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        materialsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(materialsTable);

        jPanel2.setLayout(new java.awt.GridLayout(4, 0));

        addMaterial.setText("Add Material");
        addMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMaterialActionPerformed(evt);
            }
        });
        jPanel2.add(addMaterial);

        editMaterial.setText("Edit Selected");
        editMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMaterialActionPerformed(evt);
            }
        });
        jPanel2.add(editMaterial);

        deleteMaterial.setText("Delete Selected");
        deleteMaterial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMaterialActionPerformed(evt);
            }
        });
        jPanel2.add(deleteMaterial);

        refresh.setText("Refresh");
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        jPanel2.add(refresh);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(searchInput, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(search)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(searchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(search))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane3.addTab("Materials Management", jPanel1);

        jLabel1.setText("Search By Date:");

        Date.setText("YY/MM/DD");

        stockTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(stockTable);

        jPanel4.setLayout(new java.awt.GridLayout(2, 0));

        addTransaction.setText("Add Transaction");
        addTransaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTransactionActionPerformed(evt);
            }
        });
        jPanel4.add(addTransaction);

        deleteStock.setText("Delete Selected");
        deleteStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteStockActionPerformed(evt);
            }
        });
        jPanel4.add(deleteStock);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(Date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane3.addTab("Stock Transactions", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 955, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jTabbedPane3)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane3))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMaterialActionPerformed
        MaterialDialog dialog = new MaterialDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        refreshMaterialsTable();
    }//GEN-LAST:event_addMaterialActionPerformed

    private void addTransactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTransactionActionPerformed
        StockTransactionDialog dialog = new StockTransactionDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        refreshStockTable();
        refreshMaterialsTable();
    }//GEN-LAST:event_addTransactionActionPerformed

    private void deleteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteStockActionPerformed
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this transaction?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Integer transactionId = (Integer) stockTable.getValueAt(selectedRow, 0);
                try {
                    // First, get transaction details to reverse the stock change
                    String query = "SELECT material_id, transaction_type, quantity FROM stock_transactions "
                            + "WHERE transaction_id = " + transactionId;
                    ResultSet rs = MySQL.executeSearch(query);

                    if (rs.next()) {
                        int materialId = rs.getInt("material_id");
                        String type = rs.getString("transaction_type");
                        double quantity = rs.getDouble("quantity");

                        // Get current stock
                        ResultSet materialRs = MySQL.executeSearch("SELECT current_stock FROM materials WHERE material_id = " + materialId);
                        if (materialRs.next()) {
                            double currentStock = materialRs.getDouble("current_stock");
                            double newBalance;

                            // Reverse the transaction effect
                            if (type.equals("IN")) {
                                newBalance = currentStock - quantity;
                            } else if (type.equals("OUT")) {
                                newBalance = currentStock + quantity;
                            } else {
                                // For adjustment, we can't accurately reverse without knowing the previous value
                                // So we'll just set to 0 as a fallback
                                newBalance = 0;
                            }

                            // Update material stock
                            String updateQuery = "UPDATE materials SET current_stock = " + newBalance
                                    + " WHERE material_id = " + materialId;
                            MySQL.executeIUD(updateQuery);

                            // Delete the transaction
                            String deleteQuery = "DELETE FROM stock_transactions WHERE transaction_id = " + transactionId;
                            MySQL.executeIUD(deleteQuery);

                            JOptionPane.showMessageDialog(this, "Transaction deleted successfully!");
                            refreshStockTable();
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting transaction: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete");
        }
    }//GEN-LAST:event_deleteStockActionPerformed

    private void editMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMaterialActionPerformed
        int selectedRow = materialsTable.getSelectedRow();
        if (selectedRow >= 0) {
            Integer materialId = (Integer) materialsTable.getValueAt(selectedRow, 0);
            MaterialDialog dialog = new MaterialDialog((Frame) SwingUtilities.getWindowAncestor(this), materialId);
            dialog.setVisible(true);
            refreshMaterialsTable();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a material to edit");
        }
    }//GEN-LAST:event_editMaterialActionPerformed

    private void deleteMaterialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMaterialActionPerformed
        int selectedRow = materialsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this material?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Integer materialId = (Integer) materialsTable.getValueAt(selectedRow, 0);
                try {
                    String query = "UPDATE materials SET is_active = 0 WHERE material_id = " + materialId;
                    MySQL.executeIUD(query);
                    refreshMaterialsTable();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting material: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a material to delete");
        }
    }//GEN-LAST:event_deleteMaterialActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        refreshMaterialsTable();
    }//GEN-LAST:event_refreshActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        searchMaterials(searchInput.getText());
    }//GEN-LAST:event_searchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField Date;
    private javax.swing.JButton addMaterial;
    private javax.swing.JButton addTransaction;
    private javax.swing.JButton deleteMaterial;
    private javax.swing.JButton deleteStock;
    private javax.swing.JButton editMaterial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTable materialsTable;
    private javax.swing.JButton refresh;
    private javax.swing.JButton search;
    private javax.swing.JTextField searchInput;
    private javax.swing.JTable stockTable;
    private javax.swing.JLabel title;
    // End of variables declaration//GEN-END:variables
}
