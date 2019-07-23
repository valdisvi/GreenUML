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
	
	public void undo() {
		for( int i=0; i<_m.size(); i++)
			_m.get(i).setLocation(opos[i][0], opos[i][1]);
	}
	
	public void redo() {
		for( int i=0; i<_m.size(); i++)
			_m.get(i).setLocation(npos[i][0], npos[i][1]);
	}
	
	
	/* 
	 * This method creates a diagram suited for tree-like structures 
	 * by drawing trees one by one.
	 */
	public void execute() {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
        RootModel root = editor.getRootModel();
		List<AbstractModel> children = root.getChildren();
		int maxLevel = getMaxLevel(children);
		// tops contains the classes that do not extend any other class shown in the diagram
		List<TypeModel> tops = new ArrayList<>();
		// nextX provides spacing between trees on the x axis
		int nextX = 0;
		
		for(AbstractModel mod : children) {
			if(mod instanceof TypeModel) {
				try {
					if((((TypeModel) mod).getType().getSuperclassName()) == null) {
						tops.add((TypeModel) mod);
					}
				} catch (JavaModelException e) {
				}
			}
		}
		
		//draws a tree for each class in tops
		for(AbstractModel t : tops) {
			if(t instanceof TypeModel) {
				nextX = drawTree((TypeModel) t, maxLevel, nextX);
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
	 * @param top 	 - the parent class (root) of the tree
	 * @param max	 - maximum depth
	 * @param prevX  - the tree will add a space between this coordinate and then place itself there
	 * @return the furthest x coordinate the tree occupies
	 */
	public int drawTree(TypeModel top, int max, int prevX) {
        DiagramEditor editor = DiagramEditor.getActiveEditor();
        List<AbstractModel> allModels = getAllModels(top);
        System.out.println("AllModels: " + allModels);
        // xPos provides spacing on the x axis between the graphical elements
        int[] xPos = new int[max + 1];
        Arrays.fill(xPos, (prevX + 100));
        int nextX = 0;
        
        /* Y coordinates for the objects are determined by their levels.
         * These levels make the tree structure more comprehensible, as
         * classes are placed under their parent class.     
         */
        
        for (AbstractModel m : allModels) {
            if (m instanceof TypeModel) {
            	int lvl = getLevel((TypeModel)m);
            	int level = max - lvl;
            	m.setSize(200, 100);
            	m.setLocation(xPos[level], (level * (-150)));
            	xPos[level] += 300;
            }
        }
        
        for(int i = 0; i < xPos.length; i++) {
        	if(xPos[i] > nextX)
        		nextX = xPos[i];
        }
        return nextX;
}
	
	/**
	 * 
	 * @param top - the parent class
	 * @return a list containing the given class and all extentions of it and their subclasses
	 */
	public List<AbstractModel> getAllModels(TypeModel top) {
		List<AbstractModel> c = new ArrayList<>();
		Set<RelationshipModel> set = top.getIncomingEdges();
		int repeat = set.size();
		
		if(repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext(); ) {
			       RelationshipModel f = it.next();
			       List<AbstractModel> temp = (getAllModels(f.getSourceModel()));
			       	for(AbstractModel m : temp) {
			       		c.add(m);
			       	}
			   }
			 
		}
		c.add(top);
		return c;
	}
	
	/**
	 * @return the maximum depth of all of these models (used when arranging it as a tree structure)
	 */
	public int getMaxLevel(List<AbstractModel> list) {
		int maxLevel = 0;
		
		for(AbstractModel mod : list) {
			if(mod instanceof TypeModel) {
				int lvl = getLevel((TypeModel) mod);
				if(lvl > maxLevel) {
					maxLevel = lvl;
				}
			}
		}
		
		return maxLevel;
	}
	
	/**
	 * @param mod - the model
	 * @return the level(depth) of a single TypeModel
	 */
	public int getLevel(TypeModel mod) {
		
		int maxLevel = 0;
		Set<RelationshipModel> set = mod.getOutgoingEdges();
		int repeat = set.size();
		
		// The method calls upon itself until it reaches an object with no outgoing edges (no subclasses)
		if(repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext(); ) {
			       RelationshipModel f = it.next();
			       	int lvl = getLevel(f.getTargetModel()) +1;
			       	if(lvl > maxLevel) {
			       		maxLevel = lvl;
			       	}
			   }
		}
		else {
			return 0;
			}
		return maxLevel;
	}
	
	
	
	
}
