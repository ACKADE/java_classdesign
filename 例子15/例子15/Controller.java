import java.awt.event.*;
public class Controller implements ActionListener {
    WindowTriangleView view;
    public void setView(WindowTriangleView view) {
        this.view = view;
    }
    public void actionPerformed(ActionEvent e) {
     try{  
        double a=Double.parseDouble(view.textA.getText().trim());   
        double b=Double.parseDouble(view.textB.getText().trim());      
        double c=Double.parseDouble(view.textC.getText().trim()); 
        //???????е?????
        view.triangle.setA(a) ; 
        view.triangle.setB(b);
        view.triangle.setC(c);
        double area=view.triangle.getArea();
       //????????????е??????
        view.textA.setText(""+view.triangle.sideA);
        view.textB.setText(""+view.triangle.sideB);
        view.textC.setText(""+view.triangle.sideC); 
        String strArea= String.format("%.3f",view.triangle.area);
        view.showArea.append("\n???(????3λС??):\n"+strArea); 
     } 
     catch(Exception ex) {
        view.showArea.append("\n"+ex+"\n");
     }
   }
}