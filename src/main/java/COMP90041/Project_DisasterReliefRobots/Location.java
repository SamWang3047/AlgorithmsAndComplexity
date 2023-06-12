package COMP90041.Project_DisasterReliefRobots;

import java.util.ArrayList;

public class Location {

    private double latitude;
    private double longitude;
    private char latitudeDirection;
    private char longitudeDirection;
    private boolean isTrespassing;
    private String status;
    private ArrayList<Resident> residents;

    public Location(double latitude, double longitude, char latitudeDirection, char longitudeDirection, boolean isTrespassing) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.latitudeDirection = latitudeDirection;
        this.longitudeDirection = longitudeDirection;
        this.isTrespassing = isTrespassing;
        if (isTrespassing){
            status = "trespassing";
        } else {
            status = "legal";
        }
        residents = new ArrayList<>();
    }

    public Location(double latitude, double longitude, char latitudeDirection, char longitudeDirection, boolean isTrespassing, ArrayList<Resident> residents) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.latitudeDirection = latitudeDirection;
        this.longitudeDirection = longitudeDirection;
        this.isTrespassing = isTrespassing;
        if (isTrespassing){
            status = "trespassing";
        } else {
            status = "legal";
        }
        this.residents = residents;
    }
    public String getLocationInfo() {
        StringBuilder info = new StringBuilder("Location: " + latitude + ", " + longitude +
                                 "\nTrespassing: " + (status.equals("trespassing") ? "yes" : "no") + "\n");
        info.append(residents.size()).append(" Characters:\n");
        for (Resident resident : residents) {
            info.append("- ").append(resident.getDescription()).append("\n");
        }
        return info.toString();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public char getLatitudeDirection() {
        return latitudeDirection;
    }

    public void setLatitudeDirection(char latitudeDirection) {
        this.latitudeDirection = latitudeDirection;
    }

    public char getLongitudeDirection() {
        return longitudeDirection;
    }

    public void setLongitudeDirection(char longitudeDirection) {
        this.longitudeDirection = longitudeDirection;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<Resident> getResidents() {
        return residents;
    }

    public void setResidents(ArrayList<Resident> residents) {
        this.residents = residents;
    }

    public boolean isTrespassing() {
        return isTrespassing;
    }

    public void setTrespassing(boolean trespassing) {
        isTrespassing = trespassing;
    }
}
