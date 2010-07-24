/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.mindmapmode.encrypt;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.encrypt.SingleDesEncrypter;
import org.freeplane.features.common.map.EncryptionModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;

@ActionLocationDescriptor(locations = { "/menu_bar/file/open" })
public class EncryptedMap extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EncryptedMap() {
		super("EncryptedMap");
	}

	public void actionPerformed(final ActionEvent e) {
		newEncryptedMap();
	}

	/**
	 * @param e 
	 */
	private StringBuilder getUsersPassword() {
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(Controller.getCurrentController().getViewController().getFrame(),
		    true);
		pwdDialog.setModal(true);
		pwdDialog.show();
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return null;
		}
		final StringBuilder password = pwdDialog.getPassword();
		return password;
	}

	/**
	 * @param e 
	 *
	 */
	private void newEncryptedMap() {
		final StringBuilder password = getUsersPassword();
		if (password == null) {
			return;
		}
		final ModeController newModeController = Controller.getCurrentModeController();
		final NodeModel node = new NodeModel(TextUtils.getText("accessories/plugins/EncryptNode.properties_select_me"),
		    null);
		final EncryptionModel encryptedMindMapNode = new EncryptionModel(node);
		encryptedMindMapNode.setEncrypter(new SingleDesEncrypter(password));
		node.addExtension(encryptedMindMapNode);
		newModeController.getMapController().newMap(node);
		encryptedMindMapNode.updateIcon();
	}
	
	@Override
	public void afterMapChange(final Object newMap) {
	}
}