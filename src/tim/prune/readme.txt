GpsPrune version 20
===================

GpsPrune is an application for viewing, editing and managing coordinate data from GPS systems,
including format conversion, charting, 3d visualisation, audio and photo correlation, and online resource lookup.
Full details can be found at https://gpsprune.activityworkshop.net/

GpsPrune is copyright 2006-2020 activityworkshop.net and distributed under the terms of the Gnu GPL version 2.
You may freely use the software, and may help others to freely use it too.  For further information
on your rights and how they are protected, see the included license.txt file.

GpsPrune comes without warranty and without guarantee - the authors cannot be held responsible
for losses incurred through use of the program, however caused.


Running
=======

To run GpsPrune from the jar file, simply call it from a command prompt or shell:
   java -jar gpsprune_20.jar

If the jar file is saved in a different directory, you will need to include the path.
Depending on your system settings, you may be able to click or double-click on the jar file
in a file manager window to execute it.  A shortcut, menu item, alias, desktop icon
or other link can of course be made should you wish.

To specify a language other than the default, use an additional parameter, eg:
   java -jar gpsprune_20.jar --lang=DE


New with version 20
=====================
The following fixes and additions were made since version 19:
  - Add option to use Nimbus look-and-feel (wishlist 77)
  - Extend the marker waypoints function to include half the distance, half the climb and half the descent
  - Remove Ukrainian language
  - Add support for entering Pluscodes
  - Add way to copy point details or range details to clipboard (wishlist 76)
  - Project point using bearing and distance (wishlist 35)
  - Possibility to paste a list of coordinates instead of just one
  - Removal of Gpsies functions (both download and upload)

New with version 19
===================
The following fixes and additions were made since version 18:
  - Wikipedia search now also includes galleries from wikimedia
  - Photo popup window now gets updated when the track selection changes
  - Function to add waypoints along the track at intervals of distance or time (eg every 5 km)
  - Optionally draw arrows on the track lines to show direction of travel
  - Waypoint rendering using icons (wishlist 71)
  - Allow user to select timezone in which timestamps are displayed (wishlist 61)
  - Provide call to geonames.org's OSM node search function, to find amenities (like bus stops) close to the current point
  - Debian and Ubuntu packages no longer rely on external libmetadata jar
  - Keyboard shortcut to access point edit dialog (part of Github issue #10)
  - Online services Inlinemap.net and Graphhopper.com (routing)
  - Cardinals in java3d view get billboard behaviour to always face camera (thanks, PeHar)
  - Lighting of 3d views from the northwest (thanks, PeHar)
  - Remember the name tag from a loaded gpx file, suggest it again for gpx export

New with version 18
===================
The following features were added since version 17:
  - New search options using opencaching.de and mapillary
  - New web options using peakfinder, geohack and panoramio
  - Autoplay function for automatically scrolling through the track
  - Marking uphill lift sections of skiing / snowboarding tracks
  - Configurable anti-aliasing for map view and profile view
  - Allow showing just the lines between track points but not the track points

New with version 17
===================
The following features were added since version 16:
  - Colouring the track points according to various criteria (such as altitude,
    speed, segment, file) in both the regular map view and the image export
  - Marking points for deletion according to their date
  - Select the current segment
  - Adding an altitude tolerance to the climb and descent calculations
  - Sorting waypoints by name or by timestamp

New with version 16
===================
The following features were added since version 15:
  - Extend povray output using terrain and/or map image
  - Extend java3d output using terrain and/or map image
  - Weather forecasts
  - Splitting a track into segments based on distance or time
  - Sewing track segments together
  - Function to download and save SRTM tiles

New with version 15
===================
The following features were added since version 14:
  - Extend povray output using map image on base plane
  - Export an image of the map and track at a selected zoom level
  - Estimation of hiking times and learning of parameter values
  - Allow altitude / speed profile to show any arbitrary field
  - Accept files dragged and dropped onto the GpsPrune window
  - Take account of timezone if present in track timestamps
  - Allow timestamp exports in KML using gx extensions
  - GPSBabel filters
  - Improved wikipedia name lookup
  - Allow loading of speeds and vertical speeds from text files

New with version 14
===================
The following features were added since version 13:
  - Dragging of existing points
  - Creation of new points by dragging the halfway point between two points
  - Nautical miles option including knots
  - Full range details dialog
  - Extension of interpolation function
  - Selecting points within a rectangle to delete

New with version 13
===================
The following features were added since version 12:
  - Name change from Prune to GpsPrune
  - Handling of description field for waypoints
  - Opening of images from within kmz files, zip files and from http links
  - Compression using Douglas-Peucker algorithm
  - Option to save settings automatically on exit
  - Dialog to show the local tile cache, report sizes and allow deletion
    of tiles, either deleting whole tilesets or individual tiles older than
    a specified number of days
  - Checkbox on GPX export to specify UTF-8 rather than default system encoding
  - Importing of files through GPSBabel
  - List of recently used files in the menu
  - Display of bearing at which a photo was taken (display only)
  - Allow edit of custom map sources
  - Russian translation

New with version 12
===================
The following features were added since version 11:
  - Options to load audio files, play them, connect to points and auto-correlate them
  - Extend Gpx export to include links to photos and audio files
  - Extend Gpx load to also load referenced photos, audio files
  - Function to search for places in Wikipedia by name
  - Function to search for places in Wikipedia near the current point
  - Function to download OSM data using the XAPI and save to osm file
  - New drawing mode to add points with one click each
  - Hungarian translations thanks to György
  - Korean translations thanks to HooAU

New with version 11
===================
The following features were added since version 10:
  - Option to select which of the named tracks to load out of a gpx file or gps
  - Function to delete all values of a single field (eg all altitudes, all timestamps)
  - Export of 3d view to svg file
  - Function to upload current track to gpsies.com
  - Option to hide sidebars (left panel, right panel and lower panel) to just show map
  - Various bugfixes, especially with gpx export, track compression and scale bar
  - Dutch and Czech translations thanks to generous volunteers

New with version 10
===================
The following features were added since version 9:
  - Function to lookup altitudes using SRTM data from the Space Shuttle
  - Choice between altitude profile and speed profile in main view
  - Caching of map tiles to disk for faster access
  - Offline mode where maps are only retrieved from disk
  - Storage of multiple custom map sources, not just one custom one
  - Use of maps with multiple layers (like maps-for-free)
  - Use of cloudmade maps

New with version 9
==================
The following features were added since version 8:
  - Ability to paste coordinates (eg from wikipedia or geocaching sites) to create new points
  - Configurable colour settings
  - Function to convert waypoint names into timestamps
  - Function to reorganise and sort photos either by filename or time
  - Ability to load gzipped xml files
  - New "full range details" dialog including pace and average gradient
  - Preservation of unrecognised gpx tags by copying source xml
  - Improved photo handling and ability to rotate photos
  - Japanese, Portuguese and Turkish languages thanks to generous user input

New with version 8
==================
The following features were added since version 7:
  - Loading of NMEA files (with suffix .nmea)
  - Loading of nearby tracks from gpsies.com
  - Function for finding waypoints by name
  - Language-sensitive keyboard shortcuts
  - Scale bar
  - Display of pace (time per km or time per mile)
  - Ability to display and save configuration settings
  - Configurable program paths and KMZ image sizes
  - Chinese language thanks to generous user input

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
The following features were added since version 4:
  - New map window in the View menu, showing points overlaid on OpenStreetMap images
  - New function to launch a browser showing the area in either Google Maps or OpenStreetMap
  - Handling of track segments, including loading, saving and exporting, and preservation during edits and undos
  - New function to merge track segments for the current selection, to make one single segment
  - Display of current and average speed on details panel
  - Statusbar showing confirmation of actions
  - Much improved French texts thanks to generous user input

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

Features of version 1
=====================
The following features were included in version 1:
  - Loading of text files, display in overhead and profile views
  - Display of track details such as distances, speeds
  - Deletion of points and ranges, and variable compression
  - Export to KML format
  - English, German and Swiss German languages


Further information and updates
===============================

To obtain the source code (if it wasn't included in your jar file), or for further information,
please visit the website:  https://gpsprune.activityworkshop.net/

You will find there user guides, screenshots and demo videos illustrating the major features.
As GpsPrune is further developed, subsequent versions of the program will also be made freely
available at this website.

You can also provide feedback on GpsPrune, and find out more about contributing to the development,
especially with regard to language translations.
