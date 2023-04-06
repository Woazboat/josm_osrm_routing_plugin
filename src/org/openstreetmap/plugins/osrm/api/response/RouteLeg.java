package osrm.api.response;

import java.util.List;

public class RouteLeg {
    public RouteLeg() { }

    public Double distance;
    public Double duration;
    public Double weight;
    public String summary;
    public List<RouteStep> steps;
    public Annotation annotation;
}
