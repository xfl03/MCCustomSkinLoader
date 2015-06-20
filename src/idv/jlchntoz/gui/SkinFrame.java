package idv.jlchntoz.gui;

import idv.jlchntoz.lang.ILanguage;
import idv.jlchntoz.lang.LangManager;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class SkinFrame {
	private static ILanguage lang=LangManager.getLanguage();
	public static boolean mod=false;
	private static String to="skin";
	
	public static final String[] urls0={
		"#"+lang.minecraftOffical(),
		"http://s3.amazonaws.com/MinecraftSkins/*.png",
		"http://skins.minecraft.net/MinecraftSkins/*.png",
		"#MineCrack",
		"http://minecrack.fr.nf/mc/skinsminecrackd/*.png",
		/*
		 * XFL03's Skin Website Closed
		"#OpenSkin",
		"http://skin.axdt.net/MinecraftSkins/*.png",
		*/
		"#SkinMe",
		"http://www.skinme.cc:88/MinecraftSkins/*.png"
		
	};
	public static final String[] urls1={
		"#"+lang.minecraftOffical(),
		"http://s3.amazonaws.com/MinecraftCloaks/*.png",
		"http://skins.minecraft.net/MinecraftCloaks/*.png",
		"#Optifine",
		"http://s.optifine.net/capes/*.png",
		"#MineCrack",
		"http://minecrack.fr.nf/mc/cloaksminecrackd/*.png",
		/*
		 * XFL03's Skin Website Closed
		"#OpenSkin",
		"http://skin.axdt.net/MinecraftCloaks/*.png",
		*/
		"#SkinMe",
		"http://www.skinme.cc:88/MinecraftCloaks/*.png"
		
	};

	public static void main(String[] args) {
		if(args.length>0)
			to=args[0];
		final String[] urls;
		String ti="";
		if(to.equalsIgnoreCase("cape")){
			urls=urls1;
			ti=lang.cloak();
		}else{
			urls=urls0;
			ti=lang.skin();
		}
		final JFrame jf = new JFrame (lang.title()+" - "+ti+lang.loadList());  
		Container con = jf.getContentPane();
		con.setLayout(null);
		con.setBackground(MainFrame.bg2);
		int windowWidth = 507;
		int windowHeight = 420;
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
        jf.setBounds(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2,
        		windowWidth , windowHeight);
        
        JLabel title=new JLabel();
        title.setForeground(MainFrame.bg5);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setText(ti+lang.loadList());
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	title.setFont(MainFrame.englishBigFont);
        else
        	title.setFont(MainFrame.defaultBigFont);
        title.setBounds(10, 5, 480, 30);
        con.add(title);
        
        final JTextArea skinList=new JTextArea();
        skinList.setBounds(15, 45, 470, 260);
        skinList.setFont(MainFrame.defaultSmallerFont);
        skinList.setForeground(MainFrame.bg5);
        con.add(skinList);
        File temp0=new File("CustomSkinLoader/"+to+"urls.txt");
        if(temp0.exists())
        	skinList.setText(read(temp0));
        else{
        	
        	skinList.setText(read(new File(""+to+"urls.txt")));
        }
        
        JButton defaultBtn=new JButton();
        defaultBtn.setText(lang.useDefault());
		defaultBtn.setBounds(15, 310, 233, 30);
		defaultBtn.setForeground(MainFrame.bg5);
        defaultBtn.setBackground(MainFrame.bg3);
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	defaultBtn.setFont(MainFrame.englishSmallFont);
        else
        	defaultBtn.setFont(MainFrame.defaultSmallFont);
        con.add(defaultBtn);
        defaultBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String temp="";
            	for(int i=0;i<=2;i++)
            		temp+=urls[i]+"\r\n";
                skinList.setText(temp);
            };  
        });
        JButton urlBtn=new JButton();
        urlBtn.setText(lang.useRecommended());
		urlBtn.setBounds(252, 310, 235, 30);
		urlBtn.setForeground(MainFrame.bg5);
		urlBtn.setBackground(MainFrame.bg3);
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	urlBtn.setFont(MainFrame.englishSmallFont);
        else
        	urlBtn.setFont(MainFrame.defaultSmallFont);
        con.add(urlBtn);
        urlBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String temp=skinList.getText()+"\r\n";
            	if(temp.equalsIgnoreCase("\r\n")){
            		temp="";
            	}
            	for(int i=0;i<urls.length;i++)
            		temp+=urls[i]+"\r\n";
                skinList.setText(temp);
            };
        });
        
        
        JButton saveBtn=new JButton();
        saveBtn.setText(lang.save());
        saveBtn.setBounds(15, 350, 233, 30);
        saveBtn.setForeground(MainFrame.bg5);
        saveBtn.setBackground(MainFrame.bg3);
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	saveBtn.setFont(MainFrame.englishSmallFont);
        else
        	saveBtn.setFont(MainFrame.defaultSmallFont);
        con.add(saveBtn);
        saveBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
                save(skinList.getText());
                JOptionPane.showMessageDialog(null,lang.saved());
            };
        });
        
        JButton closeBtn=new JButton();
        closeBtn.setText(lang.close());
        closeBtn.setBounds(252, 350, 235, 30);
        closeBtn.setForeground(MainFrame.bg5);
        closeBtn.setBackground(MainFrame.bg3);
        if(LangManager.langCode.equalsIgnoreCase("en"))
        	closeBtn.setFont(MainFrame.englishSmallFont);
        else
        	closeBtn.setFont(MainFrame.defaultSmallFont);
        con.add(closeBtn);
        closeBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
                jf.setVisible(false);
            };
        });
        
        
        jf.setVisible(true);
        jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        jf.setResizable(false); 

	}
	
	private static void save(String data){
		File a=new File(to+"urls.txt");
		File b=new File("CustomSkinLoader/"+to+"urls.txt");
		//System.out.println(b.getAbsolutePath());
		try {
			if(!MainFrame.useDataFolder)
				a.createNewFile();
			b.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!MainFrame.useDataFolder)
			write(a,data);
		if(b.exists())
			write(b,data);
	}
	
	private static boolean write(File file,String Data){
		System.out.println("Write to "+file.getAbsolutePath());
	   	 FileWriter fw=null;
	        try{
	            fw = new FileWriter(file);
	            fw.write(Data,0,Data.length()); 
	            fw.flush();
	            fw.close();
	        }catch(Exception ex){
	            ex.printStackTrace();
	            return false;
	        }
	        return true;
	   }
	private static String read(File file){
		System.out.println("Read from "+file.getAbsolutePath());
   	 BufferedReader br = null;
        String data="";
        try {
            br = new BufferedReader(new FileReader(file));
            data = br.readLine();
            while( br.ready()){   
            	data += "\r\n"+br.readLine(); 
            }
            br.close();
            return data;
        } catch (Exception ex) {
            return "";
        }
       
   }

}
