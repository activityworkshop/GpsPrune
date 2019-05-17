# Build script
# Version number
PRUNENAME=gpsprune_19.2
# remove compile directory
rm -rf compile
# remove dist directory
rm -rf dist
# create compile directory
mkdir compile
# compile java
javac -d compile $( find tim -name "*.java" -print )
# add other required resources
cp -r tim/prune/lang compile/tim/prune/
cp -r tim/prune/*.txt compile/tim/prune/
cp -r tim/prune/gui/images compile/tim/prune/gui/
cp tim/prune/function/srtm/srtmtiles.dat compile/tim/prune/function/srtm
# make dist directory
mkdir dist
# build into jar file
jar cfm dist/${PRUNENAME}.jar MANIFEST.MF -C compile .
# finished!
echo "build complete"
