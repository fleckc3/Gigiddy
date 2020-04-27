package sda.oscail.edu.gigiddy;

/**
 * This message model class is used in conjunction with the message adapter to get the
 * messages saved in the Firebase messages DB.
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 14/04/2020
 */
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
