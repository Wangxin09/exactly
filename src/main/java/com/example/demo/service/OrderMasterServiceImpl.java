package com.example.demo.service;

import com.example.demo.converter.OrderDtoToOrderMaster;
import com.example.demo.converter.OrderMasterToOrderDto;
import com.example.demo.dataobject.OrderDetail;
import com.example.demo.dataobject.OrderMaster;
import com.example.demo.dataobject.ProductInfo;
import com.example.demo.dto.CartDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.enums.OrderStatusEnum;
import com.example.demo.enums.PayStatusEnum;
import com.example.demo.enums.ResultEnum;
import com.example.demo.exception.DemoException;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderMasterRepository;
import com.example.demo.utils.KeyUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 欣 on 2018/3/25.
 */
@Service
public class OrderMasterServiceImpl implements OrderMasterService {

     private  final org.slf4j.Logger logger= LoggerFactory.getLogger(OrderMasterServiceImpl.class);

    @Autowired
    private  ProductInfoService productInfoService;
    @Autowired
    private OrderMasterRepository orderMasterRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    //入参为OrderDto但是查找为OrderMaster对象
    //创建订单
    @Override
    @Transactional  //添加事务出错回滚
    public OrderDto create(OrderDto orderDto) {
        BigDecimal orderAmount=new BigDecimal(0);
        //BigDecimal orderAmount=0;
        String orderId= KeyUtil.getKey();

        List<CartDto> cartDtoList = new ArrayList<CartDto>();
        // 1 首先从数据库查询商品（价格，数量）
        for (OrderDetail orderDetail:orderDto.getOrderDetailList()){
            //根据商品ID找到商品
            ProductInfo productInfo = productInfoService.findOne(orderDetail.getProductId());
            if(productInfo==null){
                throw new DemoException(ResultEnum.PRODUCT_NOT_EXIST);
            }
            // 2 计算订单总价
            orderAmount= productInfo.getProductPrice().
                    multiply(new BigDecimal(orderDetail.getProductQuantity()))
                    .add(orderAmount);
            // 3 写入订单数据库（  OrderDetail）
            //商品详情ID
            orderDetail.setDetailId(KeyUtil.getKey());
            //订单ID--->创建订单是生成
            orderDetail.setOrderId(orderId);
            // orderDetail.setProductIcon(productInfo.getProductIcon());
            // productInfo属性拷贝到orderDetail
            BeanUtils.copyProperties(productInfo,orderDetail);
            orderDetailRepository.save(orderDetail);
            CartDto cartDto = new CartDto(orderDetail.getProductId(), orderDetail.getProductQuantity());
            cartDtoList.add(cartDto);
        }
        // 3 写入订单数据库（OrderMaster  和  OrderDetail）
        OrderMaster orderMaster = new OrderMaster();
        BeanUtils.copyProperties(orderDto,orderMaster);
        orderMaster.setOrderId(orderId);
        orderMaster.setOrderAmount(orderAmount);
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());
        //把前段传过来的客户订单信息拷贝到订单表中
        //写入数据库
        orderMasterRepository.save(orderMaster);

        // 4 去库存
        //判断库存数量是否足够

        productInfoService.decreateStock(cartDtoList);
            /*Integer productStock = productInfo.getProductStock();
            if(productStock<=orderDetail.getProductQuantity()){
            }*/
        return orderDto;
    }
    //查找单个订单详情
    @Override
    @Transactional
    public OrderDto findOne(String orderId) {
        OrderMaster orderMaster = orderMasterRepository.findOne(orderId);
        if(orderMaster==null){
            throw  new DemoException(ResultEnum.ORDER_NOT_EXIST);
        }
        //根据订单ID查找订单详情
        List<OrderDetail> orderDetailList = orderDetailRepository.findByOrderId(orderId);
        if(orderDetailList==null){
            throw  new DemoException(ResultEnum.ORDERDETA_NOT_EXIST);
        }
        OrderDto orderDto = new OrderDto();
        //orderDto对象有基本属性以及集合
        //对象的copy
        BeanUtils.copyProperties(orderMaster,orderDto);
        //orderDetailList单个属性
        orderDto.setOrderDetailList(orderDetailList);
        return orderDto;
    }
    //订单表分页
    @Override
    @Transactional
    public Page<OrderDto> findList(String buyerOpenid, Pageable pageable) {
        Page<OrderMaster> orderMasterPage = orderMasterRepository.findByBuyerOpenid(buyerOpenid, pageable);
        if(orderMasterPage==null){
            throw  new DemoException(ResultEnum.ORDERDETA_NOT_EXIST);
        }
        //注意：orderMasterPage.getContent()
        List<OrderDto> orderDtoList = OrderMasterToOrderDto.convet(orderMasterPage.getContent());
        //创建一个返回的对象
        Page<OrderDto> OrderDtoList=new PageImpl<OrderDto>(orderDtoList,pageable,orderMasterPage.getTotalElements());

        return OrderDtoList;
    }
    //取消订单
    @Override
    @Transactional
    public OrderDto cancel(OrderDto orderDto) {

        //判断订单的状态--->新下单的才可以取消
        if(!orderDto.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())){//新订单未支付
            logger.error("取消订单 订单状态不正确,orderId={},orderStatus={}",orderDto.getOrderId(),orderDto.getOrderStatus() );
            throw new DemoException(ResultEnum.ORDER_STATUS_ERROR);
        }
        //修改订单状态--->先set值，然后再copy
        orderDto.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
        OrderMaster orderMaster = OrderDtoToOrderMaster.convet(orderDto);
        OrderMaster master = orderMasterRepository.save(orderMaster);
        if (master==null){
            logger.error("取消订单 更新失败,master={}",master );
            throw new DemoException(ResultEnum.ORDER_UPDATE_ERROR);
        }

        //返回库存

        //先判断订单是否有商品
        List<OrderDetail> orderDetailList = orderDto.getOrderDetailList();
        if(orderDetailList==null){
            logger.error("取消订单 订单中午商品详情,orderDto={}",orderDto );
            throw new DemoException(ResultEnum.ORDER_NOT_DETAIL);
        }
        List<CartDto> cartDtoList = new ArrayList<CartDto>();

        for (OrderDetail orderDetail:orderDto.getOrderDetailList()){
            //根据商品ID找到商品
            ProductInfo productInfo = productInfoService.findOne(orderDetail.getProductId());
            if(productInfo==null){
                throw new DemoException(ResultEnum.PRODUCT_NOT_EXIST);
            }
            CartDto cartDto = new CartDto(orderDetail.getProductId(), orderDetail.getProductQuantity());
            cartDtoList.add(cartDto);
        }
        //更改订单状态
        orderMaster.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
        //下订单还没有支付所以不用改支付状态orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());

        //写入数据库
       // orderMasterRepository.save(orderMaster);

        //加库存
        productInfoService.increaseStock(cartDtoList);
        //如果以支付，退款
        if(orderDto.getPayStatus().equals(PayStatusEnum.SUCCESS.getCode())){
            //TODO
        }
        return orderDto;
    }
    //订单完结
    @Override
    @Transactional
    public OrderDto finiSh(OrderDto orderDto) {
        //判断订单状态
        if(!orderDto.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())){//新订单未支付
            logger.error("完结订单 订单状态不正确,orderId={},orderStatus={}",orderDto.getOrderId(),orderDto.getOrderStatus() );
            throw new DemoException(ResultEnum.ORDER_STATUS_ERROR);
        }
        //修改状态
        OrderMaster orderMaster = new OrderMaster();
        orderDto.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
        BeanUtils.copyProperties(orderDto,orderMaster);
        OrderMaster master = orderMasterRepository.save(orderMaster);
        if (master==null){
            logger.error("完结订单 更新失败,master={}",master );
            throw new DemoException(ResultEnum.ORDER_UPDATE_ERROR);
        }

        return orderDto;
    }
    //支付订单
    @Override
    @Transactional
    public OrderDto pay(OrderDto orderDto) {
        //判断订单状态
        if(!orderDto.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())){
            logger.error("支付订单 订单状态不正确,orderId={},orderStatus={}",orderDto.getOrderId(),orderDto.getOrderStatus() );
            throw new DemoException(ResultEnum.ORDER_STATUS_ERROR);
        }

        //判断支付状态
        if(!orderDto.getPayStatus().equals(PayStatusEnum.WAIT.getCode())){
            logger.error("支付订单 支付状态不正确,orderId={},payStatus={}",orderDto.getOrderId(),orderDto.getPayStatus() );
            throw new DemoException(ResultEnum.PAY_STATUS_ERROR);
        }

        //修改支付状态
        OrderMaster orderMaster = new OrderMaster();
        //orderDto.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
        orderDto.setPayStatus(PayStatusEnum.SUCCESS.getCode());
        BeanUtils.copyProperties(orderDto,orderMaster);
        OrderMaster master = orderMasterRepository.save(orderMaster);
        if (master==null){
            logger.error("支付订单 更新失败,master={}",master );
            throw new DemoException(ResultEnum.ORDER_UPDATE_ERROR);
        }
        return orderDto;
    }

    @Override
    public Page<OrderDto> findList(Pageable pageable) {
        return null;
    }
}
