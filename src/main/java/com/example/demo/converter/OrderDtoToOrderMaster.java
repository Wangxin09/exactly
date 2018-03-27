package com.example.demo.converter;

import com.example.demo.dataobject.OrderMaster;
import com.example.demo.dto.OrderDto;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 欣 on 2018/3/26.
 */
public class OrderDtoToOrderMaster {
    public  static OrderMaster convet(OrderDto orderDto){

        OrderMaster orderMaster = new OrderMaster();
        BeanUtils.copyProperties(orderDto,orderMaster);
        return orderMaster;
    }

    public  static List<OrderMaster> convet(List<OrderDto> orderDtoList){
        ArrayList<OrderMaster> dtoArrayList = new ArrayList<>();

        for (int i=0;i<orderDtoList.size();i++){
            OrderMaster orderMaster = new OrderMaster();
            BeanUtils.copyProperties(orderDtoList.get(i),orderMaster);
            dtoArrayList.add(orderMaster);
        }
        //orderMasterList.add(orderDto);
        return dtoArrayList;
    }
}
