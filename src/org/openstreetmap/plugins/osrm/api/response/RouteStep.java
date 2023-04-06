package osrm.api.response;

import java.util.List;

public class RouteStep {
    public RouteStep() { }

    public Double distance;
    public Double duration;
    public String geometry;
    public String name;
    public String ref;
    public String pronunciation;
    public String mode;
    public StepManeuver maneuver;
    public List<Intersection> intersections;
    public String rotary_name;
    public String rotary_pronunciation;
}
