package service.product.impl;

import java.util.List;

import javax.annotation.Resource;

import entity.Product;
import entity.dao.ProductDAO;
import service.product.ProductService;

public class ProductServiceImpl implements ProductService {

	@Resource
	private ProductDAO productDAO;
	
	@Override
	public List queryProductData(String keyword){
		List <Product> result =productDAO.query(keyword);
		return result;
	}

}
