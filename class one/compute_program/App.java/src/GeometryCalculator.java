public class GeometryCalculator {
    public static void main(String[] args) {
        // 使用 SwingUtilities 启动是更规范的做法
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainView win = new MainView();
            win.setTitle("多功能图形面积计算器 v2.0");
            win.setBounds(100, 100, 600, 400); // 窗口调大一点
        });
    }
}