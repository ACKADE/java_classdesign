# Database Migration (Normalized Schema)

This folder contains SQL to migrate legacy tables (`cs_students`, `cs_teacher`) into a normalized engineering schema.

## New tables

- `departments`
- `classes`
- `students`
- `teachers`
- `teacher_class_map`

## Run

```sql
SOURCE C:/Users/32654/Desktop/java_classdesign/class two/apps/sql/01_normalize_school_schema.sql;
```

Or copy/paste into MySQL client after selecting the `text` database.

## App behavior

`src/ClassInfoApp.java` now loads from normalized tables first. If not found, it falls back to legacy tables.

