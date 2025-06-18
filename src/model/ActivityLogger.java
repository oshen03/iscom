/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.awt.Color;
/**
 *
 * @author Oshen Sathsara <oshensathsara2003@gmail.com>
 */
public class ActivityLogger {


        public enum ActivityType {
            ORDER_CREATED("New Order", new Color(0, 122, 204)),
            REPAIR_COMPLETED("Repair Completed", new Color(40, 167, 69)),
            STOCK_ALERT("Low Stock Alert", new Color(255, 193, 7)),
            PAYMENT_RECEIVED("Payment Received", new Color(0, 122, 204)),
            USER_LOGIN("User Login", new Color(23, 162, 184)),
            INVENTORY_UPDATED("Inventory Updated", new Color(40, 167, 69)),
            CUSTOMER_REGISTERED("New Customer", new Color(0, 122, 204));

            private final String displayName;
            private final Color color;

            ActivityType(String displayName, Color color) {
                this.displayName = displayName;
                this.color = color;
            }

            public String getDisplayName() {
                return displayName;
            }

            public Color getColor() {
                return color;
            }
        }


    }
