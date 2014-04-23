package idv.jlchntoz;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class MainFrame {
	private static LangParent lang=Lang.refresh();

	public static void main(String[] args) {
		
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
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setResizable(false); 

	}

}
