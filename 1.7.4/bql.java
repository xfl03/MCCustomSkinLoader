// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

import idv.jlchntoz.CustomSkinLoader;
class bql extends Thread
{

	final bqk a;

	bql(bqk bqk1, String s)
	{
		a = bqk1;
	}

	public void run()
	{
		CustomSkinLoader Loader = new CustomSkinLoader();

        try
        {
            BufferedImage var2 = ImageIO.read(Loader.getPlayerSkinStream(bqk.a(a)));
			
			a.a(var2);
        }
        catch (Exception var6)
        {
            return;
        }
		
		
		
		
	}
}
