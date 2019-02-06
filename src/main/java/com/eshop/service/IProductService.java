package com.eshop.service;

import com.eshop.common.ServerResponse;
import com.eshop.pojo.Product;
import com.eshop.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

/**
 * Description: The interface of Product Service
 * Created by Jingtao Liu on 1/02/2019.
 */
public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);
    ServerResponse setSaleStatus(Integer productId,Integer status);
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize);
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
}
