@echo off
title CustomSkinLoader 1.7.5 Builder
echo Please Build with Minecraft 1.7.5 classes
echo Building CustomSkinLoader.java[Main]
cd idv/jlchntoz
javac CustomSkinLoader.java
cd ../..
echo Building bnb.java[For HD Skin]
javac bnb.java
echo Building bqq.java[DownloadImage]
javac bqq.java
echo Building bqr.java[ThreadDownloadImage]
javac bqr.java
pause