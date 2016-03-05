// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import java.awt.Graphics;
import java.awt.image.*;

public class bnp
	implements bnj
{

	private int a[];
	private int b;
	private int c;

	public bnp()
	{
	}

	public BufferedImage a(BufferedImage image)
	{
		if (image == null)
			return null;
		
		int ratio = image.getWidth() / 64;
		b = 64 * ratio;
		c = 64 * ratio;
		BufferedImage var2 = new BufferedImage(b, c, 2);
		Graphics var3 = var2.getGraphics();
		var3.drawImage(image, 0, 0, null);
		if (image.getHeight() == 32 * ratio)
		{
			var3.drawImage(var2, 24 * ratio, 48 * ratio, 20 * ratio, 52 * ratio,  4 * ratio, 16 * ratio,  8 * ratio, 20 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 28 * ratio, 48 * ratio, 24 * ratio, 52 * ratio,  8 * ratio, 16 * ratio, 12 * ratio, 20 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 20 * ratio, 52 * ratio, 16 * ratio, 64 * ratio,  8 * ratio, 20 * ratio, 12 * ratio, 32 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 24 * ratio, 52 * ratio, 20 * ratio, 64 * ratio,  4 * ratio, 20 * ratio,  8 * ratio, 32 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 28 * ratio, 52 * ratio, 24 * ratio, 64 * ratio,  0 * ratio, 20 * ratio,  4 * ratio, 32 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 32 * ratio, 52 * ratio, 28 * ratio, 64 * ratio, 12 * ratio, 20 * ratio, 16 * ratio, 32 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 40 * ratio, 48 * ratio, 36 * ratio, 52 * ratio, 44 * ratio, 16 * ratio, 48 * ratio, 20 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 44 * ratio, 48 * ratio, 40 * ratio, 52 * ratio, 48 * ratio, 16 * ratio, 52 * ratio, 20 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 36 * ratio, 52 * ratio, 32 * ratio, 64 * ratio, 48 * ratio, 20 * ratio, 52 * ratio, 32 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 40 * ratio, 52 * ratio, 36 * ratio, 64 * ratio, 44 * ratio, 20 * ratio, 48 * ratio, 32 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 44 * ratio, 52 * ratio, 40 * ratio, 64 * ratio, 40 * ratio, 20 * ratio, 44 * ratio, 32 * ratio, (ImageObserver)null);
            var3.drawImage(var2, 48 * ratio, 52 * ratio, 44 * ratio, 64 * ratio, 52 * ratio, 20 * ratio, 56 * ratio, 32 * ratio, (ImageObserver)null);
		}
		var3.dispose();
		a = ((DataBufferInt)var2.getRaster().getDataBuffer()).getData();
		b(0  * ratio, 0  * ratio, 32 * ratio, 16 * ratio);
		a(32 * ratio, 0  * ratio, 64 * ratio, 32 * ratio);
		b(0  * ratio, 16 * ratio, 64 * ratio, 32 * ratio);
		a(0  * ratio, 32 * ratio, 16 * ratio, 48 * ratio);
		a(16 * ratio, 32 * ratio, 40 * ratio, 48 * ratio);
		a(40 * ratio, 32 * ratio, 56 * ratio, 48 * ratio);
		a(0  * ratio, 48 * ratio, 16 * ratio, 64 * ratio);
		b(16 * ratio, 48 * ratio, 48 * ratio, 64 * ratio);
		a(48 * ratio, 48 * ratio, 64 * ratio, 64 * ratio);
		return var2;
	}

	public void a()
	{
	}

	private void a(int p1, int p2, int p3, int p4)
	{
		if (c(p1, p2, p3, p4))
			return;
		for (int i = p1; i < p3; i++)
		{
			for (int j = p2; j < p4; j++)
				a[i + j * b] &= 0xffffff;

		}

	}

	private void b(int p1, int p2, int p3, int p4)
	{
		for (int i = p1; i < p3; i++)
		{
			for (int j = p2; j < p4; j++)
				a[i + j * b] |= 0xff000000;

		}

	}

	private boolean c(int p1, int p2, int p3, int p4)
	{
		for (int i = p1; i < p3; i++)
		{
			for (int j = p2; j < p4; j++)
			{
				int k = a[i + j * b];
				if ((k >> 24 & 0xff) < 128)
					return true;
			}

		}

		return false;
	}
}
