# usda-java-durum-scan-bcgv

written by @nico6bury

This repository was created by copying the usda-java-durum-scan project and modifying it to suit the needs of the durum project. BCGV is an acronym for Big-Clear-Grid-Version, since this version uses different grids than the previous version.

## Description

This repository has the purpose of collecting images from an EPSON scanner, saving the images, running them through imagej, and then processing that output for the purpose of analyzing durum samples.

## Package Explanation (TODO: Rewrite this stuff after the program is rewritten)

### Scan

This package is specifically used for communicating with the scanner and getting images. It doesn't do a whole lot else.

### Utils

This package is meant to keep smaller classes or collections of static functions for various uses in other packages.  
It also has classes used for keeping track of config files.

### View

This package contains the GUI, as one might expect. The main method in MainWindow.java is meant to be the entry point for the application, and in MVC terms, this package serves as both the view and controller, coordinating the other packages based on user selections. The GUI itself is built off Swing. The initial calls are done in the MainWindow constructor, and otherwise things are triggered by button clicks by the user.

## Distribution Info

This application supposedly uses some libraries that must be run under a 32-bit version of java. In order to not interfere with other java versions, this program is designed to be distributed with three things:

- a jar file containing the compiled java code for the application
- a subdirectory containing a 32-bit jre
- a batch file which runs the jar file using the 32-bit jre
