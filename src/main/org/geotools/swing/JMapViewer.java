/*
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geotools.swing;

import net.miginfocom.swing.MigLayout;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.swing.action.*;
import org.geotools.swing.control.JMapStatusBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * A Swing panel containing a map display pane and (optionally) a toolbar,
 * status bar and map layer table.
 * <p/>
 * This is an adaptation of {@code JMapFrame} into an embeddable JComponent.
 *
 * @author Michael Bedward
 * @author Andres Almiray
 * @see MapLayerTable
 */
public class JMapViewer extends JComponent {

    /*
    * The following toolbar button names are primarily for unit testing
    * but could also be useful for applications wanting to control appearance
    * and behaviour at run-time.
    */

    /**
     * Name assigned to toolbar button for feature info queries.
     */
    public static final String TOOLBAR_INFO_BUTTON_NAME = "ToolbarInfoButton";
    /**
     * Name assigned to toolbar button for map panning.
     */
    public static final String TOOLBAR_PAN_BUTTON_NAME = "ToolbarPanButton";
    /**
     * Name assigned to toolbar button for default pointer.
     */
    public static final String TOOLBAR_POINTER_BUTTON_NAME = "ToolbarPointerButton";
    /**
     * Name assigned to toolbar button for map reset.
     */
    public static final String TOOLBAR_RESET_BUTTON_NAME = "ToolbarResetButton";
    /**
     * Name assigned to toolbar button for map zoom in.
     */
    public static final String TOOLBAR_ZOOMIN_BUTTON_NAME = "ToolbarZoomInButton";
    /**
     * Name assigned to toolbar button for map zoom out.
     */
    public static final String TOOLBAR_ZOOMOUT_BUTTON_NAME = "ToolbarZoomOutButton";

    /**
     * Constants for available toolbar buttons used with the
     * {@link #enableTool} method.
     */
    public enum Tool {
        /**
         * Simple mouse cursor, used to unselect previous cursor tool.
         */
        POINTER,

        /**
         * The feature info cursor tool
         */
        INFO,

        /**
         * The panning cursor tool.
         */
        PAN,

        /**
         * The reset map extent cursor tool.
         */
        RESET,

        /**
         * The zoom display cursor tools.
         */
        ZOOM;
    }

    private Set<Tool> toolSet;

    /*
     * UI elements
     */
    private JMapPane mapPane;
    private MapLayerTable mapLayerTable;
    private JToolBar toolBar;
    private JMapStatusBar statusBar;

    private boolean showToolBar;
    private boolean showStatusBar;
    private boolean showLayerTable;
    private boolean uiSet;

    /**
     * Default constructor. Creates a {@code JMapViewer} with
     * no map content or renderer set
     */
    public JMapViewer() {
        this(null);
    }

    /**
     * Constructs a new {@code JMapViewer} object with specified map content.
     *
     * @param content the map content
     */
    public JMapViewer(MapContent content) {
        showLayerTable = false;
        showStatusBar = true;
        showToolBar = true;
        toolSet = EnumSet.allOf(Tool.class);

        // the map pane is the one element that is always displayed
        mapPane = new JMapPane(content);
        mapPane.setBackground(Color.WHITE);
        mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        mapPane.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                mapPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            @Override
            public void focusLost(FocusEvent e) {
                mapPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
        });

        mapPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mapPane.requestFocusInWindow();
            }
        });
    }

    /**
     * Sets whether to display the default toolbar (default is false).
     * Calling this with state == true is equivalent to
     * calling {@link #enableTool} with all {@link JMapViewer.Tool}
     * constants.
     *
     * @param enabled whether the toolbar is required
     */
    public void setShowToolBar(boolean enabled) {
        if (enabled) {
            toolSet = EnumSet.allOf(Tool.class);
        } else {
            toolSet.clear();
        }
        showToolBar = enabled;
    }

    /**
     * This method is an alternative to {@link #setShowToolBar(boolean)}.
     * It requests that a tool bar be created with specific tools, identified
     * by {@link JMapViewer.Tool} constants.
     * <p/>
     * <code><pre>
     * myMapFrame.enableTool(Tool.PAN, Tool.ZOOM);
     * </pre></code>
     * <p/>
     * Calling this method with no arguments or {@code null} is equivalent
     * to {@code enableToolBar(false)}.
     *
     * @param tool tools to display on the toolbar
     */
    public void enableTool(Tool... tool) {
        if (tool == null || tool.length == 0) {
            setShowToolBar(false);
        } else {
            toolSet = EnumSet.copyOf(Arrays.asList(tool));
            showToolBar = true;
        }
    }

    /**
     * Set whether a status bar will be displayed to display cursor position
     * and map bounds.
     *
     * @param enabled whether the status bar is required.
     */
    public void setShowStatusBar(boolean enabled) {
        showStatusBar = enabled;
    }

    /**
     * Set whether a map layer table will be displayed to show the list
     * of layers in the map content and set their order, visibility and
     * selected status.
     *
     * @param enabled whether the map layer table is required.
     */
    public void setShowLayerTable(boolean enabled) {
        showLayerTable = enabled;
    }

    @Override
    public void validate() {
        initComponents();
        super.validate();
    }

    /**
     * Creates and lays out the viewer's components that have been
     * specified with the enable methods (e.g. {@link #setShowToolBar(boolean)} ).
     * If not called explicitly by the client this method will be invoked by
     * {@link #setVisible(boolean) } when the viewer is first shown.
     */
    public void initComponents() {
        if (uiSet) {
            // @todo log a warning ?
            return;
        }

        /*
         * We use the MigLayout manager to make it easy to manually code
         * our UI design
         */
        StringBuilder sb = new StringBuilder();
        if (!toolSet.isEmpty()) {
            sb.append("[]"); // fixed size
        }
        sb.append("[grow]"); // map pane and optionally layer table fill space
        if (showStatusBar) {
            sb.append("[min!]"); // status bar height
        }

        setLayout(new MigLayout(
                "wrap 1, insets 0", // layout constrains: 1 component per row, no insets

                "[grow]", // column constraints: col grows when viewer is resized

                sb.toString()));

        /*
         * A toolbar with buttons for zooming in, zooming out,
         * panning, and resetting the map to its full extent.
         * The cursor tool buttons (zooming and panning) are put
         * in a ButtonGroup.
         *
         * Note the use of the XXXAction objects which makes constructing
         * the tool bar buttons very simple.
         */
        toolBar = new JToolBar();
        toolBar.setOrientation(JToolBar.HORIZONTAL);
        toolBar.setFloatable(false);

        JButton btn;
        ButtonGroup cursorToolGrp = new ButtonGroup();

        if (toolSet.contains(Tool.POINTER)) {
            btn = new JButton(new NoToolAction(mapPane));
            btn.setName(TOOLBAR_POINTER_BUTTON_NAME);
            toolBar.add(btn);
            cursorToolGrp.add(btn);
        }

        if (toolSet.contains(Tool.ZOOM)) {
            btn = new JButton(new ZoomInAction(mapPane));
            btn.setName(TOOLBAR_ZOOMIN_BUTTON_NAME);
            toolBar.add(btn);
            cursorToolGrp.add(btn);

            btn = new JButton(new ZoomOutAction(mapPane));
            btn.setName(TOOLBAR_ZOOMOUT_BUTTON_NAME);
            toolBar.add(btn);
            cursorToolGrp.add(btn);

            toolBar.addSeparator();
        }

        if (toolSet.contains(Tool.PAN)) {
            btn = new JButton(new PanAction(mapPane));
            btn.setName(TOOLBAR_PAN_BUTTON_NAME);
            toolBar.add(btn);
            cursorToolGrp.add(btn);

            toolBar.addSeparator();
        }

        if (toolSet.contains(Tool.INFO)) {
            btn = new JButton(new InfoAction(mapPane));
            btn.setName(TOOLBAR_INFO_BUTTON_NAME);
            toolBar.add(btn);

            toolBar.addSeparator();
        }

        if (toolSet.contains(Tool.RESET)) {
            btn = new JButton(new ResetAction(mapPane));
            btn.setName(TOOLBAR_RESET_BUTTON_NAME);
            toolBar.add(btn);
        }

        if (showToolBar) {
            add(toolBar, "grow");
        }

        mapLayerTable = new MapLayerTable(mapPane);

        /*
        * We put the map layer panel and the map pane into a JSplitPane
        * so that the user can adjust their relative sizes as needed
        * during a session. The call to setPreferredSize for the layer
        * panel has the effect of setting the initial position of the
        * JSplitPane divider
        */
        if (showLayerTable) {
            mapLayerTable.setPreferredSize(new Dimension(200, -1));
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    false,
                    mapLayerTable,
                    mapPane);
            add(splitPane, "grow");

        } else {
            /*
             * No layer table, just the map pane
             */
            add(mapPane, "grow");
        }

        statusBar = JMapStatusBar.createDefaultStatusBar(mapPane);
        if (showStatusBar) {
            add(statusBar, "grow");
        }

        uiSet = true;
    }

    /**
     * Get the map content associated with this viewer.
     * Returns {@code null} if no map content has been set explicitly with the
     * constructor or {@link #setMapContent}.
     *
     * @return the current {@code MapContent} object
     */
    public MapContent getMapContent() {
        return mapPane.getMapContent();
    }

    /**
     * Set the MapContent object used by this viewer.
     *
     * @param content the map content
     * @throws IllegalArgumentException if content is null
     */
    public void setMapContent(MapContent content) {
        if (content == null) {
            throw new IllegalArgumentException("map content must not be null");
        }

        mapPane.setMapContent(content);
    }

    /**
     * Provides access to the instance of {@code JMapPane} being used
     * by this viewer.
     *
     * @return the {@code JMapPane} object
     */
    public JMapPane getMapPane() {
        return mapPane;
    }

    public MapLayerTable getMapLayerTable() {
        return mapLayerTable;
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public JMapStatusBar getStatusBar() {
        return statusBar;
    }

    public boolean isShowToolBar() {
        return showToolBar;
    }

    public boolean isShowStatusBar() {
        return showStatusBar;
    }

    public boolean isShowLayerTable() {
        return showLayerTable;
    }

    public GTRenderer getRenderer() {
        return mapPane.getRenderer();
    }

    public void setRenderer(GTRenderer renderer) {
        mapPane.setRenderer(renderer);
    }
}
