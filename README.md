What is the Pax Exam Platform
================================
Cooking the "Integrated Testing for Modular Applications" - dish.

Targeting best of breed platforms:
- OSGi
- Android

Distinglishing Components
----------------
pax-exam : common api that is also needed inside targets. (bundle)
pax-exam-spi : brick of common logic. Only used in host VM.

Exam2 embraces a MVC pattern.
----------------
When you hear Model-View-Controller, you think about web applications. This is a design pattern where you place different concerns into different parts of your system and try not to mix them too much. For an interactive application, if you keep the part that stores data (Model) separated from the logic (Controller) and use another piece to display and interact with the user (View), then it’s easier to change the system and adapt it over time to new features.

Exam2 needed a way to allow you to use various unit testing frameworks and tools to drive its core functionality.
The idea was that if we gave you the Controller and the Model, then you can craft any View you wanted.

Model:
- options
- primitive operations on Options
Controller:
- pax-exam-spi

View: (former UI or driver components)

- pax-exam-junit4
- pax-exam-testng


How to build
----------------
Usually maven2+ with central repo connection is enough (standard).
However, currently there are some swissbox snapshots being used, so you may have to get swissbox from the svn repo before.
( build yourself from https://scm.ops4j.org/repos/ops4j/projects/pax/swissbox or add maven repo: http://repository.ops4j.org/mvn-snapshots/ )

Ingredients
----------------
 * 1 or more containers
 * 1 or more Driver
 * Extra spice if you like

Targets / Containers
----------------
  * OSGi Pax Runner (container+target) OSS
  * OSGi Native (container+target) OSS
  * OSGi Remote (target) OSS
  * Android Remote Runner Commercial

Extender (Just chose if your target supports it)
----------------
  * Service (the one that directly runs the probe in the current container)
  * Ace bridges TestTarget and Ace Client

Drivers ( former UI )
----------------
  * SPI (from core, you can use the bare api yourself of cause as a UI)
  * JUnit (the one we know from 1.x but with much more control over containers)
  * Web (a small application that let you do on-device tests)
     -- Graphical can be a web app to drive on-device testing by:
        - picking a set of prebuild tests
        - picking a remote container
        - run tests interactively

SPICE
----------------
  * Tinybundles (OSS)
  * Maven Plugins (OSS)
  * Ant Plugins (OSS)
  * BundleGrill (commercial)