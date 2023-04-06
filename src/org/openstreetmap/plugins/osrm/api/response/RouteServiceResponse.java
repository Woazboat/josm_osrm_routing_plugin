package osrm.api.response;

import java.util.List;

public class RouteServiceResponse extends BaseResponse {
    public RouteServiceResponse() {
        super();
    }

    public List<Waypoint> waypoints;
    public List<Route> routes;
}
