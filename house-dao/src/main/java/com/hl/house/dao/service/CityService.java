package com.hl.house.dao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.hl.house.common.model.City;

@Service
public class CityService {
  
  public List<City> getAllCitys(){
    City city = new City();
    city.setCityCode("110000");
    city.setCityName("北京");
    city.setId(1);
    return Lists.newArrayList(city);
  }

}
