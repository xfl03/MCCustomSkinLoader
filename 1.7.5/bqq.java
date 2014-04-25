// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;


public class bqq extends bqt
{

	private static final AtomicInteger d = new AtomicInteger(0);
	private final String e;
	private final bmw f;
	private BufferedImage g;
	private Thread h;
	private boolean i;

	public bqq(String s, brw brw, bmw bmw)
	{
		super(brw);
		e = s;
		f = bmw;
	}

	private void e()
	{
		if (i)
			return;
		if (g != null)
		{
			if (b != null)
				c();
			brh.a(super.b(), g);
			i = true;
		}
	}

	public int b()
	{
		e();
		return super.b();
	}

	public void a(BufferedImage bufferedimage)
	{
		g = bufferedimage;
	}

	public void a(brx brx1)
	{
		if (g == null && b != null)
			super.a(brx1);
		if (h == null)
		{
			h = new bqr(this, (new StringBuilder()).append("Texture Downloader #").append(d.incrementAndGet()).toString());
			h.setDaemon(true);
			h.setName((new StringBuilder()).append("Skin downloader: ").append(e).toString());
			h.start();
		}
	}

	public boolean a()
	{
		e();
		return i;
	}

	public static String a(bqq bqq1)
	{
		return bqq1.e;
	}

	static bmw b(bqq bqq1)
	{
		return bqq1.f;
	}



}
