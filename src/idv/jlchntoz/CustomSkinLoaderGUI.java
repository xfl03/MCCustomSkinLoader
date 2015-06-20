package idv.jlchntoz;

import idv.jlchntoz.gui.MainFrame;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class CustomSkinLoaderGUI {
	
	public static final String VERSION ="1.1.0";
	
	public static final Logger log=Logger.getLogger("CustomSkinLoader-GUI");
	public static final File LCK=new File("GUI.lck");
	private static final long SECONDS=30;

	public static void main(String[] args) {
		log.info("Thanks for using CustomSkinLoader!");
		log.info("Version "+VERSION+" !");
		long dt=System.currentTimeMillis()-SECONDS*1000;
		if(LCK.exists()&&LCK.lastModified()>=dt){
			log.info("Sorry! GUI has already been running!");
			log.info("If you believe it is a mistake, please delete 'GUI.lck'.");
		}else{
			LCK.delete();
			try {
				LCK.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			LCK.deleteOnExit();
			MainFrame mf=new MainFrame();
			mf.init(args);
		}
	}

}
