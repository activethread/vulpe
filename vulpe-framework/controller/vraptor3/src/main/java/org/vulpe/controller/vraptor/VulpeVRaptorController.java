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
package org.vulpe.controller.vraptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.vulpe.commons.VulpeConstants.View.Layout;
import org.vulpe.commons.beans.DownloadInfo;
import org.vulpe.commons.util.VulpeFileUtil;
import org.vulpe.controller.AbstractVulpeBaseController;
import org.vulpe.controller.VulpeController;
import org.vulpe.controller.commons.VulpeBaseDetailConfig;
import org.vulpe.controller.commons.VulpeControllerConfig.ControllerType;
import org.vulpe.exception.VulpeSystemException;
import org.vulpe.model.entity.VulpeEntity;

import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.RequestInfo;

/**
 * Vulpe Base Controller to VRaptor
 *
 * @param <ENTITY>
 *            Entity
 * @param <ID>
 *            Identifier
 * @author <a href="mailto:felipe@vulpe.org">Geraldo Felipe</a>
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings( { "unchecked", "serial", "rawtypes" })
public class VulpeVRaptorController<ENTITY extends VulpeEntity<ID>, ID extends Serializable & Comparable<?>>
		extends AbstractVulpeBaseController<ENTITY, ID> implements VulpeController {

	@Autowired
	protected RequestInfo requestInfo;

	@Autowired
	protected Result result;

	@Autowired
	protected Validator validator;

	protected void createDetails(final List<VulpeBaseDetailConfig> details, final boolean subDetail) {
		for (VulpeBaseDetailConfig detail : details) {
			if (subDetail) {
				final Map context = null;
				try {
					final Collection collection = (Collection) Ognl.getValue(vulpe.controller()
							.detail(), context, this);
					for (int i = 0; i < collection.size(); i++) {
						vulpe.controller().detail(
								detail.getParentDetailConfig().getPropertyName() + "[" + i + "]."
										+ detail.getPropertyName());
						onAddDetail(true);
					}
				} catch (OgnlException e) {
					LOG.error(e.getMessage());
				}
			} else if (detail.getParentDetailConfig() == null) {
				vulpe.controller().detail(detail.getPropertyName());
				onAddDetail(true);
			}
			if (detail.getSubDetails() != null && !detail.getSubDetails().isEmpty()) {
				createDetails(detail.getSubDetails(), true);
			}
		}
	}

	/**
	 * Extension point to delete detail items.
	 *
	 * @since 1.0
	 * @return number of items affected
	 */
	protected int onDeleteDetail() {
		final ENTITY entity = prepareEntity(Operation.DELETE);
		final Map context = null;
		try {
			final List<VulpeEntity<?>> details = (List<VulpeEntity<?>>) Ognl.getValue(vulpe
					.controller().detail(), context, this);
			final List<VulpeEntity<?>> removedDetails = new ArrayList<VulpeEntity<?>>();
			final int size = details.size();
			int removed = 0;
			if (StringUtils.isEmpty(vulpe.controller().detailIndex())) {
				for (final Iterator<VulpeEntity<?>> iterator = details.iterator(); iterator
						.hasNext();) {
					final VulpeEntity<?> detail = (VulpeEntity<?>) iterator.next();
					if (detail.isSelected()) {
						if (detail.getId() != null) {
							removedDetails.add(detail);
						}
						iterator.remove();
						++removed;
					}
				}
			} else {
				final Integer detailIndex = Integer.valueOf(vulpe.controller().detailIndex());
				final VulpeEntity<?> detail = details.get(detailIndex.intValue());
				if (detail.getId() != null) {
					removedDetails.add(detail);
				}
				details.remove(detailIndex.intValue());
				++removed;
			}
			boolean save = false;
			for (VulpeEntity<?> baseEntity : removedDetails) {
				if (baseEntity.getId() != null) {
					save = true;
					break;
				}
			}
			if (save) {
				if (vulpe.controller().type().equals(ControllerType.TABULAR)) {
					invokeServices(Operation.DELETE.getValue().concat(
							vulpe.controller().config().getEntityClass().getSimpleName()),
							new Class[] { List.class }, new Object[] { removedDetails });
					if (vulpe.controller().config().getTabularPageSize() > 0) {
						vulpe.controller().tabularSize(
								vulpe.controller().tabularSize() - removedDetails.size());
					}
				} else {
					if (entity.getId() != null && size > details.size()) {
						invokeServices(Operation.UPDATE.getValue().concat(
								vulpe.controller().config().getEntityClass().getSimpleName()),
								new Class[] { vulpe.controller().config().getEntityClass() },
								new Object[] { entity });
						invokeServices(Operation.DELETE.getValue().concat(
								vulpe.controller().config().getEntityClass().getSimpleName()),
								new Class[] { List.class }, new Object[] { removedDetails });
					}
				}
			}
			if (!vulpe.controller().type().equals(ControllerType.TABULAR)) {
				configureDetail();
			}
			vulpe.controller().executed(true);
			return removed;
		} catch (OgnlException e) {
			throw new VulpeSystemException(e);
		}
	}

	/**
	 * Extension point to add detail.
	 *
	 * @since 1.0
	 * @param start
	 *            indicates if use <code>startNewDetails</code> or
	 *            <code>newDetails</code> parameter of Detail Config
	 * @return
	 */
	protected VulpeBaseDetailConfig onAddDetail(final boolean start) {
		boolean createNullObjects = false;
		final Map context = null;
		try {
			int newDetails = 1;
			final VulpeBaseDetailConfig detailConfig = vulpe.controller().config().getDetailConfig(
					vulpe.controller().detail());
			if (detailConfig != null) {
				newDetails = start ? detailConfig.getStartNewDetails() : detailConfig
						.getNewDetails();
			}
			final Collection collection = (Collection) Ognl.getValue(vulpe.controller().detail(),
					context, this);
			if (collection != null && vulpe.controller().type().equals(ControllerType.TABULAR)) {
				vulpe.controller().tabularSize(collection.size());
			}
			for (int i = 0; i < newDetails; i++) {
				doAddDetail(collection);
			}

			if (detailConfig != null) {
				newDetails = detailConfig.getNewDetails();
				final String parentName = vulpe.controller().config().getParentName(
						vulpe.controller().detail());
				final Object parent = Ognl.getValue(parentName, context, this);
				configureDetail();
				if (detailConfig.getParentDetailConfig() != null) {
					vulpe.requestAttribute(detailConfig.getParentDetailConfig().getBaseName()
							.concat(Layout.DETAIL_ITEM), parent);
				}
			}

			if (vulpe.controller().type().equals(ControllerType.TABULAR)) {
				tabularPagingMount(true);
			}
			return detailConfig;
		} catch (Exception e) {
			throw new VulpeSystemException(e);
		} finally {
			if (createNullObjects) {
				// OgnlContextState.setCreatingNullObjects(context, false);
				LOG.info("");
			}
		}
	}

	/**
	 * Method to add detail.
	 *
	 * @param collection
	 * @since 1.0
	 * @throws OgnlException
	 */
	protected void doAddDetail(final Collection collection) throws OgnlException {
		final Map context = null;
		final PropertyAccessor accessor = OgnlRuntime.getPropertyAccessor(collection.getClass());
		final Integer index = Integer.valueOf(collection.size());
		final ENTITY detail = (ENTITY) accessor.getProperty(context, collection, index);
		vulpe.updateAuditInfo(detail);
		final ENTITY preparedDetail = prepareDetail(detail);
		if (!preparedDetail.equals(detail)) {
			accessor.setProperty(context, collection, index, preparedDetail);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vulpe.controller.AbstractVulpeBaseController#doReportLoad()
	 */
	protected DownloadInfo doReportLoad() {
		try {
			// List<VulpeEntity<?>> list = (List<VulpeEntity<?>>)
			// PropertyUtils.getProperty(
			// this, getActionConfig().getReportDataSource());
			// return StringUtils.isNotBlank(getActionConfig().getReportName())
			// ? StrutsReportUtil
			// .getInstance()
			// .getDownloadInfo(list, getActionConfig().getReportFile(),
			// getActionConfig().getSubReports(),
			// getActionConfig().getReportFormat(),
			// getActionConfig().getReportName(),
			// getActionConfig().isReportDownload())
			// : StrutsReportUtil.getInstance().getDownloadInfo(list,
			// getActionConfig().getReportFile(),
			// getActionConfig().getSubReports(),
			// getActionConfig().getReportFormat());
			return null;
		} catch (Exception e) {
			throw new VulpeSystemException(e);
		}
	}

	/**
	 * Extension point to prepare download.
	 *
	 * @since 1.0
	 */
	protected DownloadInfo prepareDownloadInfo() {
		try {
			Object value = null;
			// if (getFormParams() != null &&
			// getFormParams().containsKey(getDownloadKey())) {
			// final Object[] array = (Object[])
			// getFormParams().get(getDownloadKey());
			// value = array[1];
			// }
			if (value == null) {
				// value = ognlUtil.getValue(getDownloadKey(),
				// ActionContext.getContext()
				// .getContextMap(), this);
				LOG.info("");
			}
			final DownloadInfo downloadInfo = VulpeFileUtil.getDownloadInfo(value, vulpe
					.controller().downloadContentType(), vulpe.controller()
					.downloadContentDisposition());
			if (downloadInfo != null) {
				downloadInfo.setKey(vulpe.controller().downloadKey());
			}
			return downloadInfo;
		} catch (Exception e) {
			throw new VulpeSystemException(e);
		}
	}

	public void addActionMessage(final String message) {
		result.include("notice", message);
	}

	public void addActionError(final String message) {
		result.include("notice", message);
	}

	public void addActionError(final String key, final Object... args) {
		result.include("notice", vulpe.controller().text(key));
	}

	@Path("/")
	public void vraptor() {
		if (vulpe.controller().type().equals(ControllerType.FRONTEND)) {
			frontend();
		} else if (vulpe.controller().type().equals(ControllerType.BACKEND)) {
			backend();
		}
	}

	@Override
	protected void despiseDetail(final Object parent, final ENTITY baseEntity,
			VulpeBaseDetailConfig detailConfig) {
	}

	@Override
	protected boolean duplicatedDetail(final Object parent, final ENTITY baseEntity,
			VulpeBaseDetailConfig detailConfig) {
		return false;
	}

	@Override
	protected boolean validateDetails() {
		return false;
	}

	@Override
	public HttpServletRequest getRequest() {
		return requestInfo.getRequest();
	}

	@Override
	public HttpServletResponse getResponse() {
		return requestInfo.getResponse();
	}

	@Override
	public HttpSession getSession() {
		return requestInfo.getRequest().getSession();
	}
}