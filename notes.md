
# General notes for contributors

This file documents all changes made from GreenUML version 4.6.0 to 4.7.0. It describes added features and explains how they function in a bit more detail. Also, some notes about previously existing code can be found here.

For future contributers, the project has very little comments and method descriptions. The lack of information makes the understanding of the already developed code realy hard and time consuming. We encourage future developers of this project to document every method and/or variable to help others after you to work fast and efficiently. We ourselves did not add comments for code that already existed. but we have documented our work.


### New AutoLayout option

A new layout option has been added. This layout is suited for tree-like structures (for example, showing child classes of a superclass). For more complex diagrams, the old AutoLayout option is preferred, but this new option can make simpler diagrams more comprehensible.

Auto layout can be called using hotkeys "l" or "k". "l" arranges the diagram using a tree structure. "k" arranges the diagram using a graph structure. Auto layout can also be called using the mouse: right click > AutoArrange > Auto-Arrange diagram.
When the respective Auto-Arrange command is called either by hotkeys or mouse clicks, it rearranges the class boxes and adjusts the maximum possible zoom out.

This method works by finding all classes that are not subclasses of another class present in the diagram. For each of these classes, another method is called which organizes its model (and its child models) into a single tree like structure. The trees are created individually so that child classes can be as close as possible to their parent class.

Models are sorted on the y axis by level (depth). Two more methods have been added for this purpose- one to find the maximum level across all models in the diagram, and one to find the relative level of an individual model. The difference between the maximum and individual level is the depth at which the model is placed at, with level 0 being the top.

In the future, this method could be improved by making the parent classes centered relative to their subclasses.


### Import, interface and package inclusion

The layout takes in account only classes, leaving out interfaces and imports from other packages. So the algorithm only orders classes. When an Interface is provided for ordering it shows unsuspected results. When we try to create a layout for whole project it shows an error, that it cannot do it. This project needs a way to see all imported packages and how they relate to each other in one layout


### Adaptive zoom

Previously it was possible to zoom in and out 8 times or using 8 zoom levels. These zoom levels were not dependent on the size of layout or anything drawn in the editor viewer windows.
Adaptive zoom has been achieved using already written file ZoomFitAction.java which already used hotkey "a" for autofitting zoom for layout. Now, when auto-arrange, AutoFitAction or any changes to the editor has been introduced while editing the max zoom out range automatically adjusts so that it wouldn't be possible to zoom out more that the size of the all layout. There is a method checkDirty() in Green/src/edu/buffalo/cse/green/editor/DiagramEditor.java that is called whenever a change is made into editor (something moved, deleted etc.). An addition to this method was made, so that zoom would be recalculated every time checkDirty() is called.
When plug-in starts the checkDirty() is called and zoom level values from 0 to 2.5 (step 0.05, so it means 50 values) is given to ZoomManager. So unless any changes are introduced into editor windows the zoom will allow to zoom out more that the layout border. And this is what could be improved, so that when user starts working he/she does not have to make any changes to the diagrams to be able to zoom out not further than the size of the layout.


### Dark mode

A rather simple addition which darkens the color scheme. This can be activated from the Preferences tab.


## Notes about the project in general

To make it easier for people to start improving the project, here is a general description of some of the previously existing structure and features of the code.


#### Graph

In order to make layouts possible, a graph structure is used. It represents the different types of relationships between the classes. By accessing incoming and outgoing edges, all relationships for a single model can be found.
CCVisu methods are used in the default layout method in order to arrange the graph.


#### Changing the layout

Instead of adding the models to some kind of panel, all that needs to be done in order to update the layout is to change the properties of the models (for example, the position) and check for changes in the editor.


#### Bugs

 - Default layout method causes stack overflow error when used with a large number of models
