package osrm.api.response;

import java.util.List;

public class Waypoint {
    public Waypoint() {}

    public String hint;
    public Double distance;
    public String name;
    public List<Double> location;
}
