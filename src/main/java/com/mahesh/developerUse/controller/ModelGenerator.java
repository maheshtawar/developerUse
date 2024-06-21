/**
 * 
 */
package com.mahesh.developerUse.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mahesh.developerUse.service.ModelClassGeneratorService;

/**
 * @author MaheshT
 *
 */

@RestController
public class ModelGenerator {

    @Autowired
    private ModelClassGeneratorService modelClassGeneratorService;

    @PostMapping("/generateModelClass")
    public String generateModelClassFromCreateQuery(@RequestBody String createTableQuery) {
        return modelClassGeneratorService.generateModelClassFromCreateQuery(createTableQuery);
    }
}