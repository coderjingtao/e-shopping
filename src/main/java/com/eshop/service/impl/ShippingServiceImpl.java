package com.eshop.service.impl;

import com.eshop.common.ServerResponse;
import com.eshop.dao.ShippingMapper;
import com.eshop.pojo.Shipping;
import com.eshop.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Description: The implementation of Shipping Service
 * Created by Jingtao Liu on 4/02/2019.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * After inserting a shipping address, return the id of the new address
     * Need to modify the insert id in "ShippingMapper.xml"
     */
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);// to avoid a mock user id in shipping pojo
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("Create New Address Successfully.",result);
        }
        return ServerResponse.createByErrorMsg("Creating New Address Failed.");
    }

    /**
     * Delete a shipping address by user id and shipping id
     * If we use default delete method (delete only by shipping id) automatically created by Mybatis,
     * it would produce a Transverse unauthorized problem
     */
    @Override
    public ServerResponse<String> delete(Integer userId, Integer shippingId) {
        int rowCount = shippingMapper.deleteByUserIdAndShippingId(userId, shippingId);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMsg("Delete Address Successfully.");
        }
        return ServerResponse.createByErrorMsg("Deleting Address Failed.");
    }

    /**
     * Update a shipping address by user id and shipping id
     * If we use default update method (update only by shipping id) automatically created by Mybatis,
     * it would produce a Transverse unauthorized problem
     */
    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);// to avoid a mock user id in shipping pojo
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0)
            return ServerResponse.createBySuccessMsg("Update Address Successfully");
        return ServerResponse.createByErrorMsg("Updating Address Failed.");
    }

    /**
     * Get a shipping address detail by user id and shipping id
     * If we use default select method (select only by shipping id) automatically created by Mybatis,
     * it would produce a Transverse unauthorized problem
     */
    @Override
    public ServerResponse<Shipping> detail(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByUserIdAndShippingId(userId, shippingId);
        if(shipping != null)
            return ServerResponse.createBySuccessData(shipping);
        return ServerResponse.createByErrorMsg("Cannot find this address.");
    }

    /**
     * Get a shipping address list by user id
     * If we use default select method (select only by shipping id) automatically created by Mybatis,
     * it would produce a Transverse unauthorized problem
     */
    @Override
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo<>(shippingList);
        return ServerResponse.createBySuccessData(pageInfo);
    }
}
