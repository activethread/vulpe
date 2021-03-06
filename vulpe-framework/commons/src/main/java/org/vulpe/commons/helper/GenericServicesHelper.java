/**
 * Vulpe Framework - Quick and Smart ;)
 * Copyright (C) 2011 Active Thread
 * 
 * Este programa é software livre; você pode redistribuí-lo e/ou
 * modificá-lo sob os termos da Licença Pública Geral GNU, conforme
 * publicada pela Free Software Foundation; tanto a versão 2 da
 * Licença como (a seu critério) qualquer versão mais nova.
 * 
 * Este programa é distribuído na expectativa de ser útil, mas SEM
 * QUALQUER GARANTIA; sem mesmo a garantia implícita de
 * COMERCIALIZAÇÃO ou de ADEQUAÇÃO A QUALQUER PROPÓSITO EM
 * PARTICULAR. Consulte a Licença Pública Geral GNU para obter mais
 * detalhes.
 * 
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa; se não, escreva para a Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/**
 * Vulpe Framework - Quick and Smart ;)
 * Copyright (C) 2011 Active Thread
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.vulpe.commons.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.vulpe.commons.VulpeServiceLocator;
import org.vulpe.commons.factory.SpringBeanFactory;
import org.vulpe.model.services.GenericService;
import org.vulpe.model.services.VulpeService;


public final class GenericServicesHelper {

	private GenericServicesHelper() {
	}

	private static final Logger LOG = LoggerFactory.getLogger(GenericServicesHelper.class);

	/**
	 * Method find generic services and returns POJO or EJB implementation.
	 *
	 * @return Generic VulpeService Implementation.
	 * @since 1.0
	 * @see VulpeService
	 */
	public static GenericService getService() {
		GenericService service = null;
		try {
			service = (GenericService) SpringBeanFactory.getInstance().getBean(
					GenericService.class.getSimpleName());
		} catch (NoSuchBeanDefinitionException e) {
			if (service == null) {
				service = VulpeServiceLocator.getInstance().getEJB(
						GenericService.class);
			}
			LOG.error(e.getMessage());
		}
		return service;
	}

	/**
	 * Method find specific service and returns POJO or EJB implementation.
	 *
	 * @param serviceClass
	 * @return Service Implementation.
	 * @since 1.0
	 * @see VulpeService
	 */
	@SuppressWarnings("unchecked")
	public static <T extends VulpeService> T getService(final Class<T> serviceClass) {
		T service = null;
		try {
			service = (T) SpringBeanFactory.getInstance().getBean(
					serviceClass.getSimpleName());
		} catch (NoSuchBeanDefinitionException e) {
			if (service == null) {
				service = VulpeServiceLocator.getInstance().getEJB(serviceClass);
			}
			LOG.error(e.getMessage());
		}
		return service;
	}
}
