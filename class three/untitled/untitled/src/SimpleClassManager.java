import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleClassManager extends JFrame {
    private enum ViewType { NONE, STUDENT, TEACHER }

    private JTable table;
    private DefaultTableModel tableModel;
    private ViewType currentViewType = ViewType.NONE;
    private String currentMajor;

    private static final String[] STUDENT_COLUMNS = {"学号", "专业", "年级", "宿舍", "出生日期"};
    private static final String[] TEACHER_COLUMNS = {"教师工号", "姓名", "专业", "职称", "电话", "邮箱", "办公室", "入职日期"};

    public SimpleClassManager() {
        DatabaseInitializer.initialize();

        setTitle("计算机学院数据库管理系统");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(createTreePanel(), BorderLayout.WEST);
        add(createStudentPanel(), BorderLayout.CENTER);
    }
    // 左边树形结构
    private JPanel createTreePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("数据导航"));
        panel.setPreferredSize(new Dimension(260, 500));
        // 创建根节点
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("计算机学院");
        DefaultMutableTreeNode studentsNode = new DefaultMutableTreeNode("学生表 cs_students");
        DefaultMutableTreeNode teacherNode = new DefaultMutableTreeNode("教师表 cs_teacher");

        String[] majors = {"计算机科学与技术", "软件工程", "人工智能", "数据科学与大数据技术", "网络工程", "信息安全"};
        for (String major : majors) {
            studentsNode.add(new DefaultMutableTreeNode(major));
            teacherNode.add(new DefaultMutableTreeNode(major));
        }

        root.add(studentsNode);
        root.add(teacherNode);
        // 创建树
        JTree tree = new JTree(root);
        tree.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        tree.setRowHeight(25);

        // 隐藏图标
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);

        // 点击事件
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.isLeaf()) {
                TreePath path = tree.getSelectionPath();
                if (path != null && path.getPathCount() >= 3) {
                    String tableNodeName = path.getPathComponent(1).toString();
                    String major = node.toString();
                    if (tableNodeName.contains("cs_students")) {
                        showStudentsByMajor(major);
                    } else if (tableNodeName.contains("cs_teacher")) {
                        showTeachersByMajor(major);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 右边学生表格
    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("数据列表"));

        tableModel = new DefaultTableModel(STUDENT_COLUMNS, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);

        JPanel actionPanel = createActionPanel();
        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("新增");
        JButton editButton = new JButton("修改");
        JButton deleteButton = new JButton("删除");
        JButton refreshButton = new JButton("刷新");

        addButton.addActionListener(e -> handleAdd());
        editButton.addActionListener(e -> handleEdit());
        deleteButton.addActionListener(e -> handleDelete());
        refreshButton.addActionListener(e -> refreshCurrentView());

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(refreshButton);
        return panel;
    }

    private void resetColumns(String[] columns) {
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
    }

    // 按专业查询学生表
    private void showStudentsByMajor(String major) {
        currentViewType = ViewType.STUDENT;
        currentMajor = major;
        resetColumns(STUDENT_COLUMNS);
        String sql = "SELECT student_id, major, grade, dorm_address, birth_date FROM cs_students WHERE major = ? ORDER BY grade, student_id";

        try (Connection connection = DBUtil.getAppConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, major);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tableModel.addRow(new Object[]{
                            resultSet.getString("student_id"),
                            resultSet.getString("major"),
                            resultSet.getString("grade"),
                            resultSet.getString("dorm_address"),
                            resultSet.getDate("birth_date")
                    });
                }
            }
            setTitle(major + " - 学生信息");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "查询学生失败：" + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 按专业查询教师表
    private void showTeachersByMajor(String major) {
        currentViewType = ViewType.TEACHER;
        currentMajor = major;
        resetColumns(TEACHER_COLUMNS);
        String sql = "SELECT teacher_id, name, major, title, phone, email, office_address, hire_date FROM cs_teacher WHERE major = ? ORDER BY teacher_id";

        try (Connection connection = DBUtil.getAppConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, major);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tableModel.addRow(new Object[]{
                            resultSet.getString("teacher_id"),
                            resultSet.getString("name"),
                            resultSet.getString("major"),
                            resultSet.getString("title"),
                            resultSet.getString("phone"),
                            resultSet.getString("email"),
                            resultSet.getString("office_address"),
                            resultSet.getDate("hire_date")
                    });
                }
            }
            setTitle(major + " - 教师信息");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "查询教师失败：" + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAdd() {
        if (!ensureViewReady()) {
            return;
        }
        if (currentViewType == ViewType.STUDENT) {
            addStudent();
        } else if (currentViewType == ViewType.TEACHER) {
            addTeacher();
        }
    }

    private void handleEdit() {
        if (!ensureViewReady()) {
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (currentViewType == ViewType.STUDENT) {
            editStudent(row);
        } else if (currentViewType == ViewType.TEACHER) {
            editTeacher(row);
        }
    }

    private void handleDelete() {
        if (!ensureViewReady()) {
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (currentViewType == ViewType.STUDENT) {
            deleteStudent(row);
        } else if (currentViewType == ViewType.TEACHER) {
            deleteTeacher(row);
        }
    }

    private boolean ensureViewReady() {
        if (currentViewType == ViewType.NONE || currentMajor == null) {
            JOptionPane.showMessageDialog(this, "请先在左侧选择一个专业", "提示", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    private void refreshCurrentView() {
        if (!ensureViewReady()) {
            return;
        }
        if (currentViewType == ViewType.STUDENT) {
            showStudentsByMajor(currentMajor);
        } else if (currentViewType == ViewType.TEACHER) {
            showTeachersByMajor(currentMajor);
        }
    }

    private void addStudent() {
        JTextField idField = new JTextField();
        JTextField majorField = new JTextField(currentMajor);
        JTextField gradeField = new JTextField();
        JTextField dormField = new JTextField();
        JTextField birthField = new JTextField("2006-01-01");

        if (showForm("新增学生", new String[]{"学号", "专业", "年级", "宿舍", "出生日期(yyyy-MM-dd)"},
                new JComponent[]{idField, majorField, gradeField, dormField, birthField})) {
            String sql = "INSERT INTO cs_students(student_id, major, grade, dorm_address, birth_date) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = DBUtil.getAppConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, idField.getText().trim());
                statement.setString(2, majorField.getText().trim());
                statement.setString(3, gradeField.getText().trim());
                statement.setString(4, dormField.getText().trim());
                statement.setDate(5, parseSqlDate(birthField.getText().trim()));
                statement.executeUpdate();
                currentMajor = majorField.getText().trim();
                refreshCurrentView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "新增学生失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editStudent(int row) {
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        JTextField majorField = new JTextField(String.valueOf(tableModel.getValueAt(row, 1)));
        JTextField gradeField = new JTextField(String.valueOf(tableModel.getValueAt(row, 2)));
        JTextField dormField = new JTextField(String.valueOf(tableModel.getValueAt(row, 3)));
        JTextField birthField = new JTextField(String.valueOf(tableModel.getValueAt(row, 4)));

        if (showForm("修改学生（学号: " + id + "）", new String[]{"专业", "年级", "宿舍", "出生日期(yyyy-MM-dd)"},
                new JComponent[]{majorField, gradeField, dormField, birthField})) {
            String sql = "UPDATE cs_students SET major = ?, grade = ?, dorm_address = ?, birth_date = ? WHERE student_id = ?";
            try (Connection connection = DBUtil.getAppConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, majorField.getText().trim());
                statement.setString(2, gradeField.getText().trim());
                statement.setString(3, dormField.getText().trim());
                statement.setDate(4, parseSqlDate(birthField.getText().trim()));
                statement.setString(5, id);
                statement.executeUpdate();
                currentMajor = majorField.getText().trim();
                refreshCurrentView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "修改学生失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStudent(int row) {
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        int option = JOptionPane.showConfirmDialog(this, "确认删除学号 " + id + " ?", "确认删除", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        String sql = "DELETE FROM cs_students WHERE student_id = ?";
        try (Connection connection = DBUtil.getAppConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
            refreshCurrentView();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "删除学生失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTeacher() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField majorField = new JTextField(currentMajor);
        JTextField titleField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField officeField = new JTextField();
        JTextField hireDateField = new JTextField("2024-09-01");

        if (showForm("新增教师", new String[]{"教师工号", "姓名", "专业", "职称", "电话", "邮箱", "办公室", "入职日期(yyyy-MM-dd)"},
                new JComponent[]{idField, nameField, majorField, titleField, phoneField, emailField, officeField, hireDateField})) {
            String sql = "INSERT INTO cs_teacher(teacher_id, name, major, title, phone, email, office_address, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = DBUtil.getAppConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, idField.getText().trim());
                statement.setString(2, nameField.getText().trim());
                statement.setString(3, majorField.getText().trim());
                statement.setString(4, titleField.getText().trim());
                statement.setString(5, phoneField.getText().trim());
                statement.setString(6, emailField.getText().trim());
                statement.setString(7, officeField.getText().trim());
                statement.setDate(8, parseSqlDate(hireDateField.getText().trim()));
                statement.executeUpdate();
                currentMajor = majorField.getText().trim();
                refreshCurrentView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "新增教师失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editTeacher(int row) {
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        JTextField nameField = new JTextField(String.valueOf(tableModel.getValueAt(row, 1)));
        JTextField majorField = new JTextField(String.valueOf(tableModel.getValueAt(row, 2)));
        JTextField titleField = new JTextField(String.valueOf(tableModel.getValueAt(row, 3)));
        JTextField phoneField = new JTextField(String.valueOf(tableModel.getValueAt(row, 4)));
        JTextField emailField = new JTextField(String.valueOf(tableModel.getValueAt(row, 5)));
        JTextField officeField = new JTextField(String.valueOf(tableModel.getValueAt(row, 6)));
        JTextField hireDateField = new JTextField(String.valueOf(tableModel.getValueAt(row, 7)));

        if (showForm("修改教师（工号: " + id + "）", new String[]{"姓名", "专业", "职称", "电话", "邮箱", "办公室", "入职日期(yyyy-MM-dd)"},
                new JComponent[]{nameField, majorField, titleField, phoneField, emailField, officeField, hireDateField})) {
            String sql = "UPDATE cs_teacher SET name = ?, major = ?, title = ?, phone = ?, email = ?, office_address = ?, hire_date = ? WHERE teacher_id = ?";
            try (Connection connection = DBUtil.getAppConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, nameField.getText().trim());
                statement.setString(2, majorField.getText().trim());
                statement.setString(3, titleField.getText().trim());
                statement.setString(4, phoneField.getText().trim());
                statement.setString(5, emailField.getText().trim());
                statement.setString(6, officeField.getText().trim());
                statement.setDate(7, parseSqlDate(hireDateField.getText().trim()));
                statement.setString(8, id);
                statement.executeUpdate();
                currentMajor = majorField.getText().trim();
                refreshCurrentView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "修改教师失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTeacher(int row) {
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        int option = JOptionPane.showConfirmDialog(this, "确认删除工号 " + id + " ?", "确认删除", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        String sql = "DELETE FROM cs_teacher WHERE teacher_id = ?";
        try (Connection connection = DBUtil.getAppConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
            refreshCurrentView();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "删除教师失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean showForm(String title, String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i] + "："));
            panel.add(fields[i]);
        }
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION;
    }

    private Date parseSqlDate(String text) {
        return Date.valueOf(text);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleClassManager().setVisible(true));
    }
}