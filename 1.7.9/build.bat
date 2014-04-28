@echo off
title CustomSkinLoader 1.7.9 Builder
echo Please Build with Minecraft 1.7.9 classes
echo Building CustomSkinLoader.java[Main]
cd idv/jlchntoz
javac CustomSkinLoader.java
cd ../..
echo Building boo.java[For HD Skin]
javac boo.java
echo Building bsd.java[DownloadImage]
javac bsd.java
echo Building bse.java[ThreadDownloadImage]
javac bse.java
pause