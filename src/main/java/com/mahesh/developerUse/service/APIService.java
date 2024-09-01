/**
 * 
 */
package com.mahesh.developerUse.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * 
 */

@Service
public class APIService {
	
	 public static String replaceNewlineWithDelimiter(String input, String newDelimiter) {
	        // Split the input by newline characters
	        List<String> tokens = Arrays.asList(input.split("\\r?\\n"));

	        // Trim each token to remove extra spaces and filter out any empty tokens
	        List<String> trimmedTokens = tokens.stream()
	                                           .map(String::trim)          // Trim each token
	                                           .filter(token -> !token.isEmpty()) // Remove empty tokens
	                                           .collect(Collectors.toList());

	        // Join the trimmed tokens with the specified delimiter
	        return String.join(newDelimiter, trimmedTokens);
	    }

}
