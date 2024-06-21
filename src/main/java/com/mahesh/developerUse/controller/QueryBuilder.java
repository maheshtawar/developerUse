/**
 * 
 */
package com.mahesh.developerUse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.mahesh.developerUse.service.QueryBuilderService;

/**
 * @author MaheshT
 *
 */

@RestController
public class QueryBuilder {

    @Autowired
    private QueryBuilderService queryBuilderService;

    @PostMapping("/queryBuilder")
    public String convertSqlQuery(@RequestBody String query) {
        return queryBuilderService.convertQueryToAppendFormat(query).toString();
    }
}