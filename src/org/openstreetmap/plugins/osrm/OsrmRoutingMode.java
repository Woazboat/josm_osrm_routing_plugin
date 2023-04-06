// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osrm;

import static org.openstreetmap.josm.tools.I18n.tr;

// import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URI;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.util.KeyPressReleaseListener;
import org.openstreetmap.josm.gui.util.ModifierExListener;
import org.openstreetmap.josm.gui.util.HighlightHelper;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import osrm.api.response.Route;
import osrm.api.response.RouteLeg;
import osrm.api.response.RouteServiceResponse;

/**
 * OSRM routing mode.
 */
public class OsrmRoutingMode extends MapMode implements MapViewPaintable, /* KeyPressReleaseListener, */ ModifierExListener {

    private MapView mv;

    private final Cursor cursorDraw;

    private List<LatLon> waypoints = new ArrayList<>();
    private List<LatLon> routeResult = new ArrayList<>();
    HighlightHelper highlightHelper = new HighlightHelper();

    public OsrmRoutingMode(MapFrame mapFrame) {
        super(tr("OSRM Routing"), "turbopen.png", tr("OSRM routing mode"), 
        Shortcut.registerShortcut("mapmode:osrm_routing", tr("Mode: {0}", tr("OSRM routing mode")), KeyEvent.VK_R, Shortcut.SHIFT),
        ImageProvider.getCursor("crosshair", null));

        cursorDraw = ImageProvider.getCursor("crosshair", null);
    }

    @Override
    public void enterMode() {
        if (!isEnabled())
            return;
        super.enterMode();

        waypoints.clear();

        MapFrame map = MainApplication.getMap();
        // eps = settings.startingEps;
        mv = map.mapView;
        // line.setMv(mv);

        // if (getLayerManager().getEditDataSet() == null) return;

        map.mapView.addMouseListener(this);
        map.mapView.addMouseMotionListener(this);
        map.mapView.addTemporaryLayer(this);

        // map.keyDetector.addKeyListener(this);
        map.keyDetector.addModifierExListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MapFrame map = MainApplication.getMap();

        map.mapView.removeMouseListener(this);
        map.mapView.removeMouseMotionListener(this);

        map.mapView.removeTemporaryLayer(this);

        // map.keyDetector.removeKeyListener(this);
        map.keyDetector.removeModifierExListener(this);

        // settings.savePrefs();
        // map.mapView.setCursor(cursorDraw);
        repaint();
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return isEditableDataLayer(l);
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditLayer() != null);
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color waypointColor = Color.BLUE;
        Color routeColor = Color.RED;

        Stroke waypointStroke = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{10f}, 5f);
        Stroke routeStroke = new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        g.setColor(waypointColor);
        g.setStroke(waypointStroke);
        if (waypoints.size() > 1) {
            GeneralPath waypointPath = new GeneralPath();

            boolean first = true;
            for (LatLon latLon : waypoints) {
                Point p = mv.getPoint(latLon);
                if (first) {
                    waypointPath.moveTo(p.x, p.y);
                    first = false;
                } else {
                    waypointPath.lineTo(p.x, p.y);
                }
                
            }

            // waypointPath.closePath();

            g.draw(waypointPath);

            // Iterator<LatLon> it1, it2;
            // it1 = waypoints.listIterator(0);
            // it2 = waypoints.listIterator(1);
            // Point p1, p2;
            // LatLon pp1, pp2;
            // for (int i = 0; i < waypoints.size() - 1; i++) {
            //     pp1 = it1.next();
            //     p1 = mv.getPoint(pp1);
            //     pp2 = it2.next();
            //     p2 = mv.getPoint(pp2);

            //     g.drawLine(p1.x, p1.y, p2.x, p2.y);
            // }
        }

        Point mp = mv.getMousePosition();
        if (mp != null && waypoints.size() > 0) {
            Point lp = mv.getPoint(waypoints.get(waypoints.size()-1));
            g.drawLine(lp.x, lp.y, mp.x, mp.y);
        }

        if (routeResult.size() > 2) {
            g.setColor(routeColor);
            g.setStroke(routeStroke);

            GeneralPath routePath = new GeneralPath();

            boolean first = true;
            for (LatLon latLon : routeResult) {
                Point p = mv.getPoint(latLon);
                if (first) {
                    routePath.moveTo(p.x, p.y);
                    first = false;
                } else {
                    routePath.lineTo(p.x, p.y);
                }
            }

            // routePath.closePath();

            g.draw(routePath);

            // Iterator<LatLon> it1, it2;
            // it1 = routeResult.listIterator(0);
            // it2 = routeResult.listIterator(1);
            // Point p1, p2;
            // LatLon pp1, pp2;
            // for (int i = 0; i < routeResult.size() - 1; i++) {
            //     pp1 = it1.next();
            //     p1 = mv.getPoint(pp1);
            //     pp2 = it2.next();
            //     p2 = mv.getPoint(pp2);

            //     g.drawLine(p1.x, p1.y, p2.x, p2.y);
            // }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isEnabled()) return;
        if (e.getButton() != MouseEvent.BUTTON1) return;
        updateKeyModifiers(e);

        requestFocusInMapView();
        
        waypoints.add(getLatLon(e));
        Logging.info("Routing waypoints {0}", waypoints);
        if (waypoints.size() > 1) {
            getRoute("car", waypoints);
        }
    }

    private void getRoute(String profile, List<LatLon> waypoints) {

        routeResult.clear();
        highlightHelper.clear();

        boolean first = true;
        StringBuilder coordinates = new StringBuilder();
        for (LatLon latLon : waypoints) {
            if (!first) {
                coordinates.append(";");
            }
            coordinates.append(latLon.lon());
            coordinates.append(",");
            coordinates.append(latLon.lat());
            first = false;
        }

        String url_string = String.format("http://router.project-osrm.org/route/v1/%s/%s?geometries=geojson&overview=full&annotations=true", profile, coordinates);
        Logging.info("OSRM request url {0}", url_string);
		// HttpClient httpClient = HttpClients.createDefault();
		// JSONObject result = null;
		try {
            URL url = new URL(url_string);
            URLConnection con = url.openConnection();
            con.setRequestProperty("accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();

            JsonParser parser = mapper.getFactory().createParser(new BufferedInputStream(con.getInputStream()));
            
            // ObjectNode node = mapper.readTree(parser);

            // Logging.info("OSRM response: {0}", node);

            RouteServiceResponse res = mapper.readValue(parser, RouteServiceResponse.class);

            Logging.info("OSRM parsed response: {0}", res);

            DataSet ds = getLayerManager().getActiveDataSet();

            if (res.routes != null) {
                for (Route route : res.routes) {
                    if (route.legs != null) {
                        for (RouteLeg leg : route.legs) {
                            if (leg.annotation != null && leg.annotation.nodes != null) {
                                for (Long nodeId : leg.annotation.nodes) {
                                    Logging.info("Route passes node: {0}", nodeId.longValue());
                                    if (ds != null) {
                                        OsmPrimitive n = ds.getPrimitiveById(nodeId.longValue(), OsmPrimitiveType.NODE);
                                        if (n != null) {
                                            highlightHelper.setHighlight(n, true);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Logging.info("Geometry: {0}, type: {1}, coordinates: {2}", route.geometry, route.geometry.type, route.geometry.coordinates);
                    if (route.geometry != null && route.geometry.type != null && route.geometry.type.equals("LineString")) {
                        Logging.info("Have LineString geometry");
                        if (route.geometry.coordinates != null) {
                            for (List<Double> coords : route.geometry.coordinates) {
                                Logging.info("Route coords: {0}", coords);
                                if (coords.size() == 2) {
                                    LatLon latLon = new LatLon(coords.get(1), coords.get(0));
                                    Logging.info("Route LatLon: {0}", latLon);
                                    routeResult.add(latLon);
                                }
                            }
                        }                        
                    }
                }
            }
            
            
		} catch (Exception e){
            Logging.warn("Error while fetching route from OSRM: {0}", e);
		}
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateKeyModifiers(e);
        repaint();
    }

/*     @Override
    public void doKeyPressed(KeyEvent e) {
    }

    @Override
    public void doKeyReleased(KeyEvent keyEvent) {
    } */

    private void repaint() {
        MainApplication.getMap().mapView.repaint();
    }

    @Override
    public void modifiersExChanged(int modifiers) {
        updateKeyModifiersEx(modifiers);
        // updateCursor();
    }

    LatLon getLatLon(MouseEvent e) {
        return mv.getLatLon(e.getX(), e.getY());
    }
}
