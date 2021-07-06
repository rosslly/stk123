package com.stk123.app.web;

import com.stk123.app.util.WebUtils;
import com.stk123.common.CommonUtils;
import com.stk123.model.core.Stock;
import com.stk123.service.core.StockService;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/bk")
@CommonsLog
public class BkWebController {

    @Autowired
    private StockService stockService;

    @RequestMapping("/{bkCode}")
    public String bk(@PathVariable(value = "bkCode", required = true)String bkCode, Model model){
        List<Stock> bks = stockService.buildStocks(bkCode);
        List<Stock> stocks = bks.get(0).getStocks();
        stocks = stockService.buildNews(stocks, CommonUtils.addDay(new Date(), -180));

        model.addAttribute("stocks", WebUtils.getStockMap(stocks));
        return "k";
    }


    @RequestMapping("/list/{codes}")
    public String show(@PathVariable(value = "codes", required = true)String codes, Model model){
        String[] stks = StringUtils.split(codes, ",");
        List<Stock> stocks = stockService.buildStocks(stks);
        stocks = stockService.buildNews(stocks, CommonUtils.addDay(new Date(), -180));

        model.addAttribute("stocks", WebUtils.getStockMap(stocks));
        return "k";
    }

}
