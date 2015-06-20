package idv.jlchntoz.gui;

import idv.jlchntoz.Version;
import idv.jlchntoz.lang.ILanguage;
import idv.jlchntoz.lang.LangManager;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class MainFrame {
	private static final ILanguage lang=LangManager.getLanguage();
	public Version patchVersion=new Version("1.0");
	public static boolean useDataFolder=false;
	
	public static final Color bg2=new Color(244, 243, 222, 255);
	public static final Color bg3=new Color(190, 173, 146, 150);
	public static final Color bg5=new Color(91, 73, 70, 255);
	
	public static final Font defaultBigFont=new Font("Î¢ÈíÑÅºÚ", Font.BOLD, 30);
	public static final Font defaultSmallFont=new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 15);
	public static final Font defaultSmallerFont=new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13);
	public static final Font englishBigFont=new Font("Comic Sans MS", Font.BOLD, 30);
	public static final Font englishSmallFont=new Font("Comic Sans MS", Font.PLAIN, 15);

	public void init(String[] args) {
		
		JFrame jf = new JFrame (lang.title()+lang.gui());  
		Container con = jf.getContentPane();
		con.setLayout(null);
		//setBounds(x,y,width,height)
		int windowWidth = 400;
		int windowHeight = 300;
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
        jf.setBounds(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2,
        		windowWidth , windowHeight);
        
        con.setBackground(bg2);
        
        JLabel title=new JLabel();
        title.setForeground(bg5);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setText(lang.title()+lang.index());
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	title.setFont(englishBigFont);
        else
        	title.setFont(defaultBigFont);
        title.setBounds(15, 5, 370, 40);
        con.add(title);
        
        if(args.length>0 && args[0].equalsIgnoreCase("f")){
			JLabel tip1=new JLabel();
			tip1.setForeground(bg5);
			tip1.setHorizontalAlignment(JLabel.CENTER);
			tip1.setText(lang.first1());
			tip1.setBounds(10, 50, 370, 20);
			if(LangManager.langCode.equalsIgnoreCase("en"))
				tip1.setFont(englishSmallFont);
	        else
	        	tip1.setFont(defaultSmallFont);
			con.add(tip1);
        
			JLabel tip2=new JLabel();
			tip2.setForeground(bg5);
			tip2.setHorizontalAlignment(JLabel.CENTER);
			tip2.setText(lang.first2());
			tip2.setBounds(10, 70, 370, 20);
			if(LangManager.langCode.equalsIgnoreCase("en"))
				tip2.setFont(englishSmallFont);
	        else
	        	tip2.setFont(defaultSmallFont);
			con.add(tip2);
        }
        if(args.length>1){
        	patchVersion=new Version(args[1]);
        	if(patchVersion.isNewerThanOrEquals("12.1"))
        		useDataFolder=true;
			JLabel tip3=new JLabel();
			tip3.setForeground(bg5);
			tip3.setHorizontalAlignment(JLabel.CENTER);
			tip3.setText(lang.yourVersion()+" "+args[1]);
			tip3.setBounds(10, 100, 370, 20);
			if(LangManager.langCode.equalsIgnoreCase("en"))
				tip3.setFont(englishSmallFont);
	        else
	        	tip3.setFont(defaultSmallFont);
			con.add(tip3);
        }
        
        JButton skinBtn=new JButton();
        skinBtn.setForeground(bg5);
        skinBtn.setBackground(bg3);
        skinBtn.setText(lang.skin()+lang.loadList());
        skinBtn.setBounds(15, 180, 370, 30);
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	skinBtn.setFont(englishSmallFont);
        else
        	skinBtn.setFont(defaultSmallFont);
        con.add(skinBtn);
        skinBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed (ActionEvent e){
            	String[] aa={"skin"};
                SkinFrame.main(aa);
            };  
        });
        
        JButton cloakBtn=new JButton();
        cloakBtn.setForeground(bg5);
        cloakBtn.setBackground(bg3);
        cloakBtn.setText(lang.cloak()+lang.loadList());
        cloakBtn.setBounds(15, 215, 370, 30);
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	cloakBtn.setFont(englishSmallFont);
        else
        	cloakBtn.setFont(defaultSmallFont);
        con.add(cloakBtn);
        cloakBtn.addActionListener(new ActionListener(){ 
            @Override
            public void actionPerformed (ActionEvent e){
            	String[] aa={"cape"};
                SkinFrame.main(aa);
            };  
        });
        
        
        jf.setVisible(true);
        if(args.length>=3&&args[2].equalsIgnoreCase("in")){
            jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }else{
            jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        jf.setResizable(false);
	}

}
