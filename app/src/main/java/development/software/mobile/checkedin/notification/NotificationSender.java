package development.software.mobile.checkedin.notification;

public class NotificationSender {

    private Data data;
    private String to;

    public NotificationSender(){};

    public NotificationSender(Data data, String to) {
        this.data = data;
        this.to = to;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
