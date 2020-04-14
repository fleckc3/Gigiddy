package sda.oscail.edu.gigiddy;

// ref: https://www.youtube.com/watch?v=n8QWeqeUeA0&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=50
public class Messages {

    private String from, message, type;

    public Messages(String from, String message, String type) {
        this.from = from;
        this.message = message;
        this.type = type;
    }


    public Messages() {
        //empty default constructor
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
