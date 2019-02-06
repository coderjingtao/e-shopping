package com.eshop.service;

import com.eshop.common.ServerResponse;
import com.eshop.pojo.Shipping;
import com.github.pagehelper.PageInfo;

/**
 * Description: The interface of Shipping Service
 * Created by Jingtao Liu on 4/02/2019.
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);
    ServerResponse<String> delete(Integer userId,Integer shippingId);
    ServerResponse update(Integer userId, Shipping shipping);
    ServerResponse<Shipping> detail(Integer userId, Integer shippingId);
    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
