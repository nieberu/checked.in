package development.software.mobile.checkedin.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String userName;
    private long phoneNumber;
    private Map<String, String> groupMap;
    private Map<String, String> checkInMap;

    public User(String uid, String firstName, String lastName, String email, String userName, long phoneNumber){
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }
    public User() {}

    public String getUid(){
        return uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Map<String, String> getGroupMap() {
        if(groupMap == null){
            groupMap = new HashMap<>();
        }
        return groupMap;
    }

    public void setGroupMap(Map<String, String> groupMap) {
        this.groupMap = groupMap;
    }

    public Map<String, String> getCheckInMap() {
        if(checkInMap == null){
            checkInMap = new HashMap<>();
        }
        return checkInMap;
    }

    public void setCheckInMap(Map<String, String> checkInMap) {
        this.checkInMap = checkInMap;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", userName='" + userName + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", groupMap=" + groupMap +
                ", checkInMap=" + checkInMap +
                '}';
    }
}
