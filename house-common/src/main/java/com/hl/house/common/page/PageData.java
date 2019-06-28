package com.hl.house.common.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class PageData<T> {

	private static final Logger logger = LoggerFactory.getLogger(PageData.class);
	private List<T> list;
	private Pagination pagination;//pageNum,pageSize,page list
	
	public PageData(Pagination pagination,List<T> list){
		this.pagination = pagination;
		this.list = list;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}
	
	public static  <T> PageData<T> buildPage(List<T> list,Long count,Integer pageSize,Integer pageNum){
		Pagination _pagination = new Pagination(pageSize, pageNum, count);
		logger.info("pageSize={}", pageSize);
		logger.info("pageNum={}", pageNum);
		return new PageData<>(_pagination, list);
	}
	
}
