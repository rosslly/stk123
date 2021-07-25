package com.stk123.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.stk123.model.RequestResult;
import com.stk123.model.core.Rating;
import com.stk123.model.core.Rps;
import com.stk123.model.core.Stock;
import com.stk123.model.core.Stocks;
import com.stk123.model.enumeration.EnumCate;
import com.stk123.model.enumeration.EnumMarket;
import com.stk123.model.json.View;
import com.stk123.model.projection.StockBasicProjection;
import com.stk123.repository.StkRepository;
import com.stk123.repository.StkTextRepository;
import com.stk123.service.core.BarService;
import com.stk123.service.core.StockService;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stock")
@CommonsLog
public class StockController {

    @Autowired
    private StkRepository stkRepository;
    @Autowired
    private StockService stockService;
    @Autowired
    private BarService barService;
    @Autowired
    private StkTextRepository stkTextRepository;


    @RequestMapping(value = {"/list/{market:1|2|3|cn|us|hk}/{cate}"})
    @ResponseBody
    public RequestResult list(@PathVariable(value = "market", required = false)String market,
                               @PathVariable(value = "cate", required = false)Integer cate){
        EnumMarket em = EnumMarket.getMarket(market);
        if(em == null){
            return RequestResult.failure("Should not be here.");
        }
        List<StockBasicProjection> list = stkRepository.findAllByMarketAndCateOrderByCode(em.getMarket(), cate);
        return RequestResult.success(list);
    }

    @RequestMapping(value = {"/info/{code}"})
    @ResponseBody
    public RequestResult info(@PathVariable(value = "code")String code){
        return RequestResult.success(stockService.findInfo(code));
    }

    @RequestMapping(value = {"/updatekline/{code}"})
    @ResponseBody
    public RequestResult updateKline(@PathVariable(value = "code")String code){
        try {
            Stock stock = stockService.buildStocks(code).stream().findFirst().get();
            log.info("update k line start:"+code);
            barService.updateKline(stock, Integer.MAX_VALUE);
            log.info("update k line end:"+code);
        } catch (Exception e) {
            log.error("",e);
            return RequestResult.success(e.getMessage());
        }
        return RequestResult.success();
    }

    @RequestMapping(value = {"/score/{code}"})
    @ResponseBody
    public RequestResult score(@PathVariable(value = "code")String code){
        Stock stock = Stock.build(code);
        Rating rating = stock.getRating();
        return RequestResult.success(rating);
    }

    @RequestMapping(value = {"/score"})
    @ResponseBody
    public RequestResult score1(@RequestParam(value = "from", required = false, defaultValue = "0")Double percentileFrom,
                                @RequestParam(value = "to", required = false, defaultValue = "100")Double percentileTo
    ){
        if(Stocks.StocksAllCN == null) {
            Stocks.StocksAllCN = stockService.getStocksWithBks(EnumMarket.CN, EnumCate.INDEX_eastmoney_gn, false);
        }
        List<Stock> stocks = Stocks.StocksAllCN;
        stocks = stocks.stream().sorted(Comparator.comparing(Stock::getScore, Comparator.reverseOrder())).collect(Collectors.toList());
        List<Map> list = new ArrayList<>();
        int size = stocks.size();
        stocks = stocks.subList((int)(size*(100-percentileTo)/100), (int)(size*(100-percentileFrom)/100));
        for(Stock stock : stocks){
            Map map = new HashMap();
            map.put("code", stock.getNameAndCode());
            map.put("rating", stock.getRating().toMap());
            map.put("rps", stock.getRps());
            list.add(map);
        }
        Map result = new HashMap();
        result.put("codes", StringUtils.join(stocks.stream().map(Stock::getCode).collect(Collectors.toList()), ","));
        result.put("stocks", list);
        return RequestResult.success(result);
    }

    @RequestMapping(value = {"/clear"})
    @ResponseBody
    public RequestResult clear(){
        Stocks.StocksAllCN = null;
        return RequestResult.success();
    }

    @RequestMapping(value = {"/{code}"})
    @ResponseBody
    @JsonView(View.All.class)
    public RequestResult stocks(@PathVariable(value = "code")String code){
        boolean isRps = Rps.getName(code) != null;
        List<Stock> stocks;
        if(isRps){
            if (Stocks.StocksAllCN == null) {
                if(Stocks.BKsEasymoneyGn == null){
                    Stocks.BKsEasymoneyGn = stockService.getBks(EnumMarket.CN, EnumCate.INDEX_eastmoney_gn);
                }
                Stocks.StocksAllCN = stockService.getStocksWithBks(EnumMarket.CN, Stocks.BKsEasymoneyGn, false);
            }
            stocks = Stocks.StocksAllCN;
            stocks = stockService.calcRps(stocks, code);
            stocks = stocks.subList(0, Math.min(200, stocks.size()));
        }else {
            String[] stks = StringUtils.split(code, ",");
            stocks = stockService.buildStocks(stks);
            if(Stocks.BKsEasymoneyGn == null){
                Stocks.BKsEasymoneyGn = stockService.getBks(EnumMarket.CN, EnumCate.INDEX_eastmoney_gn);
            }
            stocks = stockService.getStocksWithBks(stocks, Stocks.BKsEasymoneyGn, false);
            //stocks = stockService.getStocksWithAllBuilds(stocks, false);
        }

        Set<Stock> bks = stockService.getBks(stocks);
        List<Map> bksList = new ArrayList<>();
        for(Stock bk : bks){
            Map map = new HashMap();
            map.put("name", bk.getNameAndCode());
            map.put("code", bk.getCode());
            List<Stock> finalStocks = stocks;
            map.put("stocks", bk.getStocks().stream().filter(stock -> finalStocks.stream().anyMatch(stock::equals)).map(Stock::getCode).collect(Collectors.toList()));
            bksList.add(map);
        }
        bksList = bksList.stream().sorted(Comparator.comparing(bk -> ((List)bk.get("stocks")).size(), Comparator.reverseOrder())).collect(Collectors.toList());

        Map result = new HashMap();
        result.put("bks", bksList);
        result.put("stocks", stocks);
        return RequestResult.success(result);
    }
}
