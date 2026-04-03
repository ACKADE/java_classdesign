SHOW DATABASES;
CREATE DATABASE IF NOT EXISTS text;
USE text;

CREATE TABLE IF NOT EXISTS cs_students (
    student_id VARCHAR(20) PRIMARY KEY COMMENT '学号',
    major VARCHAR(50) NOT NULL COMMENT '专业',
    grade VARCHAR(10) NOT NULL COMMENT '年级（如：2024级）',
    dorm_address VARCHAR(100) COMMENT '宿舍住址',
    birth_date DATE COMMENT '出生日期'
);

CREATE TABLE IF NOT EXISTS cs_teacher (
    teacher_id VARCHAR(20) PRIMARY KEY COMMENT '教师工号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    major VARCHAR(50) NOT NULL COMMENT '所属专业',
    title VARCHAR(30) COMMENT '职称（如：教授、副教授、讲师）',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    office_address VARCHAR(100) COMMENT '办公室地址',
    hire_date DATE COMMENT '入职日期'
);

INSERT INTO cs_students (student_id, major, grade, dorm_address, birth_date) VALUES
('202411010101', '计算机科学与技术', '2024级', '杏园公寓1幢101室', '2006-03-15'),
('202411020202', '软件工程', '2024级', '杏园公寓1幢102室', '2005-11-20'),
('202311030303', '人工智能', '2023级', '桂苑公寓2幢205室', '2005-08-10'),
('202211040404', '数据科学与大数据技术', '2022级', '桃源公寓3幢310室', '2004-02-28'),
('202511050505', '计算机科学与技术', '2025级', '杏园公寓1幢201室', '2007-01-05')
ON DUPLICATE KEY UPDATE dorm_address = VALUES(dorm_address);

UPDATE cs_students SET dorm_address = '杏园公寓2幢303室' WHERE student_id = '202411010101';

INSERT INTO cs_students (student_id, major, grade, dorm_address, birth_date) VALUES
('202401001', '计算机科学与技术', '2024级', '学生公寓1号楼101室', '2006-03-12'),
('202401002', '软件工程', '2024级', '学生公寓2号楼205室', '2005-11-23'),
('202401003', '数据科学与大数据技术', '2024级', '学生公寓1号楼308室', '2006-01-05'),
('202301045', '计算机科学与技术', '2023级', '学生公寓3号楼412室', '2005-07-19'),
('202301078', '网络工程', '2023级', '学生公寓2号楼118室', '2004-12-30'),
('202201112', '信息安全', '2022级', '学生公寓4号楼502室', '2004-04-09')
ON DUPLICATE KEY UPDATE dorm_address = VALUES(dorm_address);

INSERT INTO cs_teacher (teacher_id, name, major, title, phone, email, office_address, hire_date) VALUES
('T202001', '张明远', '计算机科学与技术', '教授', '13812345678', 'zhangmy@univ.edu', '信息学院楼A301', '2020-09-01'),
('T201902', '李婉清', '软件工程', '副教授', '13987654321', 'liwq@univ.edu', '信息学院楼B205', '2019-06-15'),
('T202103', '王建华', '数据科学与大数据技术', '讲师', '13711223344', 'wangjh@univ.edu', '信息学院楼A409', '2021-08-20'),
('T202204', '陈思敏', '网络工程', '副教授', '13655667788', 'chensm@univ.edu', '信息学院楼C102', '2022-03-10'),
('T201801', '赵志远', '信息安全', '教授', '13599887766', 'zhaozy@univ.edu', '信息学院楼D203', '2018-12-01'),
('T202305', '刘欣然', '计算机科学与技术', '讲师', '18766554433', 'liuxr@univ.edu', '信息学院楼A105', '2023-07-15')
ON DUPLICATE KEY UPDATE email = VALUES(email);

SELECT * FROM cs_students;
SELECT * FROM cs_teacher;
SHOW TABLES;

