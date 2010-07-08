/**
 * Vulpe Framework - Copyright (c) Active Thread
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vulpe.security.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vulpe.commons.annotations.DetailConfig;
import org.vulpe.commons.annotations.DetailConfig.CardinalityType;
import org.vulpe.controller.annotations.Controller;
import org.vulpe.controller.struts.VulpeStrutsController;
import org.vulpe.security.model.entity.User;
import org.vulpe.security.model.services.SecurityServices;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("security.UserController")
@Controller(serviceClass = SecurityServices.class, detailsConfig = { @DetailConfig(name = "userRoles", propertyName = "entity.userRoles", despiseFields = "role", startNewDetails = 1, cardinalityType = CardinalityType.ONE_OR_MORE) }, pageSize = 5)
@SuppressWarnings("serial")
public class UserController extends VulpeStrutsController<User, Long> {

	@Override
	public String createPost() {
		if ((StringUtils.isNotBlank(getEntity().getPassword()) && StringUtils
				.isNotBlank(getEntity().getPasswordConfirm()))
				&& (!getEntity().getPassword().equals(
						getEntity().getPasswordConfirm()))) {
			return showError("vulpe.security.user.password.not.match");
		}
		setPassword(getEntity().getPassword());
		return super.createPost();
	}

	@Override
	protected void onUpdate() {
		super.onUpdate();
		setPassword(getEntity().getPassword());
	}

	@Override
	protected boolean onUpdatePost() {
		if (StringUtils.isBlank(getEntity().getPassword())
				&& StringUtils.isBlank(getEntity().getPasswordConfirm())) {
			getEntity().setPassword(getPassword());
		} else {
			setPassword(getEntity().getPassword());
		}
		return super.onUpdatePost();
	}

	@Override
	public String updatePost() {
		if ((StringUtils.isNotBlank(getEntity().getPassword()) && StringUtils
				.isNotBlank(getEntity().getPasswordConfirm()))
				&& (!getEntity().getPassword().equals(
						getEntity().getPasswordConfirm()))) {
			return showError("vulpe.security.user.password.not.match");
		}
		return super.updatePost();
	}

	public String getPassword() {
		return (String) getRequest().getAttribute("password");
	}

	public void setPassword(final String password) {
		getRequest().setAttribute("password", password);
	}
}
