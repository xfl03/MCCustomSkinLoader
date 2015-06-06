package idv.jlchntoz.gui;

import idv.jlchntoz.lang.ILanguage;
import idv.jlchntoz.lang.LangManager;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class MainFrame {
	private static ILanguage lang=LangManager.getLanguage();

	public void init(String[] args) {
		
		JFrame jf = new JFrame (lang.title()+lang.gui());  
		Container con = jf.getContentPane();
		con.setLayout(null);
		//setBounds(x,y,width,height)
        jf.setBounds(400, 200, 400, 300);
        
        Font boldCourier=new Font("Courier", Font.BOLD, 30);
        
        JLabel title=new JLabel();
        title.setText(lang.title()+lang.index());
        title.setFont(boldCourier);
        title.setBounds(15, 5, 370, 30);
        con.add(title);
        
        if(args.length>0 && args[0].equalsIgnoreCase("f")){
			JLabel tip1=new JLabel();
			tip1.setText(lang.first1());
			tip1.setBounds(30, 55, 370, 15);
			con.add(tip1);
        
			JLabel tip2=new JLabel();
			tip2.setText(lang.first2());
			tip2.setBounds(30, 70, 370, 15);
			con.add(tip2);
        }
        if(args.length>1){
			JLabel tip3=new JLabel();
			tip3.setText(lang.yourVersion()+" "+args[1]);
			tip3.setBounds(30, 90, 370, 15);
			con.add(tip3);
        }
        
        JButton skinBtn=new JButton();
        skinBtn.setText(lang.skin()+lang.loadList());
        skinBtn.setBounds(15, 180, 370, 30);
        con.add(skinBtn);
        skinBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String[] aa={};
                SkinFrame.main(aa);
            };  
        });
        
        JButton clokeBtn=new JButton();
        clokeBtn.setText(lang.cloak()+lang.loadList());
        clokeBtn.setBounds(15, 215, 370, 30);
        con.add(clokeBtn);
        clokeBtn.addActionListener(new ActionListener(){                                                                 //单击事件 
            @Override
            public void actionPerformed (ActionEvent e){
            	String[] aa={};
                CloakFrame.main(aa);
            };  
        });
        
        
        jf.setVisible(true);
        if(args.length>=3&&args[2].equalsIgnoreCase("ingame")){
            jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }else{
            jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        jf.setResizable(false);
	}

}
