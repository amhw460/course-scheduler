import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

public class Timing {

    //TODO: Set final for now, setup to auto-update with context size later
    
    //Offset x% px from the top of the window
    public static final int upperMargin = (int)(VisualizerDriver.height * 0.1);
    //Offset x% px from the bottom of the window
    public static final int lowerMargin = (int)((VisualizerDriver.height * 0.1));
    //Leftwards Gap
    public static final double leftMargin = 30;
    //Rightwards Gap
    public static final double rightMargin = leftMargin;
    
    //The day starts at 7 A.M.
    public static final int startOfDay = toMinutes(7, 0);
    //The day ends at 8 P.M.
    public static final int endOfDay = toMinutes(8 + 12, 0);
    //Scaling between minutes and pixels
    public static final double minuteToPXScale = (VisualizerDriver.height - upperMargin - lowerMargin) / (endOfDay - startOfDay);
    
    public static Rectangle2D[] slots;
    
    // Modern color palette
    private static final Color[] courseColors = {
        new Color(52, 152, 219),   // Blue
        new Color(231, 76, 60),    // Red
        new Color(46, 204, 113),   // Green
        new Color(155, 89, 182),   // Purple
        new Color(241, 196, 15),   // Yellow
        new Color(230, 126, 34),   // Orange
        new Color(26, 188, 156),   // Teal
        new Color(142, 68, 173),   // Dark Purple
        new Color(39, 174, 96),    // Dark Green
        new Color(192, 57, 43)     // Dark Red
    };
    
    //Setup for the slots to be used
    {
        slots = new Rectangle2D[DayOfTheWeek.values().length-2];
        System.out.println("SLOTS: " + slots.length);
        double slotWidth = (VisualizerDriver.width - leftMargin - rightMargin) / slots.length;
        int allottedSpace = (VisualizerDriver.height - upperMargin - lowerMargin);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Rectangle2D.Double(
                leftMargin + i * slotWidth,
                upperMargin,
                slotWidth,
                allottedSpace
            );
        }
    }

    private DayOfTheWeek weekdate;
    private int startTime;
    private int endTime;

    public Timing(DayOfTheWeek dayoftheweek, int startTime, int endTime) {
        this.weekdate = dayoftheweek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Timing(String jsonInput) {
        int endOfDOW = jsonInput.indexOf(':');
        this.weekdate = DayOfTheWeek.valueOf(jsonInput.substring(0, endOfDOW));
        jsonInput = jsonInput.substring(endOfDOW + 2);
        String[] times = jsonInput.split(" - ");
        this.startTime = strTimeToMinutes(times[0]);
        this.endTime = strTimeToMinutes(times[1]);
    }

    public static ArrayList<Timing> of(ArrayList<String> inputs) {
        ArrayList<Timing> timings = new ArrayList<Timing>();
        for (String s : inputs) {
            timings.add(new Timing(s));
        }
        return timings;
    }

    public int strTimeToMinutes(String time) {
        String[] splits = time.split(":");
        return toMinutes(Integer.valueOf(splits[0]), 
        Integer.valueOf(splits[1]));
    }
    
    public int duration() {
        return endTime - startTime;
    }

    public String toString() {
        return displayTiming();
    }

    /**
     * @return The timing in "Weekday (HH:MM-HH:MM)" format
     */
    public String displayTiming() {
        return weekdate.name() + "(" +
        displayTime(startTime) + "-" +
        displayTime(endTime)   + ")";
    }

    /** Takes a time in minutes and turns it into HH:MM format
     * @param totalMinutes
     * @return
     */
    public static String displayTime(int totalMinutes) {
        int[] HandM = toHoursAndMinutes(totalMinutes);
        int hours = HandM[0];
        int mins = HandM[1];
        String timeOfDay = "AM";
        if (hours > 12) {
            hours -= 12;
            timeOfDay = "PM";
        }

        return String.valueOf(hours) + ":" + String.valueOf(mins) + timeOfDay;
    }

    /** Converts hours and minutes to plain out minutes
     * @param hours Number of hours
     * @param minutes Number of minutes
     * @return Sum of hours and minutes converted purely to minutes
     */
    public static int toMinutes(int hours, int minutes) {
        return hours * 60 + minutes;
    }

    /** Return the total hours and minutes specified by a certain minute time
     * @param minutes number of minutes
     * @return 2D array where int[0] = hours, int[1] = minutes
     */
    public static int[] toHoursAndMinutes(int minutes) {
        return new int[] {minutes / 60, minutes % 60};
    }

    /**
     * @return The visual block corresponding to this Timing
     */
    public Rectangle2D getScaledBlock() {
        return new Rectangle2D.Double( //Rectangle2D
            slots[this.weekdate.ordinal()].getX(), //X position of the weekday
            upperMargin + (startTime - startOfDay) * minuteToPXScale, //Go down by the upper offset and by the time, scaled to pixels
            slots[this.weekdate.ordinal()].getWidth(), //The block has the width of its weekday
            duration() * minuteToPXScale //The block is as tall as its duration scaled to pixels
        );
    }

    public static void paintSlots(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        
        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //One-hour-size brick
        int h = (int)(minuteToPXScale * 60);

        // Modern background colors
        Color[] backgroundColors = {
            new Color(248, 249, 250),  // Light gray
            new Color(241, 243, 244)   // Slightly darker gray
        };

        //For every slot:
        for (int i = 0; i < slots.length; i++) {
            Rectangle2D s = slots[i];
            //Get the starting coords
            int x = (int)s.getX();
            int y = (int)s.getY();
            //get the width of the slot
            int w = (int)s.getWidth();

            //Color each set in an alternating manner
            Color mainColor = backgroundColors[i % 2];
            g2d.setStroke(new BasicStroke(1));

            //Fill corresponding rectangles up until the last
            while (y < s.getMaxY() - h) {
                g.setColor(mainColor);
                g2d.fillRoundRect(x, y, w, h, 8, 8);
                g.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(x, y, w, h, 8, 8);
                y += h;
            }
            g.setColor(mainColor);
            g2d.fillRoundRect(x, y, w, (int)(VisualizerDriver.height - lowerMargin - y), 8, 8);
            g.setColor(new Color(220, 220, 220));
            g2d.drawRoundRect(x, y, w, (int)(VisualizerDriver.height - lowerMargin - y), 8, 8);
        }
    }

    public void paintInBlock(Graphics g, ScheduleBlock b) {
        Graphics2D g2d = (Graphics2D)g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle2D s = this.getScaledBlock();
        
        // Get color based on course ID hash
        int colorIndex = Math.abs(b.courseID.hashCode()) % courseColors.length;
        Color baseColor = courseColors[colorIndex];
        
        // Create gradient colors
        Color gradientStart = baseColor;
        Color gradientEnd = new Color(
            Math.max(0, baseColor.getRed() - 30),
            Math.max(0, baseColor.getGreen() - 30),
            Math.max(0, baseColor.getBlue() - 30)
        );
        
        // Create gradient paint
        GradientPaint gradient = new GradientPaint(
            (float)s.getX(), (float)s.getY(), gradientStart,
            (float)s.getX(), (float)(s.getY() + s.getHeight()), gradientEnd
        );
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect((int)s.getX() + 3, (int)s.getY() + 3, 
                         (int)s.getWidth(), (int)s.getHeight(), 12, 12);
        
        // Draw main block with gradient
        g2d.setPaint(gradient);
        g2d.fillRoundRect((int)s.getX(), (int)s.getY(), 
                         (int)s.getWidth(), (int)s.getHeight(), 12, 12);
        
        // Draw border
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect((int)s.getX(), (int)s.getY(), 
                         (int)s.getWidth(), (int)s.getHeight(), 12, 12);
        
        // Draw course ID text
        g2d.setColor(Color.WHITE);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 16);
        drawCenteredText(g2d, b.courseID, s, titleFont);
        
        // Draw additional information if there's enough space
        if (s.getHeight() > 60) {
            // Draw time information
            String timeText = displayTime(startTime) + " - " + displayTime(endTime);
            Font timeFont = new Font("Segoe UI", Font.PLAIN, 11);
            g2d.setColor(new Color(255, 255, 255, 200));
            
            // Create a smaller rectangle for time text
            Rectangle2D timeRect = new Rectangle2D.Double(
                s.getX() + 5, s.getY() + s.getHeight() - 35,
                s.getWidth() - 10, 15
            );
            drawCenteredText(g2d, timeText, timeRect, timeFont);
            
            // Draw instructor information if available
            if (b.getInstructorString() != null && !b.getInstructorString().equals("TBA")) {
                Font instructorFont = new Font("Segoe UI", Font.ITALIC, 10);
                g2d.setColor(new Color(255, 255, 255, 180));
                
                Rectangle2D instructorRect = new Rectangle2D.Double(
                    s.getX() + 5, s.getY() + s.getHeight() - 20,
                    s.getWidth() - 10, 15
                );
                drawCenteredText(g2d, b.getInstructorString(), instructorRect, instructorFont);
            }
        } else if (s.getHeight() > 40) {
            // Just draw time if limited space
            String timeText = displayTime(startTime) + " - " + displayTime(endTime);
            Font timeFont = new Font("Segoe UI", Font.PLAIN, 11);
            g2d.setColor(new Color(255, 255, 255, 200));
            
            Rectangle2D timeRect = new Rectangle2D.Double(
                s.getX() + 5, s.getY() + s.getHeight() - 20,
                s.getWidth() - 10, 15
            );
            drawCenteredText(g2d, timeText, timeRect, timeFont);
        }
    }   

    //W Claude 
    public void drawCenteredText(Graphics2D g2d, String text, Rectangle2D rect, Font font) {
        // Set the font
        g2d.setFont(font);
        
        // Get font metrics to calculate text dimensions
        FontMetrics metrics = g2d.getFontMetrics(font);
        
        // Calculate the x coordinate for horizontal centering
        int x = (int)(rect.getX() + (rect.getWidth() - metrics.stringWidth(text)) / 2);
        
        // Calculate the y coordinate for vertical centering
        // Note: drawString draws from the baseline, so we add ascent
        int y = (int)(rect.getY() + ((rect.getHeight() - metrics.getHeight()) / 2) + metrics.getAscent());
        
        // Draw the text
        g2d.drawString(text, x, y);
    }
}