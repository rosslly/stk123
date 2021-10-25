package com.stk123.app.web;

import com.stk123.entity.StkPeEntity;
import com.stk123.model.core.Stock;
import com.stk123.model.projection.StockProjection;
import com.stk123.repository.StkPeRepository;
import com.stk123.repository.StkRepository;
import com.stk123.service.core.StockService;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = {"/s","/S"})
@CommonsLog
public class StockWebController {

    @Autowired
    private StkRepository stkRepository;
    @Autowired
    private StockService stockService;
    @Autowired
    private StkPeRepository stkPeRepository;


    @RequestMapping(value = {"/{code}", "/"})
    public String stock(@PathVariable(value = "code", required = false) String code,
                        @RequestParam(value="title", required = false) String title,
                        Model model){
        if(StringUtils.isEmpty(code)){
            throw new RuntimeException("Stock code is empty!");
        }
        model.addAttribute("title", title);
        model.addAttribute("code", code);
        model.addAttribute("codeType", "stock");
        if(StringUtils.contains(code, ",")) {
            return "stocks";
        }
        StockProjection stockProjection = stkRepository.getByCode(code);
        if(stockProjection == null){
            throw new RuntimeException("Stock code is not found in database : " + code);
        }
        Stock stock = Stock.build(stockProjection);
        model.addAttribute("title", stock.getNameAndCode());
        model.addAttribute("name", stock.getName());
        model.addAttribute("placeCode", stock.getPlace().getPlace());
        model.addAttribute("placeName", stock.getPlace().name());
        model.addAttribute("codeWithPlace", stock.getCodeWithPlace());
        return "stock";
    }

    @RequestMapping("/mystocks")
    public String mystocks(Model model){
        StkPeEntity stkPeEntity = stkPeRepository.findTopByOrderByReportDateDesc();
        System.out.println(stkPeEntity);
        String code = stkPeEntity.getReportText();
        model.addAttribute("title", "自选股");
        model.addAttribute("code", code);
        model.addAttribute("codeType", "stock");
        return "stocks";
    }
}
