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

import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.preference.IPreferenceStore;

import ccvisu.GraphData;
import ccvisu.GraphEdge;
import ccvisu.Minimizer;
import ccvisu.MinimizerBarnesHut;
import ccvisu.Options;
import edu.buffalo.cse.green.PlugIn;
import edu.buffalo.cse.green.ccvisu.CCVisuUtil;
import edu.buffalo.cse.green.ccvisu.GraphVertex;
import edu.buffalo.cse.green.editor.DiagramEditor;
import edu.buffalo.cse.green.editor.model.AbstractModel;
import edu.buffalo.cse.green.editor.model.RelationshipModel;
import edu.buffalo.cse.green.editor.model.RootModel;
import edu.buffalo.cse.green.editor.model.TypeModel;
import edu.buffalo.cse.green.preferences.PreferenceInitializer;

import javax.xml.bind.*;

/**
 * @author zgwang
 *
 */
public class AutoArrangeCommand extends Command {

	private int[][] opos;
	private int[][] npos;
	private Vector<TypeModel> _m;

	/**
	 * 
	 */
	public AutoArrangeCommand() {
		_m = new Vector<TypeModel>();
	}


	public void undo() {
		for (int i = 0; i < _m.size(); i++)
			_m.get(i).setLocation(opos[i][0], opos[i][1]);
	}


	public void redo() {
		for (int i = 0; i < _m.size(); i++)
			_m.get(i).setLocation(npos[i][0], npos[i][1]);
	}


	public void execute() {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		RootModel root = editor.getRootModel();

		List<RelationshipModel> rels = root.getRelationships();

		List<AbstractModel> mods = root.getChildren();

		GraphData gd = new GraphData();
		for (AbstractModel m : mods) {

			if (m instanceof TypeModel) {
				final TypeModel n = (TypeModel) m;
				GraphVertex me = new GraphVertex(n);
				me.id = gd.vertices.size();
				me.name = "" + me.id;
				Dimension size = editor.getRootPart().getPartFromModel(n).getFigure().getSize();
				me.degree = n.getIncomingEdges().size() + n.getOutgoingEdges().size()
						+ (size.height * size.width / 20000.0f);
				
				me.isSource = me.degree > 0;
				me.pos.x = n.getLocation().x / 200.0f;
				me.pos.y = n.getLocation().y / 200.0f;
				if (!gd.vertices.contains(me)) {
					gd.vertices.add(me);
					_m.add(n);
				} else
					me = (GraphVertex) gd.vertices.get(gd.vertices.indexOf(me));

				for (RelationshipModel e : n.getOutgoingEdges()) {
					TypeModel y = e.getTargetModel();
					if (n.equals(y))
						continue; // no reflexive graph
					GraphVertex you = new GraphVertex(y);
					you.id = gd.vertices.size();
					you.name = "" + you.id;
					you.degree = y.getIncomingEdges().size()
							+ y.getOutgoingEdges().size() /* (y.getSize().height + y.getSize().width) */;
					you.isSource = you.degree > 0;
					you.pos.x = y.getLocation().x / 200.0f;
					you.pos.y = y.getLocation().y / 200.0f;
					if (!gd.vertices.contains(you)) {
						gd.vertices.add(you);
						_m.add(y);
					} else
						you = (GraphVertex) gd.vertices.get(gd.vertices.indexOf(you));

					// have me, you
					// create edge

					if (me.id != you.id) {
						GraphEdge ed = new GraphEdge();
						ed.x = me.id;
						ed.y = you.id;
						ed.w = 1.0f;
						gd.edges.add(ed);
					}
				}
			}
		}

		Options options = CCVisuUtil.newOptions(gd, 100, 3, 1, false, false, 2.001f, null, false);
		Minimizer me = new MinimizerBarnesHut(options);

		me.minimizeEnergy();

		// normalize
		float lx = 999, ly = 999;
		for (ccvisu.GraphVertex v : gd.vertices) {
			if (v.pos.x < lx)
				lx = v.pos.x;
			if (v.pos.y < ly)
				ly = v.pos.y;
		}

		for (ccvisu.GraphVertex v : gd.vertices) {
			v.pos.x -= lx;
			v.pos.y -= ly;
		}

		opos = new int[_m.size()][2];
		npos = new int[_m.size()][2];

		for (int i = 0; i < gd.vertices.size(); i++) {

			opos[i][0] = _m.get(i).getLocation().x;
			opos[i][1] = _m.get(i).getLocation().y;
			GraphVertex v = (GraphVertex) gd.vertices.get(i);
			v.me.setLocation((int) (v.pos.x * 200), (int) (v.pos.y * 200));
			npos[i][0] = v.me.getLocation().x;
			npos[i][1] = v.me.getLocation().y;
		}

		editor.checkDirty();

	}


}
