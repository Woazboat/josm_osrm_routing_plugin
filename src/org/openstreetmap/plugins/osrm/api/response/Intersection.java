package osrm.api.response;

import java.util.List;

public class Intersection {
    public Intersection() {
        super();
    }

    public List<Double> location;
    public List<Integer> bearings;
    public List<String> classes;
    public List<String> entry;
    public Integer in;
    public Integer out;
    public List<Lane> lanes;
}
