package osrm.api.response;

import java.util.List;

public class Annotation {
    public Annotation() {}

    public List<Long> distance;
    public List<Long> duration;
    public List<Long> datasources;
    public List<Long> nodes;
    public List<Long> weight;
    public List<Double> speed;
    public AnnotationMetadata metadata;
}
