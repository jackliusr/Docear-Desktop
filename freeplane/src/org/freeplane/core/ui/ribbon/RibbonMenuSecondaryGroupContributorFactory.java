package org.freeplane.core.ui.ribbon;

import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Properties;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory.RibbonActionListener;
import org.freeplane.core.ui.ribbon.RibbonMenuPrimaryContributorFactory.SecondaryEntryGroup;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class RibbonMenuSecondaryGroupContributorFactory implements IRibbonContributorFactory {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			SecondaryEntryGroup group;
			public String getKey() {
				return attributes.getProperty("name", null);
			}
			
			public void contribute(RibbonBuildContext context, IRibbonContributor parent) {
				group = new SecondaryEntryGroup(TextUtils.getRawText("ribbon.menu.group."+getKey()));
				Enumeration<?> children = context.getStructureNode(this).children();
				while(children.hasMoreElements()) {
					IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
					((IRibbonContributor)node.getUserObject()).contribute(context, this);
				}
				parent.addChild(group, null);
			}
			
			public void addChild(Object child, Object properties) {
				if(child instanceof RibbonApplicationMenuEntrySecondary) {
					group.addEntry((RibbonApplicationMenuEntrySecondary) child);
				}
				else if(child instanceof AbstractCommandButton) {
					group.addEntry(wrapButton((AbstractCommandButton) child));
				}
			}

			private RibbonApplicationMenuEntrySecondary wrapButton(AbstractCommandButton button) {
				ActionListener listener = null;
				PopupPanelCallback callback = null;
				CommandButtonKind kind = CommandButtonKind.ACTION_ONLY;
				if(button instanceof JCommandButton) {
					if(((JCommandButton) button).getPopupCallback() != null) {
						kind = (((JCommandButton) button).getCommandButtonKind());
						callback = ((JCommandButton) button).getPopupCallback();
					}
				}
				for (ActionListener l : button.getListeners(ActionListener.class)) {
					if(listener instanceof RibbonActionListener) {
						listener = l;
						break;
					}
				}
				RibbonApplicationMenuEntrySecondary entry = new RibbonApplicationMenuEntrySecondary(button.getIcon(), button.getText(), listener, kind);
				if(callback != null) {
					entry.setPopupCallback(callback);
				}
				return entry;
			}
		};
	}
	
}