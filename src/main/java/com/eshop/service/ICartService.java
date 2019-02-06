package com.eshop.service;

import com.eshop.common.ServerResponse;
import com.eshop.vo.CartVo;

/**
 * Description: The interface of Cart Service
 * Created by Jingtao Liu on 4/02/2019.
 */
public interface ICartService {
    ServerResponse<CartVo> list(Integer userId);
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count);
    ServerResponse<CartVo> removeProductFromCart(Integer userId,String productIds);
    ServerResponse<CartVo> selectOrUnselect (Integer userId,Integer productId,Integer checked);
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
