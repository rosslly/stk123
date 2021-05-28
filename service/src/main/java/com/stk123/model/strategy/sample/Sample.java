package com.stk123.model.strategy.sample;

import com.stk123.common.util.CacheUtils;
import com.stk123.model.core.Bar;
import com.stk123.model.core.BarSeries;
import com.stk123.model.core.Stock;
import com.stk123.model.strategy.Filter;
import com.stk123.model.strategy.Strategy;
import com.stk123.model.strategy.StrategyResult;
import com.stk123.model.strategy.result.FilterResult;

import java.util.List;
import java.util.stream.Collectors;

public class Sample {

    // ignore: 02a 选出来的标的太多，由02b替换
    public static String STRATEGIES = "01a,01b,01d,02b,03a,03b,04a,04b,04c,05a,05b,06a,06b,08a,08b,08c,09a,10a";

    public static String STRATEGIES_FOR_ALL_STOCKS = "01a,01b,01c,05b";


    /**** 阳线放量 阴线缩量 *****/
    //2天放量3天缩量
    public static Strategy strategy_01a() {
        Strategy<BarSeries> strategy = new Strategy<>("strategy_01a","策略603096新经典20201106，一段跌幅后底部放量(01a)", BarSeries.class);
        strategy.addFilter("过去3天到80天的跌幅", BarSeries::getFirst, Filters.filter_001b(3,80,-50,-30));
        strategy.addFilter("底部2天放量3天缩量", Filters.filter_002());
        strategy.addFilter("今日十字星", BarSeries::getFirst, Filters.filter_003(0.45));
        strategy.setExpectFilter("10日内涨幅>12%", Filters.expectFilter(10, 12));
        return strategy;
    }
    //4天放量1天缩量 000519 20210421
    public static Strategy strategy_01b() {
        Strategy<Stock> strategy = new Strategy<>("strategy_01b","4天放量1天缩量(01b)", Stock.class);
        strategy.addFilter("过去3天到80天的跌幅", Stock::getBar, Filters.filter_001b(3,60,-50,-10));
        strategy.addFilter("n天放量1天缩量", Filters.filter_015a(20,4,0.20));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }
    //阳线放量 阴线缩量
    public static Strategy strategy_01(String code, int topN) {
        Strategy<Stock> strategy = new Strategy<>("strategy_"+code,"阳线放量阴线缩量("+code+")", Stock.class);
        strategy.setSortable(topN).setAsc(false);
        strategy.setPostExecutor(strgy -> {
            List<StrategyResult> srs = strgy.getStrategyResults();
            List<StrategyResult> list = srs.stream().filter(strategyResult -> strategyResult.isFilterAllPassed()).collect(Collectors.toList());
            list.forEach(strategyResult -> {
                CacheUtils.put(CacheUtils.KEY_50_HOURS, "strategy_01_"+strategyResult.getCode(), strategyResult.getCode());
            });
        });
        //strategy.addFilter("过去3天到80天的跌幅", Stock::getBar, Filters.filter_001b(1,60,-30,-10));
        //strategy.addFilter("箱体上沿整荡整理", Filters.filter_016a(30, 0.3, 0.4, 0.9, 10, 8));
        strategy.addFilter("50小时没有出现在topN里", (strgy, stock) -> {
            return CacheUtils.get(CacheUtils.KEY_50_HOURS, "strategy_01_"+stock.getCode()) == null ? FilterResult.TRUE() : FilterResult.FALSE();
        });
        strategy.addFilter("K线数量", Filters.filter_mustBarSizeGreatThan(120));
        strategy.addFilter("低点到今天的涨幅", Filters.filter_017a(30, 0, 0.25));
        strategy.addFilter("60天换手率大于200%", Filters.filter_mustHSLGreatThan(60, 200));
        strategy.addFilter("阳线放量阴线缩量", Filters.filter_015b(30,60));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }
    public static Strategy strategy_01c() {
        return strategy_01("01c", 20);
    }
    public static Strategy strategy_01d() {
        return strategy_01("01d", 5);
    }


    /**** 一阳吃多阴 ****/
    public static Strategy strategy_02a() {
        Strategy<BarSeries> strategy = new Strategy<>("strategy_02a","策略002044美年健康20201231，底部一阳吃多阴(02a)", BarSeries.class);
        strategy.addFilter("一阳吃5阴或阳", Filters.filter_004(5));
        strategy.addFilter("一阳穿过5,10日均线", BarSeries::getFirst, Filters.filter_005a(5, 10));
        strategy.addFilter("120均线斜率平缓或向上", BarSeries::getFirst, Filters.filter_maSlope(60, 120, -12, 100));
        strategy.addFilter("过去3天到100天的跌幅[-100,-20] or 过去3天到60天内最高点到低点的跌幅[-100,-30]",
                BarSeries::getFirst,
                Filter.<Bar>or(
                    Filters.filter_001a(3,100,-100,-20),
                    Filters.filter_001b(3,60,-100,-20)
                ));
        strategy.setExpectFilter("60日内涨幅>20%", Filters.expectFilter(60, 20));
        return strategy;
    }
    //比strategy_02a多了MACD底背离
    public static Strategy strategy_02b() {
        Strategy<Stock> strategy = new Strategy<>("strategy_02b","策略002044美年健康20201231，底部一阳吃多阴，MACD底背离(02b)", Stock.class);
        strategy.addFilter("股票", Filters.filter_mustStockCate(Stock.EnumCate.STOCK));
        strategy.addFilter("一阳吃5阴或阳", Stock::getBarSeries, Filters.filter_004(5));
        strategy.addFilter("一阳穿过5,10日均线", Stock::getBar, Filters.filter_005a(5, 10));
        strategy.addFilter("过去3天到100天的跌幅[-100,-20] or 过去3天到60天内最高点到低点的跌幅[-100,-30]",
                Stock::getBar,
                Filter.<Bar>or(
                        Filters.filter_001a(3,100,-100,-20),
                        Filters.filter_001b(3,60,-100,-25)
                ));
        strategy.addFilter("MACD和close底背离", Stock::getBar, Filters.filter_006c());
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }
    public static Strategy strategy_02c() {
        Strategy<Stock> strategy = new Strategy<>("strategy_02c","策略002044美年健康20201231，底部一阳吃多阴，MACD底背离(02c)", Stock.class);
        strategy.addFilter("行业", Filters.filter_mustStockCate(Stock.EnumCate.INDEX_eastmoney_gn));
        strategy.addFilter("一阳吃5阴或阳", Stock::getBarSeries, Filters.filter_004(3));
        strategy.addFilter("过去3天到100天的跌幅[-100,-20] or 过去3天到60天内最高点到低点的跌幅[-100,-30]",
                Stock::getBar,
                Filter.<Bar>or(
                        Filters.filter_001a(3,100,-100,-20),
                        Filters.filter_001b(3,60,-100,-25)
                ));
        strategy.addFilter("MACD和close或ma(60)底背离", Stock::getBar, Filters.filter_006a(60));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }
    public static Strategy strategy_02d() {
        Strategy<Stock> strategy = new Strategy<>("strategy_02d","策略002044美年健康20201231，底部一阳吃多阴，MACD底背离(02d)", Stock.class);
        strategy.addFilter("股票", Filters.filter_mustStockCate(Stock.EnumCate.STOCK));
        strategy.addFilter("一阳吃5阴或阳", Stock::getBarSeries, Filters.filter_004(5));
        strategy.addFilter("一阳穿过5,10日均线", Stock::getBar, Filters.filter_005a(5, 10));
        strategy.addFilter("过去3天到100天的跌幅[-100,-20] or 过去3天到60天内最高点到低点的跌幅[-100,-30]",
                Stock::getBar,
                Filter.<Bar>or(
                        Filters.filter_001a(3,100,-100,-20),
                        Filters.filter_001b(3,60,-100,-25)
                ));
        strategy.addFilter("MACD和close或ma(60)底背离", Stock::getBar, Filters.filter_006c()); //MACD标准背离
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }


    /**** 均线缠绕 ****/
    //002538 20200703 100天内，放量涨缩量跌，之后均线缠绕突破买入
    public static Strategy strategy_03a() {
        Strategy<Stock> strategy = new Strategy<>("strategy_03a","策略002538司尔特20200703，底部均线缠绕，一阳吃多阴(03a)", Stock.class);
        strategy.addFilter("股票", Filters.filter_mustStockCate(Stock.EnumCate.STOCK));
        strategy.addFilter("一阳吃4阴或阳", Stock::getBarSeries, Filters.filter_004(4));
        strategy.addFilter("一阳穿过5, 10, 20, 30, 60日均线中的任何2根", Stock::getBar, Filters.filter_005b(2, 5, 10, 20, 30, 60));
        strategy.addFilter("均线线缠绕，且前100天内放量涨缩量跌", Filters.filter_007a(100, 13 ));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }
    public static Strategy strategy_03b() {
        Strategy<Stock> strategy = new Strategy<>("strategy_03b","策略002538司尔特20200703，底部均线缠绕，一阳吃多阴(03b)", Stock.class);
        strategy.addFilter("行业", Filters.filter_mustStockCate(Stock.EnumCate.INDEX_eastmoney_gn));
        strategy.addFilter("一阳吃3阴或阳", Stock::getBarSeries, Filters.filter_004(3));
        strategy.addFilter("一阳穿过5, 10, 20, 30, 60日均线中的任何2根", Stock::getBar, Filters.filter_005b(2, 5, 10, 20, 30, 60));
        strategy.addFilter("均线线缠绕，且前100天内放量涨缩量跌", Filters.filter_007b(100, 6 ));
        strategy.setExpectFilter("60日内涨幅>10%", Stock::getBarSeries, Filters.expectFilter(60, 10));
        return strategy;
    }


    /**** 突破趋势线 ****/
    //突破长期趋势线
    public static Strategy strategy_04a() {
        Strategy<BarSeries> strategy = new Strategy<>("strategy_04a","突破长期趋势线(04a)", BarSeries.class);
        strategy.addFilter("突破长期趋势线", Filters.filter_008b(300, 15, 0.10, 0.15));
        strategy.setExpectFilter("250日内涨幅>25%", Filters.expectFilter(250, 25));
        return strategy;
    }
    //突破中期趋势线
    public static Strategy strategy_04b() {
        Strategy<BarSeries> strategy = new Strategy<>("strategy_04b","突破中期趋势线(04b)", BarSeries.class);
        strategy.addFilter("突破中期趋势线", Filters.filter_008b(100, 7, 0.02, 0.13));
        strategy.setExpectFilter("250日内涨幅>25%", Filters.expectFilter(250, 25));
        return strategy;
    }
    //突破短期趋势线
    public static Strategy strategy_04c() {
        Strategy<Stock> strategy = new Strategy<>("strategy_04c","突破短期趋势线(04c)", Stock.class);
        strategy.addFilter("突破短期趋势线", Filters.filter_008c(100, 6, 20, 0.13));
        strategy.setExpectFilter("250日内涨幅>25%",Stock::getBarSeries, Filters.expectFilter(250, 25));
        return strategy;
    }


    /**** 突破底部平台 ****/
    //突破底部平台 300464, 20200618
    public static Strategy strategy_05a() {
        Strategy<Stock> strategy = new Strategy<>("strategy_05a","突破底部平台(05a)", Stock.class);
        strategy.addFilter("过去3天到80天的跌幅", Stock::getBar, Filters.filter_001b(3,60,-50,-20));
        strategy.addFilter("突破底部平台", Filters.filter_009());
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }
    //均线缠绕，突破底部平台 002177 20210323
    public static Strategy strategy_05b() {
        Strategy<Stock> strategy = new Strategy<>("strategy_05b","均线缠绕，高低点收敛，突破底部平台(05b)", Stock.class);
        strategy.addFilter("突破底部平台", Filters.filter_009());
        strategy.addFilter("高低点收敛", Filters.filter_0014a(80, 8));
        strategy.addFilter("均线线缠绕，且前100天内放量涨缩量跌", Filters.filter_007c(80, 10 ));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }


    /**** 站上放量  ****/
    //站上单根巨量 09926, 20201203
    public static Strategy strategy_06a() {
        Strategy<BarSeries> strategy = new Strategy<>("strategy_06a","站上单根巨量(06a)", BarSeries.class);
        strategy.addFilter("站上单根巨量", Filters.filter_010(30,5));
        strategy.setExpectFilter("60日内涨幅>20%", Filters.expectFilter(60, 20));
        return strategy;
    }
    //站上底部一堆放量 00005,20201021
    public static Strategy strategy_06b() {
        Strategy<BarSeries> strategy = new Strategy<>("strategy_06b","站上底部一堆放量(06b)", BarSeries.class);
        strategy.addFilter("过去3天到80天的跌幅", BarSeries::getFirst, Filters.filter_001b(3,60,-50,-20));
        strategy.addFilter("站上底部一堆放量", Filters.filter_011(120,4));
        strategy.setExpectFilter("60日内涨幅>20%", Filters.expectFilter(60, 20));
        return strategy;
    }


    /**** 相似K线 ****/
    public static Strategy strategy_07a() {
        Strategy<Stock> strategy = new Strategy<>("strategy_07a","策略相似K线(07a)", Stock.class);
        Stock stock = Stock.build("002572");
        Bar a = stock.getBarSeries().getBar("20210118");
        strategy.addFilter("相似K线", Filters.filter_0012a(a, 100));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }


    /**** 阶段强势 ****/
    public static Strategy strategy_08a() {
        String turningPoint20 = Sample.getTurningPoint(20);
        return strategy_08("strategy_08a","20日板块阶段强势(08a)，自"+turningPoint20+"以来", turningPoint20);
    }
    public static Strategy strategy_08b() {
        String turningPoint20 = Sample.getTurningPoint(20);
        String turningPoint60 = Sample.getTurningPoint(60);
        if(turningPoint20.equals(turningPoint60)){
            return null;
        }
        return strategy_08("strategy_08b","60日板块阶段强势(08b)，自"+turningPoint60+"以来", turningPoint60);
    }
    public static Strategy strategy_08c() {
        String turningPoint20 = Sample.getTurningPoint(20);
        String turningPoint60 = Sample.getTurningPoint(60);
        String turningPoint120 = Sample.getTurningPoint(120);
        if(turningPoint20.equals(turningPoint120) || turningPoint60.equals(turningPoint120)){
            return null;
        }
        return strategy_08("strategy_08c","120日板块阶段强势(08c)，自"+turningPoint120+"以来", turningPoint120);
    }
    private static Strategy strategy_08(String code, String name, String turningPoint) {
        Strategy<Stock> strategy = new Strategy<>(code, name, Stock.class);
        strategy.setSortable(5).setAsc(false).setCanTestHistory(false);

        strategy.addFilter("行业", Filters.filter_mustStockCate(Stock.EnumCate.INDEX_eastmoney_gn));
        Filter<Stock> filter = (strg, stock) -> {
            Bar bar = stock.getBar();
            Bar k = bar.before(turningPoint);
            return FilterResult.Sortable(bar.getChange(bar.getDaysBetween(bar.getDate(), k.getDate()), Bar.EnumValue.C));
        };
        strategy.addFilter("自"+turningPoint+"以来排行", filter);
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }
    private static String getTurningPoint(int days){
        Stock stock = Stock.build("999999");
        Bar bar = stock.getTurningPoint(days);
        return bar.getDate();
    }


    /**** 跳空缺口 ****/
    public static Strategy strategy_09a() {
        Strategy<Stock> strategy = new Strategy<>("strategy_09a","跳空缺口，前期突破趋势(09a)", Stock.class);
        strategy.addFilter("跳空缺口", Filters.filter_mustGapUp(10));
        strategy.addFilter("突破短期趋势线", Filters.filter_008c(100, 6, 20, 0.13));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }


    /**** V型缩量反转 ****/
    public static Strategy strategy_10a() {
        Strategy<Stock> strategy = new Strategy<>("strategy_10a", "V型缩量反转(10a)", Stock.class);
        strategy.addFilter("过去3天到80天的跌幅", Stock::getBar, Filters.filter_001b(7,60,-50,-25));
        strategy.addFilter("V型缩量反转", Filters.filter_018a(30));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }


    //大跌后，有减持，问询函？
    public static Strategy strategy_0() {
        return null;
    }

    public static Strategy strategy_TEST() {
        Strategy<Stock> strategy = new Strategy<>("strategy_TEST","Strategy TEST", Stock.class);
        strategy.setSortable(5).setAsc(false);

        String turningPoint = Sample.getTurningPoint(60);
        System.out.println("turningPoint=="+turningPoint);

        strategy.addFilter("行业", Filters.filter_mustStockCate(Stock.EnumCate.INDEX_eastmoney_gn));
        Filter<Stock> filter = (strg, stock) -> {
            Bar bar = stock.getBar();
            Bar k = bar.before(turningPoint);
            return FilterResult.Sortable(bar.getChange(bar.getDaysBetween(bar.getDate(), k.getDate()), Bar.EnumValue.C));
        };
        strategy.addFilter("test filter", filter);
        //strategy.addFilter("过去3天到80天的跌幅", BarSeries::getFirst, Filters.filter_001b(3,60,-50,-30));

        //strategy.setExpectFilter("60日内涨幅>20%", Filters.expectFilter(250, 25));
        strategy.setExpectFilter("60日内涨幅>20%", Stock::getBarSeries, Filters.expectFilter(60, 20));
        return strategy;
    }

}
