@echo off
SET MC_VERSION=1.9.2
SET CSL_VERSION=13.7a
cd src
javac -source 1.6 -target 1.6 -d ../bin -cp ;../lib/commons-codec-1.9.jar;../lib/commons-io-2.4.jar;../lib/gson-2.2.4.jar;../lib/guava-17.0.jar;../lib/authlib-1.5.22.jar;../%MC_VERSION%.jar *.java
javac -source 1.6 -target 1.6 -d ../bin -cp ;../lib/commons-io-2.4.jar;../lib/launchwrapper-1.7.jar customskinloader/tweaker/*.java
cd ../bin
del ../out/CustomSkinLoader_%MC_VERSION%-%CSL_VERSION%.jar
jar cfm ../out/CustomSkinLoader_%MC_VERSION%-%CSL_VERSION%.jar META-INF/MANIFEST.MF .
pause