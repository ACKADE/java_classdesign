public class GeometryHelper {

    // 静态内部类：矩形
    public static class Rectangle {
        double width, height;
        public Rectangle(double w, double h) {
            this.width = w;
            this.height = h;
        }
        public double getArea() {
            return width * height;
        }
    }

    // 静态内部类：圆形
    public static class Circle {
        double radius;
        public Circle(double r) {
            this.radius = r;
        }
        public double getArea() {
            return Math.PI * radius * radius;
        }
    }

    // 静态内部类：菱形
    public static class Rhombus {
        double diag1, diag2; // 两条对角线
        public Rhombus(double d1, double d2) {
            this.diag1 = d1;
            this.diag2 = d2;
        }
        public double getArea() {
            return (diag1 * diag2) / 2.0;
        }
    }
}