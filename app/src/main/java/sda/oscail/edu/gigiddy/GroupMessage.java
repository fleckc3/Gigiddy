package sda.oscail.edu.gigiddy;

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
