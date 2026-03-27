import java.awt.*;
import javax.swing.*;

public class MainView extends JFrame {
    // 结果显示区（公共的）
    JTextArea showArea;

    // 三角形组件
    JTextField triA, triB, triC;
    JButton btnCalcTri;

    // 矩形组件
    JTextField rectW, rectH;
    JButton btnCalcRect;

    // 圆形组件
    JTextField circleR;
    JButton btnCalcCircle;

    // 菱形组件
    JTextField rhomD1, rhomD2; // 两条对角线
    JButton btnCalcRhom;

    Controller controller;

    public MainView() {
        init();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    void init() {
        // 1. 初始化结果显示区 (放在最下面)
        showArea = new JTextArea();
        showArea.setEditable(false); // 结果区不允许用户乱改
        showArea.setFont(new Font("宋体", Font.BOLD, 18));

        // 2. 使用 JTabbedPane (选项卡) 来管理不同图形
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("黑体", Font.PLAIN, 16));

        // --- 添加三角形面板 ---
        JPanel pTri = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        triA = new JTextField(5);
        triB = new JTextField(5);
        triC = new JTextField(5);
        btnCalcTri = new JButton("计算三角形面积");
        pTri.add(new JLabel("边A:")); pTri.add(triA);
        pTri.add(new JLabel("边B:")); pTri.add(triB);
        pTri.add(new JLabel("边C:")); pTri.add(triC);
        pTri.add(btnCalcTri); // 按钮跟在输入框后面
        tabbedPane.addTab("三角形", pTri);

        // --- 添加矩形面板 ---
        JPanel pRect = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        rectW = new JTextField(8);
        rectH = new JTextField(8);
        btnCalcRect = new JButton("计算矩形面积");
        pRect.add(new JLabel("宽度:")); pRect.add(rectW);
        pRect.add(new JLabel("高度:")); pRect.add(rectH);
        pRect.add(btnCalcRect);
        tabbedPane.addTab("矩形", pRect);

        // --- 添加圆形面板 ---
        JPanel pCircle = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        circleR = new JTextField(8);
        btnCalcCircle = new JButton("计算圆形面积");
        pCircle.add(new JLabel("半径:")); pCircle.add(circleR);
        pCircle.add(btnCalcCircle);
        tabbedPane.addTab("圆形", pCircle);

        // --- 添加菱形面板 ---
        JPanel pRhom = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        rhomD1 = new JTextField(8);
        rhomD2 = new JTextField(8);
        btnCalcRhom = new JButton("计算菱形面积");
        pRhom.add(new JLabel("对角线1:")); pRhom.add(rhomD1);
        pRhom.add(new JLabel("对角线2:")); pRhom.add(rhomD2);
        pRhom.add(btnCalcRhom);
        tabbedPane.addTab("菱形", pRhom);

        // 3. 组装整体布局
        add(tabbedPane, BorderLayout.NORTH); // 上半部分是选项卡
        add(new JScrollPane(showArea), BorderLayout.CENTER); // 下半部分是结果

        // 4. 连接控制器
        controller = new Controller();
        controller.setView(this);

        // 给所有按钮绑定同一个控制器，但我们在 Controller 里区分是谁点的
        btnCalcTri.addActionListener(controller);
        btnCalcRect.addActionListener(controller);
        btnCalcCircle.addActionListener(controller);
        btnCalcRhom.addActionListener(controller);

        // 5. 简单美化组件字体
        Font inputFont = new Font("Microsoft YaHei UI", Font.PLAIN, 16);
        setComponentFont(pTri, inputFont);
        setComponentFont(pRect, inputFont);
        setComponentFont(pCircle, inputFont);
        setComponentFont(pRhom, inputFont);
    }

    // 一个辅助小方法，不用那个 SetFont 类了
    private void setComponentFont(JPanel panel, Font f) {
        for (Component c : panel.getComponents()) {
            c.setFont(f);
        }
    }
}