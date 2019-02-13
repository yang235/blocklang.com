package com.blocklang.core.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.dao.UserAvatarDao;
import com.blocklang.core.dao.UserBindDao;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserAvatar;
import com.blocklang.core.model.UserBind;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.test.AbstractServiceTest;

public class GithubLoginServiceImplTest extends AbstractServiceTest{

	@Autowired
	private GithubLoginService githubLoginService;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserAvatarDao userAvatarDao;
	
	@Autowired
	private UserBindDao userBindDao;
	
	@Test
	public void update_user_new_success() {
		OAuth2AccessToken accessToken = new OAuth2AccessToken(TokenType.BEARER, 
				"access_token_value", 
				Instant.now().minusSeconds(1), 
				Instant.now());
		
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		userAttributes.put("login", "login");
		userAttributes.put("id", "1");
		userAttributes.put("avatar_url", "https://avatars2.githubusercontent.com/u/1?v=4");
		userAttributes.put("name", "name");
		userAttributes.put("company", "company");
		userAttributes.put("blog", "blog");
		userAttributes.put("location", "location");
		userAttributes.put("email", "email");
		userAttributes.put("bio", "bio");
		
		List<OAuth2UserAuthority> authorities = new ArrayList<OAuth2UserAuthority>();
		OAuth2UserAuthority authority = new OAuth2UserAuthority(userAttributes);
		authorities.add(authority);

		OAuth2User oauthUser = new DefaultOAuth2User(authorities, userAttributes, "id");
		
		Integer userId = githubLoginService.updateUser(accessToken, oauthUser).getId();
		
		// 断言
		// 用户基本信息
		Optional<UserInfo> userOption = userDao.findById(userId);
		assertThat(userOption.isPresent(), is(true));
		UserInfo user = userOption.get();
		assertThat(user.getLoginName(), equalTo("login"));
		assertThat(user.getNickname(), equalTo("name"));
		assertThat(user.getCompany(), equalTo("company"));
		assertThat(user.getWebsiteUrl(), equalTo("blog"));
		assertThat(user.getLocation(), equalTo("location"));
		assertThat(user.getEmail(), equalTo("email"));
		assertThat(user.getBio(), equalTo("bio"));
		assertThat(user.getAvatarUrl(), equalTo("https://avatars2.githubusercontent.com/u/1?v=4&s=40"));
		// 用户头像
		List<UserAvatar> avatars = userAvatarDao.findByUserId(userId);
		assertThat(avatars.size(), is(3));
		assertThat(avatars.stream().map(avatar -> avatar.getSizeType().getKey()).collect(Collectors.toList()),
				hasItem(startsWith("0")));
		// 用户与社交帐号绑定关系
		Optional<UserBind> userBindOption = userBindDao.findBySiteAndOpenId(OauthSite.GITHUB, "1");
		assertThat(userBindOption.get().getUserId(), is(userId));
	}
	
	@Test
	public void update_user_update_success() {
		Instant now = Instant.now();
		// 第一次创建
		OAuth2AccessToken accessToken = new OAuth2AccessToken(TokenType.BEARER, 
				"access_token_value", 
				now.minusSeconds(4), 
				now.minusSeconds(3));
		
		Map<String, Object> userAttributes = new HashMap<String, Object>();
		userAttributes.put("login", "login_1");
		userAttributes.put("id", "1");
		userAttributes.put("avatar_url", "https://avatars2.githubusercontent.com/u/1?v=4_1");
		userAttributes.put("name", "name_1");
		userAttributes.put("company", "company_1");
		userAttributes.put("blog", "blog_1");
		userAttributes.put("location", "location_1");
		userAttributes.put("email", "email_1");
		userAttributes.put("bio", "bio_1");
		
		List<OAuth2UserAuthority> authorities = new ArrayList<OAuth2UserAuthority>();
		OAuth2UserAuthority authority = new OAuth2UserAuthority(userAttributes);
		authorities.add(authority);

		OAuth2User oauthUser = new DefaultOAuth2User(authorities, userAttributes, "id");
		
		Integer userId1 = githubLoginService.updateUser(accessToken, oauthUser).getId();
		
		// 第二次更新（openId 相同）
		accessToken = new OAuth2AccessToken(TokenType.BEARER, 
				"access_token_value", 
				now.minusSeconds(2), 
				now.minusSeconds(1));
		
		userAttributes = new HashMap<String, Object>();
		userAttributes.put("login", "login_2");
		userAttributes.put("id", "1"); // 注意，这就是 openId,两次的值必须相同
		userAttributes.put("avatar_url", "https://avatars2.githubusercontent.com/u/1?v=4_2");
		userAttributes.put("name", "name_2");
		userAttributes.put("company", "company_2");
		userAttributes.put("blog", "blog_2");
		userAttributes.put("location", "location_2");
		userAttributes.put("email", "email_2");
		userAttributes.put("bio", "bio_2");
		
		authorities = new ArrayList<OAuth2UserAuthority>();
		authority = new OAuth2UserAuthority(userAttributes);
		authorities.add(authority);

		oauthUser = new DefaultOAuth2User(authorities, userAttributes, "id");
		
		Integer userId2 = githubLoginService.updateUser(accessToken, oauthUser).getId();
		
		assertThat(countRowsInTable("user_info"), is(1));
		assertThat(userId1, is(userId2));
		
		// 断言
		// 用户基本信息
		Optional<UserInfo> userOption = userDao.findById(userId2);
		assertThat(userOption.isPresent(), is(true));
		UserInfo user = userOption.get();
		assertThat(user.getLoginName(), equalTo("login_2"));
		assertThat(user.getNickname(), equalTo("name_2"));
		assertThat(user.getCompany(), equalTo("company_2"));
		assertThat(user.getWebsiteUrl(), equalTo("blog_2"));
		assertThat(user.getLocation(), equalTo("location_2"));
		assertThat(user.getEmail(), equalTo("email_2"));
		assertThat(user.getBio(), equalTo("bio_2"));
		assertThat(user.getAvatarUrl(), equalTo("https://avatars2.githubusercontent.com/u/1?v=4_2&s=40"));
		// 用户头像
		List<UserAvatar> avatars = userAvatarDao.findByUserId(userId2);
		assertThat(avatars.stream().map(avatar -> avatar.getAvatarUrl()).collect(Collectors.toList()), 
				hasItem(containsString("https://avatars2.githubusercontent.com/u/1?v=4_2&s=")));
	}
}