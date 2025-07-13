import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Set;

//Block containing details of each part of a schedule
public class ScheduleBlock {
    // public final String description;
    // public final String title;
    public final String courseID;
    public final String section;
    public final ArrayList<String> instructor;
    public ArrayList<Timing> timings;

    // public ScheduleBlock(String description, String title, int courseID, String subject, String section,
    //         Timing[] timings) {
    //     this.description = description;
    //     this.title = title;
    //     this.courseID = courseID;
    //     this.subject = subject;
    //     this.section = section;
    //     this.timings = timings;
    // }

    public ScheduleBlock(String courseID, String section, ArrayList<String> instructor, ArrayList<Timing> timings) {
        this.courseID = courseID;
        this.section = section;
        this.instructor = instructor;
        this.timings = timings;
    }

    // public ScheduleBlock(Timing[] timings) {
    //     this("Class", "OOP II", 132, "CMSC", "303", timings);
    // }

    public ScheduleBlock(ArrayList<Timing> timings) {
        this("CMSC132", "303", new ArrayList<String>(Set.of("Kauffman")), timings);
    }

    public void paintMe(Graphics g) {
        for (Timing t : timings) {
            t.paintInBlock(g, this);
        }
    }

    public String getCourseID() {
        return courseID;
    }

    public String getSection() {
        return section;
    }

    public ArrayList<String> getInstructor() {
        return instructor;
    }

    public ArrayList<Timing> getTimings() {
        return timings;
    }

    public void setTimings(ArrayList<Timing> timings) {
        this.timings = timings;
    }
    
    /**
     * Get a formatted string of all instructors
     */
    public String getInstructorString() {
        if (instructor == null || instructor.isEmpty()) {
            return "TBA";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructor.size(); i++) {
            sb.append(instructor.get(i));
            if (i < instructor.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    
    /**
     * Get the total duration of all timings in hours
     */
    public double getTotalHours() {
        if (timings == null) return 0;
        double totalMinutes = 0;
        for (Timing timing : timings) {
            totalMinutes += timing.duration();
        }
        return totalMinutes / 60.0;
    }
}
