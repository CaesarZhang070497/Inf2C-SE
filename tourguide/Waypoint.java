package tourguide;

/**
 * Created by asus on 2017/11/23.
 */
public class Waypoint{
    private String waypointIdentifier;
    private Annotation waypointAnnotation;
    private Location waypointLocation;

    public Waypoint(String waypointIdentifier, Annotation waypointAnnotation) {
        this.waypointIdentifier = waypointIdentifier;
        this.waypointAnnotation = waypointAnnotation;
    }

    public String getWaypointIdentifier() {
        return waypointIdentifier;
    }

    public Annotation getWaypointAnnotation() {
        return waypointAnnotation;
    }

    public void setWaypointLocation(Location location) {
        waypointLocation = location;
    }

    public Location getWaypointLocation() {
        return waypointLocation;
    }
}

