package com.hl.house.common.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageParams {
	private static final Logger logger = LoggerFactory.getLogger(PageParams.class);

	private static final Integer PAGE_SIZE = 5;

	private Integer pageSize;
	private Integer pageNum;
	private Integer offset;
	private Integer limit;
	
	public static PageParams build(Integer pageSize,Integer pageNum){
		logger.info("pageSize={}", pageSize);
		logger.info("pageNum={}", pageNum);
		if (pageSize == null) {
			pageSize = PAGE_SIZE;
		}
		if (pageNum == null) {
			pageNum = 1;
		}
		return new PageParams(pageSize, pageNum);
	}
	
	public PageParams(){
		this(PAGE_SIZE, 1);
	}
	
	public PageParams(Integer pageSize,Integer pageNum){
		this.pageSize = pageSize;
		this.pageNum = pageNum;
		this.offset = pageSize * (pageNum -1);
		this.limit = pageSize;

		logger.info("offset={}", this.offset);
		logger.info("limit={}", this.limit);
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

}
