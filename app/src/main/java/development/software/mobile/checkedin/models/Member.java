package development.software.mobile.checkedin.models;

import java.io.Serializable;

public class Member implements Serializable {

    private String uid;

    private String email;

    private String type;

    public Member(){};

    public Member(String uid, String email, String type) {
        this.uid = uid;
        this.email = email;
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
