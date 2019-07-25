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
		List<TypeModel> ends = new ArrayList<>();
		// nextX provides spacing between trees on the x axis
		int nextX = 0;

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
		System.out.println(tops);

		// draws a tree for each class in tops
		for (AbstractModel t : tops) {
			if (t instanceof TypeModel) {

				// nextX = drawTree((TypeModel) t, maxLevel, nextX);
				drawT((TypeModel) t, maxLevel);
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
	 * 
	 * @param top
	 *            - the parent class (root) of the tree
	 * @param max
	 *            - maximum depth
	 * @param prevX
	 *            - the tree will add a space between this coordinate and then place
	 *            itself there
	 * @return the furthest x coordinate the tree occupies
	 */
	public int drawTree(TypeModel top, int max, int prevX) {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		List<AbstractModel> allModels = getAllModels(top);

		// xPos provides spacing on the x axis between the graphical elements
		int[] xPos = new int[max + 1];
		Arrays.fill(xPos, (prevX + 100));
		for (int el : xPos) {

		}
		int nextX = 0;

		/*
		 * Y coordinates for the objects are determined by their levels. These levels
		 * make the tree structure more comprehensible, as classes are placed under
		 * their parent class.
		 */

		for (AbstractModel m : allModels) {
			if (m instanceof TypeModel) {
				int lvl = getLevel((TypeModel) m);
				int level = max - lvl;
				int childrenCount, parentCount;
				// System.out.println("lvl: " + lvl);
				//
				//
				// System.out.println("AbstractModel: " + m);
				childrenCount = ((TypeModel) m).getIncomingEdges().size();
				parentCount = ((TypeModel) m).getOutgoingEdges().size();
				// System.out.println("Children: " + childrenCount);
				// System.out.println("Parents: " + parentCount);

				m.setSize(200, 100);

				m.setLocation(xPos[level], (level * (-150)));

				xPos[level] += 250;

				// System.out.println("level: " + level);
				// System.out.println();
			}
		}

		for (int i = 0; i < xPos.length; i++) {
			if (xPos[i] > nextX)
				nextX = xPos[i];

		}

		return nextX;
	}

	public void drawT(TypeModel top, int maxLayoutLevel) {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		List<AbstractModel> allModels = getAllModels(top);
		int[] xGlobalOffset = new int[maxLayoutLevel + 1];
		int xGOffset = 0;
		Arrays.fill(xGlobalOffset, 0);
		TypeModel prevModel = null, currModel;
		
		
		for (AbstractModel m : allModels) {
			System.out.println("AbstractModel m: " + m);
			
			if (m instanceof TypeModel) {
				int lvl = getLevel((TypeModel) m);
				currModel = (TypeModel)m;
				int currModelIncomingEdges = currModel.getIncomingEdges().size();
				System.out.println(currModel + "Incoming edges: " + currModelIncomingEdges);
				
				// Set size for each TypeModel
				m.setSize(200, 100);
				System.out.println("prevModParent: " + getParent(prevModel) + ", currModParent: " + getParent(currModel));
				// Check if previous TypeModels parent is the same as the current ones
				if(getParent(prevModel) == getParent(currModel)) {
						System.out.println("(" + (prevModel.getBounds().x + 250) + ";" + prevModel.getBounds().y + ")");
						
						// If it is, set its location next to his brother with 50 pixels white space on the same level
						currModel.setLocation(prevModel.getBounds().x + 250, prevModel.getBounds().y);
						
						// Remember TypeModels x axis that is the most to the right in the layout
						if(currModel.getBounds().x >= xGOffset) {
							System.out.println("xOffset: " + xGOffset);
							xGOffset = currModel.getBounds().x + 250;
							System.out.println("xOffset: " + xGOffset);
							
						
					}
					
				// If TypeModel has more than 2 children, it needs to be drawn on his own level, but so that it would be in the middle of his children regarding x axis
				} else if(currModelIncomingEdges > 1){
					TypeModel [] childrenArray = new TypeModel[currModelIncomingEdges];
	
					// Array holds currentModel children TypeModels
					childrenArray = getChildrenTypeModels(currModel);
					
					// x axis for the most left child
					int xLeft = childrenArray[0].getBounds().x;
					
					// x axis for the most right child
					int xRight = childrenArray[childrenArray.length - 1].getBounds().x;
					
					// 200 is added, because 200 is width of the box
					// divided with 2, because parent needs to be in the middle of the children
					// minus 100 because the box x coordinate is for upper left corner, so if we 
					//	put this corner int the middle, we need to push it back to the left 200/2
					currModel.setLocation(xLeft + ((xRight - xLeft + 200)/2)-100, lvl * (-150));
					
				// If TypeModel has 1 children, it needs to be drawn right on top of it
				}else if (currModelIncomingEdges == 1){
					currModel.setLocation(prevModel.getBounds().x, lvl * (-150));
					
					if(currModel.getBounds().x >= xGOffset) {
						System.out.println("xOffset: " + xGOffset);
						xGOffset = currModel.getBounds().x + 250;
						System.out.println("xOffset: " + xGOffset);
						
					}
				
				// If TypeModel has no children
				}else if(currModelIncomingEdges == 0){
					//System.out.println("(" + (prevModel.getBounds().x + 250) + ";" + prevModel.getBounds().y + ")");
					
					currModel.setLocation(xGOffset, lvl * (-150));
					if(currModel.getBounds().x >= xGOffset) {
						System.out.println("xOffset: " + xGOffset);
						xGOffset = currModel.getBounds().x + 250;
						System.out.println("xOffset: " + xGOffset);
						
					}
					
				} else {
					currModel.setLocation(prevModel.getBounds().x + 250, lvl * (-150));
					if(xGOffset < currModel.getBounds().x) {
						xGOffset = currModel.getBounds().x + 250;
						
				}
					
//			for(int o = 0; o < xLeftBound.length; o++) {
//				System.out.println("xLeftBound[" + o + "]: " + xLeftBound[o]);
//				
			}
			
			prevModel = (TypeModel) m;
			System.out.println();
			}
		}
	}
	
	/**
	 * Gets the parent TypeModel object for given TypeModel. If this method is called for null TypeModel, return will be null
	 * 
	 * @param TypeModel
	 * @return TypeModel of passed TypeModel. Null if has no parents.
	 */
	public TypeModel getParent (TypeModel currModel) {
		TypeModel parentModel = null;
		
		if(currModel == null) {
			return null;
		}
		
		Set<RelationshipModel> outgoingBound;
		
		outgoingBound = ((TypeModel) currModel).getOutgoingEdges();
		for(RelationshipModel e : outgoingBound) {
			parentModel = e.getTargetModel();
		}
		
		return parentModel;
	}

	/**
	 * Gets a TypeModel array which holds all the children TypeModels for given TypeModel
	 * 
	 * @param TypeModel
	 * @return TypeModel array
	 */
	public TypeModel[] getChildrenTypeModels (TypeModel currModel) {
		int i = currModel.getIncomingEdges().size();
		TypeModel [] childrenArray = new TypeModel[i];
		Set<RelationshipModel> incomingBounds;
		incomingBounds = currModel.getIncomingEdges();
		i = 0;
		System.out.println("Children: ");
		for(RelationshipModel incBound : incomingBounds) {
			childrenArray[i] = incBound.getSourceModel();
			System.out.println(i + ") " + childrenArray[i]);
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
