import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Visualization extends JPanel {
    public ArrayList<ScheduleBlock> blocks = new ArrayList<ScheduleBlock>();
    
    public Visualization(JSONReader reader) {
        ArrayList<Object> bloques = (ArrayList<Object>)(reader.parse());
        for (Object map : bloques) {
            HashMap myMap = (HashMap)map;
            blocks.add(new ScheduleBlock(
                (String)myMap.get("Course"),
                (String)myMap.get("ID"),
                (ArrayList<String>)myMap.get("Instructor(s)"),
                Timing.of((ArrayList<String>)myMap.get("Times")))
            );
        }
    }
    
    public void begin() {
        // String mySchedule = VisualizerDriver.requestString(this, "What's your schedule?", "Schedule");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw modern background
        drawBackground(g2d);
        
        // Draw title
        drawTitle(g2d);
        
        // Draw day headers
        drawDayHeaders(g2d);
        
        // Draw time labels
        drawTimeLabels(g2d);
        
        // Draw the schedule grid
        Timing.paintSlots(g);
        
        // Draw schedule blocks
        for (ScheduleBlock s : blocks) {
            s.paintMe(g);
        }
    }
    
    private void drawBackground(Graphics2D g2d) {
        // Create a subtle gradient background
        Color topColor = new Color(245, 247, 250);
        Color bottomColor = new Color(235, 237, 240);
        
        g2d.setColor(topColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    private void drawTitle(Graphics2D g2d) {
        String title = "Course Schedule";
        Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
        g2d.setFont(titleFont);
        
        FontMetrics metrics = g2d.getFontMetrics(titleFont);
        int titleX = (getWidth() - metrics.stringWidth(title)) / 2;
        int titleY = 40;
        
        // Draw title shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.drawString(title, titleX + 2, titleY + 2);
        
        // Draw title
        g2d.setColor(new Color(44, 62, 80));
        g2d.drawString(title, titleX, titleY);
    }
    
    private void drawDayHeaders(Graphics2D g2d) {
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
        g2d.setFont(headerFont);
        
        double slotWidth = (VisualizerDriver.width - Timing.leftMargin - Timing.rightMargin) / dayNames.length;
        
        for (int i = 0; i < dayNames.length; i++) {
            double x = Timing.leftMargin + i * slotWidth;
            double y = Timing.upperMargin - 30;
            
            // Draw header background
            g2d.setColor(new Color(52, 73, 94, 200));
            g2d.fillRoundRect((int)x, (int)y, (int)slotWidth, 25, 8, 8);
            
            // Draw header text
            g2d.setColor(Color.WHITE);
            FontMetrics metrics = g2d.getFontMetrics(headerFont);
            int textX = (int)(x + (slotWidth - metrics.stringWidth(dayNames[i])) / 2);
            int textY = (int)(y + 17);
            g2d.drawString(dayNames[i], textX, textY);
        }
    }
    
    private void drawTimeLabels(Graphics2D g2d) {
        Font timeFont = new Font("Segoe UI", Font.PLAIN, 12);
        g2d.setFont(timeFont);
        g2d.setColor(new Color(100, 100, 100));
        
        int hourHeight = (int)(Timing.minuteToPXScale * 60);
        
        for (int hour = 8; hour <= 13; hour++) {
            int y = Timing.upperMargin + (hour - 8) * hourHeight;
            String timeText = String.format("%d:00", hour > 12 ? hour - 12 : hour) + (hour >= 12 ? " PM" : " AM");
            
            // Draw time label
            g2d.drawString(timeText, 10, y + 15);
            
            // Draw horizontal line
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.drawLine((int)Timing.leftMargin, y, getWidth() - 20, y);
        }
    }
}

     
