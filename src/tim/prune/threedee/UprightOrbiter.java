package tim.prune.threedee;

import java.awt.event.InputEvent;

/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 * 
 * Copyright (c) 2021 ActivityWorkshop simplifications and renamings,
 * and restriction to upright orientations.
 */

import java.awt.event.MouseEvent;
import java.awt.AWTEvent;

import javax.media.j3d.Transform3D;
import javax.media.j3d.Canvas3D;

import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import javax.vecmath.Matrix3d;

import com.sun.j3d.utils.behaviors.vp.ViewPlatformAWTBehavior;
import com.sun.j3d.utils.universe.ViewingPlatform;


/**
 * Moves the View around a point of interest when the mouse is dragged with
 * a mouse button pressed.  Includes rotation, zoom, and translation
 * actions. Zooming can also be obtained by using mouse wheel.
 * <p>
 * The rotate action rotates the ViewPlatform around the point of interest
 * when the mouse is moved with the main mouse button pressed.  The
 * rotation is in the direction of the mouse movement, with a default
 * rotation of 0.01 radians for each pixel of mouse movement.
 * <p>
 * The zoom action moves the ViewPlatform closer to or further from the
 * point of interest when the mouse is moved with the middle mouse button
 * pressed (or Alt-main mouse button on systems without a middle mouse button).
 * The default zoom action is to translate the ViewPlatform 0.01 units for each
 * pixel of mouse movement.  Moving the mouse up moves the ViewPlatform closer,
 * moving the mouse down moves the ViewPlatform further away.
 * <p>
 * The translate action translates the ViewPlatform when the mouse is moved
 * with the right mouse button pressed.  The translation is in the direction
 * of the mouse movement, with a default translation of 0.01 units for each
 * pixel of mouse movement.
 * <p>
 * The actions can be reversed using the <code>REVERSE_</code><i>ACTION</i>
 * constructor flags.  The default action moves the ViewPlatform around the
 * objects in the scene.  The <code>REVERSE_</code><i>ACTION</i> flags can
 * make the objects in the scene appear to be moving in the direction
 * of the mouse movement.
 */
public class UprightOrbiter extends ViewPlatformAWTBehavior
{
	private Transform3D _longitudeTransform = new Transform3D();
	private Transform3D _latitudeTransform = new Transform3D();
	private Transform3D _rotateTransform = new Transform3D();

	// needed for integrateTransforms but don't want to new every time
	private Transform3D _temp1 = new Transform3D();
	private Transform3D _temp2 = new Transform3D();
	private Transform3D _translation = new Transform3D();
	private Vector3d _transVector = new Vector3d();
	private Vector3d _distanceVector = new Vector3d();
	private Vector3d _centerVector = new Vector3d();
	private Vector3d _invertCenterVector = new Vector3d();

	private double _deltaYaw = 0.0, _deltaPitch = 0.0;
	private double _startDistanceFromCenter = 20.0;
	private double _distanceFromCenter = 20.0;
	private Point3d _rotationCenter = new Point3d();
	private Matrix3d _rotMatrix = new Matrix3d();

	private int _mouseX = 0, _mouseY = 0;

	private double _xtrans = 0.0, _ytrans = 0.0, _ztrans = 0.0;

	private static final double MIN_RADIUS = 0.0;

	// the factor to be applied to wheel zooming so that it does not 
	// look much different with mouse movement zooming. 
	private static final float wheelZoomFactor = 50.0f;

	private static final double NOMINAL_ZOOM_FACTOR = .01;
	private static final double NOMINAL_ROT_FACTOR = .008;
	private static final double NOMINAL_TRANS_FACTOR = .003;

	private double _pitchAngle = 0.0;


	/**
	 * Creates a new OrbitBehaviour
	 * @param inCanvas The Canvas3D to add the behaviour to
	 * @param inInitialPitch pitch angle in degrees
	 */
	public UprightOrbiter(Canvas3D inCanvas, double inInitialPitch)
	{
		super(inCanvas, MOUSE_LISTENER | MOUSE_MOTION_LISTENER | MOUSE_WHEEL_LISTENER );
		_pitchAngle = Math.toRadians(inInitialPitch);
	}

	protected synchronized void processAWTEvents( final AWTEvent[] events )
	{
		motion = false;
		for (AWTEvent event : events) {
			if (event instanceof MouseEvent) {
				processMouseEvent((MouseEvent) event);
			}
		}
	}

	protected void processMouseEvent( final MouseEvent evt )
	{
		if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
			_mouseX = evt.getX();
			_mouseY = evt.getY();
			motion = true;
		}
		else if (evt.getID() == MouseEvent.MOUSE_DRAGGED)
		{
			int xchange = evt.getX() - _mouseX;
			int ychange = evt.getY() - _mouseY;
			// rotate
			if (isRotateEvent(evt))
			{
				_deltaYaw -= xchange * NOMINAL_ROT_FACTOR;
				_deltaPitch -= ychange * NOMINAL_ROT_FACTOR;
			}
			// translate
			else if (isTranslateEvent(evt))
			{
				_xtrans -= xchange * NOMINAL_TRANS_FACTOR;
				_ytrans += ychange * NOMINAL_TRANS_FACTOR;
			}
			// zoom
			else if (isZoomEvent(evt)) {
				doZoomOperations( ychange );
			}
			_mouseX = evt.getX();
			_mouseY = evt.getY();
			motion = true;
		}
		else if (evt.getID() == MouseEvent.MOUSE_WHEEL )
		{
			if (isZoomEvent(evt))
			{
				// if zooming is done through mouse wheel, the number of wheel increments
				// is multiplied by the wheelZoomFactor, to make zoom speed look natural
				if ( evt instanceof java.awt.event.MouseWheelEvent)
				{
					int zoom = ((int)(((java.awt.event.MouseWheelEvent)evt).getWheelRotation()
						* wheelZoomFactor));
					doZoomOperations( zoom );
					motion = true;
				}
			}
		}
	}

	/*
	 * zoom but stop at MIN_RADIUS
	 */
	private void doZoomOperations( int ychange )
	{
		if ((_distanceFromCenter - ychange * NOMINAL_ZOOM_FACTOR) > MIN_RADIUS) {
			_distanceFromCenter -= ychange * NOMINAL_ZOOM_FACTOR;
		}
		else {
			_distanceFromCenter = MIN_RADIUS;
		}
	}

	/**
	 * Sets the ViewingPlatform for this behaviour.  This method is
	 * called by the ViewingPlatform.
	 * If a sub-calls overrides this method, it must call
	 * super.setViewingPlatform(vp).
	 * NOTE: Applications should <i>not</i> call this method.
	 */
	@Override
	public void setViewingPlatform(ViewingPlatform vp)
	{
		super.setViewingPlatform( vp );

		if (vp != null) {
			resetView();
			integrateTransforms();
		}
	}

	/**
	 * Reset the orientation and distance of this behaviour to the current
	 * values in the ViewPlatform Transform Group
	 */
	private void resetView()
	{
		Vector3d centerToView = new Vector3d();

		targetTG.getTransform( targetTransform );

		targetTransform.get( _rotMatrix, _transVector );
		centerToView.sub( _transVector, _rotationCenter );
		_distanceFromCenter = centerToView.length();
		_startDistanceFromCenter = _distanceFromCenter;

		targetTransform.get( _rotMatrix );
		_rotateTransform.set( _rotMatrix );

		// compute the initial x/y/z offset
		_temp1.set(centerToView);
		_rotateTransform.invert();
		_rotateTransform.mul(_temp1);
		_rotateTransform.get(centerToView);
		_xtrans = centerToView.x;
		_ytrans = centerToView.y;
		_ztrans = centerToView.z;

		// reset rotMatrix
		_rotateTransform.set( _rotMatrix );
	}

	protected synchronized void integrateTransforms()
	{
		// Check if the transform has been changed by another behaviour
		Transform3D currentXfm = new Transform3D();
		targetTG.getTransform(currentXfm);
		if (! targetTransform.equals(currentXfm))
			resetView();

		// Three-step rotation process, firstly undo the pitch and apply the delta yaw
		_latitudeTransform.rotX(_pitchAngle);
		_rotateTransform.mul(_rotateTransform, _latitudeTransform);
		_longitudeTransform.rotY( _deltaYaw );
		_rotateTransform.mul(_rotateTransform, _longitudeTransform);
		// Now update pitch angle according to delta and apply
		_pitchAngle = Math.min(Math.max(0.0, _pitchAngle - _deltaPitch), Math.PI/2.0);
		_latitudeTransform.rotX(-_pitchAngle);
		_rotateTransform.mul(_rotateTransform, _latitudeTransform);

		_distanceVector.z = _distanceFromCenter - _startDistanceFromCenter;

		_temp1.set(_distanceVector);
		_temp1.mul(_rotateTransform, _temp1);

		// want to look at rotationCenter
		_transVector.x = _rotationCenter.x + _xtrans;
		_transVector.y = _rotationCenter.y + _ytrans;
		_transVector.z = _rotationCenter.z + _ztrans;

		_translation.set(_transVector);
		targetTransform.mul(_temp1, _translation);

		// handle rotationCenter
		_temp1.set(_centerVector);
		_temp1.mul(targetTransform);

		_invertCenterVector.x = -_centerVector.x;
		_invertCenterVector.y = -_centerVector.y;
		_invertCenterVector.z = -_centerVector.z;

		_temp2.set(_invertCenterVector);
		targetTransform.mul(_temp1, _temp2);

		Vector3d finalTranslation = new Vector3d();
		targetTransform.get(finalTranslation);

		targetTG.setTransform(targetTransform);

		// reset yaw and pitch deltas
		_deltaYaw = 0.0;
		_deltaPitch = 0.0;
	}

	private boolean isRotateEvent(MouseEvent evt)
	{
		final boolean isRightDrag = (evt.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) > 0;
		return !evt.isAltDown() && !isRightDrag;
	}

	private boolean isZoomEvent(MouseEvent evt)
	{
		if (evt instanceof java.awt.event.MouseWheelEvent) {
			return true;
		}
		return evt.isAltDown() && !evt.isMetaDown();
	}

	private boolean isTranslateEvent(MouseEvent evt)
	{
		final boolean isRightDrag = (evt.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) > 0;
		return !evt.isAltDown() && isRightDrag;
	}
}
