package idv.jlchntoz;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class MainFrame {

	public static void main(String[] args) {
		JFrame jf = new JFrame ("CustomSkinLoader-GUI");  
		Container con = jf.getContentPane();
		con.setLayout(null);
		//setBounds(x,y,width,height)
        jf.setBounds(400, 200, 400, 300);
        
        Font boldCourier=new Font("Courier", Font.BOLD, 30);
        
        JLabel title=new JLabel();
        title.setText("CustomSkinLoader Index");
        title.setFont(boldCourier);
        title.setBounds(15, 5, 360, 30);
        con.add(title);
        
        
        
        jf.setVisible(true);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setResizable(false); 

	}

}
