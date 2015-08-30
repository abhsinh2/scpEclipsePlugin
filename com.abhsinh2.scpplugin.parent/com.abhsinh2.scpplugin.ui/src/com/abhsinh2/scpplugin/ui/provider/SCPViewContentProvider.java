package com.abhsinh2.scpplugin.ui.provider;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.abhsinh2.scpplugin.ui.model.SCPLocationManager;
import com.abhsinh2.scpplugin.ui.model.SCPLocationManagerEvent;
import com.abhsinh2.scpplugin.ui.model.SCPLocationManagerListener;

class SCPViewContentProvider implements IStructuredContentProvider,
		SCPLocationManagerListener {
	private SCPLocationManager manager;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (manager != null)
			manager.removeLocationManagerListener(this);

		manager = (SCPLocationManager) newInput;

		if (manager != null)
			manager.addLocationManagerListener(this);
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		return manager.getLocations().toArray();
	}

	@Override
	public void locationChanged(SCPLocationManagerEvent event) {

	}
}