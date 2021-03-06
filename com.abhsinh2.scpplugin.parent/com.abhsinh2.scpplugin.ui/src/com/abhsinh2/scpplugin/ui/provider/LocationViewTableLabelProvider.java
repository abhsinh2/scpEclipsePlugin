package com.abhsinh2.scpplugin.ui.provider;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.abhsinh2.scpplugin.ui.model.Location;

/**
 * Provides Label to View table.
 * 
 * @author abhsinh2
 * 
 */
public class LocationViewTableLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	public Image getColumnImage(Object element, int index) {
		return null;
	}

	public String getColumnText(Object element, int index) {
		Location location = (Location) element;

		switch (index) {
		case 0:
			return location.getName();
		case 1:
			return location.getRemoteLocation().getRemoteAddress();
		case 2:
			return location.getRemoteLocation().getRemoteLocation();
		default:
			return "unknown " + index;
		}
	}
}
