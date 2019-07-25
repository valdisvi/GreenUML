/* This file is part of Green.
 *
 * Copyright (C) 2005 The Research Foundation of State University of New York
 * All Rights Under Copyright Reserved, The Research Foundation of S.U.N.Y.
 * 
 * Green is free software, licensed under the terms of the Eclipse
 * Public License, version 1.0.  The license is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * 
 */
package edu.buffalo.cse.green.editor.model.commands;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.emf.common.util.Reflect;
import org.eclipse.gef.commands.Command;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import ccvisu.GraphData;
import ccvisu.GraphEdge;
import ccvisu.Minimizer;
import ccvisu.MinimizerBarnesHut;
import ccvisu.Options;
import edu.buffalo.cse.green.ccvisu.CCVisuUtil;
import edu.buffalo.cse.green.ccvisu.GraphVertex;
import edu.buffalo.cse.green.editor.DiagramEditor;
import edu.buffalo.cse.green.editor.model.AbstractModel;
import edu.buffalo.cse.green.editor.model.CompartmentModel;
import edu.buffalo.cse.green.editor.model.FieldModel;
import edu.buffalo.cse.green.editor.model.MethodModel;
import edu.buffalo.cse.green.editor.model.RelationshipModel;
import edu.buffalo.cse.green.editor.model.RootModel;
import edu.buffalo.cse.green.editor.model.TypeModel;
import sun.reflect.Reflection;

/**
 * @author zgwang
 * 
 */
public class ArrangeTreeCommand extends Command {

	private int[][] opos;
	private int[][] npos;
	private Vector<TypeModel> _m;

	/**
	 * 
	 */
	public ArrangeTreeCommand() {
		_m = new Vector<TypeModel>();
	}

	@Override
	public void undo() {
		for (int i = 0; i < _m.size(); i++)
			_m.get(i).setLocation(opos[i][0], opos[i][1]);
	}

	@Override
	public void redo() {
		for (int i = 0; i < _m.size(); i++)
			_m.get(i).setLocation(npos[i][0], npos[i][1]);
	}

	/*
	 * This method creates a diagram suited for tree-like structures by drawing
	 * trees one by one.
	 */
	@Override
	public void execute() {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		RootModel root = editor.getRootModel();
		List<AbstractModel> children = root.getChildren();
		int maxLevel = getMaxLevel(children);

		// tops contains the classes that do not extend any other class shown in the
		// diagram
		List<TypeModel> tops = new ArrayList<>();

		// xGlobalOffset provides spacing between drawn trees
		int xGlobalOffset = 0;

		for (AbstractModel mod : children) {
			if (mod instanceof TypeModel) {
				try {
					if ((((TypeModel) mod).getType().getSuperclassName()) == null) {
						tops.add((TypeModel) mod);
					}
				} catch (JavaModelException e) {
				}
			}
		}

		// draws a tree for each class in tops
		for (AbstractModel t : tops) {
			if (t instanceof TypeModel) {

				xGlobalOffset = drawTreeCentered((TypeModel) t, maxLevel, xGlobalOffset);
			}
		}

		editor.checkDirty();

		// Simulate a key press for key "a", to call AutoFitACtion and show all layout
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_A);
			robot.keyRelease(KeyEvent.VK_A);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Draws a tree hierarchy and returns int value that shows the right bound of
	 * the layout where this tree ends. This value is used for the next tree to be
	 * drawn next to the one already drawn.
	 * 
	 * @param top
	 *            - the root model for tree
	 * @param maxLayoutLevel
	 *            - max level of the hierarchy
	 * @param xGlobalOffset
	 *            - x coordinate from which to start drawing
	 * @return int
	 */
	public int drawTreeCentered(TypeModel top, int maxLayoutLevel, int xGlobalOffset) {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		List<AbstractModel> allModels = getAllModels(top);

		TypeModel prevModel = null, currModel;

		for (AbstractModel m : allModels) {

			if (m instanceof TypeModel) {
				int level = getLevel((TypeModel) m);
				currModel = (TypeModel) m;
				int currModelIncomingEdges = currModel.getIncomingEdges().size();
				int lvl = maxLayoutLevel - level;

				// Set size for each TypeModel
				m.setSize(200, 100);

				// Check if previous TypeModels parent is the same as the current ones
				// prevModel != null is because of the first TypeModel in the list, it has no
				// previous model recorded
				if (getParentTypeModel(prevModel) == getParentTypeModel(currModel) && prevModel != null) {

					// If it is, set its location next to his brother with 50 pixels white space on
					// the same level
					currModel.setLocation(xGlobalOffset, prevModel.getBounds().y);

					// Remember TypeModels x axis that is the most to the right in the layout
					if (currModel.getBounds().x >= xGlobalOffset) {

						xGlobalOffset = currModel.getBounds().x + 250;

					}

					// If TypeModel has more than 2 children, it needs to be drawn on his own level,
					// but so that it would be in the middle of his children regarding x axis
				} else if (currModelIncomingEdges > 1) {
					TypeModel[] childrenArray = new TypeModel[currModelIncomingEdges];

					// Array holds currentModel children TypeModels
					childrenArray = getChildrenTypeModels(currModel);

					// x axis for the most left child
					int xLeft = childrenArray[0].getBounds().x;

					// x axis for the most right child
					int xRight = childrenArray[childrenArray.length - 1].getBounds().x;

					// 200 is added, because 200 is width of the box
					// divided with 2, because parent needs to be in the middle of the children
					// minus 100 because the box x coordinate is for upper left corner, so if we
					// put this corner int the middle, we need to push it back to the left 200/2
					currModel.setLocation(xLeft + ((xRight - xLeft + 200) / 2) - 100, lvl * (-150));

					// If TypeModel has 1 children, it needs to be drawn right on top of it
				} else if (currModelIncomingEdges == 1) {
					currModel.setLocation(prevModel.getBounds().x, lvl * (-150));

					if (currModel.getBounds().x >= xGlobalOffset) {

						xGlobalOffset = currModel.getBounds().x + 250;

					}

					// If TypeModel has no children
				} else if (currModelIncomingEdges == 0) {

					currModel.setLocation(xGlobalOffset, lvl * (-150));
					if (currModel.getBounds().x >= xGlobalOffset) {

						xGlobalOffset = currModel.getBounds().x + 250;

					}

				} else {
					currModel.setLocation(prevModel.getBounds().x + 250, lvl * (-150));
					if (xGlobalOffset < currModel.getBounds().x) {
						xGlobalOffset = currModel.getBounds().x + 250;

					}

				}

				// Sets previous model to this model, so that the next TypeModel could use this
				// TypeModel as reference for calculations
				prevModel = (TypeModel) m;
			}
		}
		// Add 200 pixels to make space between trees with different roots bigger
		return xGlobalOffset + 200;
	}

	/**
	 * Gets the parent TypeModel object for given TypeModel. If this method is
	 * called for null TypeModel, return will be null
	 * 
	 * @param TypeModel
	 * @return TypeModel of passed TypeModel. Null if has no parents.
	 */
	public TypeModel getParentTypeModel(TypeModel currModel) {
		TypeModel parentModel = null;

		if (currModel == null) {
			return null;
		}

		Set<RelationshipModel> outgoingBound;

		outgoingBound = ((TypeModel) currModel).getOutgoingEdges();
		for (RelationshipModel e : outgoingBound) {
			parentModel = e.getTargetModel();
		}

		return parentModel;
	}

	/**
	 * Gets a TypeModel array which holds all the children TypeModels for given
	 * TypeModel
	 * 
	 * @param TypeModel
	 * @return TypeModel array
	 */
	public TypeModel[] getChildrenTypeModels(TypeModel currModel) {
		int i = currModel.getIncomingEdges().size();
		TypeModel[] childrenArray = new TypeModel[i];
		Set<RelationshipModel> incomingBounds;

		incomingBounds = currModel.getIncomingEdges();
		i = 0;

		for (RelationshipModel incBound : incomingBounds) {
			childrenArray[i] = incBound.getSourceModel();
			i++;

		}

		return childrenArray;
	}

	/**
	 * 
	 * @param top
	 *            - the parent class
	 * @return a list containing the given class and all extentions of it and their
	 *         subclasses
	 */
	public List<AbstractModel> getAllModels(TypeModel top) {
		List<AbstractModel> c = new ArrayList<>();
		Set<RelationshipModel> set = top.getIncomingEdges();
		int repeat = set.size();

		if (repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext();) {
				RelationshipModel f = it.next();
				List<AbstractModel> temp = (getAllModels(f.getSourceModel()));
				for (AbstractModel m : temp) {
					c.add(m);
				}
			}

		}
		c.add(top);
		return c;
	}

	/**
	 * @return the maximum depth of all of these models (used when arranging it as a
	 *         tree structure)
	 */
	public int getMaxLevel(List<AbstractModel> list) {
		int maxLevel = 0;

		for (AbstractModel mod : list) {
			if (mod instanceof TypeModel) {
				int lvl = getLevel((TypeModel) mod);
				if (lvl > maxLevel) {
					maxLevel = lvl;
				}
			}
		}

		return maxLevel;
	}

	/**
	 * @param mod
	 *            - the model
	 * @return the level(depth) of a single TypeModel
	 */
	public int getLevel(TypeModel mod) {

		int maxLevel = 0;
		Set<RelationshipModel> set = mod.getOutgoingEdges();
		int repeat = set.size();

		// The method calls upon itself until it reaches an object with no outgoing
		// edges (no subclasses)
		if (repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext();) {
				RelationshipModel f = it.next();
				int lvl = getLevel(f.getTargetModel()) + 1;
				if (lvl > maxLevel) {
					maxLevel = lvl;
				}
			}
		} else {
			return 0;
		}
		return maxLevel;
	}

}
