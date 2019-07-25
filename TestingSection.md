# Testing
This section goes over the processes that were applied to verify the correctness of the updated GreenUML plug-in.  


## Contents

1. Instalation testing
2. GreenUML Palette testing
3. JUnit tests
4. Autoarange testing 
5. AutoFit testing 
6. SonarLint tests 
7. Dark mode testing
8. Linux/Windows environment testing 

### Installation testing
At the beginning newly implemented Plug-In worked separately with Graphical Modeling Framework (GMF) - Tooling 3.0.1. So Plug-In only worked if user downloaded GMF from Eclipse Marketplace. But later on GMF was implemented into GreenUML plug-in itself so that user doesn't have to download two separate files. Plug-In's initial testing was done by fallowing the order of instructions listed below:
1. Download Eclipse Oxygen 4.7.3 from  Eclipse website: https://www.eclipse.org/oxygen/
2. Download GreenUML 4.7.3 Plug-In from: .....
3. In Eclipse navigate to section "Help", then click on "Install New Software".
4. Press "Add" and select downloaded Plug-In
5. Check "Group items by category" and select all Plug-In items for install and press "Finish"

After completing these steps GreenUML worked successfully (like it works with older Eclipse versions) with Eclipse Oxygen 4.7.3

//testing with merged gmf tooling and gUML
//test results


### GreenUML Palette testing
Palette testing was done by creating new classes through GreenUML. Tests consisted of: 
1. class creation;
2. subclass creation;
3. arrow testing to see relations between classes;
4. enum creation;
5. interface creation;
6. note testing.

All functionalities provided by palette were drawn successfully (without any errors and just like in previous version of GreenUML).
### JUnit tests
To confirm that newly created objects via palette were implemented correctly, few JUnit tests were conducted. Fallowing JUnit tets were:
1. Class object creation test;
2. Subclass object creation test;
3. Method overriding test;
4. Business logic test;
5. Return value test;
6. Interface test;
7. Inheritance test.

All tests passed and classes which where created with GreenUML behaved like classes that were made using Eclipse class creation wizard or just written manually.   
### AutoArrangeCommand method testing
AutoArrangeCommand is a method from previous versions of GreenUML Plug-In which arranges all classes in a graph-like structure. Our team decided to rewrite method to not only arrange classes but also sort them in hierarchical tree-like structure. There were only two tests while testing this method:
1. Graph-like structure test;
2. Tree-like structure test.

When using graph-like structure sorting which can be accessed by right click > AutoArrange > Auto-Arrange Diagram or just  "k", all objects are arranged correctly (objects were organized like in previous versions of GreenUML). Tree-like test by pressing "l" also worked as expected (objects were organized in hierarchical tree-like structure which sorts super classes higher and sub classes lower).



### AutoFit command test
Our team also improved AutoFit fit command which can be accessed by right click > Zoom > ZoomFit or just "a" button. AutoFit allows to show all diagram objects by zooming out/in so that all objects are shown and the most outer objects are located by the borders of current picture.
Test for this command was done side by side with AutoArrange commands. Test was conducted by drawing related and not related objects in one diagram and applying AutoArrange commands on them. After doing so AutoFit command is used.
After conducting test AutoFit command works as expected (all objects were in one picture disregarding the relations between them)

### SonarLint testing
SonarLint test where conducted using SonarLint 4.1 Eclipse extension. Not all SonarLint reports were fixed. Most of the SonarLint red flags were fixed while most of the green flags were left how they were. Fixed red flags consisted of: commented-out code which was used for test purposes, missing method annotations e.g. ("@Override", "@Test"), and in general making the code easier to read. 

### Dark mode testing
One of the additional features that our team added to new GreenUML plug-In was dark mode. Although previous plug-in version provides option in Eclipse preferences to change colors of diagram's objects, it does not have a preset of colors for dark mode and the palette of tools color cannot be changed and stays white. Two tests were conducted regarding the dark mode:
1. Eclipse preference page "dark mode preset for objects" test;
2. Eclipse dark mode for palette test. 

Out of two tests only one passed because our team couldn't implement dark mode for palette, so for now it stays white. But the preset in Eclipse preference page works as expected (after ticking the "Dark mode" box in  Window > Preferences > Green UML > Color and Fonts page and pressing "apply" a darker set of colors was set to all objects).

### Linux/Windows environment testing 
Updated GreenUML was created in Linux environment. One of the requirements for the plug-in was to be supported by multiple platforms, in this case (Linux, Windows). Therefore all above mentioned tests were conducted on Windows by applying same instructions.

GreenUML worked the same on Windows as it did on Linux. No extra actions were needed to make GreenUML plug-in work on Windows platform.
