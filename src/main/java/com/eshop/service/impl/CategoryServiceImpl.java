package com.eshop.service.impl;

import com.eshop.common.ServerResponse;
import com.eshop.dao.CategoryMapper;
import com.eshop.pojo.Category;
import com.eshop.service.ICategoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by Liujingtao on 2018/7/6.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(Integer parentId, String categoryName) {
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMsg("New category's parameters are incorrect.");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); // be available
        int insertCount = categoryMapper.insert(category);
        if(insertCount < 1){
            return ServerResponse.createByErrorMsg("Adding new category failed.");
        }
        return ServerResponse.createBySuccessMsg("Adding new category succeed.");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMsg("New category's parameters are incorrect.");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int updateCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(updateCount < 1){
            return ServerResponse.createByErrorMsg("Editing category name failed.");
        }
        return ServerResponse.createBySuccessMsg("Editing category name succeed.");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenCategoryById(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("Cannot find children of this category id: "+categoryId);
        }
        return ServerResponse.createBySuccessData(categoryList);
    }

    @Override
    public ServerResponse<List<Integer>> getRecursiveChildrenCategoryById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        recursiveSearch(categorySet,categoryId);
        List<Integer> categoryIds = Lists.newArrayList();
        if(categorySet.size()>0){
            for(Category item : categorySet){
                categoryIds.add(item.getId());
            }
        }
        return ServerResponse.createBySuccessData(categoryIds);
    }

    //recursive algorithm
    private Set<Category> recursiveSearch(Set<Category> categorySet,Integer parentId){
        // current node
        Category parent = categoryMapper.selectByPrimaryKey(parentId);
        if(parent != null){
            categorySet.add(parent);
        }
        // children nodes of current node
        List<Category> children = categoryMapper.selectByParentId(parentId);
        if(children.size()>0){
            for(Category child : children){
                //recursive invoke
                recursiveSearch(categorySet,child.getId());
            }
        }
        return categorySet;
    }
}
