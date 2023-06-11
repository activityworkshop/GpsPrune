package tim.prune.cmd;

import org.junit.jupiter.api.Test;
import tim.prune.data.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command to rotate a photo and undo the rotation
 */
class RotatePhotoTest
{
	@Test
	public void testRotations()
	{
		testRotate(true);
		testRotate(false);
	}

	public void testRotate(boolean isClockwise)
	{
		for (int i=0; i<4; i++)
		{
			Photo photo = new Photo(new File("abc.jpg"));
			photo.setRotation(i);
			assertEquals(i * 90, photo.getRotationDegrees());
			RotatePhotoCmd command = new RotatePhotoCmd(photo, isClockwise);
			// rotate
			command.execute(null);
			// check that the angle is now what we expect
			int expectedI = (i + (isClockwise ? 1 : 3)) % 4;
			int expectedAngle = expectedI * 90;
			assertEquals(expectedAngle, photo.getRotationDegrees());
			// now undo
			command.getInverse().executeCommand(null);
			assertEquals(i * 90, photo.getRotationDegrees());
		}
	}
}
