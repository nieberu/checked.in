package development.software.mobile.checkedin.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String userName;
    private long phoneNumber;
    private List<String> groupNames;

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

    public List<String> getGroupNames() {
        if(groupNames == null){
            groupNames = new ArrayList<>();
        }
        return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
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
                '}';
    }
}
