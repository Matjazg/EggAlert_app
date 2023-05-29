package si.uni_lj.fe.tnuv.eggalert_v1.Notifications;

public class Notifications {
    private String source;
    private String event;
    private String message;
    private String timestamp;

    public Notifications(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public Notifications(String source, String event, String message, String timestamp) {
        this.source = source;
        this.event = event;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
