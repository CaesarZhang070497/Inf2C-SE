package tourguide;

/**
 * Created by asus on 2017/11/22.
 */
import java.util.ArrayList;
import java.util.List;

public class Tour {
    private String tourIdentifier;
    private String tourName;
    private Annotation tourDescription;
    private Waypoint startWaypoint;
    private Waypoint endWaypoint;

    private List<Waypoint> waypoints = new ArrayList<>();
    private List<Leg> legs = new ArrayList<>();
    private List<Stage> stages = new ArrayList<>();

    public Tour(String tourIdentifier, String tourName, Annotation tourDescription) {
        this.tourIdentifier = tourIdentifier;
        this.tourName = tourName;
        this.tourDescription = tourDescription;
    }

    public String getTourIdentifier() {
        return tourIdentifier;
    }

    public String getTourName() {
        return tourName;
    }

    public Annotation getTourDescription() {
        return tourDescription;
    }

    public void setStartWaypoint(Waypoint waypoint) {
        startWaypoint = waypoint;
    }

    public Waypoint getStartWaypoint() {
        return startWaypoint;
    }

    public void setEndWaypoint(Waypoint waypoint) {
        endWaypoint = waypoint;
    }

    public Waypoint getEndWaypoint() {
        return endWaypoint;
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void addLeg(Leg leg) {
        legs.add(leg);
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setStages() {
        int n = waypoints.size();
        Stage stageStart = new Stage(0);
        stageStart.setLeg(legs.get(0));
        stages.add(stageStart);
        if (waypoints.size() > 1 && legs.size() > 1) {
            for (int count = 1; count < n; count ++) {
                Stage stage = new Stage(count);
                stage.setWaypoint(waypoints.get(count - 1));
                stage.setLeg(legs.get(count));
            }
        }
        Stage stageEnd = new Stage(n);
        stageEnd.setWaypoint(waypoints.get(n - 1));
        stages.add(stageEnd);
    }

    public List<Stage> getStages() {
        return stages;
    }

    public Stage getLastStage() {
        int last = stages.size();
        return stages.get(last - 1);
    }

    public Waypoint getLastWaypoint() {
        int last = waypoints.size();
        return waypoints.get(last - 1);
    }


}
