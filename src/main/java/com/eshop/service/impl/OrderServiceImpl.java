package com.eshop.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.eshop.common.Constant;
import com.eshop.common.ServerResponse;
import com.eshop.dao.*;
import com.eshop.pojo.*;
import com.eshop.service.IOrderService;
import com.eshop.util.BigDecimalUtil;
import com.eshop.util.DateTimeUtil;
import com.eshop.util.FTPUtil;
import com.eshop.util.PropertiesUtil;
import com.eshop.vo.OrderItemVo;
import com.eshop.vo.OrderProductVo;
import com.eshop.vo.OrderVo;
import com.eshop.vo.ShippingVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Description: The Implementation of Order Service, divided into frontend and backend.
 * Created by Jingtao Liu on 6/02/2019.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        Map<String,String> result = Maps.newHashMap();
        Order order  = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if(order == null)
            return ServerResponse.createByErrorMsg("No such order of user");
        result.put("orderNo",order.getOrderNo().toString());

        // Start to pay, copy from Alipay demo

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "Eshop QR Code Pay Test, Order NO.:"+outTradeNo;

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = "Order:"+outTradeNo+", totally cost ￥"+totalAmount;

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItems = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(),order.getUserId());
        for(OrderItem orderItem : orderItems){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(),
                                                        orderItem.getProductName(),
                    BigDecimalUtil.multiply(orderItem.getCurrentUnitPrice().doubleValue(), 100d).longValue(),
                                                        orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        AlipayF2FPrecreateResult tradeResult = tradeService.tradePrecreate(builder);
        switch (tradeResult.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = tradeResult.getResponse();
                dumpResponse(response);

                File qrDirectory = new File(path);
                if(!qrDirectory.exists()){
                    qrDirectory.setWritable(true);
                    qrDirectory.mkdirs();
                }
                // 需要修改为运行机器上的路径
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());
                logger.info("qrPath:" + qrPath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File ftpFile = new File(path,qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(ftpFile));
                } catch (IOException e) {
                    logger.error("Upload QR Code Image to FTP Server Failed",e);
                }
                String ftpQRImageUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + ftpFile.getName();
                result.put("qrUrl",ftpQRImageUrl);
                return ServerResponse.createBySuccessData(result);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMsg("Alipay PreOrder Failed.");
            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMsg("Unknown Alipay PreOrder Status.");
            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMsg("Unsupported Alipay Trade Status.");
        }
        // End to pay

    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    @Override
    public ServerResponse aliCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null)
            return ServerResponse.createByErrorMsg("This order is not generated by eshop. Ignore it.");
        if(order.getStatus() >= Constant.OrderStatus.PAID.getCode())
            return ServerResponse.createBySuccessMsg("Alipay sends duplicate callback.");
        if(Constant.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Constant.OrderStatus.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Constant.PayPlatform.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse queryIsOrderPaid(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if(order == null)
            return ServerResponse.createByErrorMsg("Cannot find this order.");
        if(order.getStatus() >= Constant.OrderStatus.PAID.getCode())
            return ServerResponse.createBySuccess();
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        List<Cart> selectedCartItems = cartMapper.selectCheckedCartItemByUserId(userId);
        //1.transform Cart Items into Order Items
        ServerResponse response = transformCartItemIntoOrderItem(userId,selectedCartItems);
        if(!response.isSuccess())
            return response;
        List<OrderItem> orderItems = (List<OrderItem>)response.getData();
        BigDecimal orderTotalPrice = computeOrderTotalPrice(orderItems);
        //2.generate a main order in database
        Order order = assembleOrder(userId,shippingId,orderTotalPrice);
        if(order == null)
            return ServerResponse.createByErrorMsg("Create Order Failed.");
        if(CollectionUtils.isEmpty(orderItems))
            return ServerResponse.createByErrorMsg("No item is selected in cart.");
        for(OrderItem orderItem: orderItems)
            orderItem.setOrderNo(order.getOrderNo());
        //3.generate a batch of order items in database
        orderItemMapper.batchInsert(orderItems);
        //4.reduce stock of products in the order
        reduceStock(orderItems);
        //5.clear up selectedCartItems in user's cart
        cleanSelectedCartItems(selectedCartItems);
        //6.assemble and return Order View Objects to display on website for front-end users
        OrderVo orderVo = assembleOrderVo(order,orderItems);

        return ServerResponse.createBySuccessData(orderVo);
    }

    /**
     * It's necessary to transform Cart Item into Order Item
     * as the product's price or status have been changed compared with its original when it was put into cart.
     */
    private  ServerResponse transformCartItemIntoOrderItem(Integer userId, List<Cart> cartItems){
        if(CollectionUtils.isEmpty(cartItems))
            return ServerResponse.createByErrorMsg("No item is selected in cart.");

        List<OrderItem> orderItems = Lists.newArrayList();
        for(Cart cartItem: cartItems){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if(Constant.ProductStatusEnum.ON_SALE.getCode() != product.getStatus())
                return ServerResponse.createByErrorMsg(product.getName()+" is off-shelf yet.");
            if(cartItem.getQuantity() > product.getStock())
                return ServerResponse.createByErrorMsg(product.getName()+" is insufficient to buy.");
            //begin to transform
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItems.add(orderItem);
        }
        return  ServerResponse.createBySuccessData(orderItems);
    }

    private BigDecimal computeOrderTotalPrice(List<OrderItem> orderItems){
        BigDecimal total = new BigDecimal("0");
        for(OrderItem orderItem : orderItems)
            total = BigDecimalUtil.add(total.doubleValue(),orderItem.getTotalPrice().doubleValue());
        return total;
    }

    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal totalPrice){
        Order order = new Order();
        long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(Constant.OrderStatus.UNPAID.getCode());
        order.setPostage(0);
        order.setPaymentType(Constant.PaymentType.ONLINE_PAY.getCode());
        order.setPayment(totalPrice);
        order.setShippingId(shippingId);
        int rowCount = orderMapper.insert(order);
        if(rowCount <= 0)
            return null;
        return order;
    }

    /**
     * A simple way to generate a order NO., which needs to be improved for distributed system.
     */
    private long generateOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    /**
     * Update the stock of products in a order which is created by user
     */
    private void reduceStock(List<OrderItem> orderItems){
        for (OrderItem orderItem: orderItems) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * Clean up the selected cart items in a order which is created by user
     */
    private void cleanSelectedCartItems(List<Cart> selectedCartItems){
        for(Cart cartItem : selectedCartItems)
            cartMapper.deleteByPrimaryKey(cartItem.getId());
    }

    /**
     * Assemble a Order View Object to display for front-end users
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItems){
        OrderVo orderVo =  new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPostage(order.getPostage());

        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Constant.PaymentType.codeOf(order.getPaymentType()).getValue());

        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Constant.OrderStatus.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem: orderItems){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }

        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }
    /**
     * Assemble a Shipping View Object to display for front-end users
     */
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;
    }

    /**
     * Assemble a Order Item View Object to display for front-end users
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    @Override
    public ServerResponse<String> cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if(order == null)
            return ServerResponse.createByErrorMsg("Cannot find this order.");
        if(order.getStatus() == Constant.OrderStatus.CANCELED.getCode())
            return ServerResponse.createByErrorMsg("This order was canceled before.");
        if(order.getStatus() != Constant.OrderStatus.UNPAID.getCode())
            return ServerResponse.createByErrorMsg("This order is paid yet.");
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Constant.OrderStatus.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(rowCount <= 0)
            return ServerResponse.createByError();
        return ServerResponse.createBySuccess();
    }

    /**
     * Get the products of order from cart for filling in order page and confirming order page.
     * 展示未支付的订单中的商品列表，用于从购物车到订单生成之间的订单填写页和订单确认页的订单商品展示
     */
    @Override
    public ServerResponse getOrderProductsFromCart(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        List<Cart> selectedCartItems = cartMapper.selectCheckedCartItemByUserId(userId);
        ServerResponse response = this.transformCartItemIntoOrderItem(userId,selectedCartItems);
        if(!response.isSuccess())
            return response;
        List<OrderItem> orderItems = (List<OrderItem>)response.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal totalPrice = new BigDecimal("0");
        for(OrderItem orderItem: orderItems){
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(totalPrice);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccessData(orderProductVo);
    }

    @Override
    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if(order != null){
            List<OrderItem> orderItems = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
            OrderVo orderVo = assembleOrderVo(order,orderItems);
            return ServerResponse.createBySuccessData(orderVo);
        }
        return ServerResponse.createByErrorMsg("Cannot find this order.");
    }

    @Override
    public ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
        PageInfo page = new PageInfo<>(orderList);
        page.setList(orderVoList);
        return ServerResponse.createBySuccessData(page);
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();

        for(Order order : orderList){
            List<OrderItem> orderItems = Lists.newArrayList();
            if(userId == null){ // admin
                orderItems = orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else{ // user
                orderItems = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItems);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    //---------------For Administrator ------------------------

    @Override
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,null);
        PageInfo pageResult = new PageInfo<>(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccessData(pageResult);
    }

    @Override
    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null)
            return ServerResponse.createByErrorMsg("Cannot find this order.");
        List<OrderItem> orderItems = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItems);
        return ServerResponse.createBySuccessData(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null)
            return ServerResponse.createByErrorMsg("Cannot find this order.");
        List<OrderItem> orderItems = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItems);
        PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
        pageResult.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccessData(pageResult);
    }

    @Override
    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null)
            return ServerResponse.createByErrorMsg("Cannot find this order.");
        if(order.getStatus() != Constant.OrderStatus.PAID.getCode())
            return ServerResponse.createByErrorMsg("The order is unpaid.");

        order.setStatus(Constant.OrderStatus.SHIPPED.getCode());
        order.setSendTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
        return ServerResponse.createBySuccessMsg("Goods is sending out successfully.");
    }
}
