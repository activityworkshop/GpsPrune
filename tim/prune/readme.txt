Prune version 7
===============

Prune is an application for viewing, editing and managing coordinate data from GPS systems,
including format conversion, charting and photo correlation.
Full details can be found at http://activityworkshop.net/software/prune/

Prune is copyright activityworkshop.net and distributed under the terms of the Gnu GPL version 2.
You may freely use the software, and may help others to freely use it too.  For further information
on your rights and how they are protected, see the included license.txt file.

Prune comes without warranty and without guarantee - the authors cannot be held responsible for
losses incurred through use of the program, however caused.


Running
=======

To run Prune from the jar file, simply call it from a command prompt or shell:
   java -jar prune_07.jar

If the jar file is saved in a different directory, you will need to include the path.
Depending on your system settings, you may be able to click or double-click on the jar file
in a file manager window to execute it.  A shortcut, menu item, alias, desktop icon
or other link can of course be made should you wish.

To specify a language other than the default, use an additional parameter, eg:
   java -jar prune_07.jar --lang=DE


New with version 7
==================

The following features were added since version 6:
  - Loading of KMZ files and zipped GPX
  - Improved compression functions with four configurable algorithms
  - New function to call gpsbabel to send data directly to GPS receiver
  - Charting functions (eg altitude or speed against distance) using gnuplot
  - Map view can now use other OpenStreetMap images such as OpenCycleMap or any other tile server
  - New function to create an average position from a track section
  - Display of straight line distances between waypoints

New with version 6
==================

The following features were added since version 5:
  - Map view using OpenStreetMap images is now integrated in the main window, with control for map transparency
  - Pov export has new option to use sphere sweeps for better appearance
  - New function to check online for a newer version of Prune
  - New function to take a section of track and cut/paste it to another position
  - New function to add or subtract a time offset from point timestamps
  - New function to call gpsbabel to load data directly from GPS receiver
  - Additional file filter options on load and save
  - Italian language thanks to generous user input

New with version 5
==================

The following features were added since version 4.1:
  - New map window in the View menu, showing points overlaid on OpenStreetMap images
  - New function to launch a browser showing the area in either Google Maps or OpenStreetMap
  - Handling of track segments, including loading, saving and exporting, and preservation during edits and undos
  - New function to merge track segments for the current selection, to make one single segment
  - Display of current and average speed on details panel
  - Statusbar showing confirmation of actions
  - Much improved French texts thanks to generous user input

New with version 4.1
====================

The following fix was added since version 4:
  - Exiftool calls now made in series rather than in parallel to avoid performance problems in Windows

New with version 4
==================

The following features were added since version 3:
  - Automatic correlation of photos with points based on timestamps
  - Manual disconnection of photos from points
  - Reading of photo thumbnails from exif data (speeds up photo loading)
  - Export to GPX format
  - KML and KMZ export now includes altitudes option for airborne tracks
  - Track points in map can be connected by lines
  - On loading a text file, fields are now guessed according to data or column headings
  - Polish language

New with version 3
==================

The following features were added since version 2:
  - Loading of GPX and KML files
  - Loading of jpeg photos with or without coordinate data
  - Manual correlation of photos with points
  - Saving of coordinates in exif data of jpegs
  - Exporting to KMZ format including thumbnails of photos
  - Four-panel layout with toolbar
  - French language

New with version 2
==================

The following features were added since version 1:
  - Display of data in 3d view using Java3D library
  - Export of 3d model to POV format for rendering by povray
  - Point edit dialog, waypoint name edit dialog
  - Waypoint list
  - Spanish language


Further information and updates
===============================

To obtain the source code (if it wasn't included in your jar file), or for further information,
please visit the website:  http://activityworkshop.net/

You will find there user guides and screenshots illustrating the major features.
As Prune is further developed, subsequent versions of the program will also be made freely
available at this website.

You can also provide feedback on Prune, and find out more about contributing to the development,
especially with regard to language translations.
