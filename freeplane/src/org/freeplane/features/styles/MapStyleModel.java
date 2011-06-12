/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.styles;

import java.awt.Color;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
public class MapStyleModel implements IExtension {
    public static final IStyle DEFAULT_STYLE = new StyleNamedObject("default");
    public static final IStyle DETAILS_STYLE = new StyleNamedObject("defaultstyle.details");
    public static final IStyle NOTE_STYLE = new StyleNamedObject("defaultstyle.note");
	private Map<IStyle, NodeModel> styleNodes;
	private MapModel styleMap;
	private ConditionalStyleModel conditionalStyleModel;
	final private Map<String, String> properties;

	Map<String, String> getProperties() {
    	return properties;
    }

	public static MapStyleModel getExtension(final MapModel map) {
		return MapStyleModel.getExtension(map.getRootNode());
	}

	public MapModel getStyleMap() {
		return styleMap;
	}

	public static MapStyleModel getExtension(final NodeModel node) {
		return (MapStyleModel) node.getExtension(MapStyleModel.class);
	}

	private Color backgroundColor;

	public MapStyleModel() {
		conditionalStyleModel = new ConditionalStyleModel();
		styleNodes = new LinkedHashMap<IStyle, NodeModel>();
		properties = new LinkedHashMap<String, String>();
	}

	public ConditionalStyleModel getConditionalStyleModel() {
    	return conditionalStyleModel;
    }


	private void insertStyleMap(MapModel map, MapModel styleMap) {
	    this.styleMap = styleMap;
		final NodeModel rootNode = styleMap.getRootNode();
		createNodeStyleMap(rootNode);
		styleMap.setReadOnly(false);
		styleMap.putExtension(IUndoHandler.class, map.getExtension(IUndoHandler.class));
		final MapStyleModel defaultStyleModel = new MapStyleModel();
		defaultStyleModel.styleNodes = styleNodes;
		rootNode.putExtension(defaultStyleModel);
	}
	
	public void refreshStyles() {
		final NodeModel rootNode = styleMap.getRootNode();
		styleNodes.clear();
		createNodeStyleMap(rootNode);
    }

	void createStyleMap(final MapModel parentMap, MapStyleModel mapStyleModel, final String styleMapStr) {
		final ModeController modeController = Controller.getCurrentModeController();
		MapModel styleMap = new StyleMapModel(null);
		modeController.getMapController().fireMapCreated(styleMap);
		final MapReader mapReader = modeController.getMapController().getMapReader();
		final Reader styleReader = new StringReader(styleMapStr);
		NodeModel root;
        try {
	        root = mapReader.createNodeTreeFromXml(styleMap, styleReader, Mode.FILE);
			styleMap.setRoot(root);
			insertStyleMap(parentMap, styleMap);
			if(styleNodes.get(DEFAULT_STYLE) != null
			        && styleNodes.get(DETAILS_STYLE) != null
			        && styleNodes.get(NOTE_STYLE) != null)
			    return;
			final NodeModel predefinedStyleParentNode = getPredefinedStyleParentNode(styleMap);
            if(styleNodes.get(DEFAULT_STYLE) == null){
                final NodeModel newNode = new NodeModel(DEFAULT_STYLE, styleMap);
                predefinedStyleParentNode.insert(newNode, 0);
                addStyleNode(newNode);
            }
            if(styleNodes.get(DETAILS_STYLE) == null){
                final NodeModel newNode = new NodeModel(DETAILS_STYLE, styleMap);
                predefinedStyleParentNode.insert(newNode, 1);
                addStyleNode(newNode);
            }
            if(styleNodes.get(NOTE_STYLE) == null){
                final NodeModel newNode = new NodeModel(NOTE_STYLE, styleMap);
                predefinedStyleParentNode.insert(newNode, 2);
                addStyleNode(newNode);
            }
        }
         catch (Exception e) {
	        e.printStackTrace();
        }
	}

	private void createNodeStyleMap(final NodeModel node) {
		if (node.hasChildren()) {
			final Enumeration<NodeModel> children = node.children();
			while (children.hasMoreElements()) {
				createNodeStyleMap(children.nextElement());
			}
			return;
		}
		if (node.depth() >= 2) {
			addStyleNode(node);
		}
	}

	public void addStyleNode(final NodeModel node) {
		final IStyle userObject = (IStyle) node.getUserObject();
		styleNodes.put(userObject, node);
	}

	public void removeStyleNode(final NodeModel node) {
		final Object userObject = node.getUserObject();
		styleNodes.remove(userObject);
	}

    public NodeModel getStyleNodeSafe(final IStyle style) {
        final NodeModel node = getStyleNode(style);
        if(node != null)
            return node;
        return  getStyleNode(DEFAULT_STYLE);
    }
    public NodeModel getStyleNode(final IStyle style) {
		if(style instanceof StyleNode){
			return ((StyleNode)style).getNode();
		}
		final NodeModel node = styleNodes.get(style);
        return node;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Set<IStyle> getStyles() {
		return styleNodes.keySet();
	}

	private float zoom = 1f;

	public float getZoom() {
		return zoom;
	}

	public MapViewLayout getMapViewLayout() {
		return mapViewLayout;
	}

	void setMapViewLayout(final MapViewLayout mapViewLayout) {
		this.mapViewLayout = mapViewLayout;
	}

	void setZoom(final float zoom) {
		this.zoom = zoom;
	}

	private MapViewLayout mapViewLayout = MapViewLayout.MAP;
	private int maxNodeWidth = MapStyleModel.getDefaultMaxNodeWidth();

	public int getMaxNodeWidth() {
		return maxNodeWidth;
	}

	public void setMaxNodeWidth(final int maxNodeWidth) {
		this.maxNodeWidth = maxNodeWidth;
	}

	static int getDefaultMaxNodeWidth() {
		try {
			return Integer.parseInt(ResourceController.getResourceController().getProperty("max_node_width"));
		}
		catch (final NumberFormatException e) {
			return Integer.parseInt(ResourceController.getResourceController().getProperty(
			    "el__max_default_window_width")) * 2 / 3;
		}
	}

	void copyFrom(MapStyleModel source, boolean overwrite) {
		if(overwrite && source.styleMap != null  || styleMap == null){
			styleMap = source.styleMap;
			styleNodes = source.styleNodes;
			conditionalStyleModel = source.conditionalStyleModel;
		}
		if(overwrite && source.backgroundColor != null|| backgroundColor == null){
			backgroundColor = source.backgroundColor;
		}
		if(overwrite){
			maxNodeWidth = source.maxNodeWidth;
		}
    }
	
	public void setProperty(String key, String value){
		if (value != null){ 
			properties.put(key, value);
		}
		else{
			properties.remove(key);
		}
	}
	
	public String getProperty(String key){
		return properties.get(key);
	}

    NodeModel getPredefinedStyleParentNode(final MapModel styleMap) {
        final String group = "styles.predefined";
        return getStyleNodeGroup(styleMap, group);
    }
    
    public NodeModel getUserStyleParentNode(final MapModel styleMap) {
        final String group = "styles.user-defined";
        return getStyleNodeGroup(styleMap, group);
    }

    private NodeModel getStyleNodeGroup(final MapModel styleMap, final String group) {
        final NodeModel rootNode = styleMap.getRootNode();
        final int childCount = rootNode.getChildCount();
        for(int i = 0; i < childCount; i++){
            final NodeModel childNode = (NodeModel) rootNode.getChildAt(i);
            final StyleNamedObject userObject = (StyleNamedObject) childNode.getUserObject();
            if(userObject.getObject().equals(group)){
                return childNode;
            }
        }
        throw new NoSuchElementException();
    }
}