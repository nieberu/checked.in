package development.software.mobile.checkedin.models;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {

    private String uid;
    private String name;
    private List<String> members;
    private String owner;
    private String key;

    public Group(String uid, String name, List<String> members, String owner, String key) {
        this.uid = uid;
        this.name = name;
        this.members = members;
        this.owner = owner;
        this.key = key;
    }

    public Group(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
