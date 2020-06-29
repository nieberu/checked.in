package development.software.mobile.checkedin.models;

public class Position {
    private double latitude;
    private double longitude;

    public Position(){
        latitude = 0;
        longitude = 0;
    }

    public Position(double lat, double longi){
        latitude = lat;
        longitude = longi;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
