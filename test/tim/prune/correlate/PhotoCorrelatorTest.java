package tim.prune.correlate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tim.prune.cmd.PointAndMedia;
import tim.prune.data.*;

import java.io.File;
import java.util.ArrayList;

public class PhotoCorrelatorTest
{
    @Test
    public void testNoPairs()
    {
        PointMediaPair[] pointPairs = new PointMediaPair[] {};
        ArrayList<PointAndMedia> pointPhotoPairs = new ArrayList<>();
        ArrayList<DataPoint> pointsToCreate = new ArrayList<>();
        PhotoCorrelator.fillListsForCommand(pointPairs, pointsToCreate, pointPhotoPairs);
        Assertions.assertTrue(pointsToCreate.isEmpty());
        Assertions.assertTrue(pointPhotoPairs.isEmpty());
    }

    @Test
    public void testSingleConnectNoCreate()
    {
        DataPoint point = new DataPoint(new Latitude("1.81"), new Longitude("5.44"), null);
        Photo photo = new Photo(new File("abc.jpg"));
        PointMediaPair pair = new PointMediaPair(photo);
        pair.addPoint(point, 0L);
        PointMediaPair[] pointPairs = new PointMediaPair[] {pair};

        // Prepare the two lists
        ArrayList<PointAndMedia> pointPhotoPairs = new ArrayList<>();
        ArrayList<DataPoint> pointsToCreate = new ArrayList<>();
        PhotoCorrelator.fillListsForCommand(pointPairs, pointsToCreate, pointPhotoPairs);
        // Expect no points to be created, but one link to be made
        Assertions.assertTrue(pointsToCreate.isEmpty());
        Assertions.assertEquals(1, pointPhotoPairs.size());
        Assertions.assertEquals(point, pointPhotoPairs.get(0).getPoint());
        Assertions.assertEquals(photo, pointPhotoPairs.get(0).getPhoto());
    }

    @Test
    public void testSingleInterpolate()
    {
        DataPoint point1 = new DataPoint(new Latitude("1.81"), new Longitude("5.44"), null);
        DataPoint point2 = new DataPoint(new Latitude("1.822"), new Longitude("5.432"), null);
        Photo photo = new Photo(new File("abc.jpg"));
        PointMediaPair pair = new PointMediaPair(photo);
        pair.addPoint(point1, -10L);
        pair.addPoint(point2, 20L);
        PointMediaPair[] pointPairs = new PointMediaPair[] {pair};

        // Prepare the two lists
        ArrayList<PointAndMedia> pointPhotoPairs = new ArrayList<>();
        ArrayList<DataPoint> pointsToCreate = new ArrayList<>();
        PhotoCorrelator.fillListsForCommand(pointPairs, pointsToCreate, pointPhotoPairs);
        // Expect one point to be created, and one link to be made
        Assertions.assertEquals(1, pointsToCreate.size());
        DataPoint createdPoint = pointsToCreate.get(0);
        Assertions.assertNotEquals(point1, createdPoint);
        Assertions.assertNotEquals(point2, createdPoint);
        Assertions.assertEquals(1, pointPhotoPairs.size());
        Assertions.assertEquals(createdPoint, pointPhotoPairs.get(0).getPoint());
        Assertions.assertEquals(photo, pointPhotoPairs.get(0).getPhoto());
    }

    @Test
    public void testSingleConnectWithCreate()
    {
        // Make point already connected to photo1
        DataPoint point = new DataPoint(new Latitude("1.81"), new Longitude("5.44"), null);
        Photo photo1 = new Photo(new File("abc.jpg"));
        point.setPhoto(photo1);
        photo1.setDataPoint(point);
        // Correlate photo2 directly to point
        Photo photo2 = new Photo(new File("def.jpg"));
        PointMediaPair pair = new PointMediaPair(photo2);
        pair.addPoint(point, 0L);
        PointMediaPair[] pointPairs = new PointMediaPair[] {pair};

        // Prepare the two lists
        ArrayList<PointAndMedia> pointPhotoPairs = new ArrayList<>();
        ArrayList<DataPoint> pointsToCreate = new ArrayList<>();
        PhotoCorrelator.fillListsForCommand(pointPairs, pointsToCreate, pointPhotoPairs);
        // Expect single point to be created as duplicate of point1
        Assertions.assertEquals(1, pointsToCreate.size());
        final DataPoint newPoint = pointsToCreate.get(0);
        Assertions.assertNotSame(point, newPoint);
        Assertions.assertEquals(point.getLatitude().output(Coordinate.FORMAT_DEG_MIN_SEC), newPoint.getLatitude().output(Coordinate.FORMAT_DEG_MIN_SEC));
        Assertions.assertEquals(point.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC), newPoint.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC));
        Assertions.assertEquals(1, pointPhotoPairs.size());
        Assertions.assertEquals(newPoint, pointPhotoPairs.get(0).getPoint());
        Assertions.assertEquals(photo2, pointPhotoPairs.get(0).getPhoto());
    }

    @Test
    public void testDoubleConnectToSamePoint()
    {
        DataPoint point = new DataPoint(new Latitude("1.81"), new Longitude("5.44"), null);
        Photo photo1 = new Photo(new File("abc.jpg"));
        Photo photo2 = new Photo(new File("def.jpg"));
        PointMediaPair pair1 = new PointMediaPair(photo1);
        pair1.addPoint(point, 0L);
        PointMediaPair pair2 = new PointMediaPair(photo2);
        pair2.addPoint(point, 0L);
        PointMediaPair[] pointPairs = new PointMediaPair[] {pair1, pair2};

        // Both photos should be connected to the same point, but this isn't possible
        ArrayList<PointAndMedia> pointPhotoPairs = new ArrayList<>();
        ArrayList<DataPoint> pointsToCreate = new ArrayList<>();
        PhotoCorrelator.fillListsForCommand(pointPairs, pointsToCreate, pointPhotoPairs);
        // Expect one duplicate to be necessary
        Assertions.assertEquals(1, pointsToCreate.size());
        final DataPoint newPoint = pointsToCreate.get(0);
        Assertions.assertNotSame(point, newPoint);
        Assertions.assertEquals(point.getLatitude().output(Coordinate.FORMAT_DEG_MIN_SEC), newPoint.getLatitude().output(Coordinate.FORMAT_DEG_MIN_SEC));
        Assertions.assertEquals(point.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC), newPoint.getLongitude().output(Coordinate.FORMAT_DEG_MIN_SEC));
        // Two photos to be connected
        Assertions.assertEquals(2, pointPhotoPairs.size());
        Assertions.assertEquals(point, pointPhotoPairs.get(0).getPoint());
        Assertions.assertEquals(newPoint, pointPhotoPairs.get(1).getPoint());
        Assertions.assertEquals(photo1, pointPhotoPairs.get(0).getPhoto());
        Assertions.assertEquals(photo2, pointPhotoPairs.get(1).getPhoto());
    }
}
