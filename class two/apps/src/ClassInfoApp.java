import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.*;
import java.util.*;
import java.util.List;

public  class ClassInfoApp {
    private static final String DEFAULT_DB_HOST = "127.0.0.1";
    private static final String DEFAULT_DB_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "text";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "";
    private static final String LEGACY_STUDENT_TABLE = "cs_students";
    private static final String LEGACY_TEACHER_TABLE = "cs_teacher";
    private static final String CLASS_ROLE_TEACHER_TABLE = "cs_class_role_teacher";
    private static final List<String> DB_NAME_FALLBACKS = List.of("test", "text");

    static class Student {
        final String id;
        final String major;
        final String grade;
        final String dormAddress;
        final String birthDate;

        Student(String id, String major, String grade, String dormAddress, String birthDate) {
            this.id = id;
            this.major = major;
            this.grade = grade;
            this.dormAddress = dormAddress;
            this.birthDate = birthDate;
        }
    }

    static class Teacher {
        final String id;
        final String name;
        final String major;
        final String title;
        final String phone;
        final String email;
        final String officeAddress;
        final String hireDate;

        Teacher(String id, String name, String major, String title,
                String phone, String email, String officeAddress, String hireDate) {
            this.id = id;
            this.name = name;
            this.major = major;
            this.title = title;
            this.phone = phone;
            this.email = email;
            this.officeAddress = officeAddress;
            this.hireDate = hireDate;
        }
    }

    static class ClassRoleTeacher {
        final String major;
        final String clazz;
        final String role;
        final String teacherId;
        final String teacherName;

        ClassRoleTeacher(String major, String clazz, String role, String teacherId, String teacherName) {
            this.major = major;
            this.clazz = clazz;
            this.role = role;
            this.teacherId = teacherId;
            this.teacherName = teacherName;
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
        private final String[] columns = {"学号", "专业", "年级", "宿舍", "出生日期"};
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
                case 1 -> s.major;
                case 2 -> s.grade;
                case 3 -> s.dormAddress;
                case 4 -> s.birthDate;
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // 课程设计先做展示最稳
        }

        public Student getStudentAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= students.size()) return null;
            return students.get(rowIndex);
        }
    }

    static class TeacherTableModel extends AbstractTableModel {
        private final String[] columns = {"工号", "姓名", "专业", "职称", "电话", "邮箱", "办公室", "入职日期"};
        private List<Teacher> teachers = List.of();

        public void setTeachers(List<Teacher> teachers) {
            this.teachers = (teachers == null) ? List.of() : teachers;
            fireTableDataChanged();
        }

        @Override public int getRowCount() {
            return teachers.size();
        }

        @Override public int getColumnCount() {
            return columns.length;
        }

        @Override public String getColumnName(int column) {
            return columns[column];
        }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Teacher t = teachers.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> t.id;
                case 1 -> t.name;
                case 2 -> t.major;
                case 3 -> t.title;
                case 4 -> t.phone;
                case 5 -> t.email;
                case 6 -> t.officeAddress;
                case 7 -> t.hireDate;
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Teacher getTeacherAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= teachers.size()) return null;
            return teachers.get(rowIndex);
        }
    }

    static class ClassRoleTeacherTableModel extends AbstractTableModel {
        private final String[] columns = {"班级", "角色", "教师工号", "教师姓名"};
        private List<ClassRoleTeacher> rows = List.of();

        public void setRows(List<ClassRoleTeacher> rows) {
            this.rows = (rows == null) ? List.of() : rows;
            fireTableDataChanged();
        }

        @Override public int getRowCount() {
            return rows.size();
        }

        @Override public int getColumnCount() {
            return columns.length;
        }

        @Override public String getColumnName(int column) {
            return columns[column];
        }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            ClassRoleTeacher r = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.clazz;
                case 1 -> r.role;
                case 2 -> r.teacherId;
                case 3 -> r.teacherName;
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public ClassRoleTeacher getRowAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= rows.size()) return null;
            return rows.get(rowIndex);
        }
    }

    // ---------- UI ----------
    private final Map<String, List<Teacher>> teacherData = new LinkedHashMap<>();
    private final Map<String, Map<String, List<ClassRoleTeacher>>> classRoleTeacherData = new LinkedHashMap<>();
    private final Map<String, Map<String, List<Student>>> data = new LinkedHashMap<>();

    private JTree tree;
    private JFrame frame;
    private ClassKey selectedClassKey;
    private boolean usingMockData;
    private boolean dbFallbackWarningShown;
    private String lastLoadErrorMessage = "";
    private JTable table;
    private JTable teacherTable;
    private JTable classRoleTeacherTable;
    private final StudentTableModel tableModel = new StudentTableModel();
    private final TeacherTableModel teacherTableModel = new TeacherTableModel();
    private final ClassRoleTeacherTableModel classRoleTeacherTableModel = new ClassRoleTeacherTableModel();

    private void initAndShow() {
        frame = new JFrame("班级信息管理（Swing）");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        tree = new JTree(buildTreeModel(data));
        tree.setRootVisible(true);

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);

        teacherTable = new JTable(teacherTableModel);
        teacherTable.setFillsViewportHeight(true);
        teacherTable.setRowHeight(24);

        classRoleTeacherTable = new JTable(classRoleTeacherTableModel);
        classRoleTeacherTable.setFillsViewportHeight(true);
        classRoleTeacherTable.setRowHeight(24);

        // 监听树选择
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) return;

                Object obj = node.getUserObject();
                if (obj instanceof ClassKey key) {
                    selectedClassKey = key;
                    refreshTablesBySelection();
                } else {
                    selectedClassKey = null;
                    refreshTablesBySelection();
                }
            }
        });

        JPanel studentPanel = createStudentPanel();
        JPanel teacherPanel = createTeacherPanel();
        JPanel classRoleTeacherPanel = createClassRoleTeacherPanel();

        JSplitPane lowerRightSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                teacherPanel,
                classRoleTeacherPanel
        );
        lowerRightSplit.setResizeWeight(0.5);
        lowerRightSplit.setDividerLocation(160);

        JSplitPane rightSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                studentPanel,
                lowerRightSplit
        );
        rightSplit.setResizeWeight(0.6);
        rightSplit.setDividerLocation(320);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                rightSplit
        );
        splitPane.setDividerLocation(260);

        frame.setContentPane(splitPane);
        frame.setVisible(true);

        // 避免在 EDT 上阻塞数据库连接，界面先显示再异步加载数据。
        reloadFromDatabaseAndRefresh();
    }

    private JPanel createStudentPanel() {
        JButton addBtn = new JButton("新增");
        JButton editBtn = new JButton("修改");
        JButton deleteBtn = new JButton("删除");
        JButton refreshBtn = new JButton("刷新");

        addBtn.addActionListener(e -> addStudent());
        editBtn.addActionListener(e -> editStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        refreshBtn.addActionListener(e -> reloadFromDatabaseAndRefresh());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(refreshBtn);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("学生"));
        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTeacherPanel() {
        JButton addBtn = new JButton("新增");
        JButton editBtn = new JButton("修改");
        JButton deleteBtn = new JButton("删除");
        JButton refreshBtn = new JButton("刷新");

        addBtn.addActionListener(e -> addTeacher());
        editBtn.addActionListener(e -> editTeacher());
        deleteBtn.addActionListener(e -> deleteTeacher());
        refreshBtn.addActionListener(e -> reloadFromDatabaseAndRefresh());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(refreshBtn);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("教师"));
        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(teacherTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createClassRoleTeacherPanel() {
        JButton addBtn = new JButton("新增");
        JButton editBtn = new JButton("修改");
        JButton deleteBtn = new JButton("删除");
        JButton refreshBtn = new JButton("刷新");

        addBtn.addActionListener(e -> addClassRoleTeacher());
        editBtn.addActionListener(e -> editClassRoleTeacher());
        deleteBtn.addActionListener(e -> deleteClassRoleTeacher());
        refreshBtn.addActionListener(e -> reloadFromDatabaseAndRefresh());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(refreshBtn);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("班级角色-教师映射"));
        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(classRoleTeacherTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshTablesBySelection() {
        if (selectedClassKey == null) {
            tableModel.setStudents(List.of());
            teacherTableModel.setTeachers(List.of());
            classRoleTeacherTableModel.setRows(List.of());
            return;
        }

        List<Student> students = data.getOrDefault(selectedClassKey.major, Map.of())
                .getOrDefault(selectedClassKey.clazz, List.of());
        tableModel.setStudents(students);
        teacherTableModel.setTeachers(teacherData.getOrDefault(selectedClassKey.major, List.of()));
        classRoleTeacherTableModel.setRows(
                classRoleTeacherData.getOrDefault(selectedClassKey.major, Map.of())
                        .getOrDefault(selectedClassKey.clazz, List.of())
        );
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

    // ---------- 从数据库加载数据，失败时回退到模拟数据 ----------
    private Map<String, Map<String, List<Student>>> loadData() {
        try {
            Map<String, Map<String, List<Student>>> loaded = loadFromDatabase();
            usingMockData = false;
            lastLoadErrorMessage = "";
            return loaded;
        } catch (SQLException e) {
            System.err.printf(
                    "数据库加载失败(SQL): state=%s, code=%d, message=%s%n",
                    e.getSQLState(), e.getErrorCode(), e.getMessage()
            );
            usingMockData = true;
            lastLoadErrorMessage = String.format(
                    "SQLState=%s, 错误码=%d, 原因=%s",
                    e.getSQLState(), e.getErrorCode(), e.getMessage()
            );
            teacherData.clear();
            teacherData.putAll(mockTeacherData());
            classRoleTeacherData.clear();
            classRoleTeacherData.putAll(mockClassRoleTeacherData());
            return mockData();
        } catch (Exception e) {
            System.err.println("数据库加载失败，使用模拟数据：" + e.getClass().getSimpleName() + " - " + e.getMessage());
            usingMockData = true;
            lastLoadErrorMessage = e.getClass().getSimpleName() + " - " + e.getMessage();
            teacherData.clear();
            teacherData.putAll(mockTeacherData());
            classRoleTeacherData.clear();
            classRoleTeacherData.putAll(mockClassRoleTeacherData());
            return mockData();
        }
    }

    private Map<String, Map<String, List<Student>>> loadFromDatabase() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String explicitUrl = firstNonBlank(
                System.getProperty("db.url"),
                System.getenv("DB_URL")
        );
        String user = firstNonBlank(
                System.getProperty("db.user"),
                System.getenv("DB_USER"),
                DEFAULT_DB_USER
        );
        String password = firstNonBlank(
                System.getProperty("db.password"),
                System.getenv("DB_PASSWORD"),
                DEFAULT_DB_PASSWORD
        );

        Map<String, Map<String, List<Student>>> m = new LinkedHashMap<>();

        List<String> urlsToTry = buildUrlsToTry(explicitUrl);
        String legacyTableName = firstNonBlank(
                System.getProperty("db.table"),
                System.getenv("DB_TABLE"),
                LEGACY_STUDENT_TABLE
        );
        Exception lastError = null;

        for (String url : urlsToTry) {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                try {
                    m.clear();
                    loadStudentsFromNormalizedSchema(conn, m);
                    teacherData.clear();
                    teacherData.putAll(loadTeachersFromNormalizedSchema(conn));
                    classRoleTeacherData.clear();
                    classRoleTeacherData.putAll(loadClassRoleTeacherData(conn, m, teacherData));
                    System.out.println("数据库加载成功(规范化表): " + url + " , rows=" + countStudents(m));
                    return m;
                } catch (SQLException normalizedError) {
                    try {
                        m.clear();
                        loadStudentsFromLegacySchema(conn, m, legacyTableName);
                        teacherData.clear();
                        teacherData.putAll(loadTeachersFromLegacySchema(conn));
                        classRoleTeacherData.clear();
                        classRoleTeacherData.putAll(loadClassRoleTeacherData(conn, m, teacherData));
                        System.out.println("数据库加载成功(旧表兼容): " + url + " , table=" + legacyTableName + " , rows=" + countStudents(m));
                        return m;
                    } catch (SQLException legacyError) {
                        lastError = legacyError;
                    }
                }
            } catch (SQLException connError) {
                lastError = connError;
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new SQLException("数据库连接可用，但未读取到有效学生数据。请检查数据库名、表名和字段名是否匹配。");
    }

    private void loadStudentsFromNormalizedSchema(Connection conn, Map<String, Map<String, List<Student>>> target) throws SQLException {
        String sql =
                "SELECT s.student_id, d.dept_name AS major, c.class_name, s.dorm_address, s.birth_date " +
                "FROM students s " +
                "JOIN classes c ON s.class_id = c.class_id " +
                "JOIN departments d ON c.dept_id = d.dept_id " +
                "ORDER BY d.dept_name, c.class_name, s.student_id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String major = rs.getString("major");
                String grade = rs.getString("class_name");
                String dormAddress = rs.getString("dorm_address");
                java.sql.Date birthDate = rs.getDate("birth_date");

                target.computeIfAbsent(major, k -> new LinkedHashMap<>())
                      .computeIfAbsent(grade, k -> new ArrayList<>())
                      .add(new Student(
                              studentId,
                              major,
                              grade,
                              dormAddress,
                              birthDate == null ? "" : birthDate.toString()
                      ));
            }
        }
    }

    private void loadStudentsFromLegacySchema(Connection conn, Map<String, Map<String, List<Student>>> target, String legacyTableName) throws SQLException {
        String sql = "SELECT student_id, major, grade, dorm_address, birth_date FROM " + legacyTableName + " ORDER BY major, grade, student_id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String major = rs.getString("major");
                String grade = rs.getString("grade");
                String dormAddress = rs.getString("dorm_address");
                java.sql.Date birthDate = rs.getDate("birth_date");

                target.computeIfAbsent(major, k -> new LinkedHashMap<>())
                      .computeIfAbsent(grade, k -> new ArrayList<>())
                      .add(new Student(
                              studentId,
                              major,
                              grade,
                              dormAddress,
                              birthDate == null ? "" : birthDate.toString()
                      ));
            }
        }
    }

    private Map<String, List<Teacher>> loadTeachersFromNormalizedSchema(Connection conn) throws SQLException {
        Map<String, List<Teacher>> result = new LinkedHashMap<>();
        String sql =
                "SELECT t.teacher_id, t.name, d.dept_name AS major, t.title, t.phone, t.email, t.office_address, t.hire_date " +
                "FROM teachers t " +
                "JOIN departments d ON t.dept_id = d.dept_id " +
                "ORDER BY d.dept_name, t.teacher_id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String teacherId = rs.getString("teacher_id");
                String name = rs.getString("name");
                String major = rs.getString("major");
                String title = rs.getString("title");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String officeAddress = rs.getString("office_address");
                java.sql.Date hireDate = rs.getDate("hire_date");

                result.computeIfAbsent(major, k -> new ArrayList<>())
                      .add(new Teacher(
                              teacherId,
                              name,
                              major,
                              title,
                              phone,
                              email,
                              officeAddress,
                              hireDate == null ? "" : hireDate.toString()
                      ));
            }
        }
        return result;
    }

    private Map<String, List<Teacher>> loadTeachersFromLegacySchema(Connection conn) throws SQLException {
        Map<String, List<Teacher>> result = new LinkedHashMap<>();
        String sql = "SELECT teacher_id, name, major, title, phone, email, office_address, hire_date FROM " + LEGACY_TEACHER_TABLE + " ORDER BY major, teacher_id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String teacherId = rs.getString("teacher_id");
                String name = rs.getString("name");
                String major = rs.getString("major");
                String title = rs.getString("title");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String officeAddress = rs.getString("office_address");
                java.sql.Date hireDate = rs.getDate("hire_date");

                result.computeIfAbsent(major, k -> new ArrayList<>())
                      .add(new Teacher(
                              teacherId,
                              name,
                              major,
                              title,
                              phone,
                              email,
                              officeAddress,
                              hireDate == null ? "" : hireDate.toString()
                      ));
            }
        }
        return result;
    }

    private static int countStudents(Map<String, Map<String, List<Student>>> data) {
        return data.values().stream().mapToInt(x -> x.values().stream().mapToInt(List::size).sum()).sum();
    }

    private Map<String, Map<String, List<ClassRoleTeacher>>> loadClassRoleTeacherData(
            Connection conn,
            Map<String, Map<String, List<Student>>> studentData,
            Map<String, List<Teacher>> teachersByMajor
    ) {
        Map<String, Map<String, List<ClassRoleTeacher>>> result = new LinkedHashMap<>();
        String sql = "SELECT major, grade, role_name, teacher_id FROM " + CLASS_ROLE_TEACHER_TABLE + " ORDER BY major, grade";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            Map<String, String> teacherNameById = new HashMap<>();
            for (List<Teacher> teachers : teachersByMajor.values()) {
                for (Teacher t : teachers) {
                    teacherNameById.put(t.id, t.name);
                }
            }

            while (rs.next()) {
                String major = rs.getString("major");
                String grade = rs.getString("grade");
                String role = rs.getString("role_name");
                String teacherId = rs.getString("teacher_id");
                String teacherName = teacherNameById.getOrDefault(teacherId, "");

                result.computeIfAbsent(major, k -> new LinkedHashMap<>())
                        .computeIfAbsent(grade, k -> new ArrayList<>())
                        .add(new ClassRoleTeacher(major, grade, role == null ? "" : role, teacherId == null ? "" : teacherId, teacherName));
            }

            if (!result.isEmpty()) {
                return result;
            }
        } catch (SQLException ignored) {
            // Fallback to inferred mapping when table does not exist or query fails.
        }

        return inferClassRoleTeacherData(studentData, teachersByMajor);
    }

    private Map<String, Map<String, List<ClassRoleTeacher>>> inferClassRoleTeacherData(
            Map<String, Map<String, List<Student>>> studentData,
            Map<String, List<Teacher>> teachersByMajor
    ) {
        Map<String, Map<String, List<ClassRoleTeacher>>> result = new LinkedHashMap<>();

        for (Map.Entry<String, Map<String, List<Student>>> majorEntry : studentData.entrySet()) {
            String major = majorEntry.getKey();
            List<Teacher> teachers = teachersByMajor.getOrDefault(major, List.of());
            if (teachers.isEmpty()) continue;

            Teacher defaultTeacher = teachers.get(0);
            for (String grade : majorEntry.getValue().keySet()) {
                result.computeIfAbsent(major, k -> new LinkedHashMap<>())
                        .computeIfAbsent(grade, k -> new ArrayList<>())
                        .add(new ClassRoleTeacher(major, grade, "班主任", defaultTeacher.id, defaultTeacher.name));
            }
        }
        return result;
    }

    private static String buildDefaultUrl() {
        String host = firstNonBlank(
                System.getProperty("db.host"),
                System.getenv("DB_HOST"),
                DEFAULT_DB_HOST
        );
        String port = firstNonBlank(
                System.getProperty("db.port"),
                System.getenv("DB_PORT"),
                DEFAULT_DB_PORT
        );
        String dbName = firstNonBlank(
                System.getProperty("db.name"),
                System.getenv("DB_NAME"),
                DEFAULT_DB_NAME
        );

        return String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8",
                host, port, dbName
        );
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static List<String> buildUrlsToTry(String explicitUrl) {
        if (!explicitUrl.isBlank()) {
            return List.of(explicitUrl);
        }

        LinkedHashSet<String> urls = new LinkedHashSet<>();
        urls.add(buildDefaultUrl());

        for (String dbName : DB_NAME_FALLBACKS) {
            urls.add(buildUrlByDbName(dbName));
        }
        return new ArrayList<>(urls);
    }

    private static String buildUrlByDbName(String dbName) {
        String host = firstNonBlank(
                System.getProperty("db.host"),
                System.getenv("DB_HOST"),
                DEFAULT_DB_HOST
        );
        String port = firstNonBlank(
                System.getProperty("db.port"),
                System.getenv("DB_PORT"),
                DEFAULT_DB_PORT
        );
        return String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8",
                host, port, dbName
        );
    }

    private interface SqlWork<T> {
        T run(Connection conn) throws Exception;
    }

    private <T> T executeSqlWork(SqlWork<T> work) throws Exception {
        String explicitUrl = firstNonBlank(System.getProperty("db.url"), System.getenv("DB_URL"));
        String user = firstNonBlank(System.getProperty("db.user"), System.getenv("DB_USER"), DEFAULT_DB_USER);
        String password = firstNonBlank(System.getProperty("db.password"), System.getenv("DB_PASSWORD"), DEFAULT_DB_PASSWORD);

        Exception lastError = null;
        for (String url : buildUrlsToTry(explicitUrl)) {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                return work.run(conn);
            } catch (Exception e) {
                lastError = e;
            }
        }
        if (lastError != null) throw lastError;
        throw new SQLException("未找到可用数据库连接");
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, tableName.toUpperCase(Locale.ROOT), new String[]{"TABLE"})) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, tableName.toLowerCase(Locale.ROOT), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static int findOrCreateDepartment(Connection conn, String major) throws SQLException {
        String query = "SELECT dept_id FROM departments WHERE dept_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, major);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String insert = "INSERT INTO departments(dept_name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, major);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("创建院系失败: " + major);
    }

    private static int findOrCreateClass(Connection conn, String major, String className) throws SQLException {
        int deptId = findOrCreateDepartment(conn, major);
        String query = "SELECT class_id FROM classes WHERE class_name = ? AND dept_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, className);
            ps.setInt(2, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String insert = "INSERT INTO classes(class_name, dept_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, className);
            ps.setInt(2, deptId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("创建班级失败: " + className);
    }

    private static java.sql.Date parseSqlDateOrNull(String text) {
        if (text == null || text.isBlank()) return null;
        return java.sql.Date.valueOf(text.trim());
    }

    private void reloadFromDatabaseAndRefresh() {
        ClassKey oldSelection = selectedClassKey;
        setLoadingState(true);

        SwingWorker<Map<String, Map<String, List<Student>>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Map<String, List<Student>>> doInBackground() {
                return loadData();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Map<String, List<Student>>> newData = get();
                    data.clear();
                    data.putAll(newData);
                    tree.setModel(buildTreeModel(data));

                    if (oldSelection != null && selectClassNode(oldSelection)) {
                        selectedClassKey = oldSelection;
                    } else if (!selectFirstClassNode()) {
                        selectedClassKey = null;
                        refreshTablesBySelection();
                    }

                    // 连接失败时明确提示，避免误以为程序“没执行”。
                    if (usingMockData && !dbFallbackWarningShown) {
                        dbFallbackWarningShown = true;
                        JOptionPane.showMessageDialog(
                                frame,
                                "数据库连接失败，当前显示的是模拟数据。\n" +
                                "请检查 MySQL 是否启动、账号密码和库名是否正确。\n\n" +
                                "详细原因: " + lastLoadErrorMessage,
                                "数据库未连接",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                    if (!usingMockData) {
                        dbFallbackWarningShown = false;
                    }
                } catch (Exception e) {
                    selectedClassKey = null;
                    refreshTablesBySelection();
                    JOptionPane.showMessageDialog(frame, "刷新数据失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setLoadingState(false);
                }
            }
        };
        worker.execute();
    }

    private boolean selectFirstClassNode() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration<?> enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            if (node.getUserObject() instanceof ClassKey) {
                TreePath path = new TreePath(node.getPath());
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                return true;
            }
        }
        return false;
    }

    private void setLoadingState(boolean loading) {
        if (frame == null) return;
        frame.setCursor(loading
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
    }

    private boolean selectClassNode(ClassKey key) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration<?> enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            Object userObject = node.getUserObject();
            if (userObject instanceof ClassKey classKey
                    && Objects.equals(classKey.major, key.major)
                    && Objects.equals(classKey.clazz, key.clazz)) {
                TreePath path = new TreePath(node.getPath());
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                return true;
            }
        }
        return false;
    }

    private void addStudent() {
        if (selectedClassKey == null) {
            JOptionPane.showMessageDialog(frame, "请先在左侧选择班级，再新增学生。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextField idField = new JTextField();
        JTextField dormField = new JTextField();
        JTextField birthField = new JTextField();
        Object[] message = {
                "学号:", idField,
                "宿舍:", dormField,
                "出生日期(yyyy-MM-dd):", birthField
        };
        int option = JOptionPane.showConfirmDialog(frame, message, "新增学生", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        String studentId = idField.getText().trim();
        String dorm = dormField.getText().trim();
        String birth = birthField.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "学号不能为空。", "校验失败", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            executeSqlWork(conn -> {
                if (tableExists(conn, "students") && tableExists(conn, "classes") && tableExists(conn, "departments")) {
                    int classId = findOrCreateClass(conn, selectedClassKey.major, selectedClassKey.clazz);
                    String sql = "INSERT INTO students(student_id, class_id, dorm_address, birth_date) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, studentId);
                        ps.setInt(2, classId);
                        ps.setString(3, dorm);
                        ps.setDate(4, parseSqlDateOrNull(birth));
                        ps.executeUpdate();
                    }
                    return null;
                }

                if (tableExists(conn, LEGACY_STUDENT_TABLE)) {
                    String sql = "INSERT INTO " + LEGACY_STUDENT_TABLE + "(student_id, major, grade, dorm_address, birth_date) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, studentId);
                        ps.setString(2, selectedClassKey.major);
                        ps.setString(3, selectedClassKey.clazz);
                        ps.setString(4, dorm);
                        ps.setDate(5, parseSqlDateOrNull(birth));
                        ps.executeUpdate();
                    }
                    return null;
                }
                throw new SQLException("未找到可写入的学生表(规范化或旧表)");
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "新增学生失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editStudent() {
        int row = table.getSelectedRow();
        Student selected = tableModel.getStudentAt(row);
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "请先选择要修改的学生。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField dormField = new JTextField(selected.dormAddress);
        JTextField birthField = new JTextField(selected.birthDate);
        Object[] message = {
                "学号(只读):", new JLabel(selected.id),
                "宿舍:", dormField,
                "出生日期(yyyy-MM-dd):", birthField
        };
        int option = JOptionPane.showConfirmDialog(frame, message, "修改学生", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            executeSqlWork(conn -> {
                String dorm = dormField.getText().trim();
                java.sql.Date birth = parseSqlDateOrNull(birthField.getText().trim());

                if (tableExists(conn, "students")) {
                    String sql = "UPDATE students SET dorm_address = ?, birth_date = ? WHERE student_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, dorm);
                        ps.setDate(2, birth);
                        ps.setString(3, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }

                if (tableExists(conn, LEGACY_STUDENT_TABLE)) {
                    String sql = "UPDATE " + LEGACY_STUDENT_TABLE + " SET dorm_address = ?, birth_date = ? WHERE student_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, dorm);
                        ps.setDate(2, birth);
                        ps.setString(3, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
                throw new SQLException("未找到可更新的学生表");
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "修改学生失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        Student selected = tableModel.getStudentAt(row);
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "请先选择要删除的学生。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame, "确认删除学号为 " + selected.id + " 的学生吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            executeSqlWork(conn -> {
                if (tableExists(conn, "students")) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE student_id = ?")) {
                        ps.setString(1, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
                if (tableExists(conn, LEGACY_STUDENT_TABLE)) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + LEGACY_STUDENT_TABLE + " WHERE student_id = ?")) {
                        ps.setString(1, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
                throw new SQLException("未找到可删除的学生表");
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "删除学生失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTeacher() {
        if (selectedClassKey == null) {
            JOptionPane.showMessageDialog(frame, "请先在左侧选择一个班级，以确定教师所属专业。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField officeField = new JTextField();
        JTextField hireField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.add(new JLabel("专业:"));
        panel.add(new JLabel(selectedClassKey.major));
        panel.add(new JLabel("工号:"));
        panel.add(idField);
        panel.add(new JLabel("姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("职称:"));
        panel.add(titleField);
        panel.add(new JLabel("电话:"));
        panel.add(phoneField);
        panel.add(new JLabel("邮箱:"));
        panel.add(emailField);
        panel.add(new JLabel("办公室:"));
        panel.add(officeField);
        panel.add(new JLabel("入职日期(yyyy-MM-dd):"));
        panel.add(hireField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "新增教师", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        String teacherId = idField.getText().trim();
        String name = nameField.getText().trim();
        if (teacherId.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "工号和姓名不能为空。", "校验失败", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            executeSqlWork(conn -> {
                if (tableExists(conn, "teachers") && tableExists(conn, "departments")) {
                    int deptId = findOrCreateDepartment(conn, selectedClassKey.major);
                    String sql = "INSERT INTO teachers(teacher_id, name, dept_id, title, phone, email, office_address, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, teacherId);
                        ps.setString(2, name);
                        ps.setInt(3, deptId);
                        ps.setString(4, titleField.getText().trim());
                        ps.setString(5, phoneField.getText().trim());
                        ps.setString(6, emailField.getText().trim());
                        ps.setString(7, officeField.getText().trim());
                        ps.setDate(8, parseSqlDateOrNull(hireField.getText().trim()));
                        ps.executeUpdate();
                    }
                    return null;
                }

                if (tableExists(conn, LEGACY_TEACHER_TABLE)) {
                    String sql = "INSERT INTO " + LEGACY_TEACHER_TABLE + "(teacher_id, name, major, title, phone, email, office_address, hire_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, teacherId);
                        ps.setString(2, name);
                        ps.setString(3, selectedClassKey.major);
                        ps.setString(4, titleField.getText().trim());
                        ps.setString(5, phoneField.getText().trim());
                        ps.setString(6, emailField.getText().trim());
                        ps.setString(7, officeField.getText().trim());
                        ps.setDate(8, parseSqlDateOrNull(hireField.getText().trim()));
                        ps.executeUpdate();
                    }
                    return null;
                }
                throw new SQLException("未找到可写入的教师表");
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "新增教师失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editTeacher() {
        int row = teacherTable.getSelectedRow();
        Teacher selected = teacherTableModel.getTeacherAt(row);
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "请先选择要修改的教师。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField nameField = new JTextField(selected.name);
        JTextField titleField = new JTextField(selected.title);
        JTextField phoneField = new JTextField(selected.phone);
        JTextField emailField = new JTextField(selected.email);
        JTextField officeField = new JTextField(selected.officeAddress);
        JTextField hireField = new JTextField(selected.hireDate);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.add(new JLabel("工号(只读):"));
        panel.add(new JLabel(selected.id));
        panel.add(new JLabel("姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("职称:"));
        panel.add(titleField);
        panel.add(new JLabel("电话:"));
        panel.add(phoneField);
        panel.add(new JLabel("邮箱:"));
        panel.add(emailField);
        panel.add(new JLabel("办公室:"));
        panel.add(officeField);
        panel.add(new JLabel("入职日期(yyyy-MM-dd):"));
        panel.add(hireField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "修改教师", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            executeSqlWork(conn -> {
                java.sql.Date hireDate = parseSqlDateOrNull(hireField.getText().trim());
                if (tableExists(conn, "teachers") && tableExists(conn, "departments")) {
                    int deptId = findOrCreateDepartment(conn, selected.major);
                    String sql = "UPDATE teachers SET name = ?, dept_id = ?, title = ?, phone = ?, email = ?, office_address = ?, hire_date = ? WHERE teacher_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, nameField.getText().trim());
                        ps.setInt(2, deptId);
                        ps.setString(3, titleField.getText().trim());
                        ps.setString(4, phoneField.getText().trim());
                        ps.setString(5, emailField.getText().trim());
                        ps.setString(6, officeField.getText().trim());
                        ps.setDate(7, hireDate);
                        ps.setString(8, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }

                if (tableExists(conn, LEGACY_TEACHER_TABLE)) {
                    String sql = "UPDATE " + LEGACY_TEACHER_TABLE + " SET name = ?, major = ?, title = ?, phone = ?, email = ?, office_address = ?, hire_date = ? WHERE teacher_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, nameField.getText().trim());
                        ps.setString(2, selected.major);
                        ps.setString(3, titleField.getText().trim());
                        ps.setString(4, phoneField.getText().trim());
                        ps.setString(5, emailField.getText().trim());
                        ps.setString(6, officeField.getText().trim());
                        ps.setDate(7, hireDate);
                        ps.setString(8, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
                throw new SQLException("未找到可更新的教师表");
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "修改教师失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTeacher() {
        int row = teacherTable.getSelectedRow();
        Teacher selected = teacherTableModel.getTeacherAt(row);
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "请先选择要删除的教师。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame, "确认删除工号为 " + selected.id + " 的教师吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            executeSqlWork(conn -> {
                if (tableExists(conn, "teachers")) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM teachers WHERE teacher_id = ?")) {
                        ps.setString(1, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
                if (tableExists(conn, LEGACY_TEACHER_TABLE)) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + LEGACY_TEACHER_TABLE + " WHERE teacher_id = ?")) {
                        ps.setString(1, selected.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
                throw new SQLException("未找到可删除的教师表");
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "删除教师失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addClassRoleTeacher() {
        if (selectedClassKey == null) {
            JOptionPane.showMessageDialog(frame, "请先选择班级。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<Teacher> teachers = teacherData.getOrDefault(selectedClassKey.major, List.of());
        if (teachers.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "当前专业没有教师数据，无法创建映射。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField roleField = new JTextField();
        JComboBox<String> teacherCombo = new JComboBox<>();
        for (Teacher t : teachers) {
            teacherCombo.addItem(t.id + " - " + t.name);
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.add(new JLabel("专业:"));
        panel.add(new JLabel(selectedClassKey.major));
        panel.add(new JLabel("班级:"));
        panel.add(new JLabel(selectedClassKey.clazz));
        panel.add(new JLabel("角色:"));
        panel.add(roleField);
        panel.add(new JLabel("教师:"));
        panel.add(teacherCombo);

        int option = JOptionPane.showConfirmDialog(frame, panel, "新增班级角色映射", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        String roleName = roleField.getText().trim();
        String teacherId = String.valueOf(teacherCombo.getSelectedItem()).split(" - ")[0].trim();
        if (roleName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "角色不能为空。", "校验失败", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            executeSqlWork(conn -> {
                if (!tableExists(conn, CLASS_ROLE_TEACHER_TABLE)) {
                    throw new SQLException("缺少映射表 " + CLASS_ROLE_TEACHER_TABLE + "，请先创建该表后再维护映射。");
                }
                String sql = "INSERT INTO " + CLASS_ROLE_TEACHER_TABLE + "(major, grade, role_name, teacher_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, selectedClassKey.major);
                    ps.setString(2, selectedClassKey.clazz);
                    ps.setString(3, roleName);
                    ps.setString(4, teacherId);
                    ps.executeUpdate();
                }
                return null;
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "新增班级角色映射失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editClassRoleTeacher() {
        int row = classRoleTeacherTable.getSelectedRow();
        ClassRoleTeacher selected = classRoleTeacherTableModel.getRowAt(row);
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "请先选择要修改的映射记录。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Teacher> teachers = teacherData.getOrDefault(selected.major, List.of());
        JComboBox<String> teacherCombo = new JComboBox<>();
        int selectedIdx = 0;
        for (int i = 0; i < teachers.size(); i++) {
            Teacher t = teachers.get(i);
            String item = t.id + " - " + t.name;
            teacherCombo.addItem(item);
            if (Objects.equals(t.id, selected.teacherId)) selectedIdx = i;
        }
        if (teacherCombo.getItemCount() > 0) {
            teacherCombo.setSelectedIndex(selectedIdx);
        }

        JTextField roleField = new JTextField(selected.role);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.add(new JLabel("专业:"));
        panel.add(new JLabel(selected.major));
        panel.add(new JLabel("班级:"));
        panel.add(new JLabel(selected.clazz));
        panel.add(new JLabel("角色:"));
        panel.add(roleField);
        panel.add(new JLabel("教师:"));
        panel.add(teacherCombo);

        int option = JOptionPane.showConfirmDialog(frame, panel, "修改班级角色映射", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        String newRole = roleField.getText().trim();
        String newTeacherId = teacherCombo.getSelectedItem() == null ? "" : String.valueOf(teacherCombo.getSelectedItem()).split(" - ")[0].trim();
        if (newRole.isEmpty() || newTeacherId.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "角色和教师不能为空。", "校验失败", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            executeSqlWork(conn -> {
                if (!tableExists(conn, CLASS_ROLE_TEACHER_TABLE)) {
                    throw new SQLException("缺少映射表 " + CLASS_ROLE_TEACHER_TABLE);
                }
                String sql = "UPDATE " + CLASS_ROLE_TEACHER_TABLE + " SET role_name = ?, teacher_id = ? WHERE major = ? AND grade = ? AND role_name = ? AND teacher_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, newRole);
                    ps.setString(2, newTeacherId);
                    ps.setString(3, selected.major);
                    ps.setString(4, selected.clazz);
                    ps.setString(5, selected.role);
                    ps.setString(6, selected.teacherId);
                    ps.executeUpdate();
                }
                return null;
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "修改班级角色映射失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClassRoleTeacher() {
        int row = classRoleTeacherTable.getSelectedRow();
        ClassRoleTeacher selected = classRoleTeacherTableModel.getRowAt(row);
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "请先选择要删除的映射记录。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame, "确认删除角色 " + selected.role + " 对应教师 " + selected.teacherId + " 吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            executeSqlWork(conn -> {
                if (!tableExists(conn, CLASS_ROLE_TEACHER_TABLE)) {
                    throw new SQLException("缺少映射表 " + CLASS_ROLE_TEACHER_TABLE);
                }
                String sql = "DELETE FROM " + CLASS_ROLE_TEACHER_TABLE + " WHERE major = ? AND grade = ? AND role_name = ? AND teacher_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, selected.major);
                    ps.setString(2, selected.clazz);
                    ps.setString(3, selected.role);
                    ps.setString(4, selected.teacherId);
                    ps.executeUpdate();
                }
                return null;
            });
            reloadFromDatabaseAndRefresh();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "删除班级角色映射失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- 模拟数据（数据库不可用时的回退） ----------
    private Map<String, Map<String, List<Student>>> mockData() {
        Map<String, Map<String, List<Student>>> m = new LinkedHashMap<>();

        m.put("计算机科学与技术", new LinkedHashMap<>());
        m.get("计算机科学与技术").put("计科2201", List.of(
                new Student("20220001", "计算机科学与技术", "2022级", "杏园1-101", "2003-03-15"),
                new Student("20220002", "计算机科学与技术", "2022级", "杏园1-102", "2002-07-20"),
                new Student("20220003", "计算机科学与技术", "2022级", "杏园1-103", "2003-01-05")
        ));
        m.get("计算机科学与技术").put("计科2202", List.of(
                new Student("20220011", "计算机科学与技术", "2022级", "杏园2-201", "2003-05-12"),
                new Student("20220012", "计算机科学与技术", "2022级", "杏园2-202", "2002-11-09")
        ));

        m.put("软件工程", new LinkedHashMap<>());
        m.get("软件工程").put("软工2201", List.of(
                new Student("20221001", "软件工程", "2022级", "桂苑1-201", "2003-04-13"),
                new Student("20221002", "软件工程", "2022级", "桂苑1-202", "2003-09-19")
        ));
        m.get("软件工程").put("软工2202", List.of(
                new Student("20221011", "软件工程", "2022级", "桂苑2-301", "2002-12-30")
        ));

        return m;
    }

    private Map<String, List<Teacher>> mockTeacherData() {
        Map<String, List<Teacher>> m = new LinkedHashMap<>();
        m.put("计算机科学与技术", List.of(
                new Teacher("T202001", "张明远", "计算机科学与技术", "教授", "13812345678", "zhangmy@univ.edu", "信息学院楼A301", "2020-09-01"),
                new Teacher("T202305", "刘欣然", "计算机科学与技术", "讲师", "18766554433", "liuxr@univ.edu", "信息学院楼A105", "2023-07-15")
        ));
        m.put("软件工程", List.of(
                new Teacher("T201902", "李婉清", "软件工程", "副教授", "13987654321", "liwq@univ.edu", "信息学院楼B205", "2019-06-15")
        ));
        m.put("数据科学与大数据技术", List.of(
                new Teacher("T202103", "王建华", "数据科学与大数据技术", "讲师", "13711223344", "wangjh@univ.edu", "信息学院楼A409", "2021-08-20")
        ));
        m.put("网络工程", List.of(
                new Teacher("T202204", "陈思敏", "网络工程", "副教授", "13655667788", "chensm@univ.edu", "信息学院楼C102", "2022-03-10")
        ));
        m.put("信息安全", List.of(
                new Teacher("T201801", "赵志远", "信息安全", "教授", "13599887766", "zhaozy@univ.edu", "信息学院楼D203", "2018-12-01")
        ));
        return m;
    }

    private Map<String, Map<String, List<ClassRoleTeacher>>> mockClassRoleTeacherData() {
        return inferClassRoleTeacherData(mockData(), mockTeacherData());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClassInfoApp().initAndShow());
    }
}