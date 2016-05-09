package entity.hibernate;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

public class RootDAO {
	protected static final Log log = LogFactory.getLog(RootDAO.class);

	protected SessionFactory sessionFactory;
	/** For MariaDB查詢用 */
	protected SessionFactory sessionFactorySelect;

	/**
	 * 新增物件資料至DB,此時已是持久化物件
	 * 
	 * @param transientInstance
	 *            需新增的物件
	 */
	public void persist(Object transientInstance) {
		log.debug("persisting instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			flush();
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(Object instance) {
		log.debug("attaching dirty instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(Object instance) {
		log.debug("attaching clean instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	/**
	 * 刪除DB中物件資料
	 * 
	 * @param persistentInstance
	 *            需刪除的物件
	 */
	public void delete(Object persistentInstance) {
		log.debug("deleting instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			flush();
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	/**
	 * 會先判斷該物件資料是否存在於DB,若存在則修改,不存在則新增
	 * 
	 * @param detachedInstance
	 *            需新增或修改的物件
	 * @return 已更新的物件資料
	 */
	public Object merge(Object detachedInstance) {
		log.debug("merging instance");
		try {
			Object result = sessionFactory.getCurrentSession().merge(
					detachedInstance);
			flush();
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	/**
	 * 會先判斷該物件資料是否存在於DB,若存在則修改,不存在則新增
	 * 
	 * @param detachedInstance 需新增或修改的物件資料
	 */
	public void saveOrUpdate(Object detachedInstance) {
		log.debug("saveOrUpdate instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(detachedInstance);
			flush();
			log.debug("saveOrUpdate successful");
		} catch (RuntimeException re) {
			log.error("saveOrUpdate failed", re);
			throw re;
		}
	}

	/**
	 * 透過 session.save method 將 entity 存到 db 裡
	 * @param entity
	 */
	public void save(Object entity) {
		log.debug("saveOrUpdate instance");
		try {
			sessionFactory.getCurrentSession().save(entity);
			flush();
			log.debug("saveOrUpdate successful");
		} catch (RuntimeException re) {
			log.error("saveOrUpdate failed", re);
			throw re;
		}
	}	
	
	/**
	 * 將緩衝區的資料新增到DB
	 */
	public void flush() {
		sessionFactory.getCurrentSession().flush();
	}

	/**
	 * 將持久化物件從session的缓衝區中清空
	 * 
	 * @param o 需清空的物件
	 */
	public void evict(Object o) {
		sessionFactory.getCurrentSession().evict(o);
	}

	/**
	 * 將持久化物件從session的缓衝區中清空
	 * 
	 * @param list 需清空的物件List
	 */
	public void evict(Collection list) {
		if (list != null) {
			for (Iterator itr = list.iterator(); itr.hasNext();) {
				Object o = itr.next();
				evict(o);
			}
		}
	}
	
	/**
	 * 透過 Hibernate Criteria 物件取得資料且可做distinct，並傳回 Page分頁物件
	 * (分頁物件resultList內為object物件,不是entity)
	 * 
	 * @param crit Hibernate Criteria
	 * @param page 分頁物件
	 * @param propertyName distinct屬性
	 * @return 分頁物件
	 */
	public Page page(Criteria crit, Page page,String propertyName) {
		Long totalCount = Long.parseLong(crit.setProjection(Projections.countDistinct(propertyName)).uniqueResult().toString());
		page.setTotalCount(totalCount != null ? totalCount.intValue() : 0);
		page.setLast(page.getTotalCount() % page.getSize() == 0 ? page.getTotalCount() / page.getSize(): (page.getTotalCount() / page.getSize()) + 1);
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property(propertyName));
		crit.setProjection(Projections.distinct(projectionList));
		
		if (page.getOrderByColumns() != null) {
			for (int i = 0; i < page.getOrderByColumns().length; i++) {
				String orderBy = page.getOrderByColumns()[i];
				String[] s = orderBy.split(" ");
				String order = "asc";
				if (s.length > 2) {
					order = s[s.length - 1].trim();
				}
				if ("asc".equalsIgnoreCase(order)) {
					crit.addOrder(Order.asc(s[0].trim()));
				} else if ("desc".equalsIgnoreCase(order)) {
					crit.addOrder(Order.desc(s[0].trim()));
				}
			}
		}
		List list = crit.setMaxResults(page.getSize())
					.setFirstResult(page.getSize() * (page.getCurrent() - 1))
					.list();
		page.setResults(list);
		return page;
	}


	/**
	 * 透過 Hibernate Query 物件取得資料，並傳回 Page分頁物件
	 * 
	 * @param query Hibernate Query
	 * @param page 分頁物件
	 * @return 分頁物件
	 */
	public Page page(SQLQuery query, Page page) {
		return getPage(query,  page, false);

	}

	/**
	 * 透過 Hibernate Query 物件取得資料，並傳回 Page分頁物件
	 * 
	 * @param query Hibernate Query
	 * @param page 分頁物件
	 * @param hasEntity 原始查詢物件是否有額外提供Entity的對映名稱，有的話此參數帶 True
	 * @return 分頁物件
	 */
	public Page page(SQLQuery query, Page page,boolean hasEntity) {
		return getPage(query, page, false);
	}
	
	/**
	 * 透過 Hibernate Query 物件取得資料，並傳回 Page分頁物件
	 * 
	 * @param query Hibernate Query
	 * @param page 分頁物件
	 * @return 分頁物件
	 */
	public Page page(Query query, Page page) {
		return getPage(query, page,false);
	}

	/**
	 * 透過 Hibernate Query 物件取得資料，並傳回 Page分頁物件
	 * 
	 * @param query Hibernate Query
	 * @param page 分頁物件
	 * @param hasEntity 原始查詢物件是否有額外提供Entity的對映名稱，有的話此參數帶 True
	 * @return 分頁物件
	 */
	public Page page(Query query, Page page,boolean hasEntity) {
		return getPage(query, page, hasEntity);
//		ScrollableResults results = query.scroll(ScrollMode.SCROLL_INSENSITIVE);
//
//		int total = 0, start = 0;
//		ArrayList list = new ArrayList();
//		if (results.last()) {
//			total = results.getRowNumber() + 1;
//			start = page.getSize() * (page.getCurrent() - 1);
//			if (start < total) {
//				results.first();
//				if (results.scroll(start)) {
//					for (int i = 0; i < page.getSize(); i++) {
//						if (hasEntity)
//							list.add(results.get()[0]);
//						else
//							list.add(results.get());
//						if (!results.next()) {
//							break;
//						}
//					}
//				}
//			}
//		}
//
//		page.setTotalCount(total);
//		page.setLast(page.getTotalCount() % page.getSize() == 0 ? page.getTotalCount() / page.getSize()	: (page.getTotalCount() / page.getSize()) + 1);
//		page.setResults(list);
//		return page;

	}
	
	/**
	 * 透過 Hibernate Query 物件取得資料，並傳回 Page分頁物件
	 * 
	 * @param query Hibernate Query
	 * @param page 分頁物件
	 * @param hasEntity 原始查詢物件是否有額外提供Entity的對映名稱，有的話此參數帶 True
	 * @return 分頁物件
	 */
	private Page getPage(Query query, Page page,boolean hasEntity) {
		ScrollableResults results = query.scroll(ScrollMode.SCROLL_INSENSITIVE);

		int total = 0, start = 0;
		ArrayList list = new ArrayList();
		if (results.last()) {
			total = results.getRowNumber() + 1;
			start = page.getSize() * (page.getCurrent() - 1);
			if (start < total) {
				results.first();
				if (results.scroll(start)) {
					for (int i = 0; i < page.getSize(); i++) {
						if (hasEntity)
							list.add(results.get()[0]);
						else
							list.add(results.get());
						if (!results.next()) {
							break;
						}
					}
				}
			}
		}

		page.setTotalCount(total);
		page.setLast(page.getTotalCount() % page.getSize() == 0 ? page.getTotalCount() / page.getSize()	: (page.getTotalCount() / page.getSize()) + 1);
		page.setResults(list);
		return page;

	}		
	
	
	/**
	 * 若table的Hibernate mapping有設定延遲初始，某些時候有需要在session關閉後取得相關物件， 可使用此方法來先行載入相關物件
	 * 
	 * @param obj 欲取得的物件資料
	 */
	public void initial(Object obj) {
		Hibernate.initialize(obj);
	}

	/**
	 * 設定資料庫連線物件
	 * 
	 * @param sessionFactory 連線物件
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 設定只能查詢資料庫連線物件
	 * 
	 * @param sessionFactorySelect 連線物件
	 */
	public void setSessionFactorySelect(SessionFactory sessionFactorySelect) {
		this.sessionFactorySelect = sessionFactorySelect;
	}
}
