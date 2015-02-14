Prune version 4
===============

Prune is an application for viewing, editing and managing coordinate data from GPS systems,
including format conversion and photo correlation.

Prune is copyright activityworkshop.net and distributed under the terms of the Gnu GPL version 2.
You may freely use the software, and may help others to freely use it too.  For further information
on your rights and how they are protected, see the included license.txt file.

Prune comes without warranty and without guarantee - the authors cannot be held responsible for
losses incurred through use of the program, however caused.


Running
=======

To run Prune from the jar file, simply call it from a command prompt or shell:
   java -jar prune_04.jar

If the jar file is saved in a different directory, you will need to include the path.
Depending on your system settings, you may be able to click or double-click on the jar file
in a file manager window to execute it.  A shortcut, menu item, desktop icon or other link
can of course be made should you wish.

To specify a language other than the default, use an additional parameter, eg:
   java -jar prune_04.jar --lang=DE


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
