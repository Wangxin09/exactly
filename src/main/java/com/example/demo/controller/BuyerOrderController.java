package com.example.demo.controller;

import com.example.demo.service.OrderMasterService;
import com.example.demo.service.ProductCategoryService;
import com.example.demo.service.ProductInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 欣 on 2018/3/26.
 */
@RestController
@RequestMapping("/buyer/order")
public class BuyerOrderController {

    private final Logger logger= LoggerFactory.getLogger(BuyerOrderController.class);

    @Autowired
    private ProductInfoService productInfoService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private OrderMasterService orderMasterService;

    //创建订单
    @RequestMapping("/create")
    public void create(){

    }
    //订单详情
    @RequestMapping("/findOne")
    public void findOne(){

    }
    //订单列表
    @RequestMapping("/findList")
    public void findList(){

    }
    //取消订单
    @RequestMapping("/cancel")
    public void cancel(){

    }


}
