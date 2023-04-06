// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osrm;

import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public final class OsrmPlugin extends Plugin {
    private static OsrmPlugin instance;

    public OsrmPlugin(PluginInformation info) {
        super(info);
        instance = this;
    }

    public static OsrmPlugin getInstance() {
        return instance;
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            MainApplication.getMap().addMapMode(new IconToggleButton(new OsrmRoutingMode(MainApplication.getMap())));
        }
    }
}
