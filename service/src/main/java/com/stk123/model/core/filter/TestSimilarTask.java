package com.stk123.model.core.filter;

import com.stk123.model.core.Bar;
import com.stk123.model.core.BarSeries;
import com.stk123.model.core.Stock;
import com.stk123.model.core.filter.result.FilterResult;
import com.stk123.model.core.filter.result.FilterResultBetween;
import com.stk123.model.core.filter.result.FilterResultEquals;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestSimilarTask {

    public void run(List<Stock> stocks) {
        for (Stock stock : stocks) {
            //ResultSet resultSet = stock.getBarSeries().similar(example1());
            ResultSet resultSet = stock.similar(example1());
            System.out.println("code:"+stock.getCode()+","+resultSet);
            resultSet = stock.similar(example2());
            System.out.println("code:"+stock.getCode()+","+resultSet);
        }
    }

    /**
     * 可以指定 回测的开始结束日期
     * @param stocks
     * @param startDate
     * @param endDate
     */
    public void run(List<Stock> stocks, String startDate, String endDate) {
        for (Stock stock : stocks) {
            System.out.println("code:"+stock.getCode()+"...................................start");
            List<ResultSet> resultSets = stock.similar(example1(), startDate, endDate);
            System.out.println("example:example1..............");
            resultSets.forEach(resultSet -> System.out.println(resultSet));

            resultSets = stock.similar(example2(), startDate, endDate);
            System.out.println("example:example2..............");
            resultSets.forEach(resultSet -> System.out.println(resultSet));
            System.out.println("code:"+stock.getCode()+"...................................end");
        }
    }

    public Example<BarSeries> example1() {
        Example<BarSeries> example = new Example("Example 603096", BarSeries.class);
        Filter<Bar> filter1 = (bar) -> {
            Bar today = bar;
            Bar today4 = today.before(4);
            double change = today4.getChange(80, Bar.EnumValue.C);
            return new FilterResultEquals(change*100,-35.0, 5.0).addResult(today.getDate());
        };
        example.addFilter("filter1111",(bs)->bs.getFirst(), filter1);
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

    public Example<Stock> example2() {
        Example<Stock> example = new Example("Example 222222", Stock.class);
        Filter<Stock> filter1 = (stock) -> {
            String code = stock.getCode();
            if(StringUtils.startsWith(code,"300")){
                return FilterResult.TRUE(stock.getCode());
            }
            return FilterResult.FALSE(stock.getCode());
        };
        example.addFilter(filter1);
        return example;
    }

}
