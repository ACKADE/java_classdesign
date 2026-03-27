import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.List;

public  class ClassInfoApp {
    static class Student {
        final String id;
        final String name;
        final String gender; // "男"/"女"
        final int age;
        Student(String id, String name, String gender, int age) {
            this.id = id;
            this.name = name;
            this.gender = gender;
            this.age = age;
        }
    }

    // 用于标识“某专业-某班级”，挂在树的班级节点上，避免字符串拆分
    static class ClassKey {
        final String major;
        final String clazz;

        ClassKey(String major, String clazz) {
            this.major = major;
            this.clazz = clazz;
        }

        @Override public String toString() {
            return clazz; // 树上显示班级名即可
        }
    }

    // ---------- 表格模型 ----------
    static class StudentTableModel extends AbstractTableModel {
        private final String[] columns = {"学号", "姓名", "性别", "年龄"};
        private List<Student> students = List.of();

        public void setStudents(List<Student> students) {
            this.students = (students == null) ? List.of() : students;
            fireTableDataChanged();
        }

        @Override public int getRowCount() {
            return students.size();
        }

        @Override public int getColumnCount() {
            return columns.length;
        }

        @Override public String getColumnName(int column) {
            return columns[column];
        }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Student s = students.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> s.id;
                case 1 -> s.name;
                case 2 -> s.gender;
                case 3 -> s.age;
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 3 ? Integer.class : String.class;
        }

        @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // 课程设计先做展示最稳
        }
    }

    // ---------- UI ----------
    private final Map<String, Map<String, List<Student>>> data = mockData();

    private JTree tree;
    private JTable table;
    private final StudentTableModel tableModel = new StudentTableModel();

    private void initAndShow() {
        JFrame frame = new JFrame("班级信息管理（Swing）");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        tree = new JTree(buildTreeModel(data));
        tree.setRootVisible(true);

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);

        // 监听树选择
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) return;

                Object obj = node.getUserObject();
                if (obj instanceof ClassKey key) {
                    List<Student> students = data.getOrDefault(key.major, Map.of())
                            .getOrDefault(key.clazz, List.of());
                    tableModel.setStudents(students);
                } else {
                    // 选到“根/专业”时清空表格（也可以选择不清空）
                    tableModel.setStudents(List.of());
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                new JScrollPane(table)
        );
        splitPane.setDividerLocation(260);

        frame.setContentPane(splitPane);
        frame.setVisible(true);
    }

    // ---------- 构建树 ----------
    private DefaultTreeModel buildTreeModel(Map<String, Map<String, List<Student>>> data) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("专业-班级");

        // 为了显示稳定，按名称排序一下
        List<String> majors = new ArrayList<>(data.keySet());
        majors.sort(String::compareTo);

        for (String major : majors) {
            DefaultMutableTreeNode majorNode = new DefaultMutableTreeNode(major);
            root.add(majorNode);

            List<String> classes = new ArrayList<>(data.get(major).keySet());
            classes.sort(String::compareTo);

            for (String clazz : classes) {
                majorNode.add(new DefaultMutableTreeNode(new ClassKey(major, clazz)));
            }
        }
        return new DefaultTreeModel(root);
    }

    // ---------- 模拟数据 ----------
    private Map<String, Map<String, List<Student>>> mockData() {
        Map<String, Map<String, List<Student>>> m = new LinkedHashMap<>();

        m.put("计算机科学与技术", new LinkedHashMap<>());
        m.get("计算机科学与技术").put("计科2201", List.of(
                new Student("20220001", "张三", "男", 19),
                new Student("20220002", "李四", "男", 20),
                new Student("20220003", "王五", "女", 19)
        ));
        m.get("计算机科学与技术").put("计科2202", List.of(
                new Student("20220011", "赵六", "男", 19),
                new Student("20220012", "钱七", "女", 20)
        ));

        m.put("软件工程", new LinkedHashMap<>());
        m.get("软件工程").put("软工2201", List.of(
                new Student("20221001", "周一", "男", 19),
                new Student("20221002", "吴二", "女", 19)
        ));
        m.get("软件工程").put("软工2202", List.of(
                new Student("20221011", "郑三", "男", 20)
        ));

        return m;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClassInfoApp().initAndShow());
    }
}