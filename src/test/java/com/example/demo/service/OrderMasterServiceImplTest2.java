package com.example.demo.service;

import com.example.demo.dto.OrderDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by 欣 on 2018/3/25.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderMasterServiceImplTest2 {
    private  final Logger logger= LoggerFactory.getLogger(OrderMasterServiceImplTest.class);


    @Autowired
    private  OrderMasterServiceImpl  orderMasterService;
    @Test
    public void create() throws Exception {
    }

    @Test
    public void findOne() throws Exception {
        OrderDto orderDto = orderMasterService.findOne("15219738565175087513");
        logger.error("orderMasterService"+orderDto.toString());
    }

    @Test
    public void findList() throws Exception {
        PageRequest pageRequest = new PageRequest(0,2);

        Page<OrderDto> list = orderMasterService.findList("123123", pageRequest);
        for (OrderDto order:list) {
            logger.error("findList="+order.toString());
        }

    }

    @Test
    public void cancel() throws Exception {
        OrderDto orderDto = orderMasterService.findOne("15219738565175087513");
       // OrderDto orderDto =new OrderDto();
                logger.error("orderMasterService=findOne="+orderDto.toString());
        OrderDto orderDto1 = orderMasterService.cancel(orderDto);
        logger.error("orderMasterService="+orderDto1.toString());

    }

    @Test
    public void finiSh() throws Exception {
        OrderDto orderDto = orderMasterService.findOne("101");
        logger.error("orderMasterService=findOne="+orderDto.toString());
        OrderDto orderDto1 = orderMasterService.finiSh(orderDto);
        logger.error("orderMasterService="+orderDto1.toString());

    }

    @Test
    public void pay() throws Exception {
        OrderDto orderDto = orderMasterService.findOne("102");
        logger.error("orderMasterService=findOne="+orderDto.toString());
        OrderDto orderDto1 = orderMasterService.pay(orderDto);
        logger.error("orderMasterService="+orderDto1.toString());
    }

    @Test
    public void findList1() throws Exception {
    }

}