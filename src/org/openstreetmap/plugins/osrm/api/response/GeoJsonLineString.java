package osrm.api.response;

import java.util.List;

public class GeoJsonLineString {
    public GeoJsonLineString() {
        super();
    }

    public String type;
    public List<List<Double>> coordinates;
}
