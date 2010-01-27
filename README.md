What is the Pax Exam Platform
================================
Cooking the "Integrated Testing for Modular Applications" - dish.

Ingredients
----------------
 * 1 or more containers
 * 1 or more UIs
 * Extra spice if you like

Targets / Containers
----------------
  * Pax Runner (container+target)
  * Native (container+target)
  * Remote (target)

Extender (Just chose if your target supports it)
----------------
  * Service (the one that directly runs the probe in the current container)
  * Ace bridges TestTarget and Ace Client

UI
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
  * Tinybundles (?)
  * Maven Plugins
  * Ant Plugins
  * Bundle Grill