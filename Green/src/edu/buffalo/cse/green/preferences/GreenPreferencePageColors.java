/* This file is part of Green.
 *
 * Copyright (C) 2005 The Research Foundation of State University of New York
 * All Rights Under Copyright Reserved, The Research Foundation of S.U.N.Y.
 * 
 * Green is free software, licensed under the terms of the Eclipse
 * Public License, version 1.0.  The license is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.buffalo.cse.green.preferences;

import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_COMPARTMENT_BORDER;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_NOTE;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_NOTE_BORDER;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_NOTE_TEXT;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_REL_ARROW_FILL;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_REL_LINE;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_REL_TEXT;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_SELECTED;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_TYPE_BORDER;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_TYPE_BORDER_HIDDENR;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_TYPE_TEXT;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_COLOR_UML;
import static edu.buffalo.cse.green.preferences.PreferenceInitializer.P_FONT;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.buffalo.cse.green.PlugIn;
import edu.buffalo.cse.green.editor.DiagramEditor;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.apache.batik.ext.awt.image.codec.PNGEncodeParam.Palette;
import org.eclipse.gef.ui.palette.PaletteViewer;
/**
 * The preference page for Green's colors. 
 * 
 * @author bcmartin
 */
public class GreenPreferencePageColors extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {
	public GreenPreferencePageColors() {
		super(GRID);
		setPreferenceStore(PlugIn.getDefault().getPreferenceStore());
	}
	
	BooleanFieldEditor aaa;
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	public void createFieldEditors() {
		addField(new FontFieldEditor(P_FONT, "Font", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_SELECTED,
				"Selected Item", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_UML,
				"UML Boxes", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_TYPE_BORDER,
				"Type Borders", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_TYPE_BORDER_HIDDENR,
				"Type Borders (with hidden relationships)", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_TYPE_TEXT,
				"Type Text", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_COMPARTMENT_BORDER,
				"Compartment Borders", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_NOTE,
				"Notes", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_NOTE_BORDER,
				"Note Borders", getFieldEditorParent())); 
		addField(new ColorFieldEditor(P_COLOR_NOTE_TEXT,
				"Note Text", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_REL_ARROW_FILL,
				"Relationship Arrow Heads", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_REL_LINE,
				"Relationships", getFieldEditorParent()));
		addField(new ColorFieldEditor(P_COLOR_REL_TEXT,
				"Relationship Text", getFieldEditorParent()));	
		aaa = new BooleanFieldEditor("id", "Dark mode", 
				BooleanFieldEditor.DEFAULT, getFieldEditorParent());
		addField(aaa);
		adjustGridLayout();
		
		
	}
	


	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok = super.performOk();
		 IPreferenceStore store = PlugIn.getDefault().getPreferenceStore();
		for (DiagramEditor editor : DiagramEditor.getEditors()) {
			if (aaa.getBooleanValue() == true) {
		        
                store.setValue(PreferenceInitializer.P_COLOR_NOTE, "155,155,155"); 					// ?
                store.setValue(PreferenceInitializer.P_COLOR_UML, "100,100,100"); 					// fill
                store.setValue(PreferenceInitializer.P_COLOR_SELECTED, "16,152,61"); 				// highlight color
                store.setValue(PreferenceInitializer.P_COLOR_TYPE_BORDER, "0,0,0"); 				// borders
                store.setValue(PreferenceInitializer.P_COLOR_TYPE_BORDER_HIDDENR, "255,255,255"); 	// ?
                store.setValue(PreferenceInitializer.P_COLOR_COMPARTMENT_BORDER, "0,0,0"); 			// compartment border
                store.setValue(PreferenceInitializer.P_COLOR_TYPE_TEXT, "255,255,255"); 			// text color
                store.setValue(PreferenceInitializer.P_COLOR_NOTE_BORDER, "155,155,155");        			// ?
                store.setValue(PreferenceInitializer.P_COLOR_NOTE_TEXT, "255,255,255"); 					// ?
                store.setValue(PreferenceInitializer.P_COLOR_REL_ARROW_FILL, "255,255,255"); 		// color of the tip of the arrow
                store.setValue(PreferenceInitializer.P_COLOR_REL_LINE, "0,0,0"); 					// arrow color
                store.setValue(PreferenceInitializer.P_COLOR_REL_TEXT, "0,0,0"); 					// color of the text by the arrows
	        
			}
			editor.refresh();
			
		}
		
		return ok;
	}
}
