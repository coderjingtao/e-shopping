package com.eshop.controller.frontend;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.eshop.common.Constant;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponse;
import com.eshop.pojo.User;
import com.eshop.service.IOrderService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Description: Order Controller for the front-end user
 * Created by Jingtao Liu on 6/02/2019.
 */
@Controller
@RequestMapping("/order")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    /**
     * Call the pre-order service of Alipay to generate QR code image of the order
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo,user.getId(),path);
    }

    /**
     * After user finish QR Code Paying, Alipay call back this method to send the payment info to our system.
     * Mode: passive
     * Ref: https://docs.open.alipay.com/194/103296
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        //a. Receive all the parameters from Alipay
        Map<String,String> params = Maps.newHashMap();
        Map<String,String[]> aliReqParams = request.getParameterMap();
        for(Map.Entry<String,String[]> entry : aliReqParams.entrySet()){
            String key = entry.getKey();
            String[] values = entry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i==values.length-1)?valueStr+values[i]:valueStr+values[i]+",";
            }
            params.put(key,valueStr);
        }
        logger.info("Alipay Callback -- sign:{},trade_status:{},all_params:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //b. Validate the callback parameters(remove "sign" and "sign_type")
        //1. to ensure this callback is 100% from Alipay, not a mock one
        //2. to verify this callback is not duplicate to avoid resetting order's trade status
        params.remove("sign_type");// Alipay has removed the key "sign"
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2)
                return ServerResponse.createByErrorMsg("Validation of Alipay Sign Failed.");
        } catch (AlipayApiException e) {
            logger.error("Alipay Callback Exception",e);
        }

        // verify other parameters of the order to ensure it is generated from your system
        ServerResponse response = iOrderService.aliCallback(params);
        if(response.isSuccess())
            return Constant.AlipayCallback.RESPONSE_SUCCESS;
        return Constant.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * After user finish QR Code Paying, Our system will query whether the order is paid.
     * Mode: active
     */
    @RequestMapping("is_order_paid.do")
    @ResponseBody
    public ServerResponse isOrderPaid(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        ServerResponse response = iOrderService.queryIsOrderPaid(user.getId(),orderNo);
        if(response.isSuccess())
            return ServerResponse.createBySuccessData(true);
        return ServerResponse.createBySuccessData(false);
    }

    /**
     * According to the current selected cart items and shipping address, create a order.
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    /**
     * Cancel a order by user based on its order NO.
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    /**
     * Get a order detail info based on its order NO.
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    /**
     * Get a order list with pagination
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }
    /**
     * Get the products of order from cart for filling in order and confirming order pages.
     * 展示未支付的订单中的商品列表，用于从购物车到订单生成之间的订单填写页和订单确认页的订单商品展示
     */
    @RequestMapping("get_order_products.do")
    @ResponseBody
    public ServerResponse getOrderProductsFromCart(HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iOrderService.getOrderProductsFromCart(user.getId());
    }
}
