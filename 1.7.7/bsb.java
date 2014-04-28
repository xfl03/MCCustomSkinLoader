// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;


public class bsb extends bse
{

	private static final AtomicInteger d = new AtomicInteger(0);
	private final File e;
	private final String f;
	private final boh g;
	private BufferedImage h;
	private Thread i;
	private boolean j;

	public bsb(File file, String s, bth bth, boh boh1)
	{
		super(bth);
		e = file;
		f = s;
		g = boh1;
	}

	private void f()
	{
		if (j)
			return;
		if (h != null)
		{
			if (b != null)
				c();
			bss.a(super.b(), h);
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

	public void a(bti bti1)
	{
		if (h == null && b != null)
			super.a(bti1);
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
		i = new bsc(this, (new StringBuilder()).append("Texture Downloader #").append(d.incrementAndGet()).toString());
		i.setDaemon(true);
		i.start();
	}

	static String a(bsb bsb1)
	{
		return bsb1.f;
	}

	static File b(bsb bsb1)
	{
		return bsb1.e;
	}



	static boh c(bsb bsb1)
	{
		return bsb1.g;
	}

}
