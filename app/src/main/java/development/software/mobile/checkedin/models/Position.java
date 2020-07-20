package development.software.mobile.checkedin.models;

public class Position {
    private double latitude;
    private double longitude;
    private double speed = 0;

    public Position(){
        latitude = 0;
        longitude = 0;
        speed = 0;
    }

    public Position(double lat, double longi, double speed){
        latitude = lat;
        longitude = longi;
        this.speed = speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getSpeed() {
        return speed;
    }
}
