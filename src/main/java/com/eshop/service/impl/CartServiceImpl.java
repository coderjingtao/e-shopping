package com.eshop.service.impl;

import com.eshop.common.Constant;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponse;
import com.eshop.dao.CartMapper;
import com.eshop.dao.ProductMapper;
import com.eshop.pojo.Cart;
import com.eshop.pojo.Product;
import com.eshop.service.ICartService;
import com.eshop.util.BigDecimalUtil;
import com.eshop.util.PropertiesUtil;
import com.eshop.vo.CartItemVo;
import com.eshop.vo.CartVo;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description: The implementation of Cart Service
 * Created by Jingtao Liu on 4/02/2019.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加某个商品到购物车item后，再根据用户id，把该用户的购物车再全部刷新一遍
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null)
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        //根据用户和商品id，查看是否在购物车中已经存在该商品，作为一条 cart item.
        Cart cartItem = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        // user's cart has no this product, need to create a cart
        if(cartItem == null){
            Cart newCartItem = new Cart();
            newCartItem.setUserId(userId);
            newCartItem.setProductId(productId);
            //这里并没有对添加到购物车中商品的数量和商品的实际库存进行校验，只是单纯的做了累加的操作，并新增到了该用户的购物车数据库中
            //但这时cart item中的商品数量是临时的，还需要后续在getCartVoLimit()方法中对数量进行校验和纠正
            newCartItem.setQuantity(count);
            newCartItem.setChecked(Constant.Cart.TICK);
            cartMapper.insert(newCartItem);
        }
        else{// user's cart has this product, need to update its quantity

            //这里并没有对添加到购物车中商品的数量和商品的实际库存进行校验，只是单纯的做了累加的操作，并更新到了该用户的购物车数据库中
            //但这时cart item中的商品数量是临时的，还需要后续在getCartVoLimit()方法中对数量进行校验和纠正
            cartItem.setQuantity(cartItem.getQuantity() + count);
            cartMapper.updateByPrimaryKeySelective(cartItem);
        }
        return list(userId);
    }

    /**
     * 为了在前台展示购物车，该方法对购物车的数据进行组装加工。并对购物车中每一个条目商品，进行库存数量校验
     * 等于根据用户id，把当前用户的购物车再重新刷新一遍数据
     * CartVo ： 购物车整体类
     * CartItemVo：购物车条目类
     */
    private CartVo refreshUserCartByUserId(Integer userId){
        CartVo cartVo = new CartVo();
        List<CartItemVo> cartItemVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0.0");

        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItemDB: cartList){
                CartItemVo cartItemVo = new CartItemVo();
                cartItemVo.setId(cartItemDB.getId());
                cartItemVo.setUserId(cartItemDB.getUserId());
                cartItemVo.setProductId(cartItemDB.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItemDB.getProductId());
                if(product != null){ //给购物车item view object添加商品信息
                    cartItemVo.setProductMainImage(product.getMainImage());
                    cartItemVo.setProductName(product.getName());
                    cartItemVo.setProductSubtitle(product.getSubtitle());
                    cartItemVo.setProductStatus(product.getStatus());
                    cartItemVo.setProductPrice(product.getPrice());
                    cartItemVo.setProductStock(product.getStock());
                    //判断库存，并根据实际库存校正该用户购物车数据表中的该数据条目的购买数量
                    int canBuyNum = 0;
                    if(cartItemDB.getQuantity() <= product.getStock() ){ //购买数量小于等于商品实际库存
                        canBuyNum = cartItemDB.getQuantity();
                        cartItemVo.setLimitQuantity(Constant.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        canBuyNum = product.getStock();
                        cartItemVo.setLimitQuantity(Constant.Cart.LIMIT_NUM_FAIL);
                        //更新该用户购物车数据表中的该数据条目的购买数量为实际库存数,即为最大值
                        Cart updateItem = new Cart();
                        updateItem.setId(cartItemDB.getId());
                        updateItem.setQuantity(canBuyNum);
                        cartMapper.updateByPrimaryKeySelective(updateItem);
                    }
                    cartItemVo.setQuantity(canBuyNum);
                    //compute the total price of this product in cart
                    cartItemVo.setItemTotalPrice(BigDecimalUtil.multiply(cartItemVo.getProductPrice().doubleValue(),canBuyNum));
                    cartItemVo.setItemChecked(cartItemDB.getChecked());
                }
                if(cartItemVo.getItemChecked() == Constant.Cart.TICK)
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartItemVo.getItemTotalPrice().doubleValue());

                cartItemVoList.add(cartItemVo);
            }
        }

        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartItemVoList(cartItemVoList);
        cartVo.setAllChecked(isAllSelected(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.eshop.com/"));
        return cartVo;
    }

    /**
     * 判断当前用户购物车中的所有条目是否全选
     */
    private boolean isAllSelected(Integer userId) {
        return userId != null && cartMapper.uncheckedNumOfCartItemByUserId(userId) == 0;
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null)
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        Cart cartItem = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if(cartItem == null)
            return ServerResponse.createByErrorMsg("Cannot update. This product is not in the cart.");
        cartItem.setQuantity( count ); //包含数量减少的情况
        cartMapper.updateByPrimaryKeySelective(cartItem);
        return list(userId);
    }

    @Override
    public ServerResponse<CartVo> removeProductFromCart(Integer userId, String productIds) {
        List<String> productIdList = Splitter.on(',').splitToList(productIds);
        if(CollectionUtils.isEmpty(productIdList))
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        cartMapper.removeProductByUserIdAndProductIds(userId,productIdList);
        return list(userId);
    }

    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.refreshUserCartByUserId(userId);
        return ServerResponse.createBySuccessData(cartVo);
    }

    @Override
    public ServerResponse<CartVo> selectOrUnselect(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return list(userId);
    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        return ServerResponse.createBySuccessData(cartMapper.selectCartProductCount(userId));
    }
}
