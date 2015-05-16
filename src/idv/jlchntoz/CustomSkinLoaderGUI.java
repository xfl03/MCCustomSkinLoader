package idv.jlchntoz;

import idv.jlchntoz.gui.MainFrame;

import java.util.logging.Logger;

public class CustomSkinLoaderGUI {
	
	public static final String VERSION ="1.0.2";
	
	public static final Logger log=Logger.getLogger("CustomSkinLoader-GUI");

	public static void main(String[] args) {
		log.info("Thanks for using CustomSkinLoader!");
		log.info("Version "+VERSION+" !");
		MainFrame mf=new MainFrame();
		mf.init(args);
		
		

	}

}
