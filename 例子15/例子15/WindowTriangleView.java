import java.awt.*;
import javax.swing.*;
public class WindowTriangleView extends JFrame {
   Triangle triangle;              //模型
   JTextField textA,textB,textC;   //模型视图
   JTextArea showArea;             //模型视图
   JButton button;   
   Controller controller ;          //控制器      
   WindowTriangleView() {
      init();
      setVisible(true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   }
   void init() {
     triangle=new Triangle();
     textA=new JTextField(5);   
     textB=new JTextField(5);
     textC=new JTextField(5);
     showArea=new JTextArea();    
     button=new JButton("计算面积");
     JPanel pNorth=new JPanel();
     JLabel hintA = new JLabel("边A:");
     JLabel hintB = new JLabel("边B:");
     JLabel hintC = new JLabel("边C:");
     pNorth.add(hintA);
     pNorth.add(textA);
     pNorth.add(hintB);
     pNorth.add(textB);
     pNorth.add(hintC);
     pNorth.add(textC); 
     pNorth.add(button); 
     controller = new Controller();
     controller.setView(this);//将当前窗口传递给controller组合的窗口
     button.addActionListener(controller);
     add(pNorth,BorderLayout.NORTH);
     Font f = new Font("宋体",Font.BOLD,28);
     SetFont.setFont
    (f,hintA,hintB,hintC,textA,textB,textC,showArea,button);//见例子14的SetFont类
     add(new JScrollPane(showArea),BorderLayout.CENTER);
   }  
}
