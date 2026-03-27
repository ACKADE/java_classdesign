public class Triangle {
    double sideA, sideB, sideC;

    public Triangle(double a, double b, double c) {
        this.sideA = a;
        this.sideB = b;
        this.sideC = c;
    }

    public double getArea() throws Exception {
        // 判断是否构成三角形
        if (sideA + sideB > sideC && sideA + sideC > sideB && sideC + sideB > sideA) {
            double p = (sideA + sideB + sideC) / 2.0;
            return Math.sqrt(p * (p - sideA) * (p - sideB) * (p - sideC));
        } else {
            // 抛出异常比返回 NaN 更容易控制流程
            throw new Exception("无法构成三角形");
        }
    }
}