/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package gui.application.form.other;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.MatteBorder;
import model.ActivityLogger;

import model.MySQL;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import gui.application.Application;


/**
 *
 * @author Oshen Sathsara <oshensathsara2003@gmail.com>
 */
public class Dashboard extends javax.swing.JPanel {

    private final Color PRIMARY_COLOR = new Color(0, 122, 204);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private final Color WARNING_COLOR = new Color(255, 193, 7);
    private final Color DANGER_COLOR = new Color(220, 53, 69);
    private final Color TEXT_COLOR = new Color(33, 37, 41);
    private final Color LIGHT_TEXT_COLOR = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    private final Color INFO_COLOR = new Color(23, 162, 184);

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
        startClock();
        loadData();
        setupCharts();
        setupRecentActivities();
//        setupQuickActions();
        customizeScrollPane();

    }

    public void startClock() {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                lblTime.setText("Time: " + (timeFormat.format(new Date())));

                lblDate1.setText("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });

        timer.start();
    }

    private String dailySale;
    private String dailySalePerc;
    private String cus;
    private String cusPerc;
    private String salecount;
    private String salecountPerc;
    private String pRepair;
    private String pRepairPerc;

    private void loadData() {
        try {

            ResultSet rs = MySQL.executeSearch("SELECT today_sales.total AS today_sales, "
                    + "ROUND(((today_sales.total - yesterday_sales.total) / NULLIF(yesterday_sales.total, 0)) * 100, 2) AS sales_percentage, "
                    + "today_cust.total AS today_customers, "
                    + "ROUND(((today_cust.total - yesterday_cust.total) / NULLIF(yesterday_cust.total, 0)) * 100, 2) AS customers_percentage, "
                    + "today_orders.total AS today_orders, "
                    + "ROUND(((today_orders.total - yesterday_orders.total) / NULLIF(yesterday_orders.total, 0)) * 100, 2) AS orders_percentage, "
                    + "today_repairs.total AS today_pending_repairs, "
                    + "ROUND(((today_repairs.total - yesterday_repairs.total) / NULLIF(yesterday_repairs.total, 0)) * 100, 2) AS repair_percentage "
                    + "FROM "
                    + "(SELECT IFNULL(SUM(paid_amount), 0) AS total FROM invoice WHERE date = CURDATE()) AS today_sales, "
                    + "(SELECT IFNULL(SUM(paid_amount), 0) AS total FROM invoice WHERE date = CURDATE() - INTERVAL 1 DAY) AS yesterday_sales, "
                    + "(SELECT COUNT(*) AS total FROM customer WHERE registered_date = CURDATE()) AS today_cust, "
                    + "(SELECT COUNT(*) AS total FROM customer WHERE registered_date = CURDATE() - INTERVAL 1 DAY) AS yesterday_cust, "
                    + "(SELECT COUNT(*) AS total FROM invoice WHERE date = CURDATE()) AS today_orders, "
                    + "(SELECT COUNT(*) AS total FROM invoice WHERE date = CURDATE() - INTERVAL 1 DAY) AS yesterday_orders, "
                    + "(SELECT COUNT(*) AS total FROM repair_request_item WHERE repair_status_id = 1 AND submit_date = CURDATE()) AS today_repairs, "
                    + "(SELECT COUNT(*) AS total FROM repair_request_item WHERE repair_status_id = 1 AND submit_date = CURDATE() - INTERVAL 1 DAY) AS yesterday_repairs");

            if (rs.next()) {
                this.dailySale = rs.getString("today_sales");
                this.dailySalePerc = rs.getString("sales_percentage");
                this.cus = rs.getString("today_customers");
                this.cusPerc = rs.getString("customers_percentage");
                this.salecount = rs.getString("today_orders");
                this.salecountPerc = rs.getString("orders_percentage");
                this.pRepair = rs.getString("today_pending_repairs");
                this.pRepairPerc = rs.getString("repair_percentage");
            }

            setupStatsCards();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Something went wrong", "Warning", JOptionPane.WARNING_MESSAGE);

        }
    }

    private void setupStatsCards() {
        statspanel1.setLayout(new GridLayout(1, 4, 15, 0));
        statspanel1.add(createStatCard("Daily Sales", this.dailySale, this.dailySalePerc + "%", "/traitgen/icon/png/chart-line.png", PRIMARY_COLOR));
        statspanel1.add(createStatCard("Customers", this.cus, this.cusPerc + "%", "/traitgen/icon/png/users.png", SUCCESS_COLOR));
        statspanel1.add(createStatCard("Orders", this.salecount, this.salecountPerc + "%", "/traitgen/icon/png/shopping-cart.png", WARNING_COLOR));
        statspanel1.add(createStatCard("Repairs", this.pRepair, this.pRepairPerc + "%", "/traitgen/icon/png//tools.png", DANGER_COLOR));
    }

    private JPanel createStatCard(String title, String value, String growth, String iconPath, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 0));

        card.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 5, 0, 0, color),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            JLabel iconLabel = new JLabel(icon);
            card.add(iconLabel, BorderLayout.WEST);
        } catch (Exception e) {
            card.add(new JLabel(" "), BorderLayout.WEST);
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(LIGHT_TEXT_COLOR);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel growthLabel = new JLabel(growth);
        growthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        growthLabel.setForeground(growth.startsWith("+") ? SUCCESS_COLOR : DANGER_COLOR);

        content.add(titleLabel);
        content.add(valueLabel);
        content.add(growthLabel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void setupCharts() {
        salesChartPanel1.setLayout(new BorderLayout());
        inventoryChartPanel1.setLayout(new BorderLayout());
        salesChartPanel1.add(createSalesChart());
        inventoryChartPanel1.add(createInventoryChart());
    }
    LocalDate currentDate = LocalDate.now();

    private ChartPanel createSalesChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        LocalDate currentDate = LocalDate.now();

        try {
            // Query to get sales data for the last 7 days
            String query = "SELECT DATE(i.date) as sale_date, SUM(i.paid_amount) as daily_total "
                    + "FROM invoice i "
                    + "WHERE i.date BETWEEN DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND CURDATE() "
                    + "GROUP BY DATE(i.date) "
                    + "ORDER BY DATE(i.date)";

            ResultSet rs = MySQL.executeSearch(query);

            // Create a map to store all dates (including days with no sales)
            Map<LocalDate, Double> salesByDate = new HashMap<>();

            // Initialize all dates in the last 7 days with zero
            for (int i = 6; i >= 0; i--) {
                LocalDate date = currentDate.minusDays(i);
                salesByDate.put(date, 0.0);
            }

            // Fill in actual sales data
            while (rs.next()) {
                Date sqlDate = rs.getDate("sale_date");
                LocalDate date = LocalDate.parse(sqlDate.toString());
                double amount = rs.getDouble("daily_total");
                salesByDate.put(date, amount);
            }

            // Add all dates to the dataset in order
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
            for (int i = 6; i >= 0; i--) {
                LocalDate date = currentDate.minusDays(i);
                String dayName = date.format(dayFormatter);
                dataset.addValue(salesByDate.get(date), "Sales", dayName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Add sample data if query fails
            dataset.addValue(12500, "Sales", "Mon");
            dataset.addValue(15000, "Sales", "Tue");
            dataset.addValue(10000, "Sales", "Wed");
            dataset.addValue(18500, "Sales", "Thu");
            dataset.addValue(21500, "Sales", "Fri");
            dataset.addValue(14500, "Sales", "Sat");
            dataset.addValue(9500, "Sales", "Sun");
        }

        JFreeChart lineChart = ChartFactory.createLineChart("Weekly Sales Trend", "", "", dataset, PlotOrientation.VERTICAL, false, true, false);

        lineChart.setBackgroundPaint(null);
        lineChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        lineChart.getTitle().setPaint(new Color(50, 50, 50));
        lineChart.getTitle().setBackgroundPaint(new Color(255, 255, 255, 240));
        lineChart.getTitle().setPadding(new RectangleInsets(5, 170, 5, 170));

        String dateRange = currentDate.minusDays(6).format(DateTimeFormatter.ofPattern("MMM d"))
                + " - "
                + currentDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));

        TextTitle subtitle = new TextTitle(dateRange, new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setPaint(new Color(150, 150, 150, 200));
        lineChart.addSubtitle(subtitle);

        CategoryPlot plot = (CategoryPlot) lineChart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(new Color(150, 150, 150, 200));
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(150, 150, 150, 240));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#,###"));
        rangeAxis.setTickLabelPaint(new Color(100, 100, 100));

        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeIncludesZero(false);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(55, 151, 92));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setBaseShapesVisible(true);
        renderer.setSeriesShapesFilled(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new Dimension(400, 400));
        chartPanel.setOpaque(false);

        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        return chartPanel;
    }

    private ChartPanel createInventoryChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try {
            // SQL query to fetch daily sales data
            String query = "SELECT p.name, SUM(ii.qty) as total_qty "
                    + "FROM invoice_item ii "
                    + "INNER JOIN invoice i ON ii.invoice_id = i.id "
                    + "INNER JOIN stock s ON ii.stock_id = s.id "
                    + "INNER JOIN product p ON s.product_id = p.id "
                    + "WHERE DATE(i.date) = '" + currentDate + "' "
                    + "GROUP BY p.id, p.name";

            ResultSet rs = MySQL.executeSearch(query);
            while (rs.next()) {
                String productName = rs.getString("name");
                int quantity = rs.getInt("total_qty");
                // Add data to the pie chart dataset
                dataset.setValue(productName, quantity);
            }

        } catch (Exception e) {

            dataset.setValue("No Data", 1);
            JOptionPane.showMessageDialog(this, "Something went wrong", "Warning", JOptionPane.WARNING_MESSAGE);

        }

        JFreeChart pieChart = ChartFactory.createPieChart("Product Sales Distribution", dataset, true, true, false);

        pieChart.setBackgroundPaint(null);
        pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        pieChart.getTitle().setPaint(new Color(50, 50, 50));
        pieChart.getTitle().setBackgroundPaint(new Color(255, 255, 255, 200));
        pieChart.getTitle().setPadding(new RectangleInsets(5, 135, 5, 135));

        PiePlot plot = (PiePlot) pieChart.getPlot();

        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);

        Color[] colorPalette = {
            new Color(55, 151, 92),
            new Color(41, 128, 185),
            new Color(155, 89, 182),
            new Color(26, 188, 156),
            new Color(241, 196, 15),
            new Color(230, 126, 34),
            new Color(231, 76, 60),
            new Color(149, 165, 166),
            new Color(52, 73, 94)
        };

        int count = dataset.getItemCount();
        for (int i = 0; i < count; i++) {
            plot.setSectionPaint(dataset.getKey(i), colorPalette[i % colorPalette.length]);
        }

        plot.setShadowPaint(null);

        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 120));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        plot.setInteriorGap(0.04);
        plot.setLabelGap(0.02);

        // Set a nice label format that shows both name and percentage
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {2}", // Format: Name: Percentage
                NumberFormat.getNumberInstance(),
                new DecimalFormat("0.0%"))); // Show one decimal in percentage

        LegendTitle legend = pieChart.getLegend();
        legend.setBorder(0, 0, 0, 0);
//        legend.setBackgroundPaint(null);
        legend.setBackgroundPaint(new Color(255, 255, 255, 200));
        legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 11));

        pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        pieChart.getTitle().setPaint(new Color(50, 50, 50));

        TextTitle subtitle = new TextTitle(
                currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setPaint(new Color(150, 150, 150, 200));
        pieChart.addSubtitle(subtitle);
//        subtitle.setBackgroundPaint(new Color(255, 255, 255, 200));
//        subtitle.setPadding(new RectangleInsets(3, 200, 3, 200));

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(400, 400));

        chartPanel.setOpaque(false);
        chartPanel.setBackground(new Color(0, 0, 0, 0));

        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setZoomAroundAnchor(true);

        return chartPanel;
    }

    private void setupRecentActivities() {

        activitiesList1.removeAll();

        activitiesList1.setLayout(new BoxLayout(activitiesList1, BoxLayout.Y_AXIS));

        try {
//            ResultSet rs = ActivityLogger.getRecentActivities(10);
//            boolean hasData = false;
//
//            while (rs != null && rs.next()) {
//                hasData = true;
//                String action = rs.getString("Action");
//                String description = rs.getString("Description");
//                java.sql.Timestamp timestamp = rs.getTimestamp("Timestamp");
//                String timeAgo = formatTimeAgo(timestamp);
//                Color color = getColorForAction(action);
//
//                addActivityItem(action, description, timeAgo, color);
//            }
//
//            if (!hasData) {
//                addNoDataMessage();
//            }

        } catch (Exception e) {
            addErrorMessage();
                            JOptionPane.showMessageDialog(this, "Something went wrong", "Warning", JOptionPane.WARNING_MESSAGE);

        }

        activitiesList1.revalidate();
        activitiesList1.repaint();

        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> {
            if (jScrollPane2.getVerticalScrollBar() != null) {
                jScrollPane2.getVerticalScrollBar().setValue(0);
            }
        });
    }

    private String formatTimeAgo(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return "Unknown";
        }

        long currentTime = System.currentTimeMillis();
        long activityTime = timestamp.getTime();
        long diffInMs = currentTime - activityTime;

        long diffInMinutes = diffInMs / (1000 * 60);
        long diffInHours = diffInMs / (1000 * 60 * 60);
        long diffInDays = diffInMs / (1000 * 60 * 60 * 24);

        if (diffInMinutes < 1) {
            return "Just now";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + (diffInMinutes == 1 ? " min ago" : " mins ago");
        } else if (diffInHours < 24) {
            return diffInHours + (diffInHours == 1 ? " hr ago" : " hrs ago");
        } else if (diffInDays < 7) {
            return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
        } else {
            // For older activities, show the actual date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
            return sdf.format(timestamp);
        }
    }

    private Color getColorForAction(String action) {
        if (action == null) {
            return PRIMARY_COLOR;
        }

        // Map different actions to appropriate colors
        switch (action.toLowerCase()) {
            case "new order":
            case "order created":
                return PRIMARY_COLOR;
            case "repair completed":
            case "inventory updated":
                return SUCCESS_COLOR;
            case "low stock alert":
            case "stock alert":
                return WARNING_COLOR;
            case "payment received":
                return PRIMARY_COLOR;
            case "user login":
                return INFO_COLOR;
            case "new customer":
            case "customer registered":
                return SUCCESS_COLOR;
            default:
                return PRIMARY_COLOR;
        }
    }

    private void addNoDataMessage() {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel messageLabel = new JLabel("No recent activities found", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        messageLabel.setForeground(LIGHT_TEXT_COLOR);

        messagePanel.add(messageLabel, BorderLayout.CENTER);
        activitiesList1.add(messagePanel);
    }

    private void addErrorMessage() {
        JPanel errorPanelLocal = new JPanel(new BorderLayout());
        errorPanelLocal.setOpaque(false);
        errorPanelLocal.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        errorPanelLocal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel errorLabel = new JLabel("Error loading activities", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        errorLabel.setForeground(DANGER_COLOR);

        errorPanelLocal.add(errorLabel, BorderLayout.CENTER);
        activitiesList1.add(errorPanelLocal);
    }

    private void addActivityItem(String title, String desc, String time, Color color) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(true);
        item.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Set maximum size to prevent excessive height
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Color indicator
        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(4, 60));
        indicator.setBackground(color);
        indicator.setOpaque(true);

        // Content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html>" + desc + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(LIGHT_TEXT_COLOR);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        timeLabel.setForeground(LIGHT_TEXT_COLOR);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(descLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(timeLabel);

        item.add(indicator, BorderLayout.WEST);
        item.add(content, BorderLayout.CENTER);

        activitiesList1.add(item);
        activitiesList1.add(Box.createVerticalStrut(5));
    }

//    private void setupQuickActions() {
//        quickActionsPanel1.setLayout(new GridLayout(5, 2, 4, 10));
//        quickActionsPanel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        addQuickAction("Employee Management", PRIMARY_COLOR, "",
//                () -> Application.showForm(new Inventory()));
//
//        addQuickAction("Add Inventory", SUCCESS_COLOR, "",
//                () -> Application.showForm(new HR()));
//
//        addQuickAction("Generate Report", WARNING_COLOR, "",
//                () -> Application.showForm(new Reports()));
//
//    }

    private void addQuickAction(String text, Color bgColor, String iconPath, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Add action listener
        btn.addActionListener(e -> action.run());

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        try {
            btn.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        } catch (Exception e) {
            System.out.println("Icon not found: " + iconPath);
        }

        quickActionsPanel1.add(btn);
    }

    private void customizeScrollPane() {
        // Configure scroll pane policies
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Smooth scrolling configuration
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane2.getVerticalScrollBar().setBlockIncrement(64);

        // Visual styling
//        jScrollPane2.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(BORDER_COLOR, 1),
//                BorderFactory.createEmptyBorder(5, 5, 5, 5)
//        ));
        // Set proper size constraints
        jScrollPane2.setPreferredSize(new Dimension(400, 300));
        jScrollPane2.setMinimumSize(new Dimension(300, 200));

        // Configure viewport
        jScrollPane2.setOpaque(false);

        // Ensure the activitiesList1 can be scrolled
        activitiesList1.setAutoscrolls(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statspanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        activitiesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        activitiesList = new javax.swing.JPanel();
        quickActionsPanel = new javax.swing.JPanel();
        lblDate = new javax.swing.JLabel();
        chartsPanel = new javax.swing.JPanel();
        salesChartPanel = new javax.swing.JPanel();
        inventoryChartPanel = new javax.swing.JPanel();
        maincontainer = new javax.swing.JPanel();
        headerPanel1 = new javax.swing.JPanel();
        statspanel1 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        chartsPanel1 = new javax.swing.JPanel();
        salesChartPanel1 = new javax.swing.JPanel();
        inventoryChartPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        bottomPanel1 = new javax.swing.JPanel();
        activitiesPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        activitiesList1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        quickActionsPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        dateTimePanel = new javax.swing.JPanel();
        lblDate1 = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();

        statspanel.setLayout(new java.awt.GridLayout(1, 4));

        headerPanel.setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("Dashboard");
        headerPanel.add(lblTitle, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.GridLayout(1, 2));

        activitiesPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);

        activitiesList.setLayout(new javax.swing.BoxLayout(activitiesList, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(activitiesList);

        activitiesPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        bottomPanel.add(activitiesPanel);

        javax.swing.GroupLayout quickActionsPanelLayout = new javax.swing.GroupLayout(quickActionsPanel);
        quickActionsPanel.setLayout(quickActionsPanelLayout);
        quickActionsPanelLayout.setHorizontalGroup(
            quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 539, Short.MAX_VALUE)
        );
        quickActionsPanelLayout.setVerticalGroup(
            quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 188, Short.MAX_VALUE)
        );

        bottomPanel.add(quickActionsPanel);

        lblDate.setText("Today: YYY-MM-DD");

        chartsPanel.setLayout(new java.awt.GridLayout(1, 2));

        javax.swing.GroupLayout salesChartPanelLayout = new javax.swing.GroupLayout(salesChartPanel);
        salesChartPanel.setLayout(salesChartPanelLayout);
        salesChartPanelLayout.setHorizontalGroup(
            salesChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 539, Short.MAX_VALUE)
        );
        salesChartPanelLayout.setVerticalGroup(
            salesChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 265, Short.MAX_VALUE)
        );

        chartsPanel.add(salesChartPanel);

        javax.swing.GroupLayout inventoryChartPanelLayout = new javax.swing.GroupLayout(inventoryChartPanel);
        inventoryChartPanel.setLayout(inventoryChartPanelLayout);
        inventoryChartPanelLayout.setHorizontalGroup(
            inventoryChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 539, Short.MAX_VALUE)
        );
        inventoryChartPanelLayout.setVerticalGroup(
            inventoryChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 265, Short.MAX_VALUE)
        );

        chartsPanel.add(inventoryChartPanel);

        maincontainer.setLayout(new javax.swing.BoxLayout(maincontainer, javax.swing.BoxLayout.Y_AXIS));

        headerPanel1.setLayout(new java.awt.BorderLayout());
        maincontainer.add(headerPanel1);

        statspanel1.setLayout(new java.awt.GridLayout(1, 4));
        maincontainer.add(statspanel1);
        maincontainer.add(jSeparator2);

        chartsPanel1.setLayout(new java.awt.GridLayout(1, 2));

        javax.swing.GroupLayout salesChartPanel1Layout = new javax.swing.GroupLayout(salesChartPanel1);
        salesChartPanel1.setLayout(salesChartPanel1Layout);
        salesChartPanel1Layout.setHorizontalGroup(
            salesChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 624, Short.MAX_VALUE)
        );
        salesChartPanel1Layout.setVerticalGroup(
            salesChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        chartsPanel1.add(salesChartPanel1);

        javax.swing.GroupLayout inventoryChartPanel1Layout = new javax.swing.GroupLayout(inventoryChartPanel1);
        inventoryChartPanel1.setLayout(inventoryChartPanel1Layout);
        inventoryChartPanel1Layout.setHorizontalGroup(
            inventoryChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 624, Short.MAX_VALUE)
        );
        inventoryChartPanel1Layout.setVerticalGroup(
            inventoryChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        chartsPanel1.add(inventoryChartPanel1);

        maincontainer.add(chartsPanel1);

        jSeparator1.setMinimumSize(new java.awt.Dimension(0, 5));
        maincontainer.add(jSeparator1);

        bottomPanel1.setLayout(new java.awt.GridLayout(1, 0));

        activitiesPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        activitiesList1.setLayout(new javax.swing.BoxLayout(activitiesList1, javax.swing.BoxLayout.Y_AXIS));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Recent Activities");
        activitiesList1.add(jLabel1);

        jScrollPane2.setViewportView(activitiesList1);

        activitiesPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        activitiesList1.setLayout(new javax.swing.BoxLayout(activitiesList1, javax.swing.BoxLayout.Y_AXIS));
        activitiesList1.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel1.add(activitiesPanel1);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Quick Actions");

        dateTimePanel.setLayout(new java.awt.GridLayout(1, 2));

        lblDate1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblDate1.setText("Today: YYY-MM-DD");
        dateTimePanel.add(lblDate1);

        lblTime.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblTime.setText("Time: HH-mm");
        dateTimePanel.add(lblTime);

        javax.swing.GroupLayout quickActionsPanel1Layout = new javax.swing.GroupLayout(quickActionsPanel1);
        quickActionsPanel1.setLayout(quickActionsPanel1Layout);
        quickActionsPanel1Layout.setHorizontalGroup(
            quickActionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(quickActionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateTimePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
                    .addGroup(quickActionsPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        quickActionsPanel1Layout.setVerticalGroup(
            quickActionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateTimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(165, Short.MAX_VALUE))
        );

        bottomPanel1.add(quickActionsPanel1);

        maincontainer.add(bottomPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(maincontainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(maincontainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel activitiesList;
    private javax.swing.JPanel activitiesList1;
    private javax.swing.JPanel activitiesPanel;
    private javax.swing.JPanel activitiesPanel1;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel bottomPanel1;
    private javax.swing.JPanel chartsPanel;
    private javax.swing.JPanel chartsPanel1;
    private javax.swing.JPanel dateTimePanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel headerPanel1;
    private javax.swing.JPanel inventoryChartPanel;
    private javax.swing.JPanel inventoryChartPanel1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDate1;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel maincontainer;
    private javax.swing.JPanel quickActionsPanel;
    private javax.swing.JPanel quickActionsPanel1;
    private javax.swing.JPanel salesChartPanel;
    private javax.swing.JPanel salesChartPanel1;
    private javax.swing.JPanel statspanel;
    private javax.swing.JPanel statspanel1;
    // End of variables declaration//GEN-END:variables
}
