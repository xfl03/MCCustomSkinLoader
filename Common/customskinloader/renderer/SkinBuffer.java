package customskinloader.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;

public class SkinBuffer implements net.minecraft.client.renderer.IImageBuffer {

	private int[] imageData;
	private int ratio=1;
	
	@Override
	public BufferedImage parseUserSkin(BufferedImage image) {
		if(image==null)
			return null;
		ratio = image.getWidth() / 64;
		BufferedImage bufferedimage = new BufferedImage(64 * ratio, 64 * ratio, 2);
		Graphics graphics = bufferedimage.getGraphics();
		graphics.setColor(new Color(0,0,0,0));
		graphics.fillRect(0,0,64,64);
		graphics.drawImage(image,0,0,(ImageObserver)null);
		if (image.getHeight() == 32 * ratio){//Single Layer
			//Revert to original algorithm
			//Right Leg -> Left Leg
			graphics.drawImage(bufferedimage, 24 * ratio, 48 * ratio, 20 * ratio, 52 * ratio,  4 * ratio, 16 * ratio,  8 * ratio, 20 * ratio, (ImageObserver)null);//Top
			graphics.drawImage(bufferedimage, 28 * ratio, 48 * ratio, 24 * ratio, 52 * ratio,  8 * ratio, 16 * ratio, 12 * ratio, 20 * ratio, (ImageObserver)null);//Bottom
			graphics.drawImage(bufferedimage, 20 * ratio, 52 * ratio, 16 * ratio, 64 * ratio,  8 * ratio, 20 * ratio, 12 * ratio, 32 * ratio, (ImageObserver)null);//Left
			graphics.drawImage(bufferedimage, 24 * ratio, 52 * ratio, 20 * ratio, 64 * ratio,  4 * ratio, 20 * ratio,  8 * ratio, 32 * ratio, (ImageObserver)null);//Front
			graphics.drawImage(bufferedimage, 28 * ratio, 52 * ratio, 24 * ratio, 64 * ratio,  0 * ratio, 20 * ratio,  4 * ratio, 32 * ratio, (ImageObserver)null);//Right
			graphics.drawImage(bufferedimage, 32 * ratio, 52 * ratio, 28 * ratio, 64 * ratio, 12 * ratio, 20 * ratio, 16 * ratio, 32 * ratio, (ImageObserver)null);//Back
			//Right Arm -> Left Arm
			graphics.drawImage(bufferedimage, 40 * ratio, 48 * ratio, 36 * ratio, 52 * ratio, 44 * ratio, 16 * ratio, 48 * ratio, 20 * ratio, (ImageObserver)null);//Top
			graphics.drawImage(bufferedimage, 44 * ratio, 48 * ratio, 40 * ratio, 52 * ratio, 48 * ratio, 16 * ratio, 52 * ratio, 20 * ratio, (ImageObserver)null);//Bottom
			graphics.drawImage(bufferedimage, 36 * ratio, 52 * ratio, 32 * ratio, 64 * ratio, 48 * ratio, 20 * ratio, 52 * ratio, 32 * ratio, (ImageObserver)null);//Left
			graphics.drawImage(bufferedimage, 40 * ratio, 52 * ratio, 36 * ratio, 64 * ratio, 44 * ratio, 20 * ratio, 48 * ratio, 32 * ratio, (ImageObserver)null);//Front
			graphics.drawImage(bufferedimage, 44 * ratio, 52 * ratio, 40 * ratio, 64 * ratio, 40 * ratio, 20 * ratio, 44 * ratio, 32 * ratio, (ImageObserver)null);//Right
			graphics.drawImage(bufferedimage, 48 * ratio, 52 * ratio, 44 * ratio, 64 * ratio, 52 * ratio, 20 * ratio, 56 * ratio, 32 * ratio, (ImageObserver)null);//Back
		}
		graphics.dispose();
		this.imageData = ((DataBufferInt)bufferedimage.getRaster().getDataBuffer()).getData();
		
		setAreaDueToConfig( 0 * ratio, 0 * ratio,32 * ratio,16 * ratio);//Head - 1
		setAreaTransparent(32 * ratio, 0 * ratio,64 * ratio,16 * ratio);//Head - 2
		
		setAreaDueToConfig( 0 * ratio,16 * ratio,16 * ratio,32 * ratio);//Right Leg - 1
		setAreaDueToConfig(16 * ratio,16 * ratio,40 * ratio,32 * ratio);//Body - 1
		setAreaDueToConfig(40 * ratio,16 * ratio,56 * ratio,32 * ratio);//Right Arm - 1
		
		setAreaTransparent( 0 * ratio,32 * ratio,16 * ratio,48 * ratio);//Right Leg - 2
		setAreaTransparent(16 * ratio,32 * ratio,40 * ratio,48 * ratio);//Body - 2
		setAreaTransparent(40 * ratio,32 * ratio,56 * ratio,48 * ratio);//Right Arm - 2
		
		setAreaTransparent( 0 * ratio,48 * ratio,16 * ratio,64 * ratio);//Left Leg - 2
		setAreaDueToConfig(16 * ratio,48 * ratio,32 * ratio,64 * ratio);//Left Leg - 1
		setAreaDueToConfig(32 * ratio,48 * ratio,48 * ratio,64 * ratio);//Left Arm - 1
		setAreaTransparent(48 * ratio,48 * ratio,64 * ratio,64 * ratio);//Left Arm - 2
		
		return bufferedimage;
	}
	
	/* 2^24-1 
	 * 00000000 11111111 11111111 11111111 */
	private static final int A=16777215;
	private static final int WHITE=getARGB(255,255,255,255);
	private static final int BLACK=getARGB(255,0,0,0);
	private boolean isFilled(int x0,int y0,int x1,int y1){
		int data=this.imageData[getPosition(x0,y0)];
		if(data!=WHITE && data!=BLACK)
			return false;
		for (int x=x0;x<x1;++x)
			for (int y=y0;y<y1;++y)
				if(this.imageData[getPosition(x,y)]!=data)
					return false;
		return true;
	}
	private void setAreaTransparent(int x0,int y0,int x1,int y1){
		if(!isFilled(x0,y0,x1,y1))
			return;
		for (int x=x0;x<x1;++x)
			for (int y=y0;y<y1;++y)
				this.imageData[getPosition(x,y)] &= A;
	}
	
	/* -2^24
	 *  00000001 00000000 00000000 00000000 ->
	 *  11111110 11111111 11111111 11111111 ->
	 *  11111111 00000000 00000000 00000000 */
	private static final int B=-16777216;
	private void setAreaOpaque(int x0,int y0,int x1,int y1){
		for (int x=x0;x<x1;++x)
			for (int y=y0;y<y1;++y)
				this.imageData[getPosition(x,y)] |= B;
	}
	
	private void setAreaDueToConfig(int x0,int y0,int x1,int y1){
		if(customskinloader.CustomSkinLoader.config.enableTransparentSkin)
			setAreaTransparent(x0,y0,x1,y1);
		else
			setAreaOpaque(x0,y0,x1,y1);
	}


	@Override
	public void skinAvailable(){
		//A callback when skin loaded
	}
	
	private static int getARGB(int a,int r,int g,int b){
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	private int getPosition(int x,int y){
		return x+y*64*ratio;
	}
}
