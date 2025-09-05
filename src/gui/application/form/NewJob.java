/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package gui.application.form;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import model.MySQL;
import java.sql.ResultSet;
import java.awt.Color;

/**
 *
 * @author Oshen Sathsara <oshensathsara2003@gmail.com>
 */
public class NewJob extends javax.swing.JPanel {

    private Map<String, Integer> serviceCategories = new HashMap<>();
    private Map<String, Integer> priorityLevels = new HashMap<>();

    /**
     * Creates new form NewJob
     */
    public NewJob() {
        initComponents();
        customizedUI();
        loadServiceCategories();
        loadPriorityLevels();
        setupAutoCalculate();

    }

    private void customizedUI() {

        this.setLayout(new java.awt.BorderLayout());

        heading.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        title.putClientProperty("FlatLaf.style", "font: bold 32");

        javax.swing.JPanel contentPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 20, 20));

        javax.swing.JPanel inputWrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
        inputWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 20, 0));

        inputBox.removeAll();
        inputBox.setLayout(new java.awt.GridLayout(1, 2, 20, 0)); // 1 row, 2 columns, 20px gap

        inputBox.add(inputBoxLeft);
        inputBox.add(inputBoxRight);

        inputWrapper.add(inputBox, java.awt.BorderLayout.CENTER);

// Button panel
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(saveJob);

        contentPanel.add(inputWrapper, java.awt.BorderLayout.CENTER);
        contentPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

// Add all components to main panel
        this.add(heading, java.awt.BorderLayout.NORTH);
        this.add(contentPanel, java.awt.BorderLayout.CENTER);

        inputBoxLeft.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 10));
        inputBoxRight.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 15));

        inputBoxLeft.putClientProperty("FlatLaf.style", "background: lighten(@background, 2%)");
        inputBoxRight.putClientProperty("FlatLaf.style", "background: lighten(@background, 2%)");

        this.revalidate();
        this.repaint();
    }

    private void loadServiceCategories() {
        try {
            String query = "SELECT category_id, category_name FROM service_categories WHERE is_active = 1";
            ResultSet rs = MySQL.executeSearch(query);
            serviceCategory.removeAllItems();
            serviceCategories.clear();

            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("category_name");
                serviceCategories.put(name, id);
                serviceCategory.addItem(name);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading service categories: " + e.getMessage());
        }
    }

    private void loadPriorityLevels() {
        try {
            String query = "SELECT priority_id, priority_name FROM priority_levels WHERE is_active = 1 ORDER BY sort_order";
            ResultSet rs = MySQL.executeSearch(query);
            priority.removeAllItems();
            priorityLevels.clear();

            while (rs.next()) {
                int id = rs.getInt("priority_id");
                String name = rs.getString("priority_name");
                priorityLevels.put(name, id);
                priority.addItem(name);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading priority levels: " + e.getMessage());
        }
    }

    private void setupAutoCalculate() {
        quantity.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }
        });

        unitPrice.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }
        });
    }

    private void calculateTotal() {
        try {
            int qty = Integer.parseInt(quantity.getText());
            double price = Double.parseDouble(unitPrice.getText());
            double total = qty * price;
            totalAmount.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            totalAmount.setText("0.00");
        }
    }

    private void calculateTotalAmount() {
        try {
            int qty = Integer.parseInt(quantity.getText());
            double price = Double.parseDouble(unitPrice.getText());
            double total = qty * price;
            totalAmount.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            // Handle invalid input
            totalAmount.setText("0.00");
        }
    }

    private void calculateUnitPrice() {
        try {
            int qty = Integer.parseInt(quantity.getText());
            double total = Double.parseDouble(totalAmount.getText());
            double price = total / qty;
            unitPrice.setText(String.format("%.2f", price));
        } catch (NumberFormatException | ArithmeticException e) {
            // Handle invalid input or division by zero
            unitPrice.setText("0.00");
        }
    }

    private void validateJobNumber() {
        try {
            String jobNumber = jobNo.getText();
            if (!jobNumber.isEmpty()) {
                String query = "SELECT COUNT(*) FROM jobs WHERE job_number = '" + jobNumber + "'";
                ResultSet rs = MySQL.executeSearch(query);
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Job number already exists!");
                    jobNo.setBackground(Color.PINK);
                } else {
                    jobNo.setBackground(Color.WHITE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error validating job number: " + e.getMessage());
        }
    }

    private void autoFillCustomerDetails() {
        try {
            String customer = customerName.getText();
            if (!customer.isEmpty()) {
                String query = "SELECT phone, email, address FROM customers WHERE customer_name = '" + customer + "'";
                ResultSet rs = MySQL.executeSearch(query);
                if (rs.next()) {
                    // You could display this information in a tooltip or status bar
                    String details = "Phone: " + rs.getString("phone")
                            + ", Email: " + rs.getString("email");
                    customerName.setToolTipText(details);
                } else {
                    customerName.setToolTipText("New customer - will be added to database");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching customer details: " + e.getMessage());
        }
    }

    private void saveJob() {
        try {
            // Validate required fields
            if (jobNo.getText().trim().isEmpty()
                    || jobTitle.getText().trim().isEmpty()
                    || customerName.getText().trim().isEmpty()
                    || quantity.getText().trim().isEmpty()
                    || unitPrice.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Please fill in all required fields");
                return;
            }

            int customerId = getOrCreateCustomer();
            int categoryId = serviceCategories.get(serviceCategory.getSelectedItem().toString());
            int priorityId = priorityLevels.get(priority.getSelectedItem().toString());

            // Get default status (assuming status_id 1 is 'Pending')
            int statusId = 1;

            String query = "INSERT INTO jobs ("
                    + "job_number, customer_id, service_category_id, job_title, description, "
                    + "specifications, quantity, unit_price, total_amount, status_id, priority_id, "
                    + "order_date, due_date, notes, created_at, updated_at"
                    + ") VALUES ("
                    + "'" + jobNo.getText() + "', "
                    + customerId + ", "
                    + categoryId + ", "
                    + "'" + jobTitle.getText().replace("'", "''") + "', "
                    + "'" + description.getText().replace("'", "''") + "', "
                    + "'" + specifications.getText().replace("'", "''") + "', "
                    + quantity.getText() + ", "
                    + unitPrice.getText() + ", "
                    + totalAmount.getText() + ", "
                    + statusId + ", "
                    + priorityId + ", "
                    + "CURDATE(), "
                    + "'" + dueDate.getText() + "', "
                    + "'" + notes.getText().replace("'", "''") + "', "
                    + "NOW(), NOW())";

            int result = MySQL.executeIUD(query);

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Job saved successfully!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save job");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving job: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getOrCreateCustomer() throws Exception {
        String customerName = this.customerName.getText().trim();
        if (customerName.isEmpty()) {
            throw new Exception("Customer name is required");
        }

        // Check if customer exists
        String query = "SELECT customer_id FROM customers WHERE customer_name = '" + customerName + "'";
        ResultSet rs = MySQL.executeSearch(query);

        if (rs.next()) {
            return rs.getInt("customer_id");
        } else {
            // Create new customer
            String insertQuery = "INSERT INTO customers (customer_name, created_at, updated_at) "
                    + "VALUES ('" + customerName + "', NOW(), NOW())";
            MySQL.executeIUD(insertQuery);

            // Get the new customer ID
            ResultSet newRs = MySQL.executeSearch("SELECT LAST_INSERT_ID() as customer_id");
            if (newRs.next()) {
                return newRs.getInt("customer_id");
            }
        }
        return -1;
    }

    private void clearForm() {
        jobNo.setText("");
        jobTitle.setText("");
        customerName.setText("");
        description.setText("");
        specifications.setText("");
        quantity.setText("");
        unitPrice.setText("");
        totalAmount.setText("");
        notes.setText("");
        dueDate.setValue(new Date());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        inputBox = new javax.swing.JPanel();
        inputBoxLeft = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jobNo = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jobTitle = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        serviceCategory = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        customerName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        specifications = new javax.swing.JTextArea();
        inputBoxRight = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        quantity = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        totalAmount = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        unitPrice = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        dueDate = new javax.swing.JFormattedTextField();
        jLabel14 = new javax.swing.JLabel();
        priority = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notes = new javax.swing.JTextArea();
        heading = new javax.swing.JPanel();
        title = new javax.swing.JLabel();
        saveJob = new javax.swing.JButton();

        inputBox.setLayout(new javax.swing.BoxLayout(inputBox, javax.swing.BoxLayout.LINE_AXIS));

        inputBoxLeft.setLayout(new java.awt.GridLayout(6, 2, -1, 2));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Job No:");
        inputBoxLeft.add(jLabel2);

        jobNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jobNoActionPerformed(evt);
            }
        });
        inputBoxLeft.add(jobNo);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel5.setText("Job Title:");
        inputBoxLeft.add(jLabel5);

        jobTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jobTitleActionPerformed(evt);
            }
        });
        inputBoxLeft.add(jobTitle);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setText("Service category:");
        inputBoxLeft.add(jLabel4);

        serviceCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputBoxLeft.add(serviceCategory);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("Customer Name:");
        inputBoxLeft.add(jLabel3);

        customerName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerNameActionPerformed(evt);
            }
        });
        inputBoxLeft.add(customerName);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("Description:");
        inputBoxLeft.add(jLabel6);

        description.setColumns(20);
        description.setRows(5);
        jScrollPane2.setViewportView(description);

        inputBoxLeft.add(jScrollPane2);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel7.setText("Specifications:");
        inputBoxLeft.add(jLabel7);

        specifications.setColumns(20);
        specifications.setRows(5);
        jScrollPane3.setViewportView(specifications);

        inputBoxLeft.add(jScrollPane3);

        inputBox.add(inputBoxLeft);

        inputBoxRight.setLayout(new java.awt.GridLayout(6, 2, -3, 2));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel11.setText("Quantity:");
        inputBoxRight.add(jLabel11);

        quantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantityActionPerformed(evt);
            }
        });
        inputBoxRight.add(quantity);

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel9.setText("Total Amount:");
        inputBoxRight.add(jLabel9);

        totalAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalAmountActionPerformed(evt);
            }
        });
        inputBoxRight.add(totalAmount);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel10.setText("Unit Price:");
        inputBoxRight.add(jLabel10);

        unitPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unitPriceActionPerformed(evt);
            }
        });
        inputBoxRight.add(unitPrice);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setText("Due Date:");
        inputBoxRight.add(jLabel13);

        dueDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dueDateActionPerformed(evt);
            }
        });
        inputBoxRight.add(dueDate);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel14.setText("Priority:");
        inputBoxRight.add(jLabel14);

        priority.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputBoxRight.add(priority);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel15.setText("Notes:");
        inputBoxRight.add(jLabel15);

        notes.setColumns(20);
        notes.setRows(5);
        jScrollPane1.setViewportView(notes);

        inputBoxRight.add(jScrollPane1);

        inputBox.add(inputBoxRight);

        heading.setLayout(new java.awt.GridLayout(1, 0));

        title.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("New Job Entry");
        heading.add(title);

        saveJob.setText("Save Job");
        saveJob.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveJobActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(heading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(inputBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(saveJob, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(326, 326, 326))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(heading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(inputBox, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveJob)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jobTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jobTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jobTitleActionPerformed

    private void jobNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jobNoActionPerformed
        validateJobNumber();
    }//GEN-LAST:event_jobNoActionPerformed

    private void quantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantityActionPerformed
        calculateTotalAmount();
    }//GEN-LAST:event_quantityActionPerformed

    private void totalAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalAmountActionPerformed
        calculateUnitPrice();
    }//GEN-LAST:event_totalAmountActionPerformed

    private void unitPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unitPriceActionPerformed
        calculateTotalAmount();
    }//GEN-LAST:event_unitPriceActionPerformed

    private void customerNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerNameActionPerformed
        autoFillCustomerDetails();
    }//GEN-LAST:event_customerNameActionPerformed

    private void saveJobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveJobActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveJobActionPerformed

    private void dueDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dueDateActionPerformed
        saveJob();
    }//GEN-LAST:event_dueDateActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField customerName;
    private javax.swing.JTextArea description;
    private javax.swing.JFormattedTextField dueDate;
    private javax.swing.JPanel heading;
    private javax.swing.JPanel inputBox;
    private javax.swing.JPanel inputBoxLeft;
    private javax.swing.JPanel inputBoxRight;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jobNo;
    private javax.swing.JTextField jobTitle;
    private javax.swing.JTextArea notes;
    private javax.swing.JComboBox<String> priority;
    private javax.swing.JTextField quantity;
    private javax.swing.JButton saveJob;
    private javax.swing.JComboBox<String> serviceCategory;
    private javax.swing.JTextArea specifications;
    private javax.swing.JLabel title;
    private javax.swing.JTextField totalAmount;
    private javax.swing.JTextField unitPrice;
    // End of variables declaration//GEN-END:variables
}
