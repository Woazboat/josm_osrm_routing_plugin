package osrm.api.response;

import java.util.List;

public class Route {
    public Route() { }

    public Double distance;
    public Double duration;
    public Double weight;
    public String weight_name;
    public List<RouteLeg> legs;
    public GeoJsonLineString geometry;
}
