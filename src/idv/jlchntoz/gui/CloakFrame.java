package idv.jlchntoz.gui;

import idv.jlchntoz.lang.ILanguage;
import idv.jlchntoz.lang.LangManager;

import java.awt.Container;
import java.awt.Font;
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

public class CloakFrame {
	private static ILanguage lang=LangManager.getLanguage();
	public static boolean mod=false;
	public static final String[] urls={
		"#"+lang.minecraftOffical(),
		"http://s3.amazonaws.com/MinecraftCloaks/*.png",
		"http://skins.minecraft.net/MinecraftCloaks/*.png",
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
		final JFrame jf = new JFrame (lang.title()+" - "+lang.cloak()+lang.loadList());  
		Container con = jf.getContentPane();
		con.setLayout(null);
		//setBounds(x,y,width,height)
        jf.setBounds(400, 200, 515, 420);
        
        Font boldCourier=new Font("Courier", Font.BOLD, 30);
        
        JLabel title=new JLabel();
        title.setText(lang.cloak()+lang.loadList());
        title.setFont(boldCourier);
        title.setBounds(130, 5, 370, 30);
        con.add(title);
        
        final JTextArea cloakList=new JTextArea();
        cloakList.setBounds(15, 40, 470, 266);
        con.add(cloakList);
        cloakList.setText(read("capeurls.txt"));
        
        JButton defaultBtn=new JButton();
        defaultBtn.setText(lang.useDefault());
		defaultBtn.setBounds(15, 310, 235, 30);
        con.add(defaultBtn);
        defaultBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String temp="";
            	for(int i=0;i<=2;i++)
            		temp+=urls[i]+"\n";
                cloakList.setText(temp);
            };  
        });
        JButton urlBtn=new JButton();
        urlBtn.setText(lang.useRecommended());
		urlBtn.setBounds(250, 310, 235, 30);
        con.add(urlBtn);
        urlBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String temp=cloakList.getText()+"\n";
            	for(int i=0;i<urls.length;i++)
            		temp+=urls[i]+"\n";
                cloakList.setText(temp);
                
            };  
        });
        
        
        JButton skinBtn=new JButton();
        skinBtn.setText(lang.save());
        skinBtn.setBounds(15, 350, 235, 30);
        con.add(skinBtn);
        skinBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
                save(cloakList.getText());
                JOptionPane.showMessageDialog(null,lang.saved());
            };  
        });
        
        JButton clokeBtn=new JButton();
        clokeBtn.setText(lang.close());
        clokeBtn.setBounds(250, 350, 235, 30);
        con.add(clokeBtn);
        clokeBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
                jf.setVisible(false);
            };  
        });
        
        
        jf.setVisible(true);
        jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        jf.setResizable(false); 

	}
	
	public static void save(String data){
		File a=new File("capeurls.txt");
		File b=new File("CustomSkinLoader/capeurls.txt");
		try {
			a.createNewFile();
			b.createNewFile();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		write(a,data);
		if(b.exists())
			write(b,data);
	}
	
	public static boolean write(File file,String Data){
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
	public static String read(String FileName){
   	 BufferedReader br = null;
        String data="";
        try {
            br = new BufferedReader(new FileReader(FileName));
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
