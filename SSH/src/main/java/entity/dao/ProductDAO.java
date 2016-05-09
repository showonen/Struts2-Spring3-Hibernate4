package entity.dao;


import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import entity.Product;
import entity.hibernate.RootDAO;


public class ProductDAO extends RootDAO {

 public List <Product> query(String keyword){
	 Criteria criteria =sessionFactory.getCurrentSession().createCriteria(Product.class);
	 criteria.add(Restrictions.like("name", keyword));
	 return criteria.list();
 }
	
}
