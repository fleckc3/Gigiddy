package sda.oscail.edu.gigiddy;

/**
 * The GroupMessage model class is used by the group message activity and the GroupMessageAdapter.class to
 * get the group message data and displaye it properly in the group message activity
 *   - Adapted from: https://www.youtube.com/playlist?list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 07/04/2020
 *

 */
public class GroupMessage {

    private String date, ID, message, name, time;

    public GroupMessage(String date, String ID, String message, String name, String time) {
        this.date = date;
        this.ID = ID;
        this.message = message;
        this.name = name;
        this.time = time;

    }

    public GroupMessage() {
        // empty constructor required
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getID() { return ID; }

    public void setID(String ID) { this.ID = ID; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
