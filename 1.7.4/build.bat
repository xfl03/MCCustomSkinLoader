@echo off
title CustomSkinLoader 1.7.4 Builder
echo Please Build with Minecraft 1.7.4 classes
echo Building CustomSkinLoader.java[Main]
cd idv/jlchntoz
javac CustomSkinLoader.java
cd ../..
echo Building bmv.java[For HD Skin]
javac bmv.java
echo Building bqk.java[DownloadImage]
javac bqk.java
echo Building bql.java[ThreadDownloadImage]
javac bql.java
pause