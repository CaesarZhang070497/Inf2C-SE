package tourguide;

/**
 * Created by asus on 2017/11/23.
 */
public class Stage {
    private int stage;
    private Waypoint waypoint;
    private Leg leg;

    public Stage(int stage) {
        this.stage = stage;
    }

    public int getIndex() {
        return stage;
    }

    public void setWaypoint(Waypoint waypoint) {
        this.waypoint = waypoint;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public void setLeg(Leg leg) {
        this.leg = leg;
    }

    public Leg getLeg() {
        return leg;
    }
}
