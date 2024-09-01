/**
 * 
 */
package com.mahesh.developerUse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mahesh.developerUse.service.APIService;
import com.mahesh.developerUse.service.ModelClassGeneratorService;
import com.mahesh.developerUse.service.QueryBuilderService;

/**
 * 
 */
@Controller
public class IndexController {

	@Autowired
	private QueryBuilderService queryBuilderService;

	@Autowired
	private ModelClassGeneratorService modelClassGeneratorService;

	@Autowired
	APIService apiService;

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/queryBuilderUI")
	public String convertSqlQuery(@RequestParam("query") String query, Model model) {
		String result = queryBuilderService.convertQueryToAppendFormat(query).toString();
		model.addAttribute("result", result);
		return "index";
	}

	@PostMapping("/generateModelClassUI")
	public String generateModelClassFromCreateQuery(@RequestParam("query") String query, Model model) {
		String result = modelClassGeneratorService.generateModelClassFromCreateQuery(query);
		model.addAttribute("result", result);
		return "index";
	}

	@PostMapping("/replaceNewlineUI")
	public String replaceNewlineWithDelimiter(@RequestParam("query") String data,
			@RequestParam(value = "delimiter", defaultValue = ",") String delimiter, Model model) {
		String result = apiService.replaceNewlineWithDelimiter(data, delimiter);
		model.addAttribute("result", result);
		return "index";
	}

}
