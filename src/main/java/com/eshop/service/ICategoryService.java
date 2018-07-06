package com.eshop.service;

import com.eshop.common.ServerResponse;
import com.eshop.pojo.Category;

import java.util.List;

/**
 * Created by Liujingtao on 2018/7/6.
 */
public interface ICategoryService {
    ServerResponse addCategory(Integer parentId, String categoryName);
    ServerResponse updateCategoryName(Integer categoryId, String categoryName);
    ServerResponse<List<Category>> getChildrenCategoryById(Integer categoryId);
    ServerResponse<List<Integer>> getRecursiveChildrenCategoryById(Integer categoryId);
}
