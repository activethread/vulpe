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
package org.vulpe.model.dao.impl.jpa;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.vulpe.audit.model.entity.AuditOccurrence;
import org.vulpe.commons.VulpeConstants.Model.Entity;
import org.vulpe.commons.util.VulpeHashMap;
import org.vulpe.commons.util.VulpeReflectUtil;
import org.vulpe.commons.util.VulpeStringUtil;
import org.vulpe.commons.util.VulpeValidationUtil;
import org.vulpe.exception.VulpeApplicationException;
import org.vulpe.model.annotations.Like;
import org.vulpe.model.annotations.QueryConfiguration;
import org.vulpe.model.annotations.QueryConfigurations;
import org.vulpe.model.annotations.QueryParameter;
import org.vulpe.model.annotations.Relationship;
import org.vulpe.model.annotations.Relationship.RelationshipScope;
import org.vulpe.model.dao.impl.AbstractVulpeBaseDAO;
import org.vulpe.model.entity.Parameter;
import org.vulpe.model.entity.VulpeEntity;

/**
 * Default implementation of DAO with JPA
 *
 * @author <a href="mailto:fabio.viana@vulpe.org">Fábio Viana</a>
 * @author <a href="mailto:felipe@vulpe.org">Geraldo Felipe</a>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AbstractVulpeBaseDAOJPA<ENTITY extends VulpeEntity<ID>, ID extends Serializable & Comparable<?>>
		extends AbstractVulpeBaseDAO<ENTITY, ID> {

	protected static final String CGLIB_ENHANCER = "EnhancerByCGLIB";
	protected static final String CGLIB_CALLBACK_0 = "CGLIB$CALLBACK_0";
	protected static final String CGLIB_CALLBACK_0_TARGET = "target";

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 *
	 * @param <T>
	 * @param entity
	 */
	protected <T> void repairRelationship(final T entity) {
		final List<Field> fields = VulpeReflectUtil.getFields(entity.getClass());
		for (final Field field : fields) {
			final OneToMany oneToMany = field.getAnnotation(OneToMany.class);
			if (oneToMany != null) {
				try {
					final List<ENTITY> entities = (List<ENTITY>) PropertyUtils.getProperty(entity,
							field.getName());
					if (VulpeValidationUtil.isNotEmpty(entities)) {
						for (final ENTITY entityChild : entities) {
							repairRelationship(entityChild);
							PropertyUtils.setProperty(entityChild, oneToMany.mappedBy(), entity);
						}
					}
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}
		}
	}

	/**
	 *
	 * @param entities
	 */
	private void repairInstance(final List<ENTITY> entities) {
		for (final ENTITY entity : entities) {
			if (VulpeValidationUtil.isNotEmpty(entity)) {
				final List<Field> fields = VulpeReflectUtil.getFields(entity.getClass());
				for (final Field field : fields) {
					final ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
					// final OneToMany oneToMany =
					// field.getAnnotation(OneToMany.class);
					if (manyToOne != null) {
						try {
							ENTITY value = (ENTITY) PropertyUtils.getProperty(entity,
									field.getName());
							if (value != null
									&& value.getClass().getSimpleName().contains("_javassist_")) {
								final ENTITY newValue = (ENTITY) field.getType().newInstance();
								final Object handler = VulpeReflectUtil.getFieldValue(value,
										"handler");
								final ID id = VulpeReflectUtil.getFieldValue(handler, "id");
								newValue.setId(id);
								VulpeReflectUtil.setFieldValue(entity, field.getName(), newValue);
							}
						} catch (Exception e) {
							LOG.error(e.getMessage());
						}
						// } else if (oneToMany != null) {
						// try {
						// List<ENTITY> childs =
						// VulpeReflectUtil.getFieldValue(entity, field
						// .getName());
						// if (VulpeValidationUtil.isNotEmpty(childs)) {
						// repairInstance(childs);
						// }
						// } catch (Exception e) {
						// LOG.error(e.getMessage());
						// }
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vulpe.model.dao.VulpeDAO#merge(java.lang.Object)
	 */
	public <T> T merge(final T entity) {
		if (entity instanceof AuditOccurrence) {
			entityManager.clear();
		}
		repairRelationship(entity);
		T merged = entity;
		if (((ENTITY) entity).map().containsKey(Entity.ONLY_UPDATE_DETAILS)) {
			final List<String> detailNames = (List<String>) ((ENTITY) entity).map().get(
					Entity.ONLY_UPDATE_DETAILS);
			if (VulpeValidationUtil.isNotEmpty(detailNames)) {
				for (final String detail : detailNames) {
					List<ENTITY> details = null;
					try {
						details = (List<ENTITY>) PropertyUtils.getProperty(entity, detail);
					} catch (Exception e) {
						LOG.error(e.getMessage());
					}
					if (VulpeValidationUtil.isNotEmpty(details)) {
						for (final ENTITY entity2 : details) {
							loadEntityRelationships(entity2);
							merge(entity2);
						}
					}
				}
			}
		} else {
			merged = entityManager.merge(entity);
			((ENTITY) entity).setId(((ENTITY) merged).getId());
		}
		entityManager.flush();
		entityManager.clear();
		loadEntityRelationships((ENTITY) entity);
		return entity;
	}

	/**
	 * Execute HQL query.
	 *
	 * @param <T>
	 * @param hql
	 * @param params
	 * @return
	 * @throws VulpeApplicationException
	 */
	protected <T> List<T> execute(final String hql, final Map<String, Object> params)
			throws VulpeApplicationException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Reading object: ".concat(hql));
		}
		final Query query = entityManager.createQuery(hql);
		setParams(query, params);
		return query.getResultList();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vulpe.model.dao.VulpeDAO#executeProcedure(java.lang.String,
	 * java.util.List)
	 */
	public CallableStatement executeProcedure(final String name, final List<Parameter> parameters)
			throws VulpeApplicationException {
		return executeCallableStatement(name, null, parameters);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vulpe.model.dao.VulpeDAO#executeFunction(java.lang.String, int,
	 * java.util.List)
	 */
	@Override
	public CallableStatement executeFunction(final String name, final int returnType,
			final List<Parameter> parameters) throws VulpeApplicationException {
		return executeCallableStatement(name, returnType, parameters);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.vulpe.model.dao.VulpeDAO#executeCallableStatement(java.lang.String,
	 * java.lang.Integer, java.util.List)
	 */
	public CallableStatement executeCallableStatement(final String name, final Integer returnType,
			final List<Parameter> parameters) throws VulpeApplicationException {
		CallableStatement cstmt = null;
		try {
			final Session session = (Session) entityManager.getDelegate();
			final SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) session
					.getSessionFactory();
			final ConnectionProvider connectionProvider = sessionFactoryImplementor
					.getConnectionProvider();
			final Connection connection = connectionProvider.getConnection();

			final StringBuilder call = new StringBuilder();
			call.append(returnType == null ? "{call " : "{? = call ");
			call.append(name);
			int count = 0;
			if (parameters != null) {
				call.append("(");
				do {
					if (count > 0) {
						call.append(",");
					}
					call.append("?");
					++count;
				} while (count < parameters.size());
				call.append(")");
			}
			call.append("}");
			if (parameters != null) {
				cstmt = connection.prepareCall(call.toString());
				if (returnType != null) {
					if (returnType != 0) {
						cstmt.registerOutParameter(1, returnType);
						count = 1;
					} else {
						count = 0;
					}
				}
				for (Parameter parameter : parameters) {
					++count;
					if (parameter.getType() == Types.ARRAY) {
						// Connection nativeConnection =
						// cstmt.getConnection().getMetaData().getConnection();
						// ArrayDescriptor objectArrayDescriptor =
						// ArrayDescriptor.createDescriptor(
						// parameter.getArrayType(), nativeConnection);
						// Array array = new ARRAY(objectArrayDescriptor,
						// nativeConnection, parameter
						// .getArrayValues());
						// cstmt.setArray(count, array);
						LOG.info("");
					} else {
						cstmt.setObject(count, parameter.getValue(), parameter.getType());
					}
					if (parameter.isOut()) {
						if (parameter.getType() == Types.ARRAY) {
							cstmt.registerOutParameter(count, Types.ARRAY, parameter.getArrayType());
						} else {
							cstmt.registerOutParameter(count, parameter.getType());
						}
					}
				}
			}
			cstmt.execute();
		} catch (SQLException e) {
			throw new VulpeApplicationException(e.getMessage());
		}
		return cstmt;
	}

	/**
	 * Prepares object Query, setting parameters
	 */
	protected void setParams(final Query query, final Map<String, Object> params) {
		if (params != null) {
			for (final String name : params.keySet()) {
				Object value = params.get(name);
				final String paramName = name.replace("!", "").replace("_", "")
						.replaceAll("\\.", "_");
				if (value instanceof String) {
					value = ((String) value).replace("[like]", "");
				}
				query.setParameter(paramName, value);
			}
		}
	}

	/**
	 * Returns NamedQuery defined in entity
	 */
	protected NamedQuery getNamedQuery(final Class<?> entityClass, final String nameQuery) {
		if (entityClass.isAnnotationPresent(NamedQueries.class)) {
			final NamedQueries namedQueries = entityClass.getAnnotation(NamedQueries.class);
			for (NamedQuery namedQuery : namedQueries.value()) {
				if (namedQuery.name().equals(nameQuery)) {
					return namedQuery;
				}
			}
		} else if (entityClass.isAnnotationPresent(NamedQuery.class)) {
			final NamedQuery namedQuery = entityClass.getAnnotation(NamedQuery.class);
			if (namedQuery.name().equals(nameQuery)) {
				return namedQuery;
			}
		}
		return null;
	}

	/**
	 *
	 * @param entity
	 */
	protected void loadEntityRelationships(final ENTITY entity) {
		final List<ENTITY> entities = new ArrayList<ENTITY>();
		entities.add(entity);
		loadRelationships(entities, null, true);
	}

	/**
	 *
	 * @param attribute
	 * @param parent
	 * @return
	 */
	private String loadRelationshipsMountReference(final String attribute, final String parent) {
		final StringBuilder hqlAttribute = new StringBuilder();
		final List<String> attributeList = new ArrayList<String>();
		String first = attribute.substring(0, attribute.indexOf('['));
		loadRelationshipsSeparateAttributes(attributeList, attribute.replace(first, ""), null);
		if (StringUtils.isNotBlank(first)) {
			first += "_";
		}
		for (final String attributeName : attributeList) {
			hqlAttribute.append(", obj.").append(attributeName).append(" as ").append(first)
					.append(attributeName.replaceAll("\\.", "_"));
		}
		return hqlAttribute.toString();
	}

	/**
	 *
	 * @param attributeList
	 * @param value
	 */
	private void loadRelationshipsSeparateAttributes(final List<String> attributeList,
			String value, String parent) {
		if (StringUtils.isNotBlank(value)) {
			value = value.replaceAll(" ", "");
		}
		boolean initial = value.startsWith("[") && value.endsWith("]");
		if (!initial && value.contains("],")) {
			final String[] valueParts = value.split("\\],");
			int count = 1;
			for (String valuePart : valueParts) {
				int countBracketOpen = VulpeStringUtil.count(valuePart, "[");
				int countBracketClose = VulpeStringUtil.count(valuePart, "]");
				for (int i = countBracketOpen; i > countBracketClose; i--) {
					valuePart += "]";
				}
				if (count == valueParts.length) {
					value = value.replace(valuePart, "");
				} else {
					value = value.replace(valuePart + "],", "");
				}
				loadRelationshipsSeparateAttributes(attributeList, valuePart, parent);
				++count;
			}
		} else {
			String subParent = "";
			if (value.indexOf('[') > 0) {
				if (StringUtils.isEmpty(parent)) {
					parent = value.substring(0, value.indexOf('['));
				} else {
					final String parentTest = value.substring(0, value.indexOf('['));
					if (!parent.endsWith(parentTest)) {
						parent += parentTest;
					}
				}
				if (parent.contains(",")) {
					final String[] parentParts = parent.split(",");
					int count = 1;
					for (final String parentPart : parentParts) {
						if (count == parentParts.length) {
							parent = parentPart;
						} else {
							attributeList.add(parentPart);
						}
						++count;
					}
				}
			}
			if (value.contains("[")) {
				value = value.substring(value.indexOf('[') + 1, value.lastIndexOf("]"));
			}
			final String text = value.contains("[") ? value.substring(0, value.indexOf('['))
					: value;
			final String[] textParts = text.split(",");
			parent = StringUtils.isNotBlank(parent) ? parent + "." : "";
			int count = 1;
			for (final String textPart : textParts) {
				if (count == textParts.length && !textPart.equals(value)) {
					subParent = textPart;
				} else {
					attributeList.add(parent + textPart);
					if (textPart.equals(value)) {
						value = "";
					} else {
						value = value.substring(textPart.length() + 1);
					}
				}
				++count;
			}
			if (StringUtils.isEmpty(subParent) && StringUtils.isNotBlank(value)) {
				attributeList.add(value);
			}
			if (value.contains("[")) {
				if (value.contains("],")) {
					subParent = "";
				}
				loadRelationshipsSeparateAttributes(attributeList, value, parent + subParent);
			}
		}
	}

	/**
	 * "[teste1[teste1_1,teste1_2[teste1_2_1,teste1_2_2,teste1_2_3[teste1_2_3_1,teste1_2_3_2],teste1_2_4]],teste2,teste3[teste3_1]]"
	 * @param expression
	 */
	//TODO
	public static void build(final String expression) {
		char currentCharacter, previousCharacter;
		int open = 0;
		int initialPosition = 0;
		int finalPosition = 0;
		String value = "";
		List<String> previous = new ArrayList<String>();
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < expression.length(); i++) {
			currentCharacter = expression.charAt(i);
			if (i > 0) {
				previousCharacter = expression.charAt(i - 1);
				if (previousCharacter == '[' || previousCharacter == ',' || previousCharacter == ']') {
					initialPosition = i;
				}
				if (currentCharacter == '[' || currentCharacter == ',' || currentCharacter == ']') {
					finalPosition = i;
				}
				if (previousCharacter == '[') {
					++open;
					if (!value.isEmpty()) {
						previous.add(value);
					}
				}
				if (currentCharacter == ']') {
					--open;
					if (open > -1) {
						previous.remove(open);
					}
				}
			}
			if (finalPosition > initialPosition) {
				value = expression.substring(initialPosition, finalPosition);
				final StringBuilder hierarchy = new StringBuilder();
				for (final String parent : previous) {
					hierarchy.append(parent).append('.');
				}
				hierarchy.append(value);
				list.add(hierarchy.toString());
				System.out.println(hierarchy.toString());
			}
		}
	}

	/**
	 * Load relationships and optimize lazy load.
	 *
	 * @param entities
	 * @param params
	 */
	protected void loadRelationships(final List<ENTITY> entities, final Map<String, Object> params,
			final boolean onlyInMain) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Method loadRelationships - Start");
		}
		if (VulpeValidationUtil.isNotEmpty(entities)) {
			repairInstance(entities);
			final ENTITY entity = entities.get(0);
			final String queryConfigurationName = entity.getQueryConfigurationName();
			final Class<?> entityClass = initializeAndUnproxy(entity).getClass();
			final QueryConfigurations queryConfigurations = entityClass
					.getAnnotation(QueryConfigurations.class);
			if (queryConfigurations != null && queryConfigurations.value().length > 0) {
				for (final QueryConfiguration queryConfiguration : queryConfigurations.value()) {
					if (queryConfiguration != null
							&& queryConfiguration.name().equals(queryConfigurationName)
							&& queryConfiguration.relationships().length > 0) {
						final List<ID> parentIds = new ArrayList<ID>();
						for (final ENTITY parent : entities) {
							parentIds.add(parent.getId());
						}
						for (final Relationship relationship : queryConfiguration.relationships()) {
							final boolean loadAll = relationship.attributes().length == 1
									&& "*".equals(relationship.attributes()[0]);
							if ((!relationship.scope().equals(RelationshipScope.SELECT) && loadAll && !onlyInMain)
									|| (relationship.scope().equals(RelationshipScope.SELECT) && onlyInMain)) {
								continue;
							}
							try {
								final StringBuilder hql = new StringBuilder();
								final String parentName = VulpeStringUtil
										.getAttributeName(entityClass.getSimpleName());
								final Class propertyType = PropertyUtils.getPropertyType(
										entityClass.newInstance(), relationship.property());
								final OneToMany oneToMany = VulpeReflectUtil.getAnnotationInField(
										OneToMany.class, entityClass, relationship.property());
								final Map<String, String> hqlAttributes = new HashMap<String, String>();
								// select and from
								hql.append(loadRelationshipsMountSelectAndFrom(relationship,
										parentName, propertyType, entity, hqlAttributes, oneToMany,
										loadAll));
								// where
								hql.append(loadRelationshipsMountWhere(relationship, parentName,
										params, oneToMany));
								final Query query = entityManager.createQuery(hql.toString());
								if (oneToMany != null) {
									query.setParameter("parentIds", parentIds);
								} else {
									final List<ID> ids = new ArrayList<ID>();
									for (final ENTITY entity2 : entities) {
										final VulpeEntity<ID> propertyEntity = (VulpeEntity<ID>) PropertyUtils
												.getProperty(entity2, relationship.property());
										if (VulpeValidationUtil.isNotEmpty(propertyEntity)) {
											ids.add(propertyEntity.getId());
										}
									}
									if (VulpeValidationUtil.isEmpty(ids)) {
										continue;
									}
									query.setParameter("ids", ids);
								}
								if (relationship.parameters() != null) {
									for (final QueryParameter queryParameter : relationship
											.parameters()) {
										if (params.containsKey(queryParameter.equals().name())) {
											query.setParameter(queryParameter.equals().name(),
													params.get(queryParameter.equals().name()));
										}
									}
								}
								final List<ENTITY> childs = new ArrayList<ENTITY>();
								final Map<ID, ID> relationshipIds = new HashMap<ID, ID>();
								if (oneToMany != null && loadAll) {
									final List<ENTITY> result = query.getResultList();
									childs.addAll(loadRelationshipsMountChild(relationship, result,
											relationshipIds, parentName));
								} else {
									final List<Map> result = query.getResultList();
									childs.addAll(loadRelationshipsMountChild(relationship, result,
											relationshipIds, parentName, propertyType,
											hqlAttributes, oneToMany));
								}
								if (!onlyInMain) {
									final Session session = (Session) entityManager.getDelegate();
									session.clear();
								}
								loadRelationshipsMountEntities(relationship, entities, childs,
										relationshipIds, parentName, oneToMany);
							} catch (Exception e) {
								LOG.error(e.getMessage());
							}
						}
					}
				}
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Method loadRelationships - End");
		}
	}

	private String loadRelationshipsMountSelectAndFrom(final Relationship relationship,
			final String parentName, final Class propertyType, final ENTITY firstEntity,
			final Map<String, String> hqlAttributes, final OneToMany oneToMany,
			final boolean loadAll) {
		final StringBuilder hql = new StringBuilder();
		try {
			final List<String> hqlJoin = new ArrayList<String>();
			if (oneToMany != null && loadAll) {
				hql.append("select obj ");
			} else {
				hql.append("select new map(obj.id as id");
				for (final String attribute : relationship.attributes()) {
					if ("id".equals(attribute)) {
						continue;
					}
					if (oneToMany != null) {
						final Class attributeType = PropertyUtils.getPropertyType(oneToMany
								.targetEntity().newInstance(), attribute);
						boolean manyToOne = attributeType.isAnnotationPresent(ManyToOne.class)
								|| VulpeEntity.class.isAssignableFrom(attributeType);
						hql.append(", ").append("obj.")
								.append(attribute + (manyToOne ? ".id" : "")).append(" as ")
								.append(attribute);
					} else {
						if (attribute.contains("[")) {
							final StringBuilder hqlAttribute = new StringBuilder(
									"select new map(obj.id as id");
							String attributeParent = attribute.substring(0, attribute.indexOf('['));
							hqlAttribute.append(loadRelationshipsMountReference(attribute, null));
							if (StringUtils.isEmpty(attributeParent)) {
								attributeParent = relationship.property();
								final Class attributeParentType = PropertyUtils.getPropertyType(
										firstEntity, attributeParent);
								hqlAttribute.append(") from ")
										.append(attributeParentType.getSimpleName()).append(" obj");
							} else {
								final Class attributeParentType = PropertyUtils.getPropertyType(
										propertyType.newInstance(), attributeParent);
								hqlAttribute.append(") from ")
										.append(attributeParentType.getSimpleName()).append(" obj");
								int joinCount = hqlJoin.size() + 1;
								hqlJoin.add((joinCount > 0 ? "" : ",") + "left outer join obj."
										+ attributeParent + " obj" + joinCount);
								hql.append(", obj").append(joinCount).append(".id").append(" as ")
										.append(attributeParent);
							}
							hqlAttribute.append(" where obj.id in (:ids)");
							hqlAttributes.put(attribute, hqlAttribute.toString());
						} else {
							final Class attributeType = PropertyUtils.getPropertyType(
									propertyType.newInstance(), attribute);
							boolean manyToOne = attributeType.isAnnotationPresent(ManyToOne.class)
									|| VulpeEntity.class.isAssignableFrom(attributeType);
							hql.append(", ").append("obj.")
									.append(attribute + (manyToOne ? ".id" : "")).append(" as ")
									.append(attribute);
						}
					}
				}
				if (oneToMany != null) {
					hql.append(", obj.").append(parentName).append(".id as ").append(parentName);
				}
				hql.append(")");
			}
			final String className = oneToMany == null ? propertyType.getSimpleName() : oneToMany
					.targetEntity().getSimpleName();
			hql.append(" from ").append(className).append(" obj ");
			for (final String join : hqlJoin) {
				hql.append(join);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return hql.toString();
	}

	private String loadRelationshipsMountWhere(final Relationship relationship,
			final String parentName, final Map<String, Object> params, final OneToMany oneToMany) {
		final StringBuilder hql = new StringBuilder();
		if (oneToMany != null) {
			hql.append(" where obj.").append(parentName).append(".id in (:parentIds)");
		} else {
			hql.append(" where obj.id in (:ids)");
		}
		if (StringUtils.isNotBlank(relationship.condition())) {
			hql.append(" and ").append(relationship.condition());
		}

		if (relationship.parameters() != null) {
			for (final QueryParameter queryParameter : relationship.parameters()) {
				if (params.containsKey(queryParameter.equals().name())) {
					hql.append(" and ");
					hql.append(
							StringUtils.isNotBlank(queryParameter.equals().alias()) ? queryParameter
									.equals().alias() : "obj").append(".");
					hql.append(queryParameter.equals().name());
					final Like like = VulpeReflectUtil.getAnnotationInField(Like.class,
							oneToMany.targetEntity(), queryParameter.equals().name());
					hql.append(" ")
							.append(like != null ? "like" : queryParameter.equals().operator()
									.getValue()).append(" ");
					hql.append(":").append(queryParameter.equals().name());
				}
			}
		}
		return hql.toString();
	}

	private List<ENTITY> loadRelationshipsMountChild(final Relationship relationship,
			final List<ENTITY> result, final Map<ID, ID> relationshipIds, final String parentName) {
		final List<ENTITY> childs = new ArrayList<ENTITY>();
		try {
			for (final ENTITY entity : result) {
				entity.setQueryConfigurationName(relationship.forceLoadQueryConfiguration());
				loadEntityRelationships(entity);
				final ENTITY parent = (ENTITY) PropertyUtils.getProperty(entity, parentName);
				relationshipIds.put(entity.getId(), parent.getId());
				childs.add(entity);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return childs;
	}

	private List<ENTITY> loadRelationshipsMountChild(final Relationship relationship,
			final List<Map> result, final Map<ID, ID> relationshipIds, final String parentName,
			final Class propertyType, final Map<String, String> hqlAttributes,
			final OneToMany oneToMany) {
		final List<ENTITY> childs = new ArrayList<ENTITY>();
		try {
			for (final Map map : result) {
				final ENTITY child = (ENTITY) (oneToMany != null ? oneToMany.targetEntity()
						.newInstance() : propertyType.newInstance());
				VulpeReflectUtil.setFieldValue(child, "id", map.get("id"));
				relationshipIds.put(child.getId(), (ID) map.get(parentName));
				childs.add(child);
			}
			for (final String attribute : relationship.attributes()) {
				if (hqlAttributes.containsKey(attribute)) {
					final String attributeParent = attribute.substring(0, attribute.indexOf('['));
					final String hqlAttribute = hqlAttributes.get(attribute);
					final List<ID> ids = new ArrayList<ID>();
					for (final ENTITY child : childs) {
						Map map = null;
						for (final Map resultMap : result) {
							if (child.getId().equals((Long) resultMap.get("id"))) {
								map = resultMap;
								break;
							}
						}
						if (StringUtils.isNotBlank(attributeParent)) {
							final Class attributeType = PropertyUtils.getPropertyType(
									propertyType.newInstance(), attributeParent);
							final VulpeEntity<ID> newAttribute = (VulpeEntity<ID>) attributeType
									.newInstance();
							newAttribute.setId((ID) map.get(attributeParent));
							ids.add(newAttribute.getId());
							VulpeReflectUtil.setFieldValue(child, attributeParent, newAttribute);
						} else {
							ids.add(child.getId());
						}
					}
					if (StringUtils.isNotBlank(attributeParent)) {
						final Query queryAttribute = getEntityManager().createQuery(hqlAttribute)
								.setParameter("ids", ids);
						final List<Map> listAttributeMap = (List<Map>) queryAttribute
								.getResultList();
						for (ENTITY child : childs) {
							final Class attributeType = PropertyUtils.getPropertyType(
									propertyType.newInstance(), attributeParent);
							final VulpeEntity<ID> newAttribute = (VulpeEntity<ID>) attributeType
									.newInstance();
							for (Map map : listAttributeMap) {
								if (((Long) map.get("id")).equals(child.getId())) {
									loadRelationshipsMountProperties(map, newAttribute, attribute,
											null);
								}
							}
							VulpeReflectUtil.setFieldValue(child, attributeParent, newAttribute);
						}
					} else {
						final Query queryAttribute = getEntityManager().createQuery(hqlAttribute)
								.setParameter("ids", ids);
						final List<Map> listAttributeMap = (List<Map>) queryAttribute
								.getResultList();
						for (final ENTITY child : childs) {
							for (final Map map : listAttributeMap) {
								if (((Long) map.get("id")).equals(child.getId())) {
									loadRelationshipsMountProperties(map, child, attribute, null);
								}
							}
						}
					}
				} else {
					for (final ENTITY child : childs) {
						Map map = null;
						for (final Map resultMap : result) {
							if (child.getId().equals((Long) resultMap.get("id"))) {
								map = resultMap;
								break;
							}
						}
						final Class attributeType = PropertyUtils.getPropertyType(child, attribute);
						boolean manyToOne = attributeType.isAnnotationPresent(ManyToOne.class)
								|| VulpeEntity.class.isAssignableFrom(attributeType);
						VulpeReflectUtil.instanciate(child, attribute + (manyToOne ? ".id" : ""),
								map.get(attribute));
					}
				}
			}
			if (relationship.forceLoad()) {
				if (!"default".equals(relationship.forceLoadQueryConfiguration())) {
					for (final ENTITY entity : childs) {
						entity.setQueryConfigurationName(relationship.forceLoadQueryConfiguration());
					}
				}
				loadRelationships(childs, null, false);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return childs;
	}

	private void loadRelationshipsMountEntities(final Relationship relationship,
			final List<ENTITY> entities, final List<ENTITY> childs,
			final Map<ID, ID> relationshipIds, final String parentName, final OneToMany oneToMany) {
		try {
			for (final ENTITY parent : entities) {
				if (VulpeValidationUtil.isEmpty(childs)) {
					PropertyUtils.setProperty(parent, relationship.property(), null);
				} else {
					final List<ENTITY> loadedChilds = new ArrayList<ENTITY>();
					for (final ENTITY child : childs) {
						if (oneToMany != null) {
							final ID parentId = (ID) relationshipIds.get(child.getId());
							if (parent.getId().equals(parentId)) {
								PropertyUtils.setProperty(child, parentName, parent);
								loadedChilds.add(child);
							}
							PropertyUtils
									.setProperty(parent, relationship.property(), loadedChilds);
						} else {
							final VulpeEntity<ID> parentChild = (VulpeEntity<ID>) PropertyUtils
									.getProperty(parent, relationship.property());
							if (VulpeValidationUtil.isNotEmpty(parentChild)
									&& child.getId().equals(parentChild.getId())) {
								PropertyUtils.setProperty(parent, relationship.property(), child);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	/**
	 *
	 * @param map
	 * @param entity
	 * @param attribute
	 * @param parent
	 */
	private void loadRelationshipsMountProperties(final Map<String, Object> map,
			final VulpeEntity<ID> entity, final String attribute, final String parent) {
		try {
			final List<String> attributeList = new ArrayList<String>();
			String first = attribute.substring(0, attribute.indexOf('['));
			loadRelationshipsSeparateAttributes(attributeList, attribute.replace(first, ""), null);
			if (StringUtils.isNotBlank(first)) {
				first += "_";
			}
			for (final String attributeName : attributeList) {
				VulpeReflectUtil.instanciate(entity, attributeName,
						map.get(first + attributeName.replaceAll("\\.", "_")));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public Object findByNamedQuery(final String name) {
		try {
			return getEntityManager().createNamedQuery(name).getSingleResult();
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	public Object findByNamedQueryAndNamedParams(final String name, final Map<String, Object> map) {
		try {
			final Query query = getEntityManager().createNamedQuery(name);
			for (final String parameter : map.keySet()) {
				query.setParameter(parameter, map.get(parameter));
			}
			return query.getSingleResult();
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	public List<?> listByNamedQuery(final String name) {
		return getEntityManager().createNamedQuery(name).getResultList();
	}

	public List<?> listByNamedQueryAndNamedParams(final String name, final Map<String, Object> map) {
		final Query query = getEntityManager().createNamedQuery(name);
		for (final String parameter : map.keySet()) {
			if (parameter.equals("limit")) {
				continue;
			}
			query.setParameter(parameter, map.get(parameter));
		}
		final Object limit = map.get("limit");
		if (limit != null) {
			query.setMaxResults((Integer) limit);
		}
		return query.getResultList();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.vulpe.model.dao.VulpeDAO#updateSomeAttributes(org.vulpe.model.entity
	 * .VulpeEntity)
	 */
	@Override
	public void updateSomeAttributes(final ENTITY entity) {
		if (entity.getId() == null) {
			LOG.debug(entity.getClass().getSimpleName() + " id not found");
			return;
		}
		final StringBuilder sql = new StringBuilder(getUpdateSomeAttributtesSQL(entity));
		try {
			final VulpeHashMap<String, Object> map = getUpdateSomeAttributesMap(entity);
			final Field idField = VulpeReflectUtil.getField(entity.getClass(), "id");
			final Column column = idField.getAnnotation(Column.class);
			sql.append(column != null ? column.name() : "id").append(" = ").append(entity.getId());
			final Query query = entityManager.createNativeQuery(sql.toString());
			for (final String key : map.keySet()) {
				final Object value = map.get(key);
				if (value instanceof Enum) {
					query.setParameter(key, value.toString());
				} else if (value instanceof VulpeEntity) {
					query.setParameter(key, ((VulpeEntity<ID>) value).getId());
				} else {
					query.setParameter(key, value);
				}
			}
			query.executeUpdate();
			entityManager.flush();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	@Override
	public void updateSomeAttributes(final List<ENTITY> list) {
		for (final ENTITY entity : list) {
			updateSomeAttributes(entity);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.vulpe.model.dao.VulpeDAO#updateSomeAttributes(org.vulpe.model.entity
	 * .VulpeEntity, java.util.List)
	 */
	@Override
	public void updateSomeAttributes(final ENTITY entity, final List<ID> ids) {
		if (VulpeValidationUtil.isEmpty(ids)) {
			LOG.debug(entity.getClass().getSimpleName() + " id list not found");
			return;
		}
		final StringBuilder sql = new StringBuilder(getUpdateSomeAttributtesSQL(entity));
		try {
			final VulpeHashMap<String, Object> map = getUpdateSomeAttributesMap(entity);
			StringBuilder sqlIds = new StringBuilder();
			for (final ID id : ids) {
				if (sqlIds.length() > 0) {
					sqlIds.append(",");
				}
				sqlIds.append(((Long) id).intValue());
			}
			final Field idField = VulpeReflectUtil.getField(entity.getClass(), "id");
			final Column column = idField.getAnnotation(Column.class);
			sql.append(column != null ? column.name() : "id").append(" in (").append(sqlIds)
					.append(")");
			final Query query = entityManager.createNativeQuery(sql.toString());
			for (final String key : map.keySet()) {
				final Object value = map.get(key);
				if (value instanceof Enum) {
					query.setParameter(key, value.toString());
				} else if (value instanceof VulpeEntity) {
					query.setParameter(key, ((VulpeEntity<ID>) value).getId());
				} else {
					query.setParameter(key, value);
				}
			}
			query.executeUpdate();
			entityManager.flush();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	private String getUpdateSomeAttributtesSQL(final ENTITY entity) {
		final StringBuilder sql = new StringBuilder();
		sql.append("update ");
		final Table table = entity.getClass().getAnnotation(Table.class);
		if (table != null) {
			sql.append(table.name());
		} else {
			sql.append(entity.getClass().getSimpleName());
		}
		sql.append(" set ");
		int count = 0;
		for (final String key : getUpdateSomeAttributesMap(entity).keySet()) {
			if (count > 0) {
				sql.append(", ");
			}
			sql.append(key).append(" = ").append(":").append(key);
			++count;
		}
		sql.append(" where ");
		return sql.toString();
	}

	private VulpeHashMap<String, Object> getUpdateSomeAttributesMap(final ENTITY entity) {
		final List<Field> fields = VulpeReflectUtil.getFields(entity.getClass());
		final VulpeHashMap<String, Object> map = new VulpeHashMap<String, Object>();
		for (final Field field : fields) {
			if (field.getName().equals("id") || Modifier.isTransient(field.getModifiers())) {
				continue;
			}
			final Object value = VulpeReflectUtil.getFieldValue(entity, field.getName());
			if (VulpeValidationUtil.isNotEmpty(value)) {
				String columnName = field.getName();
				if (entity.fieldColumnMap().containsKey(field.getName())) {
					columnName = entity.fieldColumnMap().get(field.getName());
				} else {
					final Column column = field.getAnnotation(Column.class);
					final JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
					if (column != null) {
						columnName = column.name();
					} else if (joinColumn != null) {
						columnName = (StringUtils.isNotBlank(joinColumn.referencedColumnName()) ? joinColumn
								.referencedColumnName() : joinColumn.name());
					}
				}
				map.put(columnName, value);
			}
		}
		return map;
	}

	protected <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			throw new NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		return unproxy(entity);
	}

	protected <T> T unproxy(T entity) {
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
					.getImplementation();
		}
		return entity;
	}

	public class QueryFunction {
		public String execute(final ENTITY entity, final String query, final String token) {
			String queryModified = query;
			final String initialToken = token + "(";
			while (queryModified.contains(initialToken)) {
				final int initialPos = queryModified.indexOf(token);
				final int initialAttributePos = queryModified.indexOf(initialToken)
						+ initialToken.length();
				final int finalPos = queryModified.indexOf(")", initialPos);
				final String queryAttributte = queryModified.substring(initialAttributePos,
						queryModified.indexOf(")", initialAttributePos));
				queryModified = queryModified.substring(0, initialPos)
						+ queryModified.substring(finalPos + 1);
				final int openPos = queryModified.indexOf("{");
				final int closePos = queryModified.indexOf("}", openPos);
				if (checkCondition(entity, queryAttributte)) {
					queryModified = queryModified.substring(0, openPos)
							+ queryModified.substring(openPos + 1, closePos)
							+ queryModified.substring(closePos + 1);
				} else {
					queryModified = queryModified.substring(0, openPos)
							+ queryModified.substring(closePos + 1);
				}
			}
			return queryModified;
		}

		public boolean checkCondition(final ENTITY entity, final String queryAttributte) {
			return false;
		}
	}

	protected String validateQueryFunctions(final ENTITY entity, final String query) {
		String whereModified = emptyQueryFunction(entity, query);
		whereModified = notEmptyQueryFunction(entity, whereModified);
		return whereModified;
	}

	protected String notEmptyQueryFunction(final ENTITY entity, final String query) {
		return new QueryFunction() {
			@Override
			public boolean checkCondition(ENTITY entity, String queryAttributte) {
				return VulpeValidationUtil.isNotEmpty(VulpeReflectUtil.getFieldValue(entity,
						queryAttributte));
			};
		}.execute(entity, query, "notEmpty");
	}

	protected String emptyQueryFunction(final ENTITY entity, final String query) {
		return new QueryFunction() {
			@Override
			public boolean checkCondition(ENTITY entity, String queryAttributte) {
				return VulpeValidationUtil.isEmpty(VulpeReflectUtil.getFieldValue(entity,
						queryAttributte));
			};
		}.execute(entity, query, "empty");
	}

}