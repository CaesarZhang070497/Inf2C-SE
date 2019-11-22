/**
 * 
 */
package tourguide;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author pbj
 */
public class ControllerImp implements Controller {
    private static Logger logger = Logger.getLogger("tourguide");
    private static final String LS = System.lineSeparator();

    private String startBanner(String messageName) {
        return  LS 
                + "-------------------------------------------------------------" + LS
                + "MESSAGE: " + messageName + LS
                + "-------------------------------------------------------------";
    }

    Mode mode = Mode.BrowseTour;

    HashMap<String, Tour> tourList = new HashMap<>(); // this is the list of tours can be browsed or followed

    double waypointRadius;
    double waypointSeparation;

    public ControllerImp(double waypointRadius, double waypointSeparation) {
        this.waypointRadius = waypointRadius;
        this.waypointSeparation = waypointSeparation;
    }

    //--------------------------
    // Create tour mode
    //--------------------------

    // Some examples are shown below of use of logger calls.  The rest of the methods below that correspond 
    // to input messages could do with similar calls.
    int waypointIndex;
    int legIndex;
    Tour newTour;
    Waypoint lastSetWaypoint;
    
    @Override
    public Status startNewTour(String id, String title, Annotation annotation) {
        logger.fine(startBanner("startNewTour"));
        if (mode != Mode.BrowseTour) {
            return new Status.Error("Not in browse tour mode");
        }
        if (tourList.containsKey(id)) {
            return new Status.Error("Tour ID already exists");
        }
        mode = Mode.AuthorTour;
        Tour newTour = new Tour(id, title, annotation);
        waypointIndex = 0;
        legIndex = 0;

        this.newTour = newTour;
        return Status.OK;
    }

    @Override
    public Status addWaypoint(Annotation annotation) {
        logger.fine(startBanner("addWaypoint"));
        if (legIndex - waypointIndex != 1) {
            addLeg(Annotation.DEFAULT);
        }
        if (legIndex - waypointIndex != 1) {
            return new Status.Error("Two successive waypoints not allowed");
        }
        String waypointIdentifier = newTour.getTourIdentifier() + "_wp_" + waypointIndex;
        Waypoint waypoint = new Waypoint(waypointIdentifier, annotation);

        Location currentLocation = new Location(easting, northing);
        waypoint.setWaypointLocation(currentLocation);
        if (waypointIndex == 0) {
            waypointIndex ++;
            newTour.setStartWaypoint(waypoint);
            newTour.addWaypoint(waypoint);
            lastSetWaypoint = waypoint;
            return Status.OK;
        } else {
            if (!close(lastSetWaypoint, waypoint)) {
                return new Status.Error("Too close to the previous waypoint");
            }
            waypointIndex ++;
            newTour.addWaypoint(waypoint);
            lastSetWaypoint = waypoint;
            return Status.OK;
        }
    }

    @Override
    public Status addLeg(Annotation annotation) {
        logger.fine(startBanner("addLeg"));
        if (legIndex != waypointIndex) {
            return new Status.Error("Two successive legs not allowed");
        }
        String legIdentifier = newTour.getTourIdentifier() + "_leg_" + legIndex;
        Leg leg = new Leg(legIdentifier, annotation);
        legIndex ++;
        newTour.addLeg(leg);
        return Status.OK;
    }

    @Override
    public Status endNewTour() {
        logger.fine(startBanner("endNewTour"));
        if (newTour.getWaypoints().isEmpty()) {
            return new Status.Error("No waypoints");
        }
        if (newTour.getWaypoints().size() != newTour.getLegs().size()) {
            return new Status.Error("Quantity of waypoints and legs do not match");
        }
        newTour.setStages();
        if (newTour.getLastStage().getLeg() instanceof Leg) {
            return new Status.Error("The tour has no ending waypoint");
        }
        newTour.setEndWaypoint(newTour.getLastWaypoint());
        tourList.put(newTour.getTourIdentifier(), newTour);
        mode = Mode.BrowseTour;
        return Status.OK;
    }

    //--------------------------
    // Browse tours mode
    //--------------------------
    Tour browseTour;

    @Override
    public Status showTourDetails(String tourID) {
        if (!tourList.containsKey(tourID)) {
            return new Status.Error("Unable to find Tour: " + tourID);
        }
        mode = Mode.BrowseTourDetail;
        browseTour = tourList.get(tourID);
        return Status.OK;
    }
  
    @Override
    public Status showToursOverview() {
        if (mode != Mode.BrowseTour) {
            return new Status.Error("Not in browse mode");
        }
        mode = Mode.BrowseTour;
        return Status.OK;
    }

    //--------------------------
    // Follow tour mode
    //--------------------------
    Tour followTour;
    Leg currentLeg;
    Waypoint currentWaypoint;
    Waypoint nextWaypoint;
    int stage;
    List<Stage> stages;
    
    @Override
    public Status followTour(String id) {
        if (mode == Mode.AuthorTour) {
            return new Status.Error("Not in correct mode");
        }
        if (!tourList.containsKey(id)) {
            return new Status.Error("Unable to find Tour: " + id);
        }
        if (mode == Mode.BrowseTour || mode == Mode.BrowseTourDetail) {
            mode = Mode.FollowTour;
            followTour = tourList.get(id);
            stage = 0;
            stages = followTour.getStages();
            currentLeg = stages.get(stage).getLeg();
            nextWaypoint = stages.get(stage + 1).getWaypoint();
            return Status.OK;
        }
        if (mode == Mode.FollowTour) {
            if (!id.equals(followTour.getTourIdentifier())) {
                return new Status.Error("Different tour id");
            }
            if (stage == 0) {
                if (near(nextWaypoint, easting, northing)) {
                    stage ++;
                    currentWaypoint = nextWaypoint;
                    nextWaypoint = stages.get(stage + 1).getWaypoint();
                    currentLeg = stages.get(stage).getLeg();
                }
            } else if (1 < stage && stage < stages.size() - 1) {
                if (near(nextWaypoint, easting, northing)) {
                    stage ++;
                    currentWaypoint = nextWaypoint;
                    nextWaypoint = stages.get(stage + 1).getWaypoint();
                    currentLeg = stages.get(stage).getLeg();
                }
            } else if (stage == stages.size() - 1) {
                if (near(nextWaypoint, easting, northing)) {
                    currentWaypoint = nextWaypoint;
                }
            } else {
                return new Status.Error("Wrong stage index");
            }
        }
        return Status.OK;
    }

    @Override
    public Status endSelectedTour() {
        if (mode != Mode.FollowTour) {
            return new Status.Error("Not in correct mode");
        }
        mode = Mode.BrowseTour;
        getOutput();
        return Status.OK;
    }

    //--------------------------
    // Multi-mode methods
    //--------------------------
    double easting;
    double northing;
    Displacement displacement;

    public boolean close(Waypoint waypoint1, Waypoint waypoint2) {
        double easting1 = waypoint1.getWaypointLocation().getEasting();
        double easting2 = waypoint2.getWaypointLocation().getEasting();
        double northing1 = waypoint1.getWaypointLocation().getNorthing();
        double northing2 = waypoint2.getWaypointLocation().getNorthing();
        double distance = Math.sqrt(Math.pow((easting1 - easting2), 2) + Math.pow((northing1 - northing2), 2));
        if (distance > waypointSeparation && waypointSeparation > 2 * waypointRadius) {
            return false;
        }
        return true;
    }

    public boolean near(Waypoint waypoint, double easting, double northing) {
        double easting1 = waypoint.getWaypointLocation().getEasting();
        double northing1 = waypoint.getWaypointLocation().getNorthing();
        double distance = Math.sqrt(Math.pow((easting1 - easting), 2) + Math.pow((northing1 - northing), 2));
        if (distance < waypointRadius && waypointSeparation > 2 * waypointRadius) {
            return false;
        }
        return true;
    }

    @Override
    public void setLocation(double easting, double northing) {
        this.easting = easting;
        this.northing = northing;
    }

    @Override
    public List<Chunk> getOutput() {
        List<Chunk> outputs = new ArrayList<>();
        if (mode.equals(Mode.BrowseTour)) {
            Chunk.BrowseOverview output = new Chunk.BrowseOverview();
            for (String id: tourList.keySet()) {
                output.addIdAndTitle(id, tourList.get(id).getTourName());
            }
            outputs.add(output);
        } else if (mode.equals(Mode.AuthorTour)) {
            String title = newTour.getTourName();
            int legNumber = newTour.getLegs().size();
            int waypointNumber = newTour.getWaypoints().size();
            Chunk.CreateHeader output = new Chunk.CreateHeader(title, legNumber, waypointNumber);
            outputs.add(output);
        } else if (mode.equals(Mode.BrowseTourDetail)) {
            String id = browseTour.getTourIdentifier();
            String title = browseTour.getTourName();
            Annotation details = browseTour.getTourDescription();
            Chunk.BrowseDetails output = new Chunk.BrowseDetails(id, title, details);
            outputs.add(output);
        } else if (mode.equals(Mode.FollowTour)) {
            if (0 <= stage && stage < stages.size() - 1) {
                String title = followTour.getTourName();
                int numberWaypoints = followTour.getWaypoints().size();
                Chunk followHeader = new Chunk.FollowHeader(title, stage, numberWaypoints);
                outputs.add(0, followHeader);

                Annotation legAnnotation = currentLeg.getLegAnnotation();
                Chunk followLeg = new Chunk.FollowLeg(legAnnotation);
                outputs.add(1, followLeg);

                double nextEasting = nextWaypoint.getWaypointLocation().getEasting();
                double nextNorthing = nextWaypoint.getWaypointLocation().getNorthing();
                displacement = new Displacement(nextEasting - easting, nextNorthing - northing);
                Chunk followBearing = new Chunk.FollowBearing(displacement.bearing(), displacement.distance());
                outputs.add(2, followBearing);
                if (1 < stage && stage < stages.size() - 1) {
                    if (near(nextWaypoint, easting, northing)) {
                        Annotation waypointAnnotation = currentWaypoint.getWaypointAnnotation();
                        Chunk followWaypoint = new Chunk.FollowWaypoint(waypointAnnotation);
                        outputs.add(1, followWaypoint);
                    } else if (near(currentWaypoint, easting, northing)) {
                        Annotation waypointAnnotation = currentWaypoint.getWaypointAnnotation();
                        Chunk followWaypoint = new Chunk.FollowWaypoint(waypointAnnotation);
                        outputs.add(1, followWaypoint);
                    }
                }
            } else if (stage == stages.size() - 1) {
                String title = followTour.getTourName();
                int numberWaypoints = followTour.getWaypoints().size();
                Chunk followHeader = new Chunk.FollowHeader(title, stage, numberWaypoints);
                outputs.add(followHeader);

                Annotation waypointAnnotation = currentWaypoint.getWaypointAnnotation();
                Chunk followWaypoint = new Chunk.FollowWaypoint(waypointAnnotation);
                outputs.add(followWaypoint);
            }
        }
        return outputs;
    }


}
