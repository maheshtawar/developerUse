/**
 * 
 */
package com.mahesh.developerUse.service;

import org.springframework.stereotype.Service;

/**
 * @author MaheshT
 *
 */

@Service
public class QueryBuilderService {

	public StringBuilder convertQueryToAppendFormat(String query) {
		StringBuilder sb = new StringBuilder();
		sb.append("StringBuilder sql = new StringBuilder();\n");

		// Split the query by new lines
		String[] lines = query.split("\\n");

		for (String line : lines) {
			// Trim leading and trailing whitespace from each line
			line = line.trim();

			if (!line.isEmpty()) {
				sb.append("sql.append(\"").append(line).append(" \");\n");
			}
		}

		// Remove the trailing space and semicolon from the last append statement if
		// needed
		int lastIndex = sb.lastIndexOf(" ;\");\n");
		if (lastIndex != -1) {
			sb.replace(lastIndex, lastIndex + 5, ";\");\n");
		}

		return sb;
	}
}