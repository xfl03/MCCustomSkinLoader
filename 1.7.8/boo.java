// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import java.awt.Graphics;
import java.awt.image.*;

public class boo
	implements boj
{

	private int a[];
	private int b;
	private int c;

	public boo()
	{
	}

	public BufferedImage a(BufferedImage bufferedimage)
	{
			if (bufferedimage == null)
			return null;
		b = bufferedimage.getWidth(null);
		c = bufferedimage.getHeight(null);
		BufferedImage bufferedimage1 = new BufferedImage(b, c, 2);
		Graphics g = bufferedimage1.getGraphics();
		g.drawImage(bufferedimage, 0, 0, null);
		g.dispose();
		a = ((DataBufferInt)bufferedimage1.getRaster().getDataBuffer()).getData();
		if (b == 128 && c == 64)
		{
			b(0, 0, 0, 0);
			a(32, 0, 128, 64);
			b(0, 16, 0, 0);
		} else
		if (b == 256 && c == 128)
		{
			b(0, 0, 0, 0);
			a(32, 0, 256, 128);
			b(0, 16, 0, 0);
		} else
		if (b == 512 && c == 256)
		{
			b(0, 0, 32, 16);
			a(32, 0, 512, 256);
			b(0, 16, 0, 0);
		} else
		{
			b(0, 0, 32, 16);
			a(32, 0, 64, 32);
			b(0, 16, 64, 32);
		}
		boolean flag = false;
		for (int i = 32; i < 64; i++)
		{
			for (int k = 0; k < 16; k++)
			{
				int i1 = a[i + k * 64];
				if ((i1 >> 24 & 0xff) < 128)
					flag = true;
			}

		}

		if (!flag)
		{
			for (int j = 32; j < 64; j++)
			{
				for (int l = 0; l < 16; l++)
				{
					int j1 = a[j + l * 64];
					boolean flag1;
					if ((j1 >> 24 & 0xff) < 128)
						flag1 = true;
				}

			}

		}
		return bufferedimage1;
	}

	public void a()
	{
	}

	private void a(int i, int j, int k, int l)
	{
		if (c(i, j, k, l))
			return;
		for (int i1 = i; i1 < k; i1++)
		{
			for (int j1 = j; j1 < l; j1++)
				a[i1 + j1 * b] &= 0xffffff;

		}

	}

	private void b(int i, int j, int k, int l)
	{
		for (int i1 = i; i1 < k; i1++)
		{
			for (int j1 = j; j1 < l; j1++)
				a[i1 + j1 * b] |= 0xff000000;

		}

	}

	private boolean c(int i, int j, int k, int l)
	{
		for (int i1 = i; i1 < k; i1++)
		{
			for (int j1 = j; j1 < l; j1++)
			{
				int k1 = a[i1 + j1 * b];
				if ((k1 >> 24 & 0xff) < 128)
					return true;
			}

		}

		return false;
	}
}
