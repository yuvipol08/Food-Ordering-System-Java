package com.foodordering.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/**
 * Common colours, fonts and small helper methods used by every screen,
 * so that all the forms of the application look the same.
 */
public class UITheme {

    public static final Color HEADER    = new Color(211, 84, 0);    // orange title bars
    public static final Color SIDEBAR   = new Color(52, 73, 94);    // dark grey menu strip
    public static final Color BACKGROUND = new Color(245, 245, 245);
    public static final Color BUTTON    = new Color(41, 128, 185);  // blue action buttons
    public static final Color SUCCESS   = new Color(39, 174, 96);   // green save buttons
    public static final Color DANGER    = new Color(192, 57, 43);   // red delete buttons

    public static final Font TITLE_FONT  = new Font("SansSerif", Font.BOLD, 22);
    public static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 16);
    public static final Font LABEL_FONT  = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FIELD_FONT  = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font TABLE_FONT  = new Font("SansSerif", Font.PLAIN, 13);

    /** Makes a coloured button with white text used all over the application. */
    public static JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }

    /** Orange bar with the screen name, placed at the top of every window. */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(TITLE_FONT);
        label.setOpaque(true);
        label.setBackground(HEADER);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        return label;
    }

    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }

    /** Gives every data table the same row height, fonts and header colour. */
    public static void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(24);
        table.setGridColor(new Color(210, 210, 210));
        table.setSelectionBackground(new Color(214, 234, 248));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SIDEBAR);
        table.getTableHeader().setForeground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
    }

    /** Simple grey box with a caption, used to group the fields of a form. */
    public static Border createGroupBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199)), title,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        HEADING_FONT, SIDEBAR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}
