// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;

public class bsd extends bsg
{

	
	private static final AtomicInteger d = new AtomicInteger(0);
	private final File e;
	private final String f;
	private final boj g;
	private BufferedImage h;
	private Thread i;
	private boolean j;

	public bsd(File file, String s, btj btj, boj boj1)
	{
		super(btj);
		e = file;
		f = s;
		g = boj1;
	}

	private void f()
	{
		if (j)
			return;
		if (h != null)
		{
			if (b != null)
				c();
			bsu.a(super.b(), h);
			j = true;
		}
	}

	public int b()
	{
		f();
		return super.b();
	}

	public void a(BufferedImage bufferedimage)
	{
		h = bufferedimage;
		if (g != null)
			g.a();
	}

	public void a(btk btk1)
	{
		if (h == null && b != null)
			super.a(btk1);
		if (i == null)
			if (e != null && e.isFile())
			{
				
				try
				{
					h = ImageIO.read(e);
					if (g != null)
						a(g.a(h));
				}
				catch (IOException ioexception)
				{
		
					a();
				}
			} else
			{
				a();
			}
	}

	protected void a()
	{
		i = new bse(this, (new StringBuilder()).append("Texture Downloader #").append(d.incrementAndGet()).toString());
		i.setDaemon(true);
		i.start();
	}

	static String a(bsd bsd1)
	{
		return bsd1.f;
	}

	static File b(bsd bsd1)
	{
		return bsd1.e;
	}

	
	static boj c(bsd bsd1)
	{
		return bsd1.g;
	}

}
