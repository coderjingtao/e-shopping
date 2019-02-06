package com.eshop.controller.frontend;

import com.eshop.common.Constant;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponse;
import com.eshop.pojo.User;
import com.eshop.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Description: Cart Controller for the front-end user
 * Created by Jingtao Liu on 4/02/2019.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     *  Get all the product list from current user's cart
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.list(user.getId());
    }

    /**
     *  Add one product and its purchase quantity to the current user's cart
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpSession session, Integer productId,Integer count ){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.add(user.getId(),productId,count);
    }

    /**
     *  Update the purchase quantity of one product in the current user's cart
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session, Integer productId,Integer count ){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.update(user.getId(),productId,count);
    }

    /**
     *  Remove products by their ids from the current user's cart
     *  Multiple products' id is jointed by comma, such as productIds=1,2,3,4
     */
    @RequestMapping("remove_products.do")
    @ResponseBody
    public ServerResponse remove(HttpSession session, String productIds ){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.removeProductFromCart(user.getId(),productIds);
    }

    /**
     *  Select all products in the current user's cart
     *  when pass null to cart service, it means all products in cart need to be ticked.
     */
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse selectAll(HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.selectOrUnselect(user.getId(),null,Constant.Cart.TICK);
    }

    /**
     *  Unselect all products in the current user's cart
     *  when pass null to cart service, it means all products in cart need to be unticked.
     */
    @RequestMapping("unselect_all.do")
    @ResponseBody
    public ServerResponse unselectAll(HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.selectOrUnselect(user.getId(),null,Constant.Cart.UNTICK);
    }

    /**
     *  Select one product by its id in the current user's cart
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse select(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.selectOrUnselect(user.getId(),productId,Constant.Cart.TICK);
    }

    /**
     *  Unselect one product by its id in the current user's cart
     */
    @RequestMapping("unselect.do")
    @ResponseBody
    public ServerResponse unselect(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        return iCartService.selectOrUnselect(user.getId(),productId,Constant.Cart.UNTICK);
    }

    /**
     * Get the total quantity of products in current user's cart.
     * such as in cart, product A : 2, product B : 3, it will return 5
     */
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse getProductCountInCart(HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createBySuccessData(0);// when user hasn't logged, the cart display 0.
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
