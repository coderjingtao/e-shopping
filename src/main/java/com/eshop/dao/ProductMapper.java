package com.eshop.dao;

import com.eshop.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectList();

    List<Product> selectByProductIdAndName(@Param("productId")Integer id, @Param("productName") String name);

    List<Product> selectByProductNameAndCategoryIds(@Param("productName") String name, @Param("categoryIdList")List<Integer> categoryIdList);
}