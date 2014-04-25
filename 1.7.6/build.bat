@echo off
title CustomSkinLoader 1.7.6 Builder
echo Please Build with Minecraft 1.7.6 classes
echo Building CustomSkinLoader.java[Main]
cd idv/jlchntoz
javac CustomSkinLoader.java
cd ../..
echo Building bom.java[For HD Skin]
javac bom.java
echo Building bsb.java[DownloadImage]
javac bsb.java
echo Building bsc.java[ThreadDownloadImage]
javac bsc.java
pause