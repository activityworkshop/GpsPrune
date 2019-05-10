package tim.prune.threedee;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Billboard;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.PointLight;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import tim.prune.DataStatus;
import tim.prune.FunctionLibrary;
import tim.prune.I18nManager;
import tim.prune.data.Track;
import tim.prune.function.Export3dFunction;
import tim.prune.function.srtm.LookupSrtmFunction;
import tim.prune.gui.map.MapSourceLibrary;
import tim.prune.save.GroutedImage;
import tim.prune.save.MapGrouter;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;


/**
 * Class to hold main window for java3d view of data
 */
public class Java3DWindow implements ThreeDWindow
{
	private Track _track = null;
	private JFrame _parentFrame = null;
	private JFrame _frame = null;
	private ThreeDModel _model = null;
	private OrbitBehavior _orbit = null;
	private double _altFactor = -1.0;
	private ImageDefinition _imageDefinition = null;
	private GroutedImage _baseImage = null;
	private TerrainDefinition _terrainDefinition = null;
	private DataStatus _dataStatus = null;

	/** only prompt about big track size once */
	private static boolean TRACK_SIZE_WARNING_GIVEN = false;

	// Constants
	private static final double INITIAL_Y_ROTATION = -25.0;
	private static final double INITIAL_X_ROTATION = 15.0;
	private static final String CARDINALS_FONT = "Arial";
	private static final int MAX_TRACK_SIZE = 2500; // threshold for warning
	private static final double MODEL_SCALE_FACTOR = 20.0;


	/**
	 * Constructor
	 * @param inFrame parent frame
	 */
	public Java3DWindow(JFrame inFrame)
	{
		_parentFrame = inFrame;
	}


	/**
	 * Set the track object
	 * @param inTrack Track object
	 */
	public void setTrack(Track inTrack)
	{
		_track = inTrack;
	}

	/**
	 * @param inFactor altitude factor to use
	 */
	public void setAltitudeFactor(double inFactor)
	{
		_altFactor = inFactor;
	}

	/**
	 * Set the parameters for the base image and do the grouting already
	 * (setTrack should already be called by now)
	 */
	public void setBaseImageParameters(ImageDefinition inDefinition)
	{
		_imageDefinition = inDefinition;
		if (inDefinition != null && inDefinition.getUseImage())
		{
			_baseImage = new MapGrouter().createMapImage(_track, MapSourceLibrary.getSource(inDefinition.getSourceIndex()),
				inDefinition.getZoom());
		}
		else _baseImage = null;
	}

	/**
	 * Set the terrain parameters
	 */
	public void setTerrainParameters(TerrainDefinition inDefinition)
	{
		_terrainDefinition = inDefinition;
	}

	/**
	 * Set the current data status
	 */
	public void setDataStatus(DataStatus inStatus)
	{
		_dataStatus = inStatus;
	}

	/**
	 * Show the window
	 */
	public void show() throws ThreeDException
	{
		// Make sure altitude exaggeration is positive
		if (_altFactor < 0.0) {_altFactor = 1.0;}

		// Set up the graphics config
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		if (config == null)
		{
			// Config shouldn't be null, but we can try to create a new one as a workaround
			GraphicsConfigTemplate3D gc = new GraphicsConfigTemplate3D();
			gc.setDepthSize(0);
			config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getBestConfiguration(gc);
		}

		if (config == null)
		{
			// Second attempt also failed, going to have to give up here.
			throw new ThreeDException("Couldn't create graphics config");
		}

		// Check number of points in model isn't too big, and suggest compression
		Object[] buttonTexts = {I18nManager.getText("button.continue"), I18nManager.getText("button.cancel")};
		if (_track.getNumPoints() > MAX_TRACK_SIZE && !TRACK_SIZE_WARNING_GIVEN)
		{
			if (JOptionPane.showOptionDialog(_parentFrame,
					I18nManager.getText("dialog.3d.warningtracksize"),
					I18nManager.getText("function.show3d"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, buttonTexts, buttonTexts[1])
				== JOptionPane.OK_OPTION)
			{
				// opted to continue, don't show warning again
				TRACK_SIZE_WARNING_GIVEN = true;
			}
			else {
				// opted to cancel - show warning again next time
				return;
			}
		}

		Canvas3D canvas = new Canvas3D(config);
		canvas.setSize(400, 300);

		// Create the scene and attach it to the virtual universe
		BranchGroup scene = createSceneGraph();
		SimpleUniverse u = new SimpleUniverse(canvas);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		u.getViewingPlatform().setNominalViewingTransform();

		// Add behaviour to rotate using mouse
		_orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL | OrbitBehavior.STOP_ZOOM);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		_orbit.setSchedulingBounds(bounds);
		u.getViewingPlatform().setViewPlatformBehavior(_orbit);
		u.addBranchGraph(scene);

		// Don't reuse _frame object from last time, because data and/or scale might be different
		// Need to regenerate everything
		_frame = new JFrame(I18nManager.getText("dialog.3d.title"));
		_frame.getContentPane().setLayout(new BorderLayout());
		_frame.getContentPane().add(canvas, BorderLayout.CENTER);
		_frame.setIconImage(_parentFrame.getIconImage());
		// Make panel for render, close buttons
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// Add button for exporting pov
		JButton povButton = new JButton(I18nManager.getText("function.exportpov"));
		povButton.addActionListener(new ActionListener() {
			/** Export pov button pressed */
			public void actionPerformed(ActionEvent e)
			{
				if (_orbit != null) {
					callbackRender(FunctionLibrary.FUNCTION_POVEXPORT);
				}
			}});
		panel.add(povButton);
		// Close button
		JButton closeButton = new JButton(I18nManager.getText("button.close"));
		closeButton.addActionListener(new ActionListener()
		{
			/** Close button pressed - clean up */
			public void actionPerformed(ActionEvent e) {
				dispose();
				_orbit = null;
			}
		});
		panel.add(closeButton);
		_frame.getContentPane().add(panel, BorderLayout.SOUTH);
		_frame.setSize(500, 350);
		_frame.pack();
		// Add a listener to clean up when window closed
		_frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		// show frame
		_frame.setVisible(true);
		if (_frame.getState() == JFrame.ICONIFIED) {
			_frame.setState(JFrame.NORMAL);
		}
	}

	/**
	 * Dispose of the frame and its resources
	 */
	public void dispose()
	{
		if (_frame != null) {
			_frame.dispose();
			_frame = null;
		}
	}

	/**
	 * Create the whole scenery from the given track
	 * @return all objects in the scene
	 */
	private BranchGroup createSceneGraph()
	{
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create the transform group node and initialize it.
		// Enable the TRANSFORM_WRITE capability so it can be spun by the mouse
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// Create a translation
		Transform3D shiftz = new Transform3D();
		shiftz.setScale(0.055);
		TransformGroup shiftTrans = new TransformGroup(shiftz);

		objRoot.addChild(shiftTrans);
		Transform3D rotTrans = new Transform3D();
		rotTrans.rotY(Math.toRadians(INITIAL_Y_ROTATION));
		Transform3D rot2 = new Transform3D();
		rot2.rotX(Math.toRadians(INITIAL_X_ROTATION));
		TransformGroup tg2 = new TransformGroup(rot2);
		objTrans.setTransform(rotTrans);
		shiftTrans.addChild(tg2);
		tg2.addChild(objTrans);

		// Base plane
		Appearance planeAppearance = null;
		Box plane = null;
		planeAppearance = new Appearance();
		planeAppearance.setMaterial(new Material(new Color3f(0.1f, 0.2f, 0.2f),
			new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0.3f, 0.4f, 0.4f),
			new Color3f(0.3f, 0.3f, 0.3f), 0.0f));
		plane = new Box(10f, 0.04f, 10f, planeAppearance);
		objTrans.addChild(plane);

		// Image on top of base plane, if specified
		final boolean showTerrain = _terrainDefinition != null && _terrainDefinition.getUseTerrain();
		if (_baseImage != null && !showTerrain)
		{
			QuadArray baseSquare = new QuadArray (4, QuadArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
			baseSquare.setCoordinate(0, new Point3f(-10f, 0.05f, -10f));
			baseSquare.setCoordinate(1, new Point3f(-10f, 0.05f, 10f));
			baseSquare.setCoordinate(2, new Point3f( 10f, 0.05f, 10f));
			baseSquare.setCoordinate(3, new Point3f( 10f, 0.05f, -10f));
			// and set anchor points for the texture
			baseSquare.setTextureCoordinate(0, 0, new TexCoord2f(0.0f, 1.0f));
			baseSquare.setTextureCoordinate(0, 1, new TexCoord2f(0.0f, 0.0f));
			baseSquare.setTextureCoordinate(0, 2, new TexCoord2f(1.0f, 0.0f));
			baseSquare.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 1.0f));
			// Set appearance including image
			Appearance baseAppearance = new Appearance();
			Texture mapImage = new TextureLoader(_baseImage.getImage(), _frame).getTexture();
			baseAppearance.setTexture(mapImage);
			objTrans.addChild(new Shape3D(baseSquare, baseAppearance));
		}

		// Create model containing track information
		_model = new ThreeDModel(_track);
		_model.setAltitudeFactor(_altFactor);

		if (showTerrain)
		{
			TerrainHelper terrainHelper = new TerrainHelper(_terrainDefinition.getGridSize());
			// See if there's a previously saved terrain track we can reuse
			Track terrainTrack = TerrainCache.getTerrainTrack(_dataStatus, _terrainDefinition);
			if (terrainTrack == null)
			{
				// Construct the terrain track according to these extents and the grid size
				terrainTrack = terrainHelper.createGridTrack(_track);
				// Get the altitudes from SRTM for all the points in the track
				LookupSrtmFunction srtmLookup = (LookupSrtmFunction) FunctionLibrary.FUNCTION_LOOKUP_SRTM;
				srtmLookup.begin(terrainTrack);
				while (srtmLookup.isRunning())
				{
					try {
						Thread.sleep(750);  // just polling in a wait loop isn't ideal but simple
					}
					catch (InterruptedException e) {}
				}

				// Fix the voids
				terrainHelper.fixVoids(terrainTrack);

				// Store this back in the cache, maybe we'll need it again
				TerrainCache.storeTerrainTrack(terrainTrack, _dataStatus, _terrainDefinition);
			}
			// else System.out.println("Yay - reusing the cached track!");

			// Give the terrain definition to the _model as well
			_model.setTerrain(terrainTrack);
			_model.scale();

			objTrans.addChild(createTerrain(_model, terrainHelper, _baseImage));
		}
		else
		{
			// No terrain, so just scale the model as it is
			_model.scale();
		}

		// N, S, E, W
		GeneralPath bevelPath = new GeneralPath();
		bevelPath.moveTo(0.0f, 0.0f);
		for (int i=0; i<91; i+= 5)
		{
			bevelPath.lineTo((float) (0.1 - 0.1 * Math.cos(Math.toRadians(i))),
			  (float) (0.1 * Math.sin(Math.toRadians(i))));
		}
		for (int i=90; i>0; i-=5)
		{
			bevelPath.lineTo((float) (0.3 + 0.1 * Math.cos(Math.toRadians(i))),
			  (float) (0.1 * Math.sin(Math.toRadians(i))));
		}
		Font3D compassFont = new Font3D(
			new Font(CARDINALS_FONT, Font.PLAIN, 1),
			new FontExtrusion(bevelPath));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.n"), new Point3f(0f, 0f, -11.5f), compassFont));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.s"), new Point3f(0f, 0f, 11.5f), compassFont));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.w"), new Point3f(-11.5f, 0f, 0f), compassFont));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.e"), new Point3f(11.5f, 0f, 0f), compassFont));

		// Add points to model
		objTrans.addChild(createDataPoints(_model));

		// Create lights - always add ambient light
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		AmbientLight aLgt = new AmbientLight(new Color3f(1.0f, 1.0f, 1.0f));
		aLgt.setInfluencingBounds(bounds);
		objTrans.addChild(aLgt);

		// Additional lights depend on whether there's a terrain or not
		if (showTerrain)
		{
			// If there's a terrain, just have directional light from northwest
			DirectionalLight dl = new DirectionalLight(true,
				new Color3f(1.0f, 1.0f, 1.0f),
				new Vector3f(1.0f, -1.0f, 1.0f));
			dl.setInfluencingBounds(bounds);
			objTrans.addChild(dl);
		}
		else
		{
			// There is no terrain, so use point lights as before
			PointLight pLgt = new PointLight(new Color3f(1.0f, 1.0f, 1.0f),
				new Point3f(0f, 0f, 2f), new Point3f(0.25f, 0.05f, 0.0f) );
			pLgt.setInfluencingBounds(bounds);
			objTrans.addChild(pLgt);

			PointLight pl2 = new PointLight(new Color3f(0.8f, 0.9f, 0.4f),
				new Point3f(6f, 1f, 6f), new Point3f(0.2f, 0.1f, 0.05f) );
			pl2.setInfluencingBounds(bounds);
			objTrans.addChild(pl2);

			PointLight pl3 = new PointLight(new Color3f(0.7f, 0.7f, 0.7f),
				new Point3f(0.0f, 12f, -2f), new Point3f(0.1f, 0.1f, 0.0f) );
			pl3.setInfluencingBounds(bounds);
			objTrans.addChild(pl3);
		}

		// Have Java 3D perform optimizations on this scene graph.
		objRoot.compile();

		return objRoot;
	}


	/**
	 * Create a text object for compass point, N S E or W
	 * @param inText text to display
	 * @param inLocn position at which to display
	 * @param inFont 3d font to use
	 * @return compound object
	 */
	private TransformGroup createCompassPoint(String inText, Point3f inLocn, Font3D inFont)
	{
		Text3D txt = new Text3D(inFont, inText, inLocn, Text3D.ALIGN_FIRST, Text3D.PATH_RIGHT);
		Material mat = new Material(new Color3f(0.5f, 0.5f, 0.55f),
			new Color3f(0.05f, 0.05f, 0.1f), new Color3f(0.3f, 0.4f, 0.5f),
			new Color3f(0.4f, 0.5f, 0.7f), 70.0f);
		mat.setLightingEnable(true);
		Appearance app = new Appearance();
		app.setMaterial(mat);
		Shape3D shape = new Shape3D(txt, app);

		// Make transform group with billboard behaviour
		TransformGroup subGroup = new TransformGroup();
		subGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		subGroup.addChild(shape);
		Billboard billboard = new Billboard(subGroup, Billboard.ROTATE_ABOUT_POINT, inLocn);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		billboard.setSchedulingBounds(bounds);
		subGroup.addChild(billboard);
		return subGroup;
	}


	/**
	 * Make a Group of the data points to be added
	 * @param inModel model containing data
	 * @return Group object containing spheres, rods etc
	 */
	private static Group createDataPoints(ThreeDModel inModel)
	{
		// Add points to model
		Group group = new Group();
		int numPoints = inModel.getNumPoints();
		for (int i=0; i<numPoints; i++)
		{
			byte pointType = inModel.getPointType(i);
			if (pointType == ThreeDModel.POINT_TYPE_WAYPOINT)
			{
				// Add waypoint
				// Note that x, y and z are horiz, altitude, -vert
				group.addChild(createWaypoint(new Point3d(
					inModel.getScaledHorizValue(i) * MODEL_SCALE_FACTOR,
					inModel.getScaledAltValue(i)   * MODEL_SCALE_FACTOR,
					-inModel.getScaledVertValue(i) * MODEL_SCALE_FACTOR)));
			}
			else
			{
				// Add colour-coded track point
				// Note that x, y and z are horiz, altitude, -vert
				group.addChild(createTrackpoint(new Point3d(
					inModel.getScaledHorizValue(i) * MODEL_SCALE_FACTOR,
					inModel.getScaledAltValue(i)   * MODEL_SCALE_FACTOR,
					-inModel.getScaledVertValue(i) * MODEL_SCALE_FACTOR), inModel.getPointHeightCode(i)));
			}
		}
		return group;
	}


	/**
	 * Create a waypoint sphere
	 * @param inPointPos position of point
	 * @return Group object containing sphere
	 */
	private static Group createWaypoint(Point3d inPointPos)
	{
		Material mat = getWaypointMaterial();
		// MAYBE: sort symbol scaling
		Sphere dot = new Sphere(0.35f); // * symbolScaling / 100f);
		return createBall(inPointPos, dot, mat);
	}


	/**
	 * @return a new Material object to define waypoint colour / shine etc
	 */
	private static Material getWaypointMaterial()
	{
		return new Material(new Color3f(0.1f, 0.1f, 0.4f),
			 new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0.0f, 0.2f, 0.7f),
			 new Color3f(1.0f, 0.6f, 0.6f), 40.0f);
	}


	/**
	 * @return track point object
	 */
	private static Group createTrackpoint(Point3d inPointPos, byte inHeightCode)
	{
		Material mat = getTrackpointMaterial(inHeightCode);
		// MAYBE: sort symbol scaling
		Sphere dot = new Sphere(0.2f);
		return createBall(inPointPos, dot, mat);
	}


	/**
	 * @return Material object for track points with the appropriate colour for the height
	 */
	private static Material getTrackpointMaterial(byte inHeightCode)
	{
		// create default material
		Material mat = new Material(new Color3f(0.3f, 0.2f, 0.1f),
			new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0.0f, 0.6f, 0.0f),
			new Color3f(1.0f, 0.6f, 0.6f), 70.0f);
		// change colour according to height code
		if (inHeightCode == 1) mat.setDiffuseColor(new Color3f(0.4f, 0.9f, 0.2f));
		else if (inHeightCode == 2) mat.setDiffuseColor(new Color3f(0.7f, 0.8f, 0.2f));
		else if (inHeightCode == 3) mat.setDiffuseColor(new Color3f(0.3f, 0.6f, 0.4f));
		else if (inHeightCode == 4) mat.setDiffuseColor(new Color3f(0.1f, 0.9f, 0.9f));
		else if (inHeightCode >= 5) mat.setDiffuseColor(new Color3f(1.0f, 1.0f, 1.0f));
		// return object
		return mat;
	}


	/**
	 * Create a ball at the given point
	 * @param inPosition scaled position of point
	 * @param inSphere sphere object
	 * @param inMaterial material object
	 * @return Group containing sphere
	 */
	private static Group createBall(Point3d inPosition, Sphere inSphere, Material inMaterial)
	{
		Group group = new Group();
		// Create ball and add to group
		Transform3D ballShift = new Transform3D();
		ballShift.setTranslation(new Vector3d(inPosition));
		TransformGroup ballShiftTrans = new TransformGroup(ballShift);
		inMaterial.setLightingEnable(true);
		Appearance ballApp = new Appearance();
		ballApp.setMaterial(inMaterial);
		inSphere.setAppearance(ballApp);
		ballShiftTrans.addChild(inSphere);
		group.addChild(ballShiftTrans);
		// Also create rod for ball to sit on
		Cylinder rod = new Cylinder(0.1f, (float) inPosition.y);
		Material rodMat = new Material(new Color3f(0.2f, 0.2f, 0.2f),
			new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0.2f, 0.2f, 0.2f),
			new Color3f(0.05f, 0.05f, 0.05f), 0.4f);
		rodMat.setLightingEnable(true);
		Appearance rodApp = new Appearance();
		rodApp.setMaterial(rodMat);
		rod.setAppearance(rodApp);
		Transform3D rodShift = new Transform3D();
		rodShift.setTranslation(new Vector3d(inPosition.x, inPosition.y/2.0, inPosition.z));
		TransformGroup rodShiftTrans = new TransformGroup(rodShift);
		rodShiftTrans.addChild(rod);
		group.addChild(rodShiftTrans);
		// return the pair
		return group;
	}

	/**
	 * Create a java3d Shape for the terrain
	 * @param inModel threedModel
	 * @param inHelper terrain helper
	 * @param inBaseImage base image for shape, or null for no image
	 * @return Shape3D object
	 */
	private static Shape3D createTerrain(ThreeDModel inModel, TerrainHelper inHelper, GroutedImage inBaseImage)
	{
		final int numNodes = inHelper.getGridSize();
		final int RESULT_SIZE = numNodes * (numNodes * 2 - 2);
		int[] stripData = inHelper.getStripLengths();

		// Get the scaled terrainTrack coordinates (or just heights) from the model
		final int nSquared = numNodes * numNodes;
		Point3d[] rawPoints = new Point3d[nSquared];
		for (int i=0; i<nSquared; i++)
		{
			double height = inModel.getScaledTerrainValue(i) * MODEL_SCALE_FACTOR;
			rawPoints[i] = new Point3d(inModel.getScaledTerrainHorizValue(i) * MODEL_SCALE_FACTOR,
				Math.max(height, 0.05), // make sure it's above the box
				-inModel.getScaledTerrainVertValue(i) * MODEL_SCALE_FACTOR);
		}

		GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_STRIP_ARRAY);
		gi.setCoordinates(inHelper.getTerrainCoordinates(rawPoints));
		gi.setStripCounts(stripData);

		Appearance tAppearance = new Appearance();
		if (inBaseImage != null)
		{
			gi.setTextureCoordinateParams(1, 2); // one coord set of two dimensions
			gi.setTextureCoordinates(0, inHelper.getTextureCoordinates());
			Texture mapImage = new TextureLoader(inBaseImage.getImage()).getTexture();
			tAppearance.setTexture(mapImage);
			TextureAttributes texAttr = new TextureAttributes();
			texAttr.setTextureMode(TextureAttributes.MODULATE);
			tAppearance.setTextureAttributes(texAttr);
		}
		else
		{
			Color3f[] colours = new Color3f[RESULT_SIZE];
			Color3f terrainColour = new Color3f(0.1f, 0.2f, 0.2f);
			for (int i=0; i<RESULT_SIZE; i++) {colours[i] = terrainColour;}
			gi.setColors(colours);
		}
		new NormalGenerator().generateNormals(gi);
		Material terrnMat = new Material(new Color3f(0.4f, 0.4f, 0.4f), // ambient colour
			new Color3f(0f, 0f, 0f), // emissive (none)
			new Color3f(0.8f, 0.8f, 0.8f), // diffuse
			new Color3f(0.2f, 0.2f, 0.2f), //specular
			30f); // shinyness
		tAppearance.setMaterial(terrnMat);
		return new Shape3D(gi.getGeometryArray(), tAppearance);
	}

	/**
	 * Calculate the angles and call them back to the app
	 * @param inFunction function to call for export
	 */
	private void callbackRender(Export3dFunction inFunction)
	{
		Transform3D trans3d = new Transform3D();
		_orbit.getViewingPlatform().getViewPlatformTransform().getTransform(trans3d);
		Matrix3d matrix = new Matrix3d();
		trans3d.get(matrix);
		Point3d point = new Point3d(0.0, 0.0, 1.0);
		matrix.transform(point);
		// Set up initial rotations
		Transform3D firstTran = new Transform3D();
		firstTran.rotY(Math.toRadians(-INITIAL_Y_ROTATION));
		Transform3D secondTran = new Transform3D();
		secondTran.rotX(Math.toRadians(-INITIAL_X_ROTATION));
		// Apply inverse rotations in reverse order to the test point
		Point3d result = new Point3d();
		secondTran.transform(point, result);
		firstTran.transform(result);

		// Give the settings to the rendering function
		inFunction.setCameraCoordinates(result.x, result.y, result.z);
		inFunction.setAltitudeExaggeration(_altFactor);
		inFunction.setTerrainDefinition(_terrainDefinition);
		inFunction.setImageDefinition(_imageDefinition);

		inFunction.begin();
	}
}
