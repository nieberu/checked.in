package development.software.mobile.checkedin.notification;

import java.util.HashMap;
import java.util.Map;

public class Data {

    private String title;
    private String message;
    private String type;
    private Map<String, String> additionalFields;

    public Data(){}

    public Data(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getAdditionalFields() {
        if(additionalFields == null){
            additionalFields = new HashMap<>();
        }
        return additionalFields;
    }

    public void setAdditionalFields(Map<String, String> additionalFields) {
        this.additionalFields = additionalFields;
    }

}
