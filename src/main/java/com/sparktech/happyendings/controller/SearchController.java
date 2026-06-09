package com.sparktech.happyendings.controller;

import com.sparktech.happyendings.dto.ApiResponse;
import com.sparktech.happyendings.dto.SearchResult;
import com.sparktech.happyendings.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResult>> search(@RequestParam String q) {
        SearchResult results = searchService.globalSearch(q);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
