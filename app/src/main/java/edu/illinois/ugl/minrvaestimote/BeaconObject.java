package edu.illinois.ugl.minrvaestimote;

public class BeaconObject {

    private String uuid;
    private int major;
    private int minor;
    private double x, y, z;
    private String description;

    public BeaconObject(String uuid, int major, int minor, double x, double y, double z, String description) {
        this.uuid        = uuid;
        this.major       = major;
        this.minor       = minor;
        this.x           = x;
        this.y           = y;
        this.z           = z;
        this.description = description;
    }

    public String getUUID() {
        return uuid;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getDescription() {
        return description;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getUUID() + " " + getMajor() + " " + getMinor() + " " + getX() + " " + getY() + " " +
                getZ() + " " + getDescription();
    }
}
