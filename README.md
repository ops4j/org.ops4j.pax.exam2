What is the Pax Exam Platform
================================
Cooking the "Integrated Testing for Modular Applications" - dish.

Targeting best of breed platforms:
- OSGi
- Android

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
  * Android Remote Runner   
  * OSGi Pax Runner (container+target)
  * OSGi Native (container+target)
  * OSGi Remote (target)

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
  * Tinybundles
  * Maven Plugins
  * Ant Plugins
  * Bundle Grill