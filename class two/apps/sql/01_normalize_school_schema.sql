-- Normalize schema: departments / classes / students / teachers
-- Run in MySQL 8+

USE text;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS departments (
    dept_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_departments_dept_name (dept_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS classes (
    class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_classes_dept_class_name (dept_id, class_name),
    KEY idx_classes_dept_id (dept_id),
    CONSTRAINT fk_classes_dept_id FOREIGN KEY (dept_id)
        REFERENCES departments(dept_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(20) PRIMARY KEY,
    class_id BIGINT NOT NULL,
    dorm_address VARCHAR(100) NULL,
    birth_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_students_class_id (class_id),
    CONSTRAINT fk_students_class_id FOREIGN KEY (class_id)
        REFERENCES classes(class_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teachers (
    teacher_id VARCHAR(20) PRIMARY KEY,
    dept_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    title VARCHAR(30) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    office_address VARCHAR(100) NULL,
    hire_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_teachers_dept_id (dept_id),
    CONSTRAINT fk_teachers_dept_id FOREIGN KEY (dept_id)
        REFERENCES departments(dept_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teacher_class_map (
    teacher_id VARCHAR(20) NOT NULL,
    class_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (teacher_id, class_id),
    KEY idx_teacher_class_map_class_id (class_id),
    CONSTRAINT fk_teacher_class_map_teacher_id FOREIGN KEY (teacher_id)
        REFERENCES teachers(teacher_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_teacher_class_map_class_id FOREIGN KEY (class_id)
        REFERENCES classes(class_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1) Department dimension (deduplicated from both legacy tables)
INSERT INTO departments (dept_name)
SELECT DISTINCT x.major
FROM (
    SELECT major FROM cs_students
    UNION
    SELECT major FROM cs_teacher
) x
WHERE x.major IS NOT NULL AND x.major <> ''
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name);

-- 2) Class dimension (current data uses grade as class grouping)
INSERT INTO classes (dept_id, class_name)
SELECT DISTINCT d.dept_id, s.grade
FROM cs_students s
JOIN departments d ON d.dept_name = s.major
WHERE s.grade IS NOT NULL AND s.grade <> ''
ON DUPLICATE KEY UPDATE class_name = VALUES(class_name);

-- 3) Student fact
INSERT INTO students (student_id, class_id, dorm_address, birth_date)
SELECT s.student_id, c.class_id, s.dorm_address, s.birth_date
FROM cs_students s
JOIN departments d ON d.dept_name = s.major
JOIN classes c ON c.dept_id = d.dept_id AND c.class_name = s.grade
ON DUPLICATE KEY UPDATE
    class_id = VALUES(class_id),
    dorm_address = VALUES(dorm_address),
    birth_date = VALUES(birth_date);

-- 4) Teacher fact
INSERT INTO teachers (teacher_id, dept_id, name, title, phone, email, office_address, hire_date)
SELECT t.teacher_id, d.dept_id, t.name, t.title, t.phone, t.email, t.office_address, t.hire_date
FROM cs_teacher t
JOIN departments d ON d.dept_name = t.major
ON DUPLICATE KEY UPDATE
    dept_id = VALUES(dept_id),
    name = VALUES(name),
    title = VALUES(title),
    phone = VALUES(phone),
    email = VALUES(email),
    office_address = VALUES(office_address),
    hire_date = VALUES(hire_date);

-- 5) Optional mapping: each department teacher maps to all classes of the same department
INSERT IGNORE INTO teacher_class_map (teacher_id, class_id)
SELECT t.teacher_id, c.class_id
FROM teachers t
JOIN classes c ON c.dept_id = t.dept_id;

COMMIT;

-- Quick checks
SELECT COUNT(*) AS department_count FROM departments;
SELECT COUNT(*) AS class_count FROM classes;
SELECT COUNT(*) AS student_count FROM students;
SELECT COUNT(*) AS teacher_count FROM teachers;

