package com.stk123.controller;

import com.stk123.model.RequestResult;
import com.stk123.model.dto.SearchResult;
import com.stk123.service.core.EsService;
import com.stk123.service.core.StockService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@Controller
@RequestMapping("/search")
@CommonsLog
public class SearchController {

    @Autowired
    private StockService stockService;
    @Autowired
    private EsService esService;

    @RequestMapping("/{query}")
    @ResponseBody
    public RequestResult<Collection<SearchResult>> search(@PathVariable("query")String query){
        Collection<SearchResult> results = stockService.search(query);
        return RequestResult.success(results);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @ResponseBody
    public RequestResult<Collection<SearchResult>> delete(){
        stockService.delete();
        return RequestResult.success();
    }

}