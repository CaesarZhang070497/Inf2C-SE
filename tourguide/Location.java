package tourguide;

/**
 * Created by asus on 2017/11/23.
 */
public class Location {
    private static double easting;
    private static double northing;

    public Location(double easting, double northing) {
        this.easting = easting;
        this.northing = northing;
    }

    public double getEasting() {
        return easting;
    }

    public double getNorthing() {
        return northing;
    }
}