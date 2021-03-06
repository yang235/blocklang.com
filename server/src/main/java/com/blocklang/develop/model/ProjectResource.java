package com.blocklang.develop.model;

import java.time.LocalDateTime;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.IconClass;
import com.blocklang.develop.constant.ProjectResourceType;
import com.blocklang.develop.constant.converter.AppTypeConverter;
import com.blocklang.develop.constant.converter.ProjectResourceTypeConverter;
import com.nimbusds.oauth2.sdk.util.StringUtils;

@Entity
@Table(name = "project_resource", 
	uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "resource_key", "resource_type", "app_type", "parent_id" }))
public class ProjectResource extends PartialOperateFields{

	private static final long serialVersionUID = -7591405398132869438L;
	
	/**
	 * 首页
	 */
	public static final String MAIN_KEY = "main";
	public static final String MAIN_NAME = "首页";
	public static final String README_KEY = "README";
	public static final String README_NAME = "README.md";

	@Column(name = "project_id", nullable = false)
	private Integer projectId;
	
	@Column(name = "resource_key", nullable = false, length = 32)
	private String key;
	
	@Column(name = "resource_name", nullable = false, length = 32)
	private String name;
	
	@Column(name = "resource_desc", length = 64)
	private String description;

	@Convert(converter = ProjectResourceTypeConverter.class)
	@Column(name = "resource_type", nullable = false, length = 2)
	private ProjectResourceType resourceType;
	
	@Convert(converter = AppTypeConverter.class)
	@Column(name = "app_type", nullable = false, length = 2)
	private AppType appType;
	
	@Column(name = "parent_id", nullable = false)
	private Integer parentId = Constant.TREE_ROOT_ID;

	@Column(name = "seq", nullable = false)
	private Integer seq;
	
	@Transient
	private String latestCommitId;
	@Transient
	private String latestShortMessage;
	@Transient
	private String latestFullMessage;
	@Transient
	private LocalDateTime latestCommitTime;

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ProjectResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ProjectResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public AppType getAppType() {
		return appType;
	}

	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getLatestCommitId() {
		return latestCommitId;
	}

	public void setLatestCommitId(String latestCommitId) {
		this.latestCommitId = latestCommitId;
	}

	public String getLatestShortMessage() {
		return latestShortMessage;
	}

	public void setLatestShortMessage(String latestShortMessage) {
		this.latestShortMessage = latestShortMessage;
	}

	public String getLatestFullMessage() {
		return latestFullMessage;
	}

	public void setLatestFullMessage(String latestFullMessage) {
		this.latestFullMessage = latestFullMessage;
	}

	public LocalDateTime getLatestCommitTime() {
		return latestCommitTime;
	}

	public void setLatestCommitTime(LocalDateTime latestCommitTime) {
		this.latestCommitTime = latestCommitTime;
	}

	public Boolean isMain() {
		// 约定根目录下的 main 模块为入口模块
		return Constant.TREE_ROOT_ID.equals(parentId) && isProgram() && MAIN_KEY.equals(key);
	}

	public Boolean isTemplet() {
		return ProjectResourceType.PROGRAM_TEMPLET.equals(this.resourceType);
	}

	public Boolean isFunction() {
		return ProjectResourceType.FUNCTION.equals(this.resourceType);
	}

	public Boolean isProgram() {
		return ProjectResourceType.PROGRAM.equals(this.resourceType);
	}

	public Boolean isReadme() {
		return isFile() && README_KEY.equals(this.key);
	}

	public Boolean isService() {
		return ProjectResourceType.SERVICE.equals(this.resourceType);
	}
	
	public Boolean isFile() {
		return ProjectResourceType.FILE.equals(this.resourceType);
	}

	public String getIcon() {
		if(isProgram()) {
			if(isMain()) {
				return IconClass.HOME;
			}
			return AppType.getIcon(this.appType);
		}
		
		if(isFunction()) {
			return IconClass.FUNCTION;
		}
		if(isTemplet()) {
			return IconClass.TEMPLET;
		}
		if(isReadme()) {
			return IconClass.README;
		}
		if(isService()) {
			return IconClass.SERVICE;
		}
		return null;

	}

	@Transient
	private MessageSource messageSource;
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public String getTitle() {
		Assert.notNull(this.messageSource, "不能为空");

		String i18nKey = "";
		if(isProgram()) {
			if(isMain()) {
				i18nKey = "resourceTitle.main";
			}else {
				i18nKey = "resourceTitle.program";
			}
		} else if(isFunction()) {
			i18nKey = "resourceTitle.function";
		} else if(isTemplet()) {
			i18nKey = "resourceTitle.templet";
		} else if(isReadme()) {
			i18nKey = "resourceTitle.readme";
		}else if(isService()) {
			i18nKey = "resourceTitle.service";
		}
		
		if(StringUtils.isBlank(i18nKey)) {
			return "";
		}
		
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(i18nKey, null, locale);
	}

	public String getFileName() {
		if(isProgram()) {
			return this.key + ".page.json";
		}
		if(isTemplet()) {
			return this.key + ".page.tmpl.json";
		}
		if(isFile()) {
			return this.name;
		}
		if(isService()) {
			return this.key + ".api.json";
		}
		
		return "";
	}
	
}
