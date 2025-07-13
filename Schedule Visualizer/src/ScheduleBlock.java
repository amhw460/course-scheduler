import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.time.DayOfWeek;

//Block containing details of each part of a schedule
public class ScheduleBlock {
    public final String description;
    public final String title;
    public final int courseID;
    public final String subject;
    public final String section;
    public final Timing[] timings;

    

    public ScheduleBlock(String description, String title, int courseID, String subject, String section,
            Timing[] timings) {
        this.description = description;
        this.title = title;
        this.courseID = courseID;
        this.subject = subject;
        this.section = section;
        this.timings = timings;
    }

    public ScheduleBlock(Timing[] timings) {
        this("Class", "OOP II", 132, "CMSC", "303", timings);
    }

    public void paintMe(Graphics g) {
        for (Timing t : timings) {
            t.paintInBlock(g, this);
        }
    }

    

    
}
