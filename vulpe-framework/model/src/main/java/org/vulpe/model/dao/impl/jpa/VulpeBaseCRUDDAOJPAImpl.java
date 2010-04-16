package org.vulpe.model.dao.impl.jpa;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.jpa.JpaCallback;
import org.vulpe.common.ReflectUtil;
import org.vulpe.common.ValidationUtil;
import org.vulpe.common.ReflectUtil.DeclaredType;
import org.vulpe.common.audit.AuditOccurrenceType;
import org.vulpe.common.beans.Paging;
import org.vulpe.exception.VulpeApplicationException;
import org.vulpe.exception.VulpeSystemException;
import org.vulpe.model.annotations.Param;
import org.vulpe.model.entity.VulpeBaseEntity;
import org.vulpe.model.entity.LogicEntity;
import org.vulpe.model.entity.LogicEntity.Status;

/**
 * Default implementation of DAO for CRUD's with JPA.
 *
 * @author <a href="mailto:fabio.viana@activethread.com.br">F�bio Viana</a>
 * @author <a href="mailto:felipe.matos@activethread.com.br">Felipe Matos</a>
 */
@SuppressWarnings("unchecked")
public class VulpeBaseCRUDDAOJPAImpl<ENTITY extends VulpeBaseEntity<ID>, ID extends Serializable & Comparable>
		extends AbstractVulpeBaseDAOJPAImpl<ENTITY, ID> {

	private static final Logger LOG = Logger.getLogger(VulpeBaseCRUDDAOJPAImpl.class
			.getName());

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.vulpe.model.dao.VulpeBaseCRUDDAO#create(br.com.
	 * activethread.framework.model.entity.BaseEntity)
	 */
	public ENTITY create(final ENTITY entity) throws VulpeApplicationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating object: ".concat(entity.toString()));
		}
		if (entity instanceof LogicEntity) {
			final LogicEntity logicEntity = (LogicEntity) entity;
			logicEntity.setStatus(Status.C);
		}
		final ENTITY newEntity = merge(entity);
		audit(newEntity, AuditOccurrenceType.INSERT, null);
		return newEntity;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.vulpe.model.dao.VulpeBaseCRUDDAO#delete(br.com.
	 * activethread.framework.model.entity.BaseEntity)
	 */
	public void delete(final ENTITY entity) throws VulpeApplicationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Deleting object: ".concat(entity.toString()));
		}
		// persistent entity
		final ENTITY entityDeleted = (ENTITY) getJpaTemplate().getReference(
				entity.getClass(), entity.getId());
		audit(entity, AuditOccurrenceType.DELETE, null);
		if (entity instanceof LogicEntity) {
			final LogicEntity logicEntity = (LogicEntity) entityDeleted;
			logicEntity.setStatus(Status.D);
			// make merge of entity
			merge(entityDeleted);
		} else {
			getJpaTemplate().remove(entityDeleted);
		}
	}

	public void delete(final List<ENTITY> entities) throws VulpeApplicationException {
		for (ENTITY entity : entities) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Deleting object: ".concat(entity.toString()));
			}
			// persistent entity
			final ENTITY entityDeleted = (ENTITY) getJpaTemplate()
					.getReference(entity.getClass(), entity.getId());
			if (entity instanceof LogicEntity) {
				final LogicEntity logicEntity = (LogicEntity) entityDeleted;
				logicEntity.setStatus(Status.D);
				// make merge of entity
				merge(entityDeleted);
			} else {
				getJpaTemplate().remove(entityDeleted);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.vulpe.model.dao.VulpeBaseCRUDDAO#update(br.com.
	 * activethread.framework.model.entity.BaseEntity)
	 */
	public void update(final ENTITY entity) throws VulpeApplicationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Updating object: ".concat(entity.toString()));
		}
		audit(entity, AuditOccurrenceType.UPDATE, null);
		if (entity instanceof LogicEntity) {
			final LogicEntity logicEntity = (LogicEntity) entity;
			logicEntity.setStatus(Status.U);
		}
		merge(entity);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vulpe.model.dao.impl.AbstractVulpeBaseDAO#find(java .io.Serializable)
	 */
	public ENTITY find(final ID identifier) throws VulpeApplicationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Retriving id: ".concat(identifier.toString()));
		}

		final ENTITY entity = getJpaTemplate().find(getEntityClass(), identifier);
		if (entity instanceof LogicEntity) {
			final LogicEntity logicEntity = (LogicEntity) entity;
			if (Status.D.equals(logicEntity.getStatus())) {
				return null;
			}
		}
		return entity;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vulpe.model.dao.VulpeBaseCRUDDAO#read(br.com.activethread
	 * .framework.model.entity.BaseEntity)
	 */
	public List<ENTITY> read(final ENTITY entity) throws VulpeApplicationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Reading object: ".concat(entity.toString()));
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		final String hql = getHQL(entity, params);

		return execute(hql, params);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.vulpe.model.dao.VulpeBaseCRUDDAO#paging(br.com.
	 * activethread.framework.model.entity.BaseEntity, java.lang.Integer,
	 * java.lang.Integer)
	 */
	public Paging<ENTITY> paging(final ENTITY entity, final Integer pageSize,
			final Integer page) throws VulpeApplicationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Paging object: ".concat(entity.toString()));
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		final String hql = getHQL(entity, params);

		// getting total records
		final Long size = (Long) getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(final EntityManager entityManager)
					throws PersistenceException {
				final Query query = entityManager
						.createQuery("select count(*) ".concat(hql
								.substring(hql.toLowerCase().indexOf("from"))));
				setParams(query, params);
				return query.getSingleResult();
			}
		});

		final Paging paging = new Paging<ENTITY>(size.intValue(), pageSize,
				page);
		if (size.longValue() > 0) {
			// getting list by size of page
			paging.setList((List<ENTITY>) getJpaTemplate().execute(
					new JpaCallback() {
						public Object doInJpa(final EntityManager entityManager)
								throws PersistenceException {
							final Query query = entityManager.createQuery(hql);
							setParams(query, params);
							query.setFirstResult(paging.getFromIndex());
							query.setMaxResults(pageSize);
							return query.getResultList();
						}
					}));
		}

		return paging;
	}

	/**
	 *
	 * @param entity
	 * @param params
	 * @return
	 */
	protected String getHQL(final ENTITY entity,
			final Map<String, Object> params) {
		final List<Field> fields = ReflectUtil.getInstance().getFields(
				entity.getClass());
		int countParam = 0;
		for (Field field : fields) {
			if ((ReflectUtil.getInstance().isAnnotationInField(Transient.class,
					entity.getClass(), field.getName()) || Modifier
					.isTransient(field.getModifiers()))
					&& !ReflectUtil.getInstance().isAnnotationInField(
							Param.class, entity.getClass(), field.getName())) {
				continue;
			}
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(entity);
			} catch (Exception e) {
				throw new VulpeSystemException(e);
			}
			if (isNotEmpty(value)) {
				params.put(field.getName(), value);
				countParam++;
			}
		}

		final StringBuilder hql = new StringBuilder();
		final NamedQuery namedQuery = getNamedQuery(getEntityClass(),
				getEntityClass().getSimpleName().concat(".read"));
		if (namedQuery == null) {
			hql.append("select obj from ");
			hql.append(entity.getClass().getSimpleName());
			hql.append(" obj");
		} else {
			hql.append(namedQuery.query());
		}

		if (!params.isEmpty()) {
			if (!hql.toString().toLowerCase().contains("where")) {
				hql.append(" where ");
			}

			int count = 0;
			for (String name : params.keySet()) {
				final Object value = params.get(name);
				count++;
				final Param param = ReflectUtil.getInstance()
						.getAnnotationInField(Param.class, entity.getClass(),
								name);
				if (param == null) {
					hql.append("obj.");
					hql.append(name);
					if (value instanceof String) {
						hql.append(" like :");
					} else {
						hql.append(" = :");
					}
				} else {
					hql.append(param.alias());
					hql.append('.');
					if (param.name().equals("")) {
						hql.append(name);
					} else {
						hql.append(param.name());
					}
					hql.append(" ");
					hql.append(param.operator().getValue());
					hql.append(" :");
				}
				hql.append(name);
				if (count < countParam) {
					hql.append(" and ");
				}
			}
		}

		if (entity instanceof LogicEntity) {
			if (hql.toString().toLowerCase().contains("where")) {
				hql.append(" and");
			} else {
				hql.append(" where");
			}
			hql.append(" obj.status <> :status");
			params.put("status", Status.D);
		}

		// add order by
		if (StringUtils.isNotEmpty(entity.getOrderBy())) {
			if (hql.toString().toLowerCase().contains("order by")) {
				hql.append(",");
			} else {
				hql.append(" order by");
			}
			hql.append(" ");
			hql.append(entity.getOrderBy());
		}

		return hql.toString();
	}

	@Transient
	private transient Class<ENTITY> entityClass;
	{
		getEntityClass();
	}

	/**
	 * Retorna a classe de entidade do DAO
	 */
	protected Class<ENTITY> getEntityClass() {
		if (entityClass == null) {
			final DeclaredType declaredType = ReflectUtil.getInstance()
					.getDeclaredType(getClass(),
							getClass().getGenericSuperclass());
			if (declaredType.getItems().isEmpty()) {
				return null;
			}
			entityClass = (Class<ENTITY>) declaredType.getItems().get(0)
					.getType();
		}
		return entityClass;
	}

	public void setEntityClass(final Class<ENTITY> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 *
	 * @param value
	 * @return
	 */
	public boolean isNotEmpty(final Object value) {
		if (ValidationUtil.getInstance().isNotEmpty(value)) {
			if (value instanceof VulpeBaseEntity) {
				final VulpeBaseEntity entity = (VulpeBaseEntity) value;
				return ValidationUtil.getInstance().isNotEmpty(entity.getId());
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

}