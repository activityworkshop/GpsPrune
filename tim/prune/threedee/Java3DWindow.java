package tim.prune.threedee;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
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
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
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
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

import tim.prune.FunctionLibrary;
import tim.prune.I18nManager;
import tim.prune.data.Track;
import tim.prune.function.Export3dFunction;


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
	private double _altFactor = 50.0;

	/** only prompt about big track size once */
	private static boolean TRACK_SIZE_WARNING_GIVEN = false;

	// Constants
	private static final double INITIAL_Y_ROTATION = -25.0;
	private static final double INITIAL_X_ROTATION = 15.0;
	private static final String CARDINALS_FONT = "Arial";
	private static final int MAX_TRACK_SIZE = 2500; // threshold for warning


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
	 * Show the window
	 */
	public void show() throws ThreeDException
	{
		// Get the altitude exaggeration to use
		Object factorString = JOptionPane.showInputDialog(_parentFrame,
			I18nManager.getText("dialog.3d.altitudefactor"),
			I18nManager.getText("dialog.3d.title"),
			JOptionPane.QUESTION_MESSAGE, null, null, _altFactor);
		if (factorString == null) return;
		try {
			_altFactor = Double.parseDouble(factorString.toString());
		}
		catch (Exception e) {} // Ignore parse errors
		if (_altFactor < 1.0) {_altFactor = 1.0;}

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
			if (JOptionPane.showOptionDialog(_frame,
					I18nManager.getText("dialog.exportpov.warningtracksize"),
					I18nManager.getText("function.exportpov"), JOptionPane.OK_CANCEL_OPTION,
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
		// Add button for exporting svg
		JButton svgButton = new JButton(I18nManager.getText("function.exportsvg"));
		svgButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (_orbit != null) {
					callbackRender(FunctionLibrary.FUNCTION_SVGEXPORT);
				}
			}});
		panel.add(svgButton);
		// Display coordinates of lat/long lines of 3d graph in separate dialog
		JButton showLinesButton = new JButton(I18nManager.getText("button.showlines"));
		showLinesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				double[] latLines = _model.getLatitudeLines();
				double[] lonLines = _model.getLongitudeLines();
				LineDialog dialog = new LineDialog(_frame, latLines, lonLines);
				dialog.showDialog();
			}
		});
		panel.add(showLinesButton);
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

		// N, S, E, W
		GeneralPath bevelPath = new GeneralPath();
		bevelPath.moveTo(0.0f, 0.0f);
		for (int i=0; i<91; i+= 5) {
			bevelPath.lineTo((float) (0.1 - 0.1 * Math.cos(Math.toRadians(i))),
			  (float) (0.1 * Math.sin(Math.toRadians(i))));
		}
		for (int i=90; i>0; i-=5) {
			bevelPath.lineTo((float) (0.3 + 0.1 * Math.cos(Math.toRadians(i))),
			  (float) (0.1 * Math.sin(Math.toRadians(i))));
		}
		Font3D compassFont = new Font3D(
			new Font(CARDINALS_FONT, Font.PLAIN, 1),
			new FontExtrusion(bevelPath));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.n"), new Point3f(0f, 0f, -10f), compassFont));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.s"), new Point3f(0f, 0f, 10f), compassFont));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.w"), new Point3f(-11f, 0f, 0f), compassFont));
		objTrans.addChild(createCompassPoint(I18nManager.getText("cardinal.e"), new Point3f(10f, 0f, 0f), compassFont));

		// create and scale model
		_model = new ThreeDModel(_track);
		_model.setAltitudeFactor(_altFactor);
		_model.scale();

		// Lat/Long lines
		objTrans.addChild(createLatLongs(_model));

		// Add points to model
		objTrans.addChild(createDataPoints(_model));

		// Create lights
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		AmbientLight aLgt = new AmbientLight(new Color3f(1.0f, 1.0f, 1.0f));
		aLgt.setInfluencingBounds(bounds);
		objTrans.addChild(aLgt);

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

		// Have Java 3D perform optimizations on this scene graph.
		objRoot.compile();

		return objRoot;
	}


	/**
	 * Create a text object for compass point, N S E or W
	 * @param text text to display
	 * @param locn position at which to display
	 * @param font 3d font to use
	 * @return Shape3D object
	 */
	private Shape3D createCompassPoint(String inText, Point3f inLocn, Font3D inFont)
	{
		Text3D txt = new Text3D(inFont, inText, inLocn, Text3D.ALIGN_FIRST, Text3D.PATH_RIGHT);
		Material mat = new Material(new Color3f(0.5f, 0.5f, 0.55f),
			new Color3f(0.05f, 0.05f, 0.1f), new Color3f(0.3f, 0.4f, 0.5f),
			new Color3f(0.4f, 0.5f, 0.7f), 70.0f);
		mat.setLightingEnable(true);
		Appearance app = new Appearance();
		app.setMaterial(mat);
		Shape3D shape = new Shape3D(txt, app);
		return shape;
	}


	/**
	 * Create all the latitude and longitude lines on the base plane
	 * @param inModel model containing data
	 * @return Group object containing cylinders for lat and long lines
	 */
	private static Group createLatLongs(ThreeDModel inModel)
	{
		Group group = new Group();
		int numlines = inModel.getLatitudeLines().length;
		for (int i=0; i<numlines; i++)
		{
			group.addChild(createLatLine(inModel.getScaledLatitudeLine(i), inModel.getModelSize()));
		}
		numlines = inModel.getLongitudeLines().length;
		for (int i=0; i<numlines; i++)
		{
			group.addChild(createLonLine(inModel.getScaledLongitudeLine(i), inModel.getModelSize()));
		}
		return group;
	}


	/**
	 * Make a single latitude line for the specified latitude
	 * @param inLatitude latitude in scaled units
	 * @param inSize size of model, for length of line
	 * @return Group object containing cylinder for latitude line
	 */
	private static Group createLatLine(double inLatitude, double inSize)
	{
		Cylinder latline = new Cylinder(0.1f, (float) (inSize*2));
		Transform3D horizShift = new Transform3D();
		horizShift.setTranslation(new Vector3d(0.0, 0.0, -inLatitude));
		TransformGroup horizTrans = new TransformGroup(horizShift);
		Transform3D zRot = new Transform3D();
		zRot.rotZ(Math.toRadians(90.0));
		TransformGroup zTrans = new TransformGroup(zRot);
		horizTrans.addChild(zTrans);
		zTrans.addChild(latline);
		return horizTrans;
	}


	/**
	 * Make a single longitude line for the specified longitude
	 * @param inLongitude longitude in scaled units
	 * @param inSize size of model, for length of line
	 * @return Group object containing cylinder for longitude line
	 */
	private static Group createLonLine(double inLongitude, double inSize)
	{
		Cylinder lonline = new Cylinder(0.1f, (float) (inSize*2));
		Transform3D horizShift = new Transform3D();
		horizShift.setTranslation(new Vector3d(inLongitude, 0.0, 0.0));
		TransformGroup horizTrans = new TransformGroup(horizShift);
		Transform3D xRot = new Transform3D();
		xRot.rotX(Math.toRadians(90.0));
		TransformGroup xTrans = new TransformGroup(xRot);
		horizTrans.addChild(xTrans);
		xTrans.addChild(lonline);
		return horizTrans;
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
					inModel.getScaledHorizValue(i), inModel.getScaledAltValue(i), -inModel.getScaledVertValue(i))));
			}
			else
			{
				// Add colour-coded track point
				// Note that x, y and z are horiz, altitude, -vert
				group.addChild(createTrackpoint(new Point3d(
					inModel.getScaledHorizValue(i), inModel.getScaledAltValue(i), -inModel.getScaledVertValue(i)),
					inModel.getPointHeightCode(i)));
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


	/** @return track point object */
	private static Group createTrackpoint(Point3d inPointPos, byte inHeightCode)
	{
		Material mat = getTrackpointMaterial(inHeightCode);
		// MAYBE: sort symbol scaling
		Sphere dot = new Sphere(0.2f);
		return createBall(inPointPos, dot, mat);
	}


	/** @return Material object for track points with the appropriate colour for the height */
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
	 * Calculate the angles and call them back to the app
	 * @param inFunction function to call (either pov or svg)
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
		// Callback settings to pov export function
		inFunction.setCameraCoordinates(result.x, result.y, result.z);
		inFunction.setAltitudeExaggeration(_altFactor);
		inFunction.begin();
	}
}
