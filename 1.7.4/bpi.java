// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   SourceFile

import org.lwjgl.opengl.GL11;

public class bpi extends bpa
{

	private static final brq a = new brq("textures/entity/steve.png");
	private bih f;
	private bih g;
	private bih h;

	public bpi()
	{
		super(new bih(0.0F), 0.5F);
		f = (bih)i;
		g = new bih(1.0F);
		h = new bih(0.5F);
	}

	protected int a(bma bma1, int i, float f1)
	{
		abu abu1 = bma1.bm.d(3 - i);
		if (abu1 != null)
		{
			abs abs1 = abu1.b();
			if (abs1 instanceof zs)
			{
				zs zs1 = (zs)abs1;
				a(bop.a(zs1, i));
				bih bih1 = i != 2 ? g : h;
				bih1.c.j = i == 0;
				bih1.d.j = i == 0;
				bih1.e.j = i == 1 || i == 2;
				bih1.f.j = i == 1;
				bih1.g.j = i == 1;
				bih1.h.j = i == 2 || i == 3;
				bih1.i.j = i == 2 || i == 3;
				a(((bim) (bih1)));
				bih1.p = this.i.p;
				bih1.q = this.i.q;
				bih1.s = this.i.s;
				if (zs1.m_() == zu.a)
				{
					int j = zs1.b(abu1);
					float f2 = (float)(j >> 16 & 0xff) / 255F;
					float f3 = (float)(j >> 8 & 0xff) / 255F;
					float f4 = (float)(j & 0xff) / 255F;
					GL11.glColor3f(f2, f3, f4);
					return !abu1.y() ? 16 : 31;
				}
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				return !abu1.y() ? 1 : 15;
			}
		}
		return -1;
	}

	protected void b(bma bma1, int i, float f1)
	{
		abu abu1 = bma1.bm.d(3 - i);
		if (abu1 != null)
		{
			abs abs1 = abu1.b();
			if (abs1 instanceof zs)
			{
				a(bop.a((zs)abs1, i, "overlay"));
				GL11.glColor3f(1.0F, 1.0F, 1.0F);
			}
		}
	}

	public void a(bma bma1, double d, double d1, double d2, 
			float f1, float f2)
	{
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		abu abu1 = bma1.bm.h();
		g.m = h.m = f.m = abu1 == null ? 0 : 1;
		if (abu1 != null && bma1.bw() > 0)
		{
			acz acz1 = abu1.o();
			if (acz1 == acz.d)
				g.m = h.m = f.m = 3;
			else
			if (acz1 == acz.e)
				g.o = h.o = f.o = true;
		}
		g.n = h.n = f.n = bma1.am();
		double d3 = d1 - (double)bma1.L;
		if (bma1.am() && !(bma1 instanceof bmd))
			d3 -= 0.125D;
		super.a(bma1, d, d3, d2, f1, f2);
		g.o = h.o = f.o = false;
		g.n = h.n = f.n = false;
		g.m = h.m = f.m = 0;
	}

	protected brq a(bma bma1)
	{
		return bma1.t();
	}

	protected void a(bma bma1, float f1)
	{
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		super.c(bma1, f1);
		super.e(bma1, f1);
		abu abu1 = bma1.bm.d(3);
		if (abu1 != null)
		{
			GL11.glPushMatrix();
			f.c.c(0.0625F);
			if (abu1.b() instanceof zy)
			{
				if (bmf.a(ahz.a(abu1.b()).b()))
				{
					float f2 = 0.625F;
					GL11.glTranslatef(0.0F, -0.25F, 0.0F);
					GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
					GL11.glScalef(f2, -f2, -f2);
				}
				b.f.a(bma1, abu1, 0);
			} else
			if (abu1.b() == abv.bL)
			{
				float f3 = 1.0625F;
				GL11.glScalef(f3, -f3, -f3);
				String s = "";
				if (abu1.p() && abu1.q().b("SkullOwner", 8))
					s = abu1.q().j("SkullOwner");
				bnl.b.a(-0.5F, 0.0F, -0.5F, 1, 180F, abu1.k(), s);
			}
			GL11.glPopMatrix();
		}
		if (bma1.b_().equals("deadmau5") && bma1.r().a())
		{
			a(bma1.t());
			for (int i = 0; i < 2; i++)
			{
				float f4 = (bma1.A + (bma1.y - bma1.A) * f1) - (bma1.aN + (bma1.aM - bma1.aN) * f1);
				float f5 = bma1.B + (bma1.z - bma1.B) * f1;
				GL11.glPushMatrix();
				GL11.glRotatef(f4, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(f5, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.375F * (float)(i * 2 - 1), 0.0F, 0.0F);
				GL11.glTranslatef(0.0F, -0.375F, 0.0F);
				GL11.glRotatef(-f5, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-f4, 0.0F, 1.0F, 0.0F);
				float f6 = 1.333333F;
				GL11.glScalef(f6, f6, f6);
				f.b(0.0625F);
				GL11.glPopMatrix();
			}

		}
		boolean flag = bma1.s().a();
		if (flag && !bma1.ao() && !bma1.bS())
		{
			a(bma1.u());
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 0.0F, 0.125F);
			double d = (bma1.bu + (bma1.bx - bma1.bu) * (double)f1) - (bma1.p + (bma1.s - bma1.p) * (double)f1);
			double d1 = (bma1.bv + (bma1.by - bma1.bv) * (double)f1) - (bma1.q + (bma1.t - bma1.q) * (double)f1);
			double d2 = (bma1.bw + (bma1.bz - bma1.bw) * (double)f1) - (bma1.r + (bma1.u - bma1.r) * (double)f1);
			float f16 = bma1.aN + (bma1.aM - bma1.aN) * f1;
			double d3 = oy.a((f16 * 3.141593F) / 180F);
			double d4 = -oy.b((f16 * 3.141593F) / 180F);
			float f18 = (float)d1 * 10F;
			if (f18 < -6F)
				f18 = -6F;
			if (f18 > 32F)
				f18 = 32F;
			float f19 = (float)(d * d3 + d2 * d4) * 100F;
			float f20 = (float)(d * d4 - d2 * d3) * 100F;
			if (f19 < 0.0F)
				f19 = 0.0F;
			float f21 = bma1.br + (bma1.bs - bma1.br) * f1;
			f18 += oy.a((bma1.O + (bma1.P - bma1.O) * f1) * 6F) * 32F * f21;
			if (bma1.am())
				f18 += 25F;
			GL11.glRotatef(6F + f19 / 2.0F + f18, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(f20 / 2.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(-f20 / 2.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			f.c(0.0625F);
			GL11.glPopMatrix();
		}
		abu abu2 = bma1.bm.h();
		if (abu2 != null)
		{
			GL11.glPushMatrix();
			f.f.c(0.0625F);
			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
			if (bma1.bK != null)
				abu2 = new abu(abv.y);
			acz acz1 = null;
			if (bma1.bw() > 0)
				acz1 = abu2.o();
			if ((abu2.b() instanceof zy) && bmf.a(ahz.a(abu2.b()).b()))
			{
				float f7 = 0.5F;
				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
				f7 *= 0.75F;
				GL11.glRotatef(20F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(-f7, -f7, f7);
			} else
			if (abu2.b() == abv.f)
			{
				float f8 = 0.625F;
				GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
				GL11.glRotatef(-20F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(f8, -f8, f8);
				GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
			} else
			if (abu2.b().f())
			{
				float f9 = 0.625F;
				if (abu2.b().g())
				{
					GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
				}
				if (bma1.bw() > 0 && acz1 == acz.d)
				{
					GL11.glTranslatef(0.05F, 0.0F, -0.1F);
					GL11.glRotatef(-50F, 0.0F, 1.0F, 0.0F);
					GL11.glRotatef(-10F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(-60F, 0.0F, 0.0F, 1.0F);
				}
				GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
				GL11.glScalef(f9, -f9, f9);
				GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
			} else
			{
				float f10 = 0.375F;
				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
				GL11.glScalef(f10, f10, f10);
				GL11.glRotatef(60F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20F, 0.0F, 0.0F, 1.0F);
			}
			if (abu2.b().b())
			{
				for (int j = 0; j <= 1; j++)
				{
					int l = abu2.b().a(abu2, j);
					float f12 = (float)(l >> 16 & 0xff) / 255F;
					float f14 = (float)(l >> 8 & 0xff) / 255F;
					float f17 = (float)(l & 0xff) / 255F;
					GL11.glColor4f(f12, f14, f17, 1.0F);
					b.f.a(bma1, abu2, j);
				}

			} else
			{
				int k = abu2.b().a(abu2, 0);
				float f11 = (float)(k >> 16 & 0xff) / 255F;
				float f13 = (float)(k >> 8 & 0xff) / 255F;
				float f15 = (float)(k & 0xff) / 255F;
				GL11.glColor4f(f11, f13, f15, 1.0F);
				b.f.a(bma1, abu2, 0);
			}
			GL11.glPopMatrix();
		}
	}

	protected void b(bma bma1, float f1)
	{
		float f2 = 0.9375F;
		GL11.glScalef(f2, f2, f2);
	}

	protected void a(bma bma1, double d, double d1, double d2, 
			String s, float f1, double d3)
	{
		if (d3 < 100D)
		{
			ayw ayw1 = bma1.bT();
			ayr ayr1 = ayw1.a(2);
			if (ayr1 != null)
			{
				ayt ayt1 = ayw1.a(bma1.b_(), ayr1);
				if (bma1.bl())
					a(((qr) (bma1)), (new StringBuilder()).append(ayt1.c()).append(" ").append(ayr1.d()).toString(), d, d1 - 1.5D, d2, 64);
				else
					a(((qr) (bma1)), (new StringBuilder()).append(ayt1.c()).append(" ").append(ayr1.d()).toString(), d, d1, d2, 64);
				d1 += (float)c().a * 1.15F * f1;
			}
		}
		super.a(bma1, d, d1, d2, s, f1, d3);
	}

	public void a(xq xq)
	{
		float f1 = 1.0F;
		GL11.glColor3f(f1, f1, f1);
		f.p = 0.0F;
		f.a(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, xq);
		f.f.a(0.0625F);
	}

	protected void a(bma bma1, double d, double d1, double d2)
	{
		if (bma1.Y() && bma1.bl())
			super.a(bma1, d + (double)bma1.bC, d1 + (double)bma1.cc, d2 + (double)bma1.bD);
		else
			super.a(bma1, d, d1, d2);
	}

	protected void a(bma bma1, float f1, float f2, float f3)
	{
		if (bma1.Y() && bma1.bl())
		{
			GL11.glRotatef(bma1.bJ(), 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(a(((rm) (bma1))), 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, 1.0F, 0.0F);
		} else
		{
			super.a(bma1, f1, f2, f3);
		}
	}

	protected volatile void a(rm rm, double d, double d1, double d2, 
			String s, float f1, double d3)
	{
		a((bma)rm, d, d1, d2, s, f1, d3);
	}

	protected void a(rm rm, float f1)
	{
		b((bma)rm, f1);
	}

	protected void c(rm rm, int i, float f1)
	{
		b((bma)rm, i, f1);
	}

	protected volatile int a(rm rm, int i, float f1)
	{
		return a((bma)rm, i, f1);
	}

	protected void c(rm rm, float f1)
	{
		a((bma)rm, f1);
	}

	protected volatile void a(rm rm, float f1, float f2, float f3)
	{
		a((bma)rm, f1, f2, f3);
	}

	protected volatile void a(rm rm, double d, double d1, double d2)
	{
		a((bma)rm, d, d1, d2);
	}

	public volatile void a(rm rm, double d, double d1, double d2, 
			float f1, float f2)
	{
		a((bma)rm, d, d1, d2, f1, f2);
	}

	protected volatile brq a(qr qr)
	{
		return a((bma)qr);
	}

	public volatile void a(qr qr, double d, double d1, double d2, 
			float f1, float f2)
	{
		a((bma)qr, d, d1, d2, f1, f2);
	}

}
