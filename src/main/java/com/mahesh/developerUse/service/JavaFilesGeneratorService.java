package com.mahesh.developerUse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JavaFilesGeneratorService {

    @Autowired
    private ModelClassGeneratorService modelClassGeneratorService; // Inject the model generator

    public Map<String, String> generateJavaFilesFromCreateQuery(String createTableQuery) {
        Map<String, String> files = new LinkedHashMap<>();
        String className = extractClassName(createTableQuery);

        // Use the real model generator for the model class
        files.put(className + ".java", modelClassGeneratorService.generateModelClassFromCreateQuery(createTableQuery));
        files.put(className + "DAO.java", generateRepositoryInterface(className));
        files.put(className + "DAOImpl.java", generateRepository(className, createTableQuery));
        files.put(className + "ServiceInterface.java", generateServiceInterface(className));
        files.put(className + "Service.java", generateService(className));
        files.put(className + "Controller.java", generateController(className));

        return files;
    }

    private String extractClassName(String query) {
        String upper = query.toUpperCase();
        int idx = upper.indexOf("CREATE TABLE");
        if (idx == -1) throw new IllegalArgumentException("Invalid CREATE TABLE query");
        String[] parts = query.substring(idx + 12).trim().split("\\s+|\\(");
        String table = parts[0].replace("`", "");
        return capitalize(toCamelCase(table));
    }

    private String toCamelCase(String str) {
        String[] parts = str.split("_");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            sb.append(capitalize(parts[i].toLowerCase()));
        }
        return sb.toString();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String generateRepository(String className, String createTableQuery) {
        String paramName = className.substring(0, 1).toLowerCase() + className.substring(1);
        String repoName = className + "DAOImpl";
        String interfaceName = className + "DAO";

        String tableName = className.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase() + "s";
        String columnsPart = createTableQuery.substring(createTableQuery.indexOf("(") + 1, createTableQuery.lastIndexOf(")"));
        String[] lines = columnsPart.split(",\s*(\r?\n)?");

        StringBuilder fields = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        StringBuilder updates = new StringBuilder();
        StringBuilder insertParams = new StringBuilder();
        StringBuilder updateParams = new StringBuilder();
        String idColumn = null;

        java.util.List<String> columnList = new java.util.ArrayList<>();
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 2) continue;
            String col = parts[0].replace("`", "");
            if (line.toUpperCase().contains("AUTO_INCREMENT") || line.toUpperCase().contains("PRIMARY KEY")) {
                if (idColumn == null) idColumn = col;
                continue;
            }
            if (fields.length() > 0) {
                fields.append(", ");
                placeholders.append(", ");
                updates.append(", ");
            }
            fields.append(col);
            placeholders.append("?");
            updates.append(col + " = ?");
            columnList.add(col);
        }
        if (idColumn == null) idColumn = "id";

        for (int i = 0; i < columnList.size(); i++) {
            String col = columnList.get(i);
            insertParams.append("            ps.setObject(" + (i + 1) + ", " + paramName + ".get" + capitalize(toCamelCase(col)) + "());\n");
            updateParams.append("            ps.setObject(" + (i + 1) + ", " + paramName + ".get" + capitalize(toCamelCase(col)) + "());\n");
        }
        updateParams.append("            ps.setObject(" + (columnList.size() + 1) + ", " + paramName + ".get" + capitalize(toCamelCase(idColumn)) + "());\n");

        return "package org.mkcl.project.repository;\n\n" +
               "import java.sql.*;\n" +
               "import java.util.*;\n" +
               "import org.springframework.jdbc.core.JdbcTemplate;\n" +
               "import org.springframework.jdbc.support.GeneratedKeyHolder;\n" +
               "import org.springframework.jdbc.support.KeyHolder;\n" +
               "import org.springframework.stereotype.Repository;\n" +
               "import org.mkcl.project.model." + className + ";\n\n" +
               "@Repository(\"" + paramName + "DAOImpl\")\n" +
               "public class " + repoName + " implements " + interfaceName + " {\n\n" +
               "    @Override\n" +
               "    public long add" + className + "(" + className + " " + paramName + ", JdbcTemplate writer) throws Exception {\n" +
               "        String sql = \"INSERT INTO " + tableName + " (" + fields + ") VALUES (" + placeholders + ")\";\n" +
               "        KeyHolder keyHolder = new GeneratedKeyHolder();\n" +
               "        writer.update(con -> {\n" +
               "            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);\n" +
               insertParams.toString() +
               "            return ps;\n" +
               "        }, keyHolder);\n" +
               "        return keyHolder.getKey().longValue();\n" +
               "    }\n\n" +
               "    @Override\n" +
               "    public int update" + className + "(" + className + " " + paramName + ", JdbcTemplate writer) throws Exception {\n" +
               "        String sql = \"UPDATE " + tableName + " SET " + updates + " WHERE " + idColumn + " = ?\";\n" +
               "        return writer.update(con -> {\n" +
               "            PreparedStatement ps = con.prepareStatement(sql);\n" +
               updateParams.toString() +
               "            return ps;\n" +
               "        });\n" +
               "    }\n\n" +
               "    @Override\n" +
               "    public int delete" + className + "ById(Long id, JdbcTemplate writer) throws Exception {\n" +
               "        String sql = \"DELETE FROM " + tableName + " WHERE " + idColumn + " = ?\";\n" +
               "        return writer.update(sql, id);\n" +
               "    }\n\n" +
               "    @Override\n" +
               "    public " + className + " get" + className + "ById(Long id, JdbcTemplate reader) throws Exception {\n" +
               "        String sql = \"SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?\";\n" +
               "        List<" + className + "> list = reader.query(sql, new Object[]{id}, (rs, rowNum) -> {\n" +
               "            return null; // TODO: implement mapping\n" +
               "        });\n" +
               "        return list.isEmpty() ? null : list.get(0);\n" +
               "    }\n\n" +
               "    @Override\n" +
               "    public List<" + className + "> getAll" + className + "s(JdbcTemplate reader) throws Exception {\n" +
               "        String sql = \"SELECT * FROM " + tableName + "\";\n" +
               "        return reader.query(sql, (rs, rowNum) -> {\n" +
               "            return null; // TODO: implement mapping\n" +
               "        });\n" +
               "    }\n" +
               "}\n";
    }

    private String generateRepositoryInterface(String className) {
        String interfaceName = className + "DAO";
        return "package org.mkcl.project.repository;\n\n" +
               "import org.springframework.jdbc.core.JdbcTemplate;\n" +
               "import java.util.List;\n" +
               "public interface " + interfaceName + " {\n" +
               "    long add" + className + "(" + className + " obj, JdbcTemplate writer) throws Exception;\n" +
               "    int update" + className + "(" + className + " obj, JdbcTemplate writer) throws Exception;\n" +
               "    int delete" + className + "ById(Long id, JdbcTemplate writer) throws Exception;\n" +
               "    " + className + " get" + className + "ById(Long id, JdbcTemplate reader) throws Exception;\n" +
               "    List<" + className + "> getAll" + className + "s(JdbcTemplate reader) throws Exception;\n" +
               "}\n";
    }

    private String generateService(String className) {
        String paramName = className.substring(0, 1).toLowerCase() + className.substring(1);
        return "package org.mkcl.project.service;\n\n" +
               "import org.springframework.beans.factory.annotation.Autowired;\n" +
               "import org.springframework.stereotype.Service;\n" +
               "import org.springframework.jdbc.core.JdbcTemplate;\n" +
               "import java.util.List;\n" +
               "import java.util.Optional;\n" +
               "import org.mkcl.project.model." + className + ";\n" +
               "import org.mkcl.project.repository." + className + "DAO;\n\n" +
               "@Service\n" +
               "public class " + className + "Service implements " + className + "ServiceInterface {\n" +
               "    @Autowired\n" +
               "    private " + className + "DAO repository;\n\n" +
               "    @Autowired\n" +
               "    private JdbcTemplate jdbcTemplate;\n\n" +
               "    @Override\n" +
               "    public List<" + className + "> findAll() {\n" +
               "        try {\n" +
               "            return repository.getAll" + className + "s(jdbcTemplate);\n" +
               "        } catch (Exception e) {\n" +
               "            throw new RuntimeException(e);\n" +
               "        }\n" +
               "    }\n\n" +
               "    @Override\n" +
               "    public " + className + " save(" + className + " " + paramName + ") {\n" +
               "        try {\n" +
               "            if (" + paramName + ".getId() == null) {\n" +
               "                repository.add" + className + "(" + paramName + ", jdbcTemplate);\n" +
               "            } else {\n" +
               "                repository.update" + className + "(" + paramName + ", jdbcTemplate);\n" +
               "            }\n" +
               "            return " + paramName + ";\n" +
               "        } catch (Exception e) {\n" +
               "            throw new RuntimeException(e);\n" +
               "        }\n" +
               "    }\n\n" +
               "    @Override\n" +
               "    public void delete(Long id) {\n" +
               "        try {\n" +
               "            repository.delete" + className + "ById(id, jdbcTemplate);\n" +
               "        } catch (Exception e) {\n" +
               "            throw new RuntimeException(e);\n" +
               "        }\n" +
               "    }\n\n" +
               "    @Override\n" +
               "    public Optional<" + className + "> findById(Long id) {\n" +
               "        try {\n" +
               "            return Optional.ofNullable(repository.get" + className + "ById(id, jdbcTemplate));\n" +
               "        } catch (Exception e) {\n" +
               "            throw new RuntimeException(e);\n" +
               "        }\n" +
               "    }\n" +
               "}\n";
    }

    private String generateServiceInterface(String className) {
        String interfaceName = className + "Service";
        return "package org.mkcl.project.service;\n\n" +
               "import java.util.List;\n" +
               "import java.util.Optional;\n" +
               "public interface " + interfaceName + " {\n" +
               "    List<" + className + "> findAll();\n" +
               "    " + className + " save(" + className + " obj);\n" +
               "    void delete(Long id);\n" +
               "    Optional<" + className + "> findById(Long id);\n" +
               "}\n";
    }

    private String generateController(String className) {
        String lc = className.substring(0, 1).toLowerCase() + className.substring(1);
        return "import org.springframework.beans.factory.annotation.Autowired;\n" +
               "import org.springframework.http.ResponseEntity;\n" +
               "import org.springframework.web.bind.annotation.*;\n" +
               "import java.util.List;\n\n" +
               "@RestController\n" +
               "@RequestMapping(\"/" + lc + "s\")\n" +
               "public class " + className + "Controller {\n" +
               "    @Autowired\n" +
               "    private " + className + "Service service;\n\n" +
               "    @GetMapping\n" +
               "    public List<" + className + "> getAll() { return service.findAll(); }\n\n" +
               "    @GetMapping(\"/{id}\")\n" +
               "    public ResponseEntity<" + className + "> getById(@PathVariable Long id) {\n" +
               "        return service.findById(id)\n" +
               "            .map(ResponseEntity::ok)\n" +
               "            .orElse(ResponseEntity.notFound().build());\n" +
               "    }\n\n" +
               "    @PostMapping\n" +
               "    public " + className + " create(@RequestBody " + className + " obj) { return service.save(obj); }\n\n" +
               "    @PutMapping(\"/{id}\")\n" +
               "    public " + className + " update(@PathVariable Long id, @RequestBody " + className + " obj) {\n" +
               "        obj.setId(id); return service.save(obj);\n" +
               "    }\n\n" +
               "    @DeleteMapping(\"/{id}\")\n" +
               "    public void delete(@PathVariable Long id) { service.delete(id); }\n" +
               "}\n";
    }
}
