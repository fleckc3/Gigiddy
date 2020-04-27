package sda.oscail.edu.gigiddy;

/**
 * Contacts Model class helps the firebase adapters and other recycler views with access data models from the DB.
 *   - Adapted from: https://www.youtube.com/playlist?list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj
 *
 * @author Colin Fleck <colin.fleck@mail.dcu.ie>
 * @version 1.0
 * @since 07/04/2020
 */
public class Contacts {

    public String image, name, status;

    public Contacts() {
        // empty constructor required
    }

    public Contacts(String image, String name, String status) {
        this.image = image;
        this.name = name;
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
