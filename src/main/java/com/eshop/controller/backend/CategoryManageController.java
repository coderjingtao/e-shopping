package com.eshop.controller.backend;

import com.eshop.common.Constant;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponse;
import com.eshop.pojo.User;
import com.eshop.service.ICategoryService;
import com.eshop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Description: Category Management Controller for the back-end managers
 * Created by Jingtao Liu on 2018/7/6.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * Add a new product category under a parent category by admin
     * If the parent category is 0 or empty, we regard it as the top category with its value 0.
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(@RequestParam(value = "parentId",defaultValue = "0") int parentId, String categoryName, HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login.");
        }
        // only the administrator has the privilege to add a category
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.addCategory(parentId,categoryName);
        }else{
            return ServerResponse.createByErrorMsg("Have no administrative privilege");
        }
    }

    /**
     * Reset a category name by its category ID
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(Integer categoryId,String categoryName,HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login");
        }
        // only the administrator has the privilege to set a category name
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }else{
            return ServerResponse.createByErrorMsg("Have no administrative privilege");
        }
    }

    /**
     * Get all the next paralleled level categories by one parent id of category.
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId,HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login");
        }
        // only the administrator has the privilege to query
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.getChildrenCategoryById(categoryId);
        }else{
            return ServerResponse.createByErrorMsg("Have no administrative privilege");
        }
    }

    /**
     * Get itself, all the children and offspring categories by one parent id of category.
     */
    @RequestMapping("get_all_category.do")
    @ResponseBody
    public ServerResponse getRecursiveChildrenCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId,HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"Please login");
        }
        // only the administrator has the privilege to query
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.getRecursiveChildrenCategoryById(categoryId);
        }else{
            return ServerResponse.createByErrorMsg("Have no administrative privilege");
        }
    }
}
