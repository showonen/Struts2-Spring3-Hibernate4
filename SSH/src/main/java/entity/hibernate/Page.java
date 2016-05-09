package entity.hibernate;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Page implements Serializable {
	private static final long serialVersionUID = 416050371321054181L;
	public static final int DEFAULT_SIZE = 20;
	private int size = 10;
	private int totalCount;
	private int current = 1;
	private int last;
	private String[] orderByColumns;
	private List results = new ArrayList();

	/**
	 * 取得目前頁數(目前頁碼)
	 * @return 頁數
	 */
	public int getCurrent() {
		return current;
	}
	
	/**
	 * 設定目前頁數(目前頁碼)
	 * @param current 頁數
	 */
	public void setCurrent(int current) {
		this.current = current;
	}

	/**
	 * 取得總頁數(最後一頁的頁碼)
	 * @return 總頁數
	 */
	public int getLast() {
		return last;
	}

	/**
	 * 設定總頁數(最後一頁的頁碼)
	 * @param last 總頁數
	 */
	public void setLast(int last) {
		this.last = last;
	}
	
	/**
	 * 取得查詢後的資料
	 * @return 資料List
	 */
	public List getResults() {
		return results;
	}

	/**
	 * 設定查詢後的資料
	 * @param results 資料的List
	 */
	public void setResults(List results) {
		this.results = results;
	}
	
	public void addResult(Object p) {
		results.add(p);
	}
	
	/**
	 * 取得每頁顯示筆數,預設為10筆
	 * @return 筆數
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * 設定每頁顯示筆數
	 * @param size 筆數
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	/**
	 * 取得總筆數
	 * @return 總筆數
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * 取得總筆數
	 * @param totalCount 總筆數
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * 取得有排序的DB欄位名稱
	 * @return DB欄位名稱的Array
	 */
	public String[] getOrderByColumns() {
		return orderByColumns;
	}
	
	/**
	 * 設定需排序的DB欄位名稱
	 * @param orderByColumns DB欄位名稱的Array
	 */
	public void setOrderByColumns(String[] orderByColumns) {
		this.orderByColumns = orderByColumns;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + current;
		result = PRIME * result + last;
		result = PRIME * result + Arrays.hashCode(orderByColumns);
		result = PRIME * result + ((results == null) ? 0 : results.hashCode());
		result = PRIME * result + size;
		result = PRIME * result + totalCount;
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Page other = (Page) obj;
		if (current != other.current)
			return false;
		if (last != other.last)
			return false;
		if (!Arrays.equals(orderByColumns, other.orderByColumns))
			return false;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		if (size != other.size)
			return false;
		if (totalCount != other.totalCount)
			return false;
		return true;
	}	
}
