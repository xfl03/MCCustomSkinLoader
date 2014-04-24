// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

public class bqk extends bqn
{

	private static final AtomicInteger d = new AtomicInteger(0);
	private final String e;
	private final bmq f;
	private BufferedImage g;
	private Thread h;
	private boolean i;

	public bqk(String s, brq brq, bmq bmq)
	{
		super(brq);
		e = s;
		f = bmq;
	}

	private void e()
	{
		if (i)
			return;
		if (g != null)
		{
			if (b != null)
				c();
			brb.a(super.b(), g);
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

	public void a(brr brr1)
	{
		if (g == null && b != null)
			super.a(brr1);
		if (h == null)
		{
			h = new bql(this, (new StringBuilder()).append("Texture Downloader #").append(d.incrementAndGet()).toString());
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

	static String a(bqk bqk1)
	{
		return bqk1.e;
	}

	static bmq b(bqk bqk1)
	{
		return bqk1.f;
	}


}
