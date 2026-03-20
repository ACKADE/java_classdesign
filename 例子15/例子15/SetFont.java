import javax.swing.JComponent;
import java.awt.Font;
public class SetFont {
     public static void setFont(Font f,JComponent ...component){
         for(JComponent c:component)
            c.setFont(f);
     }
}