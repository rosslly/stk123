package com.stk123.model.strategy;

import com.stk123.model.core.Bar;
import com.stk123.model.core.BarSeries;
import com.stk123.model.core.Stock;
import com.stk123.model.strategy.result.FilterResult;
import com.stk123.model.strategy.result.FilterResultBetween;
import com.stk123.model.strategy.result.FilterResultEquals;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StrategyBacktesting {

    @Getter
    private List<Strategy<?>> strategies = new ArrayList<>();

    public StrategyBacktesting(){
    }

    public void addStrategy(Strategy<?> strategy){
        this.strategies.add(strategy);
    }

    public StrategyResult test(Strategy strategy, Stock stock) {
        if(strategy.getXClass().isAssignableFrom(Stock.class)) {
            return strategy.test(stock);
        }else if(strategy.getXClass().isAssignableFrom(BarSeries.class)){
            return strategy.test(stock.getBarSeries());
        }else if(strategy.getXClass().isAssignableFrom(Bar.class)){
            return strategy.test(stock.getBarSeries().getFirst());
        }else {
            throw new RuntimeException("Not support X generic class: "+strategy.getXClass());
        }
//        throw new RuntimeException("Strategy list is empty.");
    }

    public List<StrategyResult> test(Strategy strategy, Stock stock, String startDate, String endDate) {
        BarSeries bs = stock.getBarSeries();
        Bar endBar = bs.getFirst().before(endDate);
        Bar first = bs.setFirstBarFrom(startDate);
        List<StrategyResult> results = new ArrayList<>();
        if(first != null) {
            Bar bar = first;
            do {
                StrategyResult resultSet = this.test(strategy, stock);
                results.add(resultSet);
                bar = bar.after();
                if (bar == null) break;
                bs.setFirstBarFrom(bar.getDate());
            } while (bar.dateBeforeOrEquals(endBar));
        }
        return results;
    }

    public List<StrategyResult> test(List<Stock> stocks) {
        List<StrategyResult> strategyResults = new ArrayList<>();
        for (Stock stock : stocks) {
            for(Strategy strategy : strategies) {
                StrategyResult resultSet = this.test(strategy, stock);
                System.out.println("code:" + stock.getCode() + "," + resultSet);
                strategyResults.add(resultSet);
            }
        }
        return strategyResults;
    }

    /**
     * 可以指定 回测的开始结束日期
     * @param stocks
     * @param startDate
     * @param endDate
     */
    public void test(List<Stock> stocks, String startDate, String endDate) {
        for (Stock stock : stocks) {
            System.out.println("code:"+stock.getCode()+"...................................start");
            for(Strategy strategy : strategies) {
                List<StrategyResult> resultSets = this.test(strategy, stock, startDate, endDate);
                resultSets.forEach(resultSet -> System.out.println(resultSet));
                //int n = resultSets.stream().map(StrategyResult::getCountOfExecutedFilter).reduce(0, (a, b) -> a + b);
                //n = resultSets.stream().mapToInt(StrategyResult::getCountOfExecutedFilter).sum();
                //System.out.println(n);
            }
            System.out.println("code:"+stock.getCode()+"...................................end");
        }
    }

    public void print() {
        for(Strategy strategy : this.getStrategies()){
            System.out.println(strategy);
        }
    }

    public Strategy<BarSeries> example1() {
        Strategy<BarSeries> example = new Strategy<>("Strategy 603096", BarSeries.class);

        Filter<Bar> filter1 = (bar) -> {
            Bar today = bar;
            Bar today4 = today.before(4);
            double change = today4.getChange(80, Bar.EnumValue.C);
            return new FilterResultEquals(change*100,-35.0, 5.0).addResult(today.getDate());
        };
        example.addFilter("filter1111",BarSeries::getFirst, filter1);

        Filter<BarSeries> filter2 = (bs) -> {
            Bar today = bs.getFirst();
            Bar today4 = today.before(4);
            double today4Volume = today4.getVolume();
            if(today4.getClose() < today4.getLastClose()){
                return FilterResult.FALSE(today.getDate());
            }
            double minVolume = today4.getLowest(10, Bar.EnumValue.V);
            return new FilterResultBetween(today4Volume/minVolume,7, 10).addResult(today.getDate());
        };
        example.addFilter("filter222",filter2);

        example.setExpectFilter((bs) -> {
            Bar today = bs.getFirst();
            Bar tomorrwo10 = today.after(10);
            double change = tomorrwo10.getChange(10, Bar.EnumValue.C);
            return new FilterResultBetween(change*100, 15, 100);
        });
        return example;
    }

    public Strategy<Stock> example2() {
        Strategy<Stock> example = new Strategy<>("Example 222222", Stock.class);
        Filter<Stock> filter1 = (stock) -> {
            String code = stock.getCode();
            if(StringUtils.startsWith(code,"300")){
                return FilterResult.TRUE(stock.getCode());
            }
            return FilterResult.FALSE(stock.getCode());
        };
        example.addFilter("filter 300开始",filter1);
        return example;
    }

}
