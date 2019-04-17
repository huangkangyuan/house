package com.hl.house.dao.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.hl.house.dao.mapper.HouseMapper;
import com.hl.house.common.constants.HouseUserType;
import com.hl.house.common.model.Community;
import com.hl.house.common.model.House;
import com.hl.house.common.model.HouseUser;
import com.hl.house.common.model.User;
import com.hl.house.common.model.UserMsg;
import com.hl.house.common.page.PageData;
import com.hl.house.common.page.PageParams;
import com.hl.house.common.utils.BeanHelper;

@Service
public class HouseService {

	private static final Logger logger = LoggerFactory.getLogger(HouseService.class);
	
	@Autowired
	private HouseMapper houseMapper;
	
	@Value("${file.prefix}")
	private String imgPrefix;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	private AgencyService agencyService;
	
	@Autowired
	private MailService mailService;
	

	/**
	 * 1.查询小区
	 * 2.添加图片服务器地址前缀
	 * 3.构建分页结果
	 * @param query
	 * @param pageParams
	 */
	public PageData<House> queryHouse(House query, PageParams pageParams) {
		List<House> houses = Lists.newArrayList();
		if (!Strings.isNullOrEmpty(query.getName())) {
			Community community = new Community();
			logger.info("query.getName={}", query.getName());
			community.setName(query.getName());
			List<Community> communities = houseMapper.selectCommunity(community);
			logger.info("communities={}", communities);
			if (!communities.isEmpty()) {
				query.setCommunityId(communities.get(0).getId());
			}
		}
		houses = queryAndSetImg(query,pageParams);//添加图片服务器地址前缀
		Long count = houseMapper.selectPageCount(query);
		logger.info("count={}", count);
		return PageData.buildPage(houses, count, pageParams.getPageSize(), pageParams.getPageNum());
	}

	public List<House> queryAndSetImg(House query, PageParams pageParams) {
		List<House> houses =   houseMapper.selectPageHouses(query, pageParams);
		houses.forEach(h ->{
			h.setFirstImg(imgPrefix + h.getFirstImg());
			h.setImageList(h.getImageList().stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
		    h.setFloorPlanList(h.getFloorPlanList().stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
		});
		return houses;
	}
	
	public List<Community> getAllCommunitys() {
		Community community = new Community();
		return houseMapper.selectCommunity(community);
	}

	/**
	 * 添加房屋图片
	 * 添加户型图片
	 * 插入房产信息
	 * 绑定用户到房产的关系
	 * @param house
	 * @param user
	 */
	public void addHouse(House house, User user) {
		if (CollectionUtils.isNotEmpty(house.getHouseFiles())) {
			String images = Joiner.on(",").join(fileService.getImgPaths(house.getHouseFiles()));
		    house.setImages(images);
		}
		if (CollectionUtils.isNotEmpty(house.getFloorPlanFiles())) {
			String images = Joiner.on(",").join(fileService.getImgPaths(house.getFloorPlanFiles()));
		    house.setFloorPlan(images);
		}
		BeanHelper.onInsert(house);
		houseMapper.insert(house);
		bindUser2House(house.getId(),user.getId(),false);
	}

	public void bindUser2House(Long houseId, Long userId, boolean collect) {
      HouseUser existhouseUser =     houseMapper.selectHouseUser(userId,houseId,collect ? HouseUserType.BOOKMARK.value : HouseUserType.SALE.value);
	  if (existhouseUser != null) {
		  return;
	  }
	  HouseUser houseUser = new HouseUser();
	  houseUser.setHouseId(houseId);
	  houseUser.setUserId(userId);
	  houseUser.setType(collect ? HouseUserType.BOOKMARK.value : HouseUserType.SALE.value);
	  BeanHelper.setDefaultProp(houseUser, HouseUser.class);
	  BeanHelper.onInsert(houseUser);
	  houseMapper.insertHouseUser(houseUser);
	}

	public HouseUser getHouseUser(Long houseId){
		HouseUser houseUser =  houseMapper.selectSaleHouseUser(houseId);
		return houseUser;
	}
	
	public House queryOneHouse(Long id) {
		House query = new House();
		query.setId(id);
		List<House> houses = queryAndSetImg(query, PageParams.build(1, 1));
		if (!houses.isEmpty()) {
			return houses.get(0);
		}
		return null;
	}

	public void addUserMsg(UserMsg userMsg) {
        BeanHelper.onInsert(userMsg);
        houseMapper.insertUserMsg(userMsg);
        User agent = agencyService.getAgentDeail(userMsg.getAgentId());
        mailService.sendMail("来自用户"+userMsg.getEmail()+"的留言", userMsg.getMsg(), agent.getEmail());
	}

	public void updateRating(Long id, Double rating) {
		House house = queryOneHouse(id);
		Double oldRating = house.getRating();
		Double newRating  = oldRating.equals(0D)? rating : Math.min((oldRating+rating)/2, 5);
		House updateHouse = new House();
		updateHouse.setId(id);
		updateHouse.setRating(newRating);
		BeanHelper.onUpdate(updateHouse);
		houseMapper.updateHouse(updateHouse);
	}

	public void unbindUser2House(Long id, Long userId, HouseUserType type) {
	  if (type.equals(HouseUserType.SALE)) {
	      houseMapper.downHouse(id);
	    }else {
	      houseMapper.deleteHouseUser(id, userId, type.value);
	    }
	    
	}

}
