set -e
# Build script
# Version number
PRUNENAME=gpsprune_19.2
# remove compile directory
rm -rf compile
# remove dist directory
rm -rf dist
# create compile directory
mkdir compile
echo "building..."
# compile java
# TODO: If your java3d libraries are not under /usr/share/java, please edit the following line with the correct path
javac -d compile -cp /usr/share/java/vecmath.jar:/usr/share/java/j3dutils.jar:/usr/share/java/j3dcore.jar $( find src -name "*.java" -print )
# add other required resources
cp -r src/tim/prune/lang compile/tim/prune/
cp -r src/tim/prune/*.txt compile/tim/prune/
cp -r src/tim/prune/gui/images compile/tim/prune/gui/
cp src/tim/prune/function/srtm/srtmtiles.dat compile/tim/prune/function/srtm
# make dist directory
mkdir dist
# build into jar file
jar cfm dist/${PRUNENAME}.jar buildtools/MANIFEST.MF -C compile .
# finished!
echo "build complete"

