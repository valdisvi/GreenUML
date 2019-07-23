/* This file is part of Green.
 *
 * Copyright (C) 2005 The Research Foundation of State University of New York
 * All Rights Under Copyright Reserved, The Research Foundation of S.U.N.Y.
 * 
 * Green is free software, licensed under the terms of the Eclipse
 * Public License, version 1.0.  The license is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.buffalo.cse.green.editor.action;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.graphics.Rectangle;

import edu.buffalo.cse.green.editor.DiagramEditor;
import edu.buffalo.cse.green.editor.model.AbstractModel;
import edu.buffalo.cse.green.editor.model.RelationshipModel;

/**
 * Calculates and sets the zoom of the editor such that the diagram is zoomed in
 * as far as possible while still entirely in view.
 * 
 * @author zgwang
 */
public class ZoomFitAction extends ContextAction {

	private double heightScale, widthScale;

	/**
	 * Constructor
	 */
	public ZoomFitAction() {
		setAccelerator('a');
	}

	public double getHeightScale() {
		return heightScale;
	}

	public void setHeightScale(double heightScale) {
		this.heightScale = heightScale;
	}

	public double getWidthScale() {
		return widthScale;
	}

	public void setWidthScale(double widthScale) {
		this.widthScale = widthScale;
	}

	/**
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#doRun()
	 */
	@Override
	public void doRun() throws JavaModelException {

		// Array to hold total of 50 zoom level values


		// Determines which of the two is bigger, height or width. If the width is
		// bigger. Bigger means smaller zoom scale.
		// then max zoom-out is set to be maximum regarding width and vise versa
		// smaller zoom means u are zoomed out more, that is why smallest of two is
		// taken.
		;

		// Zoom levels start from maximum possible zoom that is determined by width or
		// height of the layout

		// sets newly acquired zoom levels according to layout changes
		setNewZoomLevels(calculateZoomLevels());

		DiagramEditor.getActiveEditor().getZoomManager().setZoom(getLayoutScale());
	}

	/**
	 * Calculates zoomLevels for current layout in the viewer.
	 * 
	 * @return double array
	 */
	public double [] calculateZoomLevels() {
		// There are 50 zoom levels available
		double[] zoomLevels = new double[50];
		
		// Zoom levels start from the level that shows the layout fully in the viewer
		double sum = getLayoutScale();
		
		// Every next zoom level zooms 0.05 times closer
		for (int i = 0; i < zoomLevels.length; i++) {
			zoomLevels[i] = sum;
			sum += 0.05;
		}
		return zoomLevels;
	}
	
	/**
	 * Looks at what is currently drawn in the viewer and calculates the zoomOut
	 * scale that needs to be applied to fit all the layout in the viewer. For
	 * example, if the height is bigger than width of the all layout then scale is
	 * calculated for height.
	 * 
	 * @return double zoom scale value
	 */
	public double getLayoutScale() {
		DiagramEditor activeEditor = DiagramEditor.getActiveEditor();
		List<AbstractModel> allModels = activeEditor.getRootModel().getChildren();
		List<RelationshipModel> allRels = activeEditor.getRootModel().getRelationships();
		Rectangle viewSize = activeEditor.getSize();

		int hMin = 99999;
		int hMax = 0;
		int vMin = 99999;
		int vMax = 0;

		// Find the min/max bound coordinates for all element boxes
		for (AbstractModel m : allModels) {
			IFigure f = activeEditor.getRootPart().getPartFromModel(m).getFigure();

			if (allRels.contains(m)) {
				// Necessary because this method of obtaining the figure's dimension
				// is inaccurate for relationships.
				continue;
			}

			Dimension dim = m.getSize();
			if (dim.height == -1 && dim.width == -1) {
				// Box is default size, need to get real size instead.
				dim = f.getLayoutManager().getPreferredSize(f.getParent(), -1, -1);
			}

			int mLeft = m.getLocation().x;
			int mRight = mLeft + dim.width;
			int mTop = m.getLocation().y;
			int mBottom = mTop + dim.height;

			if (mLeft < hMin)
				hMin = mLeft;
			if (mRight > hMax)
				hMax = mRight;
			if (mTop < vMin)
				vMin = mTop;
			if (mBottom > vMax)
				vMax = mBottom;
		}

		// Find the min/max bound coordinates for all relationships
		for (RelationshipModel rm : allRels) {
			int rmLeft = rm.getLocation().x;
			int rmRight = rmLeft + rm.getSize().width;
			int rmTop = rm.getLocation().y;
			int rmBottom = rmTop + rm.getSize().height;

			if (rmLeft < hMin)
				hMin = rmLeft;
			if (rmRight > hMax)
				hMax = rmRight;
			if (rmTop < vMin)
				vMin = rmTop;
			if (rmBottom > vMax)
				vMax = rmBottom;
		}

		if (hMin > 0 || vMin > 0) {
			for (AbstractModel m : allModels) {
				int mLeft = m.getLocation().x;
				int mTop = m.getLocation().y;
				m.setLocation(mLeft - hMin, mTop - vMin);
			}
		}

		// viewSize.width/height is the editor window size + scroll bars (17 pixels
		// wide)
		setWidthScale((double) (viewSize.width - 17) / (hMax - hMin));
		setHeightScale((double) (viewSize.height - 17) / (vMax - vMin));

		return Math.min(getWidthScale(), getHeightScale());
	}

	/**
	 * Sets ZoomManager available zoom levels to double values contained in double array.
	 * @param zoomLevels - a double array that holds all the new zoom levels
	 */
	public void setNewZoomLevels(double[] zoomLevels) {
		DiagramEditor.getActiveEditor().getZoomManager().setZoomLevels(zoomLevels);
	}

	/**
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Zoom Fit";
	}

	/**
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#getPath()
	 */
	@Override
	public Submenu getPath() {
		return Submenu.Zoom;
	}

	/**
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#getSupportedModels()
	 */
	@Override
	protected int getSupportedModels() {
		return CM_EDITOR;
	}

	/**
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

}