/**
 * 
 */
package com.mahesh.developerUse.service;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * @author MaheshT
 *
 */

@Service
public class ModelClassGeneratorService {

	public String generateModelClassFromCreateQuery(String createTableQuery) {
		String className = extractClassName(createTableQuery);
		String fields = extractFields(createTableQuery);

		StringBuilder classBuilder = new StringBuilder();
		classBuilder.append("public class ").append(className).append(" {\n");

		// Generate fields
		String[] fieldLines = fields.split(",\\s*(?![^\\(]*\\))"); // Split by comma, but not within parentheses
		for (String fieldLine : fieldLines) {
			String[] fieldParts = fieldLine.trim().split("\\s+");
			if (fieldParts.length < 2)
				continue; // Skip invalid lines
			String fieldType = mapSqlTypeToJavaType(fieldParts[1]);
			String fieldName = convertToCamelCase(fieldParts[0].replace("`", ""));
			classBuilder.append("    private ").append(fieldType).append(" ").append(fieldName).append(";\n");
		}
		classBuilder.append("\n");

		// Generate getter and setter methods
		for (String fieldLine : fieldLines) {
			String[] fieldParts = fieldLine.trim().split("\\s+");
			if (fieldParts.length < 2)
				continue; // Skip invalid lines
			String fieldType = mapSqlTypeToJavaType(fieldParts[1]);
			String fieldName = convertToCamelCase(fieldParts[0].replace("`", ""));
			String capitalizedFieldName = capitalize(fieldName);

			// Getter
			classBuilder.append("    public ").append(fieldType).append(" get").append(capitalizedFieldName)
					.append("() {\n");
			classBuilder.append("        return ").append(fieldName).append(";\n");
			classBuilder.append("    }\n");

			// Setter
			classBuilder.append("    public void set").append(capitalizedFieldName).append("(").append(fieldType)
					.append(" ").append(fieldName).append(") {\n");
			classBuilder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
			classBuilder.append("    }\n");
		}
		classBuilder.append("\n");

		// Generate toString method
		classBuilder.append("    @Override\n");
		classBuilder.append("    public String toString() {\n");
		classBuilder.append("        return \"").append(className).append(" [");
		for (int i = 0; i < fieldLines.length; i++) {
			String[] fieldParts = fieldLines[i].trim().split("\\s+");
			if (fieldParts.length < 2)
				continue; // Skip invalid lines
			String fieldName = convertToCamelCase(fieldParts[0].replace("`", ""));
			classBuilder.append(fieldName).append("=\" + ").append(fieldName);
			if (i < fieldLines.length - 1) {
				classBuilder.append(" + \", ");
			}
		}
		classBuilder.append(" + \"]\";\n");
		classBuilder.append("    }\n");

		classBuilder.append("}\n");

		return classBuilder.toString();
	}

	private String extractClassName(String query) {
		Pattern pattern = Pattern.compile("CREATE TABLE `?(\\w+)`? \\(");
		Matcher matcher = pattern.matcher(query);
		if (matcher.find()) {
			return capitalize(convertToCamelCase(matcher.group(1)));
		}
		throw new IllegalArgumentException("Invalid CREATE TABLE query");
	}

	private String extractFields(String query) {
		Pattern pattern = Pattern.compile("\\((.+)\\)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(query);
		if (matcher.find()) {
			return matcher.group(1).replaceAll("PRIMARY KEY \\(`\\w+`\\)", "").trim();
		}
		throw new IllegalArgumentException("Invalid CREATE TABLE query");
	}

	private String mapSqlTypeToJavaType(String sqlType) {
		sqlType = sqlType.toUpperCase().split("\\(")[0];
		switch (sqlType) {
		case "BIT":
		case "BOOLEAN":
			return "boolean";
		case "TINYINT":
		case "SMALLINT":
			return "short";
		case "INT":
		case "INTEGER":
			return "int";
		case "BIGINT":
			return "long";
		case "REAL":
			return "float";
		case "FLOAT":
		case "DOUBLE":
			return "double";
		case "NUMERIC":
		case "DECIMAL":
			return "java.math.BigDecimal";
		case "CHAR":
		case "VARCHAR":
		case "LONGVARCHAR":
		case "NCHAR":
		case "NVARCHAR":
		case "LONGNVARCHAR":
			return "String";
		case "DATE":
			return "Date";
		case "TIME":
			return "Time";
		case "TIMESTAMP":
			return "Timestamp";
		case "BINARY":
		case "VARBINARY":
		case "LONGVARBINARY":
			return "byte[]";
		case "BLOB":
			return "Blob";
		case "CLOB":
			return "Clob";
		default:
			throw new IllegalArgumentException("Unsupported SQL type: " + sqlType);
		}
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	private String convertToCamelCase(String str) {
		String[] parts = str.split("_");
		StringBuilder camelCaseString = new StringBuilder(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			camelCaseString.append(capitalize(parts[i]));
		}
		return camelCaseString.toString();
	}
}