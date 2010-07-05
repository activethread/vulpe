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
package org.vulpe.controller.vraptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.vulpe.commons.VulpeConstants.Action;
import org.vulpe.commons.VulpeConstants.Error;
import org.vulpe.commons.VulpeConstants.Action.Button;
import org.vulpe.commons.VulpeConstants.Action.Forward;
import org.vulpe.commons.VulpeConstants.Action.Validate.Cardinality;
import org.vulpe.commons.VulpeConstants.View.Layout;
import org.vulpe.commons.beans.DownloadInfo;
import org.vulpe.commons.beans.Paging;
import org.vulpe.controller.VulpeBaseController;
import org.vulpe.controller.annotations.ResetSession;
import org.vulpe.controller.commons.VulpeControllerConfig.ControllerType;
import org.vulpe.controller.commons.DuplicatedBean;
import org.vulpe.controller.commons.VulpeBaseControllerConfig;
import org.vulpe.controller.commons.VulpeBaseDetailConfig;
import org.vulpe.controller.validator.EntityValidator;
import org.vulpe.exception.VulpeSystemException;
import org.vulpe.model.annotations.CachedClass;
import org.vulpe.model.entity.VulpeBaseEntity;

/**
 * Vulpe Base Action to VRaptor
 * 
 * @param <ENTITY>
 *            Entity
 * @param <ID>
 *            Identifier
 * @author <a href="mailto:felipe.matos@activethread.com.br">Felipe Matos</a>
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
public class VulpeVRaptorController<ENTITY extends VulpeBaseEntity<ID>, ID extends Serializable & Comparable>
		extends AbstractVulpeVRaptorSimpleController implements VulpeBaseController {

	/**
	 * List of entities
	 */
	private List<ENTITY> entities;
	/**
	 * Current Entity
	 */
	private ENTITY entity;
	/**
	 * Identifier for selections
	 */
	private ID id;
	/**
	 * List of selected identifiers
	 */
	private List<ID> selected;

	/**
	 * Paginated Bean
	 */
	private Paging<ENTITY> paging;

	/**
	 * If true, define as read only
	 */
	private boolean onlyToSee = false;

	/**
	 * Selected tab index
	 */
	private int selectedTab;

	public ID getId() {
		return id;
	}

	public void setId(final ID id) {
		this.id = id;
	}

	public List<ID> getSelected() {
		return selected;
	}

	public void setSelected(final List<ID> selected) {
		this.selected = selected;
	}

	public List<ENTITY> getEntities() {
		return entities;
	}

	public void setEntities(final List<ENTITY> entities) {
		this.entities = entities;
	}

	public ENTITY getEntity() {
		return entity;
	}

	public void setEntity(final ENTITY entity) {
		this.entity = entity;
	}

	public Paging<ENTITY> getPaging() {
		return paging;
	}

	public void setPaging(final Paging<ENTITY> paging) {
		this.paging = paging;
	}

	public boolean isOnlyToSee() {
		return onlyToSee;
	}

	public void setOnlyToSee(final boolean onlyToSee) {
		this.onlyToSee = onlyToSee;
	}

	/**
	 * Returns current action configuration.
	 * 
	 * @since 1.0
	 * @return ActionConfig object for current action.
	 */
	public VulpeBaseControllerConfig<ENTITY, ID> getControllerConfig() {
		return getControllerUtil().getControllerConfig(this);
	}

	/**
	 * Returns current detail configuration.
	 * 
	 * @since 1.0
	 * @return
	 */
	public VulpeBaseDetailConfig getDetailConfig() {
		return getControllerConfig().getDetailConfig(getDetail());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#create()
	 */
	@ResetSession(before = true)
	public String create() {
		setOperation(Action.CREATE);
		createBefore();
		onCreate();
		setSelectedTab(0);
		showButtons(Action.CREATE);

		setResultName(Forward.SUCCESS);
		if (getControllerType().equals(ControllerType.SELECT)) {
			setResultName(Forward.CREATE);
			setResultForward(getControllerConfig().getOwnerController().concat(
					Action.URI.CREATE_AJAX));
		} else {
			controlResultForward();
		}

		createAfter();
		return getResultName();
	}

	/**
	 * Extension point to create record.
	 * 
	 * @since 1.0
	 */
	protected void onCreate() {
		if (getControllerType().equals(ControllerType.CRUD)) {
			try {
				setEntity(getControllerConfig().getEntityClass().newInstance());
				if (getControllerConfig().getDetails() != null
						&& !getControllerConfig().getDetails().isEmpty()) {
					for (VulpeBaseDetailConfig detail : getControllerConfig().getDetails()) {
						setDetail(detail.getPropertyName());
						onAddDetail();
					}
				}
			} catch (Exception e) {
				throw new VulpeSystemException(e);
			}
			setExecuted(false);
		}
	}

	/**
	 * Extension point to code before create.
	 * 
	 * @since 1.0
	 */
	protected void createBefore() {
		if (!getControllerType().equals(ControllerType.SELECT)
				&& !getControllerType().equals(ControllerType.CRUD)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after create.
	 * 
	 * @since 1.0
	 */
	protected void createAfter() {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#createPost()
	 */
	@ResetSession
	public String createPost() {
		setOperation(Action.CREATE_POST);
		createPostBefore();
		controlResultForward();
		if (!validateEntity()) {
			showButtons(Action.CREATE);
			return Forward.SUCCESS;
		} else if (!validateDetails()) {
			showButtons(Action.CREATE);
			return Forward.SUCCESS;
		}
		showButtons(Action.UPDATE);
		final ENTITY entity = onCreatePost();
		addActionMessage(getText("vulpe.msg.create.post"));

		setResultName(Forward.SUCCESS);

		if (entity.getClass().isAnnotationPresent(CachedClass.class)) {
			final String entityName = entity.getClass().getSimpleName();
			List<ENTITY> list = (List<ENTITY>) getCachedClass().get(entityName);
			if (list == null) {
				list = new ArrayList<ENTITY>();
			}
			list.add(entity);
			getCachedClass().put(entityName, list);
		}
		createPostAfter(entity);
		return getResultName();
	}

	/**
	 * Extension point to code in confirm create.
	 * 
	 * @since 1.0
	 * @return Entity created.
	 */
	protected ENTITY onCreatePost() {
		despiseDetails();

		final ENTITY entity = prepareEntity(Action.CREATE_POST);

		final ENTITY persistentEntity = (ENTITY) invokeServices(Action.CREATE_POST,
				Action.CREATE.concat(getControllerConfig().getEntityClass().getSimpleName()),
				new Class[] { getControllerConfig().getEntityClass() }, new Object[] { entity });
		setEntity(persistentEntity);
		setExecuted(true);
		return entity;
	}

	/**
	 * Extension point to conde before confirm create.
	 * 
	 * @since 1.0
	 */
	protected void createPostBefore() {
		if (!getControllerType().equals(ControllerType.CRUD)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after confirm create.
	 * 
	 * @since 1.0
	 * @param entity
	 *            Entity to create.
	 */
	protected void createPostAfter(final ENTITY entity) {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#update()
	 */
	@ResetSession(before = true)
	public String update() {
		setOperation(Action.UPDATE);
		updateBefore();
		onUpdate();
		showButtons(Action.UPDATE);

		setResultName(Forward.SUCCESS);
		if (getControllerType().equals(ControllerType.SELECT)) {
			setResultForward(getControllerConfig().getOwnerController().concat(
					Action.URI.UPDATE_AJAX));
			setResultName(Forward.UPDATE);
		} else {
			controlResultForward();
		}

		updateAfter();
		return getResultName();
	}

	/**
	 * Make visualization read only.
	 * 
	 * @since 1.0
	 * @return
	 */
	@ResetSession(before = true)
	public String view() {
		setOnlyToSee(true);
		final String update = update();
		showButtons(Action.VIEW);
		return update;
	}

	/**
	 * Extension point to prepare update.
	 * 
	 * @since 1.0
	 */
	protected void onUpdate() {
		if (getControllerType().equals(ControllerType.CRUD)) {
			final ENTITY entity = prepareEntity(Action.UPDATE);
			final ENTITY persistentEntity = (ENTITY) invokeServices(Action.UPDATE,
					Action.FIND.concat(getControllerConfig().getEntityClass().getSimpleName()),
					new Class[] { getControllerConfig().getIdClass() },
					new Object[] { entity.getId() });
			setEntity(persistentEntity);
			setExecuted(false);
		}
	}

	/**
	 * Extension point to code before update.
	 * 
	 * @since 1.0
	 */
	protected void updateBefore() {
		if (!getControllerType().equals(ControllerType.SELECT)
				&& !getControllerType().equals(ControllerType.CRUD)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after update.
	 * 
	 * @since 1.0
	 */
	protected void updateAfter() {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#updatePost()
	 */
	@ResetSession
	public String updatePost() {
		setOperation(Action.UPDATE_POST);
		updatePostBefore();
		controlResultForward();
		showButtons(Action.UPDATE);
		if (!validateDetails()) {
			return Forward.SUCCESS;
		}
		onUpdatePost();
		addActionMessage(getText("vulpe.msg.update.post"));

		setResultName(Forward.SUCCESS);
		if (entity.getClass().isAnnotationPresent(CachedClass.class)) {
			final String entityName = entity.getClass().getSimpleName();
			List<ENTITY> list = (List<ENTITY>) getCachedClass().get(entityName);
			if (list == null || list.isEmpty()) {
				list = new ArrayList<ENTITY>();
				list.add(entity);
			} else {
				int count = 0;
				for (ENTITY baseEntity : list) {
					if (baseEntity.getId().equals(entity.getId())) {
						list.set(count, entity);
						break;
					}
					count++;
				}
			}
			getCachedClass().put(entityName, list);
		}
		updatePostAfter();
		return getResultName();
	}

	/**
	 * Extension point prepare confirm update.
	 * 
	 * @since 1.0
	 */
	protected void onUpdatePost() {
		despiseDetails();

		final ENTITY entity = prepareEntity(Action.UPDATE_POST);

		invokeServices(Action.UPDATE_POST,
				Action.UPDATE.concat(getControllerConfig().getEntityClass().getSimpleName()),
				new Class[] { getControllerConfig().getEntityClass() }, new Object[] { entity });

		setExecuted(true);
	}

	/**
	 * Extension point to code before confirm update.
	 * 
	 * @since 1.0
	 */
	protected void updatePostBefore() {
		if (!getControllerType().equals(ControllerType.CRUD)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after confirm update.
	 * 
	 * @since 1.0
	 */
	protected void updatePostAfter() {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#delete()
	 */
	public String delete() {
		setOperation(Action.DELETE);
		deleteBefore();
		onDelete();
		showButtons(Action.DELETE);
		addActionMessage(getText("vulpe.msg.delete"));

		if (getControllerType().equals(ControllerType.SELECT)) {
			setResultForward(getControllerConfig().getControllerName().concat(Action.URI.READ));
			setResultName(Forward.READ);
		} else {
			setResultForward(getControllerConfig().getOwnerController().concat(Action.URI.READ));
			setResultName(Forward.READ);
		}

		deleteAfter();
		return getResultName();
	}

	/**
	 * Extension point to delete.
	 * 
	 * @since 1.0
	 */
	protected void onDelete() {
		final ENTITY entity = prepareEntity(Action.DELETE);
		final List<ENTITY> entities = new ArrayList<ENTITY>();
		if (getSelected() != null && !getSelected().isEmpty()) {
			for (ID id : getSelected()) {
				try {
					final ENTITY newEntity = getControllerConfig().getEntityClass().newInstance();
					newEntity.setId(id);
					entities.add(newEntity);
				} catch (Exception e) {
					throw new VulpeSystemException(e);
				}
			}
		}
		invokeServices(Action.DELETE, Action.DELETE.concat(getControllerConfig().getEntityClass()
				.getSimpleName()), new Class[] { entities.isEmpty() ? getControllerConfig()
				.getEntityClass() : List.class }, new Object[] { entities.isEmpty() ? entity
				: entities });

		setExecuted(true);
	}

	/**
	 * Extension point to code before delete.
	 * 
	 * @since
	 */
	protected void deleteBefore() {
		if (!getControllerType().equals(ControllerType.SELECT)
				&& !getControllerType().equals(ControllerType.CRUD)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after delete.
	 * 
	 * @since 1.0
	 */
	protected void deleteAfter() {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#deleteDetail()
	 */
	public String deleteDetail() {
		setOperation(Action.UPDATE_POST);
		deleteDetailBefore();
		final int size = onDeleteDetail();
		showButtons(Action.UPDATE);
		addActionMessage(getText(size > 1 ? "vulpe.msg.delete.details" : "vulpe.msg.delete.detail"));
		setResultName(Forward.SUCCESS);
		if (isAjax()) {
			final VulpeBaseDetailConfig detailConfig = getControllerConfig().getDetailConfig(
					getDetail());
			if (detailConfig == null || StringUtils.isBlank(detailConfig.getViewPath())) {
				controlResultForward();
			} else {
				setResultForward(detailConfig.getViewPath());
			}
		} else {
			controlResultForward();
		}
		deleteDetailAfter();
		return getResultName();
	}

	/**
	 * Extension point to delete detail items.
	 * 
	 * @since 1.0
	 */
	protected int onDeleteDetail() {
		final ENTITY entity = prepareEntity(Action.DELETE);
		// final Map context = ActionContext.getContext().getContextMap();
		final Map context = null;
		try {
			final List<VulpeBaseEntity<?>> details = (List<VulpeBaseEntity<?>>) Ognl.getValue(
					getDetail(), context, this);
			final List<VulpeBaseEntity<?>> removedDetails = new ArrayList<VulpeBaseEntity<?>>();
			final int size = details.size();
			int removed = 0;
			if (getDetailIndex() == null) {
				for (final Iterator<VulpeBaseEntity<?>> iterator = details.iterator(); iterator
						.hasNext();) {
					final VulpeBaseEntity<?> detail = (VulpeBaseEntity<?>) iterator.next();
					if (detail.isSelected()) {
						if (detail.getId() != null) {
							removedDetails.add(detail);
						}
						iterator.remove();
						removed++;
					}
				}
			} else {
				final VulpeBaseEntity<?> detail = details.get(getDetailIndex().intValue());
				if (detail.getId() != null) {
					removedDetails.add(detail);
				}
				details.remove(getDetailIndex().intValue());
				removed++;
			}
			boolean save = false;
			for (VulpeBaseEntity<?> baseEntity : removedDetails) {
				if (baseEntity.getId() != null) {
					save = true;
					break;
				}
			}
			if (save) {
				if (getControllerType().equals(ControllerType.TABULAR)) {
					invokeServices(Action.DELETE, Action.DELETE.concat(getControllerConfig()
							.getEntityClass().getSimpleName()), new Class[] { List.class },
							new Object[] { removedDetails });
				} else {
					if (entity.getId() != null && size > details.size()) {
						invokeServices(Action.UPDATE_POST,
								Action.UPDATE.concat(getControllerConfig().getEntityClass()
										.getSimpleName()), new Class[] { getControllerConfig()
										.getEntityClass() }, new Object[] { entity });
						invokeServices(Action.DELETE, Action.DELETE.concat(getControllerConfig()
								.getEntityClass().getSimpleName()), new Class[] { List.class },
								new Object[] { removedDetails });
					}
				}
			}
			if (!getControllerType().equals(ControllerType.TABULAR)) {
				configureDetail();
			}
			setExecuted(true);
			return removed;
		} catch (OgnlException e) {
			throw new VulpeSystemException(e);
		}
	}

	/**
	 * Extension point to code before delete detail items.
	 * 
	 * @since 1.0
	 */
	protected void deleteDetailBefore() {
		if (!getControllerType().equals(ControllerType.SELECT)
				&& !getControllerType().equals(ControllerType.CRUD)
				&& !getControllerType().equals(ControllerType.TABULAR)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after delete detail items.
	 * 
	 * @since 1.0
	 */
	protected void deleteDetailAfter() {
		// extension point
	}

	/**
	 * Configure detail to view.
	 */
	protected void configureDetail() {
		getRequest().setAttribute(Layout.TARGET_CONFIG, getDetailConfig());
		getRequest().setAttribute(Layout.TARGET_CONFIG_PROPERTY_NAME, getDetail());
	}

	/**
	 * 
	 * @return
	 */
	public String json() {
		if (getEntities() == null || getEntities().isEmpty()) {
			read();
		}
		JSONArray jsonArray = new JSONArray(getEntities());
		getRequest().setAttribute("JSON", jsonArray.toString());
		return Forward.JSON;
	}

	/**
	 * 
	 * @return
	 */
	public String autocomplete() {
		if (getEntities() == null || getEntities().isEmpty()) {
			read();
		}
		JSONArray jsonArray = new JSONArray(getEntities());
		getRequest().setAttribute("JSON", jsonArray.toString());
		return Forward.JSON;
	}

	/**
	 * Method to read record.
	 * 
	 * @since 1.0
	 * @return Navigation.
	 */
	@ResetSession
	public String read() {
		setOperation(Action.READ);
		readBefore();
		onRead();
		showButtons(Action.READ);

		setResultName(Forward.SUCCESS);
		if (getControllerType().equals(ControllerType.SELECT)) {
			if (isBack()) {
				controlResultForward();
			} else if (isAjax()) {
				setResultForward(getControllerConfig().getViewItemsPath());
			} else {
				controlResultForward();
			}
		} else if (getControllerType().equals(ControllerType.REPORT)) {
			setResultName(Forward.REPORT);
			if (isBack()) {
				controlResultForward();
			} else if (isAjax()) {
				setResultForward(getControllerConfig().getViewItemsPath());
			} else {
				controlResultForward();
			}
		} else {
			controlResultForward();
		}

		readAfter();
		return getResultName();
	}

	/**
	 * Extension point to read record.
	 * 
	 * @since 1.0
	 */
	protected void onRead() {
		if (isBack() && !isExecuted()) {
			return;
		}

		final ENTITY entity = prepareEntity(Action.READ);
		if (getControllerType().equals(ControllerType.SELECT)
				&& getControllerConfig().getPageSize() > 0) {
			final Integer page = getPaging() == null || getPaging().getPage() == null ? 1
					: getPaging().getPage();
			final Paging<ENTITY> paging = (Paging<ENTITY>) invokeServices(Action.READ,
					Action.PAGING.concat(getControllerConfig().getEntityClass().getSimpleName()),
					new Class[] { getControllerConfig().getEntityClass(), Integer.class,
							Integer.class }, new Object[] { entity,
							getControllerConfig().getPageSize(), page });
			setPaging(paging);
			setEntities(paging.getList());
		} else {
			final List<ENTITY> list = (List<ENTITY>) invokeServices(Action.READ,
					Action.READ.concat(getControllerConfig().getEntityClass().getSimpleName()),
					new Class[] { getControllerConfig().getEntityClass() }, new Object[] { entity });
			setEntities(list);

			if (getControllerType().equals(ControllerType.REPORT)) {
				final DownloadInfo downloadInfo = doReadReportLoad();
				setDownloadInfo(downloadInfo);
			}
		}
		setExecuted(true);
	}

	/**
	 * Extension point to code before read.
	 * 
	 * @since 1.0
	 */
	protected void readBefore() {
		if (!getControllerType().equals(ControllerType.SELECT)
				&& !getControllerType().equals(ControllerType.TABULAR)
				&& !getControllerType().equals(ControllerType.REPORT)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after read.
	 * 
	 * @since
	 */
	protected void readAfter() {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#tabularPost()
	 */
	@ResetSession
	public String tabularPost() {
		if (getEntities() != null) {
			setOperation(Action.TABULAR_POST);
			tabularPostBefore();
			controlResultForward();
			showButtons(Action.TABULAR_POST);
			if (!validateDetails()) {
				return Forward.SUCCESS;
			}
			onTabularPost();
			addActionMessage(getText("vulpe.msg.tabular.post"));
			if (!getEntities().isEmpty()) {
				final ENTITY entityTabular = getEntities().get(0);
				if (entityTabular.getClass().isAnnotationPresent(CachedClass.class)) {
					final String entityName = entityTabular.getClass().getSimpleName();
					getCachedClass().put(entityName, getEntities());
				}
			}
		}
		setResultName(Forward.SUCCESS);
		tabularPostAfter();
		return getResultName();
	}

	/**
	 * Extension point to logic tabulate.
	 * 
	 * @since 1.0
	 */
	protected void onTabularPost() {
		despiseDetails();

		final List<ENTITY> list = (List<ENTITY>) invokeServices(Action.TABULAR_POST,
				Action.PERSIST.concat(getControllerConfig().getEntityClass().getSimpleName()),
				new Class[] { List.class }, new Object[] { getEntities() });
		setEntities(list);

		setExecuted(true);
	}

	/**
	 * Extension point to code before logic tabulate.
	 * 
	 * @since 1.0
	 */
	protected void tabularPostBefore() {
		if (!getControllerType().equals(ControllerType.TABULAR)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after logic tabulate.
	 * 
	 * @since 1.0
	 */
	protected void tabularPostAfter() {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#addDetail()
	 */
	public String addDetail() {
		addDetailBefore();
		final VulpeBaseDetailConfig detailConfig = onAddDetail();
		showButtons(Action.ADD_DETAIL);

		setResultName(Forward.SUCCESS);
		if (isAjax()) {
			if (detailConfig == null || detailConfig.getViewPath() == null) {
				controlResultForward();
			} else {
				setResultForward(detailConfig.getViewPath());
			}
		} else {
			controlResultForward();
		}
		addDetailAfter();
		return getResultName();
	}

	/**
	 * Extension point to add detail.
	 * 
	 * @since 1.0
	 */
	protected VulpeBaseDetailConfig onAddDetail() {
		boolean createNullObjects = false;
		// final Map context = ActionContext.getContext().getContextMap();
		final Map context = null;
		try {
			// if (!OgnlContextState.isCreatingNullObjects(context)) {
			// OgnlContextState.setCreatingNullObjects(context, true);
			// createNullObjects = true;
			// }

			int newDetails = 1;
			final VulpeBaseDetailConfig detailConfig = getControllerConfig().getDetailConfig(
					getDetail());
			if (detailConfig != null) {
				newDetails = detailConfig.getNewDetails();
			}
			final Collection collection = (Collection) Ognl.getValue(getDetail(), context, this);
			for (int i = 0; i < newDetails; i++) {
				doAddDetail(collection);
			}

			if (detailConfig != null) {
				newDetails = detailConfig.getNewDetails();
				final String parentName = getControllerConfig().getParentName(getDetail());
				final Object parent = Ognl.getValue(parentName, context, this);
				configureDetail();
				if (detailConfig.getParentDetailConfig() != null) {
					getRequest().setAttribute(
							detailConfig.getParentDetailConfig().getBaseName()
									.concat(Layout.DETAIL_ITEM), parent);
				}
			}

			return detailConfig;
		} catch (Exception e) {
			throw new VulpeSystemException(e);
		} finally {
			if (createNullObjects) {
				// OgnlContextState.setCreatingNullObjects(context, false);
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
		// final Map context = ActionContext.getContext().getContextMap();
		final Map context = null;
		final PropertyAccessor accessor = OgnlRuntime.getPropertyAccessor(collection.getClass());
		final Integer index = Integer.valueOf(collection.size());
		final Object detail = accessor.getProperty(context, collection, index);
		final Object preparedDetail = prepareDetail(detail);
		if (!preparedDetail.equals(detail)) {
			accessor.setProperty(context, collection, index, preparedDetail);
		}
	}

	/**
	 * Extension point to prepare detail.
	 * 
	 * @param detail
	 *            Detail.
	 * @since 1.0
	 */
	protected Object prepareDetail(final Object detail) {
		return detail;
	}

	/**
	 * Extension point to code before add detail.
	 * 
	 * @since 1.0
	 */
	protected void addDetailBefore() {
		if (!getControllerType().equals(ControllerType.CRUD)
				&& !getControllerType().equals(ControllerType.TABULAR)) {
			throw new VulpeSystemException(Error.CONTROLLER);
		}
	}

	/**
	 * Extension point to code after add detail.
	 * 
	 * @since 1.0
	 */
	protected void addDetailAfter() {
		// extension point
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseController#prepare()
	 */
	@ResetSession(before = true)
	public String prepare() {
		prepareBefore();
		onPrepare();
		showButtons(Action.PREPARE);

		setResultName(Forward.SUCCESS);
		if (getControllerType().equals(ControllerType.SELECT)
				|| getControllerType().equals(ControllerType.REPORT)) {
			controlResultForward();
		} else if (getControllerType().equals(ControllerType.CRUD)) {
			if (isAjax()) {
				setResultForward(getControllerConfig().getOwnerController().concat(
						Action.URI.READ_AJAX));
				setBack(true);
				setResultName(Forward.READ);
			} else {
				controlResultForward();
			}
		} else if (getControllerType().equals(ControllerType.TABULAR)) {
			if (isAjax()) {
				setResultForward(getControllerConfig().getControllerName().concat(
						Action.URI.READ_AJAX));
				setResultName(Forward.READ);
			} else {
				controlResultForward();
			}
		} else {
			controlResultForward();
		}

		prepareAfter();
		return getResultName();
	}

	/**
	 * Extension point to prepare show.
	 * 
	 * @since 1.0
	 */
	protected void onPrepare() {
		setEntities(null);
		try {
			setEntity(getControllerConfig().getEntityClass().newInstance());
		} catch (Exception e) {
			setEntity(null);
		}
		setPaging(null);
		setExecuted(false);
	}

	/**
	 * Extension point to prepare entity.
	 * 
	 * @since 1.0
	 */
	protected ENTITY prepareEntity(final String method) {
		try {
			if (Action.READ.equals(method) && getEntity() == null) {
				setEntity(getControllerConfig().getEntityClass().newInstance());
			} else if (Action.UPDATE.equals(method)
					|| (Action.DELETE.equals(method) && getControllerType().equals(
							ControllerType.SELECT))) {
				final ENTITY entity = getControllerConfig().getEntityClass().newInstance();
				entity.setId(getId());
				return entity;
			}
		} catch (Exception e) {
			throw new VulpeSystemException(e);
		}
		return getEntity();
	}

	/**
	 * Extension point to code before prepare.
	 */
	protected void prepareBefore() {
		// extension point
	}

	/**
	 * Extension point to code after prepare.
	 */
	protected void prepareAfter() {
		// extension point
	}

	/**
	 * Method to remove detail despised.
	 * 
	 * @since 1.0
	 */
	protected void despiseDetails() {
		for (VulpeBaseDetailConfig detail : getControllerConfig().getDetails()) {
			if (detail.getParentDetailConfig() == null) {
				despiseDetail(this, detail);
			}
		}
	}

	/**
	 * Method to validate detail.
	 * 
	 * @since 1.0
	 */
	protected boolean validateDetails() {
		// final Map context = ActionContext.getContext().getContextMap();
		final Map context = null;
		for (VulpeBaseDetailConfig detailConfig : getControllerConfig().getDetails()) {
			if (detailConfig.getParentDetailConfig() == null) {
				try {
					final Collection<VulpeBaseEntity<?>> beans = (Collection) Ognl.getValue(
							detailConfig.getPropertyName(), context, this);
					if (!validateCardinality(beans, detailConfig)) {
						return false;
					}
					if (beans != null && beans.size() > 1
							&& !validateDuplicatedDetailItens(beans, detailConfig)) {
						return false;
					}
				} catch (OgnlException e) {
					throw new VulpeSystemException(e);
				}
			}
		}
		return true;
	}

	/**
	 * Method to remove detail despised.
	 * 
	 * @param parent
	 *            Parent
	 * @param detailConfig
	 *            Configuration of detail.
	 * 
	 * @since 1.0
	 */
	protected void despiseDetail(final Object parent, final VulpeBaseDetailConfig detailConfig) {
		// final Map context = ActionContext.getContext().getContextMap();
		final Map context = null;
		try {
			final Collection<VulpeBaseEntity<?>> beans = (Collection) Ognl.getValue(
					detailConfig.getPropertyName(), context, parent);
			despiseDetailItens(beans, detailConfig);
			if (beans != null && !detailConfig.getSubDetails().isEmpty()) {
				for (VulpeBaseEntity<?> bean : beans) {
					for (VulpeBaseDetailConfig subDetailConfig : detailConfig.getSubDetails()) {
						despiseDetail(bean, subDetailConfig);
					}
				}
			}
		} catch (OgnlException e) {
			throw new VulpeSystemException(e);
		}
	}

	/**
	 * Method to remove detail despised.
	 * 
	 * @param beans
	 *            details
	 * @param detailConfig
	 *            Configuration of detail.
	 * 
	 * @since 1.0
	 */
	protected void despiseDetailItens(final Collection<VulpeBaseEntity<?>> beans,
			final VulpeBaseDetailConfig detailConfig) {
		getControllerUtil().despiseItens(beans, detailConfig.getDespiseFields(),
				getControllerType().equals(ControllerType.TABULAR));
	}

	/**
	 * Method to validate duplicated details.
	 * 
	 * @param beans
	 * @param detailConfig
	 * @return
	 */
	protected boolean validateDuplicatedDetailItens(final Collection<VulpeBaseEntity<?>> beans,
			final VulpeBaseDetailConfig detailConfig) {
		final String[] despiseFields = detailConfig.getDespiseFields();
		final Collection<DuplicatedBean> duplicatedBeans = getControllerUtil().duplicatedItens(
				beans, despiseFields);
		if (duplicatedBeans != null && !duplicatedBeans.isEmpty()) {
			if (getControllerType().equals(ControllerType.TABULAR) && duplicatedBeans.size() == 1) {
				return true;
			}
			final StringBuilder lines = new StringBuilder();
			int count = 1;
			for (DuplicatedBean duplicatedBean : duplicatedBeans) {
				if (duplicatedBeans.size() > 1 && duplicatedBeans.size() == count) {
					lines.append(" " + getText("vulpe.label.and") + " " + duplicatedBean.getLine());
				} else {
					lines.append(StringUtils.isBlank(lines.toString()) ? String
							.valueOf(duplicatedBean.getLine()) : ", " + duplicatedBean.getLine());
				}
				count++;
			}
			if (getControllerType().equals(ControllerType.TABULAR)) {
				addActionError("vulpe.error.tabular.duplicated", lines.toString());
			} else {
				addActionError("vulpe.error.details.duplicated",
						getText(detailConfig.getTitleKey()), lines.toString());
			}
			return false;
		}
		return true;
	}

	/**
	 * Method to validate cardinality on details.
	 * 
	 * @param beans
	 * @param detailConfig
	 * @return
	 */
	protected boolean validateCardinality(final Collection<VulpeBaseEntity<?>> beans,
			final VulpeBaseDetailConfig detailConfig) {
		if (!Cardinality.ZERO.equals(detailConfig.getCardinalityType().getValue())) {
			if (Cardinality.ONE.equals(detailConfig.getCardinalityType().getValue())) {
				if (beans == null || beans.size() == 0) {
					addActionError("vulpe.error.details.cardinality.one.less",
							getText(detailConfig.getTitleKey()));
					return false;
				} else if (beans.size() > 1) {
					addActionError("vulpe.error.details.cardinality.one.only",
							getText(detailConfig.getTitleKey()));
				}
			} else if (Cardinality.ONE_OR_MORE.equals(detailConfig.getCardinalityType().getValue())) {
				if (beans == null || beans.size() == 0) {
					addActionError("vulpe.error.details.cardinality.one.more",
							getText(detailConfig.getTitleKey()));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Method to show buttons and configure forward.
	 * 
	 * @since 1.0
	 */
	protected void showButtons(final String method) {
		if (getControllerType().equals(ControllerType.CRUD)) {
			if (getControllerConfig().getDetails() != null) {
				for (VulpeBaseDetailConfig detail : getControllerConfig().getDetails()) {
					if (Action.VIEW.equals(method)) {
						addDetailHide(detail.getBaseName());
						deleteHide(detail.getBaseName());
					} else {
						addDetailShow(detail.getBaseName());
						deleteDetailShow(detail.getBaseName());
					}
				}
			}
			if ((Action.CREATE.equals(method) || Action.PREPARE.equals(method))
					|| ((Action.CREATE.equals(getOperation()) || Action.CREATE_POST
							.equals(getOperation())) && Action.ADD_DETAIL.equals(method))) {
				showButtons(Button.PREPARE, Button.CREATE_POST, Button.CLEAR);
			} else if (Action.UPDATE.equals(method)
					|| ((Action.UPDATE.equals(getOperation()) || Action.UPDATE_POST
							.equals(getOperation())) && Action.ADD_DETAIL.equals(method))) {
				showButtons(Button.PREPARE, Button.CREATE, Button.UPDATE_POST, Button.DELETE,
						Button.CLEAR);
			} else if (Action.VIEW.equals(method)) {
				showButtons();
			}
		} else if (getControllerType().equals(ControllerType.SELECT)) {
			if (getControllerConfig().getController().showReport()) {
				showButtons(Button.READ, Button.REPORT, Button.PREPARE, Button.CREATE,
						Button.UPDATE, Button.DELETE);
			} else {
				showButtons(Button.READ, Button.PREPARE, Button.CREATE, Button.UPDATE,
						Button.DELETE);

			}
			if (isPopup()) {
				hideButtons(Button.CREATE, Button.UPDATE, Button.DELETE);
			}
		} else if (getControllerType().equals(ControllerType.REPORT)) {
			showButtons(Button.READ, Button.CLEAR);
		} else if (getControllerType().equals(ControllerType.TABULAR)) {
			showButtons(Button.READ, Button.PREPARE, Button.DELETE, Button.TABULAR_POST,
					Button.ADD_DETAIL);
		}
	}

	/**
	 * Detail
	 */
	private String detail;
	/**
	 * Detail index to delete
	 */
	private Integer detailIndex;

	private boolean createShow = false;
	private boolean createPostShow = false;
	private boolean updateShow = false;
	private boolean updatePostShow = false;
	private boolean deleteShow = false;
	private boolean readShow = false;
	private boolean reportShow = false;
	private boolean prepareShow = false;
	private boolean tabularPostShow = false;
	private boolean clearShow = false;

	public boolean isClearShow() {
		return clearShow;
	}

	public boolean isCreateShow() {
		return createShow;
	}

	public boolean isCreatePostShow() {
		return createPostShow;
	}

	public boolean isUpdateShow() {
		return updateShow;
	}

	public boolean isUpdatePostShow() {
		return updatePostShow;
	}

	public boolean isDeleteShow() {
		return deleteShow;
	}

	public boolean isReadShow() {
		return readShow;
	}

	public boolean isPrepareShow() {
		return prepareShow;
	}

	public boolean isAddDetailShow() {
		return (Boolean) getRequest().getAttribute(
				Button.ADD_DETAIL.concat(getControllerConfig().getTabularConfig().getBaseName()));
	}

	public boolean isAddDetailShow(final String detail) {
		return (Boolean) getRequest().getAttribute(Button.ADD_DETAIL.concat(detail));
	}

	public void addDetailShow(final String detail) {
		getRequest().setAttribute(Button.ADD_DETAIL.concat(detail), Boolean.TRUE);
	}

	public void addDetailHide(final String detail) {
		getRequest().setAttribute(Button.ADD_DETAIL.concat(detail), Boolean.FALSE);
	}

	public boolean isDeleteDetailShow(final String detail) {
		return (Boolean) getRequest().getAttribute(Button.DELETE.concat(detail));
	}

	public void deleteDetailShow(final String detail) {
		getRequest().setAttribute(Button.DELETE.concat(detail), Boolean.TRUE);
	}

	public void deleteHide(final String detail) {
		getRequest().setAttribute(Button.DELETE.concat(detail), Boolean.FALSE);
	}

	public boolean isTabularPostShow() {
		return tabularPostShow;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(final String detail) {
		this.detail = detail;
	}

	/**
	 * Method to manager button.
	 * 
	 * @param button
	 *            Button
	 * @param show
	 *            Show (true|false)
	 * @since 1.0
	 */
	public void buttonControl(final String button, final boolean show) {
		if (getControllerType().equals(ControllerType.TABULAR)) {
			setRequestAttribute(
					Button.DELETE.concat(getControllerConfig().getTabularConfig().getBaseName()),
					(Boolean) show);
		} else if (Button.ADD_DETAIL.equals(button)) {
			setRequestAttribute(Button.ADD_DETAIL.concat(getControllerConfig().getTabularConfig()
					.getBaseName()), (Boolean) show);
		} else {
			setRequestAttribute(button, show);
		}
	}

	/**
	 * Method to show button.
	 * 
	 * @param button
	 *            Button.
	 * @since 1.0
	 */
	public void showButton(final String button) {
		buttonControl(button, true);
	}

	/**
	 * Method to show buttons.
	 * 
	 * @param buttons
	 *            Buttons.
	 * @since 1.0
	 */
	public void showButtons(final String... buttons) {
		for (String button : buttons) {
			showButton(button);
		}
	}

	/**
	 * Method to hide button.
	 * 
	 * @param button
	 *            Button.
	 * @since 1.0
	 */
	public void hideButton(final String button) {
		buttonControl(button, false);
	}

	/**
	 * Method to hide buttons.
	 * 
	 * @param buttons
	 *            Buttons.
	 * @since 1.0
	 */
	public void hideButtons(final String... buttons) {
		for (String button : buttons) {
			hideButton(button);
		}
	}

	/**
	 * Method to show error
	 * 
	 * @param message
	 * @return
	 */
	protected String showError(final String message) {
		showButtons(getOperation());
		addActionError(getText(message));
		controlResultForward();
		return Forward.SUCCESS;
	}

	public void setSelectedTab(final int selectedTab) {
		this.selectedTab = selectedTab;
	}

	public int getSelectedTab() {
		return selectedTab;
	}

	public void setDetailIndex(final Integer detailIndex) {
		this.detailIndex = detailIndex;
	}

	public Integer getDetailIndex() {
		return detailIndex;
	}

	public void setReportShow(boolean reportShow) {
		this.reportShow = reportShow;
	}

	public boolean isReportShow() {
		return reportShow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vulpe.controller.VulpeBaseSimpleController#validateEntity()
	 */
	public boolean validateEntity() {
		return EntityValidator.validate(getEntity());
	}

}