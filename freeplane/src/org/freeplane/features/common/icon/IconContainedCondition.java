/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
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
package org.freeplane.features.common.icon;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.util.TextUtil;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.filter.condition.JCondition;
import org.freeplane.features.common.icon.factory.IconStoreFactory;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class IconContainedCondition implements ISelectableCondition {
	
	private static final IconStore STORE = IconStoreFactory.create();
	
	static final String ICON = "ICON";
	static final String NAME = "icon_contained_condition";

	static private int iconFirstIndex(ModeController modeController, final NodeModel node, final String iconName) {		
		final List<MindIcon> icons = IconController.getIcons(modeController, node);
		for (final ListIterator<MindIcon> i = icons.listIterator(); i.hasNext();) {
			final MindIcon nextIcon = i.next();
			if (iconName.equals(nextIcon.getName())) {
				return i.previousIndex();
			}
		}
		return -1;
	}

	static private int iconLastIndex(ModeController modeController, final NodeModel node, final String iconName) {
		final List<MindIcon> icons = IconController.getIcons(modeController, node);
		final ListIterator<MindIcon> i = icons.listIterator(icons.size());
		while (i.hasPrevious()) {
			final MindIcon nextIcon = i.previous();
			if (iconName.equals(nextIcon.getName())) {
				return i.nextIndex();
			}
		}
		return -1;
	}

	private static boolean isStateIconContained(final NodeModel node, final String iconName) {
		final Collection<UIIcon> stateIcons = node.getStateIcons().values();
		for (final UIIcon stateIcon : stateIcons) {
			if (iconName.equals(stateIcon.getName())) {
				return true;
			}
		}
		return false;
	}

	static ISelectableCondition load(final XMLElement element) {
		return new IconContainedCondition(element.getAttribute(IconContainedCondition.ICON, null));
	}

	final private String iconName;

	public IconContainedCondition(final String iconName) {
		this.iconName = iconName;
	}

	public boolean checkNode(ModeController modeController, final NodeModel node) {
		return IconContainedCondition.iconFirstIndex(modeController, node, iconName) != -1
		        || IconContainedCondition.isStateIconContained(node, iconName);
	}

	private String getIconName() {
		return iconName;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public JComponent getListCellRendererComponent() {
		final JCondition component = new JCondition();
		final String text = TextUtil.getText("filter_icon") + ' ' + TextUtil.getText("filter_contains")
		        + ' ';
		component.add(new JLabel(text));
		component.add(new JLabel(STORE.getUIIcon(getIconName()).getIcon()));
		return component;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(IconContainedCondition.NAME);
		child.setAttribute(IconContainedCondition.ICON, iconName);
		element.addChild(child);
	}
	
	@Override
	public String toString() {
		return TextUtil.getText("filter_icon") + " \"" + getIconName() + "\"";
	}
}