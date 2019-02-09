package com.eshop.service.impl;

import com.eshop.common.Constant;
import com.eshop.common.ResponseCode;
import com.eshop.common.ServerResponse;
import com.eshop.dao.CategoryMapper;
import com.eshop.dao.ProductMapper;
import com.eshop.pojo.Category;
import com.eshop.pojo.Product;
import com.eshop.service.ICategoryService;
import com.eshop.service.IProductService;
import com.eshop.util.DateTimeUtil;
import com.eshop.util.PropertiesUtil;
import com.eshop.vo.ProductDetailVo;
import com.eshop.vo.ProductVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Product Management Service Implementation.
 * Created by Jingtao Liu on 1/02/2019.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    /*
     * Save or update one product by judging its id whether is null or not.
     */
    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if(product == null)
            return ServerResponse.createByErrorMsg("Product's parameters are incorrect.");
        // set the main image of product
        if(StringUtils.isNotBlank(product.getSubImages())){
            String[] subImgArray = product.getSubImages().split(",");
            if(subImgArray.length > 0)
                product.setMainImage(subImgArray[0]);
        }

        if(product.getId() != null){ // product has id, so it's an updating operation
            int rowCount = productMapper.updateByPrimaryKeySelective(product);
            if(rowCount > 0){
                return ServerResponse.createBySuccessMsg("Update product successfully.");
            }else{
                return ServerResponse.createByErrorMsg("Updating product failed.");
            }
        }else{ // product has no id, so it's an inserting operation
            int rowCount = productMapper.insert(product);
            if(rowCount > 0){
                return ServerResponse.createBySuccessMsg("Save product successfully.");
            }else{
                return ServerResponse.createByErrorMsg("Saving product failed.");
            }

        }
    }

    /**
     * Set the status of on/off shelf of one product
     * status: 1 - on shelf  2 - off shelf  3 - deleted
     */
    @Override
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMsg("Update the on/off shelf of product successfully.");
        }else{
            return ServerResponse.createByErrorMsg("Update the on/off shelf of product failed.");
        }
    }

    /**
     * Get the detailing of product by administrator
     */
    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if(productId == null)
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null)
            return ServerResponse.createByErrorMsg("Product is off shelf or removed.");

        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccessData(productDetailVo);
    }

    /**
     * Assemble some information necessary to the VO of product
     */
    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        //add image host info to the VO of product
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.eshop.com/"));
        //add parent category id to the VO of product, there's only category id in the pojo of product
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//the root node of category
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        //add String format of time info to the VO of product
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    /**
     * Get all the product list with pagination for the product management
     */
    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();

        List<ProductVo> productVoList = Lists.newArrayList();
        for (Product product: productList) {
            ProductVo productVo = assembleProductVo(product);
            productVoList.add(productVo);
        }
        PageInfo pageResult = new PageInfo<>(productVoList);//paginate using original product list
        //pageResult.setList(productVoList); //the content of pagination using converted product VO list
        return ServerResponse.createBySuccessData(pageResult);
    }

    //Just return fewer fields of product to display on management website
    private ProductVo assembleProductVo(Product product){
        ProductVo productVo = new ProductVo();
        productVo.setId(product.getId());
        productVo.setName(product.getName());
        productVo.setCategoryId(product.getCategoryId());
        productVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.eshop.com/"));
        productVo.setMainImage(product.getMainImage());
        productVo.setPrice(product.getPrice());
        productVo.setSubtitle(product.getSubtitle());
        productVo.setStatus(product.getStatus());
        return productVo;
    }

    /**
     * Get product list by searching product name or product id for the product management
     */
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectByProductIdAndName(productId,productName);
        List<ProductVo> productVoList = Lists.newArrayList();
        for (Product product: productList) {
            ProductVo productVo = assembleProductVo(product);
            productVoList.add(productVo);
        }
        PageInfo pageResult = new PageInfo<>(productVoList);
        return ServerResponse.createBySuccessData(pageResult);
    }

    /**
     * Get the detailing of product by users
     */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if(productId == null)
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMsg("Product is deleted.");
        }
        if(product.getStatus() != Constant.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMsg("Product is off shelf.");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccessData(productDetailVo);
    }

    /**
     * Get product list by searching product keyword or product category with order
     * It's for front-end user.
     */
    @Override
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy) {
        if(StringUtils.isBlank(keyword) && categoryId == null)
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());

        //1. handle category
        List<Integer> categoryIdList = new ArrayList<Integer>();
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){// no searching result
                PageHelper.startPage(pageNum,pageSize);
                List<ProductVo> productVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo<>(productVoList);
                return ServerResponse.createBySuccessData(pageInfo);
            }
            //get all the children category and itself
            categoryIdList = iCategoryService.getRecursiveChildrenCategoryById(categoryId).getData();
        }
        //2. handle orderBy
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(orderBy)){
            if(Constant.ProductListOrderBy.PRICE_ORDER_BY.contains(orderBy)){
                String[] orderByArr = orderBy.split("_");
                PageHelper.orderBy(orderByArr[0]+" "+orderByArr[1]);
            }
        }

        //3.Search, when keyword=="" or categoryIdList.size()==0, we can't search any result, so we need pass null instead.
        List<Product> productList = productMapper.selectByProductNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);
        List<ProductVo> productVoList = Lists.newArrayList();
        for (Product product: productList ) {
            ProductVo productVo = assembleProductVo(product);
            productVoList.add(productVo);
        }
        PageInfo pageInfo = new PageInfo<>(productVoList);
        return ServerResponse.createBySuccessData(pageInfo);
    }
}
