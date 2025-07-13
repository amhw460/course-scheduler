import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

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
    
    //Setup for the slots to be used
    {
        slots = new Rectangle2D[DayOfTheWeek.values().length];
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
        boolean darker = false;

        //One-hour-size brick
        int h = (int)(minuteToPXScale * 60);

        //For every slot:
        for (Rectangle2D s : slots) {
            //Get the starting coords
            int x = (int)s.getX();
            int y = (int)s.getY();
            //get the width of the slot
            int w = (int)s.getWidth();

            //Color each set in an alternating manner
            Color mainColor = ( darker ? 
            new Color(200, 200, 200, 150) : 
            new Color(100, 100, 100, 150));
            darker = !darker;
            g2d.setStroke(new BasicStroke(3));

            //Fill corresponding rectangles up until the last
            while (y < s.getMaxY() - h) {
                g.setColor(mainColor);
                g2d.fillRoundRect(x, y, w, h, 3,3);
                g.setColor(Color.white);
                g2d.drawRoundRect(x, y, w, h, 3, 3);
                y += h;
            }
            g.setColor(mainColor);
            g2d.fillRoundRect(x, y, w, (int)(VisualizerDriver.height - lowerMargin - y), 3, 3);
            g.setColor(Color.white);
            g2d.drawRoundRect(x, y, w, (int)(VisualizerDriver.height - lowerMargin - y), 3, 3);
        }
    }

    public void paintInBlock(Graphics g, ScheduleBlock b) {
        Graphics2D g2d = (Graphics2D)g;

        //The rounded box containing it
        g.setColor(Color.red);
        Rectangle2D s = this.getScaledBlock();
        g.fillRoundRect((int)s.getX(), (int)s.getY(), (int)s.getWidth(), (int)s.getHeight(), 10, 10);
        
        //Box containing the header
        Rectangle2D headerBox = new Rectangle2D.Double(
            s.getX(), s.getY(),
            s.getWidth(), s.getHeight()
        );
        g.setColor(Color.white);
        drawCenteredText(g2d, b.subject + b.courseID, headerBox, new Font(Font.SANS_SERIF,Font.BOLD,25));
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