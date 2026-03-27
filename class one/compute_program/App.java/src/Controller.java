import java.awt.event.*;

public class Controller implements ActionListener {
    MainView view;

    public void setView(MainView view) {
        this.view = view;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            // 获取事件源，也就是“谁被点了”
            Object source = e.getSource();
            String result = "";

            if (source == view.btnCalcTri) {
                // 计算三角形
                double a = Double.parseDouble(view.triA.getText().trim());
                double b = Double.parseDouble(view.triB.getText().trim());
                double c = Double.parseDouble(view.triC.getText().trim());
                Triangle t = new Triangle(a, b, c);
                result = "三角形 (" + a + "," + b + "," + c + ") 面积: " + String.format("%.2f", t.getArea());

            } else if (source == view.btnCalcRect) {
                // 计算矩形
                double w = Double.parseDouble(view.rectW.getText().trim());
                double h = Double.parseDouble(view.rectH.getText().trim());
                GeometryHelper.Rectangle r = new GeometryHelper.Rectangle(w, h);
                result = "矩形 (宽" + w + ", 高" + h + ") 面积: " + String.format("%.2f", r.getArea());

            } else if (source == view.btnCalcCircle) {
                // 计算圆形
                double r = Double.parseDouble(view.circleR.getText().trim());
                GeometryHelper.Circle c = new GeometryHelper.Circle(r);
                result = "圆形 (半径" + r + ") 面积: " + String.format("%.2f", c.getArea());

            } else if (source == view.btnCalcRhom) {
                // 计算菱形
                double d1 = Double.parseDouble(view.rhomD1.getText().trim());
                double d2 = Double.parseDouble(view.rhomD2.getText().trim());
                GeometryHelper.Rhombus rh = new GeometryHelper.Rhombus(d1, d2);
                result = "菱形 (对角线" + d1 + "," + d2 + ") 面积: " + String.format("%.2f", rh.getArea());
            }

            // 输出结果
            view.showArea.append(result + "\n");

        } catch (NumberFormatException ex) {
            view.showArea.append("错误：请输入有效的数字！\n");
        } catch (Exception ex) {
            view.showArea.append("错误：" + ex.getMessage() + "\n");
        }
    }
}