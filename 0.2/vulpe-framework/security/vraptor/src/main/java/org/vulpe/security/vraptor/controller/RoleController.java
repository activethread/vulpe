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
package org.vulpe.security.vraptor.controller;

import org.vulpe.controller.annotations.Controller;
import org.vulpe.controller.annotations.Tabular;
import org.vulpe.controller.vraptor.VulpeVRaptorController;
import org.vulpe.security.model.entity.Role;
import org.vulpe.security.model.services.SecurityService;

import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Resource;

@Resource
@Path("/security/Role")
@Controller(serviceClass = SecurityService.class, tabular = @Tabular(startNewRecords = 4, newRecords = 1, despiseFields = {
		"name", "description" }))
@SuppressWarnings("serial")
public class RoleController extends VulpeVRaptorController<Role, Long> {

}