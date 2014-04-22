package idv.jlchntoz;

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

public class SkinFrame {
	public static boolean mod=false;
	public static final String[] urls={
		"#Minecraft Offical",
		"http://s3.amazonaws.com/MinecraftSkins/*.png",
		"http://skins.minecraft.net/MinecraftSkins/*.png",
		"#MineCrack",
		"http://minecrack.fr.nf/mc/skinsminecrackd/*.png",
		"#OpenSkin",
		"http://skin.axdt.net/MinecraftSkins/*.png",
		"#SkinMe",
		"http://www.skinme.cc:88/MinecraftSkins/*.png"
		
	};

	public static void main(String[] args) {
		final JFrame jf = new JFrame ("Skin Load List");  
		Container con = jf.getContentPane();
		con.setLayout(null);
		//setBounds(x,y,width,height)
        jf.setBounds(400, 200, 515, 420);
        
        Font boldCourier=new Font("Courier", Font.BOLD, 30);
        
        JLabel title=new JLabel();
        title.setText("Skin Load List");
        title.setFont(boldCourier);
        title.setBounds(130, 5, 370, 30);
        con.add(title);
        
        final JTextArea skinList=new JTextArea();
        skinList.setBounds(15, 40, 470, 266);
        con.add(skinList);
        skinList.setText(read("skinurls.txt"));
        
        JButton defaultBtn=new JButton();
        defaultBtn.setText("Use Default(Offical)");
		defaultBtn.setBounds(15, 310, 235, 30);
        con.add(defaultBtn);
        defaultBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String temp="";
            	for(int i=0;i<=2;i++)
            		temp+=urls[i]+"\n";
                skinList.setText(temp);
            };  
        });
        JButton urlBtn=new JButton();
        urlBtn.setText("Use Recommended");
		urlBtn.setBounds(250, 310, 235, 30);
        con.add(urlBtn);
        urlBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String temp=skinList.getText()+"\n";
            	for(int i=0;i<urls.length;i++)
            		temp+=urls[i]+"\n";
                skinList.setText(temp);
                
            };  
        });
        
        
        JButton skinBtn=new JButton();
        skinBtn.setText("Save");
        skinBtn.setBounds(15, 350, 235, 30);
        con.add(skinBtn);
        skinBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
                save(skinList.getText());
                JOptionPane.showMessageDialog(null,"Saved!");
            };  
        });
        
        JButton clokeBtn=new JButton();
        clokeBtn.setText("Close");
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
		File a=new File("skinurls.txt");
		try {
			a.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		write(a.getAbsolutePath(),data);
	}
	
	public static boolean write(String FileName,String Data){
   	 FileWriter fw=null;
        try{
            fw = new FileWriter(FileName);
            fw.write(Data,0,Data.length()); 
            fw.flush();
            fw.close();
        }catch(Exception ex){
            //ex.printStackTrace();
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
                  data += "\n"+br.readLine(); 
            }
              br.close();
              return data;
        } catch (Exception ex) {
            return "";
        }
       
   }

}
