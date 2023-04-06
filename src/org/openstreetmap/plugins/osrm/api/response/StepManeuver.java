package osrm.api.response;

import java.util.List;

public class StepManeuver {
    public StepManeuver() {
        super();
    }

    public List<Double> location;
    public Integer bearing_before;
    public Integer bearing_after;
    public String type;
    public String modifier;
    public Integer exit;
}
