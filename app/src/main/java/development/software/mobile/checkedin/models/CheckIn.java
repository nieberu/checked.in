package development.software.mobile.checkedin.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Map;

public class CheckIn implements Serializable {

    private String uid;
    private Map<String, Double> latLng;
    private String groupId;
    private String address;
    private String name;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LatLng getLatLng() {
        return new LatLng(latLng.get("latitude"), latLng.get("longitude"));
    }

    public void setLatLng(Map<String, Double> latLng) {
        this.latLng = latLng;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
