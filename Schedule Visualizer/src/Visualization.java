import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.time.DayOfWeek;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Visualization extends JPanel {
    public ScheduleBlock[] blocks = new ScheduleBlock[1];

    {
        blocks[0] = new ScheduleBlock(new Timing[] {
            new Timing(
                DayOfTheWeek.Monday, 
                Timing.toMinutes(10, 30),
                Timing.toMinutes(11, 30)
            ),
              new Timing(
                DayOfTheWeek.Tuesday, 
                Timing.toMinutes(11, 30),
                Timing.toMinutes(12, 30)
            ),
              new Timing(
                DayOfTheWeek.Wednesday, 
                Timing.toMinutes(12, 30),
                Timing.toMinutes(13, 30)
            ),
              new Timing(
                DayOfTheWeek.Thursday, 
                Timing.toMinutes(13, 30),
                Timing.toMinutes(14, 30)
            ),
              new Timing(
                DayOfTheWeek.Friday, 
                Timing.toMinutes(14, 30),
                Timing.toMinutes(15, 30)
            ),
              new Timing(
                DayOfTheWeek.Sunday, 
                Timing.toMinutes(15, 30),
                Timing.toMinutes(16, 30)
            ),
              
        });
    }
    
    public void begin() {
        String mySchedule = VisualizerDriver.requestString(this, "What's your schedule?", "Schedule");
        
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Timing.paintSlots(g);
        for (ScheduleBlock s : blocks) {
            s.paintMe(g);
        }
        
    }

    

}

     
