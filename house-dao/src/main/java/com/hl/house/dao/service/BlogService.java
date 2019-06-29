package com.hl.house.dao.service;

import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hl.house.dao.mapper.BlogMapper;
import com.hl.house.common.model.Blog;
import com.hl.house.common.page.PageData;
import com.hl.house.common.page.PageParams;

@Service
public class BlogService {
  
  @Autowired
  private BlogMapper blogMapper;

  public PageData<Blog> queryBlog(Blog query, PageParams params) {
    List<Blog> blogs =  blogMapper.selectBlog(query,params);
    populate(blogs);
    Long  count =  blogMapper.selectBlogCount(query);
    return PageData.<Blog>buildPage(blogs, count, params.getPageSize(), params.getPageNum());
  }

  private void populate(List<Blog> blogs) {
    if (!blogs.isEmpty()) {
      blogs.stream().forEach(item -> {
        String stripped =  Jsoup.parse(item.getContent()).text();
        item.setDigest(stripped.substring(0, Math.min(stripped.length(),40)));
        String tags = item.getTags();
        item.getTagList().addAll(Lists.newArrayList(Splitter.on(",").split(tags)));
      });
    }
  }

  public Blog queryOneBlog(int id) {
    Blog query = new Blog();
    query.setId(id);
    List<Blog> blogs = blogMapper.selectBlog(query, new PageParams(1, 1));
    if (!blogs.isEmpty()) {
      return blogs.get(0);
    }
    return null;
  }

  

}
