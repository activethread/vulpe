package org.vulpe.fox.dao;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;

import net.sf.jelly.apt.TemplateBlock;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.TemplateOutput;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import org.apache.commons.lang.StringUtils;
import org.vulpe.common.helper.VulpeConfigHelper;
import org.vulpe.config.annotations.VulpeDomains;
import org.vulpe.exception.VulpeSystemException;
import org.vulpe.fox.VulpeForAllTemplateStrategy;
import org.vulpe.model.annotations.db4o.SODAQueries;
import org.vulpe.model.annotations.db4o.SODAQuery;
import org.vulpe.model.annotations.db4o.SODAQueryAttribute;
import org.vulpe.model.entity.AbstractVulpeBaseEntityImpl;
import org.vulpe.model.entity.VulpeBaseSimpleEntity;

import com.sun.mirror.declaration.FieldDeclaration;

public class ForAllDAOTemplateStrategy extends VulpeForAllTemplateStrategy {

	@Override
	public boolean preProcess(final TemplateBlock block,
			final TemplateOutput<TemplateBlock> output,
			final TemplateModel model) throws IOException, TemplateException {
		if (super.preProcess(block, output, model)
				&& getDeclaration() instanceof DecoratedClassDeclaration) {
			final DecoratedClassDeclaration clazz = (DecoratedClassDeclaration) getDeclaration();
			if (getClassName(clazz.getSuperclass()).equals(
					VulpeBaseSimpleEntity.class.getName())) {
				return false;
			}
			final DecoratedDAO dao = new DecoratedDAO();
			dao.setName(clazz.getSimpleName());
			dao.setDaoName(clazz.getSimpleName() + "DAO");
			dao.setPackageName(clazz.getPackage().toString());
			dao.setDaoPackageName(StringUtils.replace(clazz.getPackage()
					.toString(), ".entity", ".dao"));

			if (clazz.getAnnotation(javax.persistence.Inheritance.class) != null
					|| clazz
							.getAnnotation(org.vulpe.model.annotations.db4o.Inheritance.class) != null) {
				dao.setInheritance(true);
				dao.setDaoSuperclassPackageName(StringUtils.replace(clazz
						.getSuperclass().getDeclaration().getPackage()
						.toString(), ".entity", ".dao"));
			}
			// if super class isn't Object
			if (clazz.getSuperclass() != null
					&& !getClassName(clazz.getSuperclass()).equals(
							Object.class.getName())
					&& !getClassName(clazz.getSuperclass()).equals(
							AbstractVulpeBaseEntityImpl.class.getName())) {
				dao.setSuperclassName(getClassName(clazz.getSuperclass()));
				dao.setDaoSuperclassName(StringUtils.replace(dao
						.getSuperclassName(), ".entity", ".dao")
						+ "DAO");
			}

			final FieldDeclaration field = getField(clazz, "id");
			dao.setIdType(field.getType().toString());

			prepareMethods(clazz, dao);

			model.setVariable(getVar(), dao);
			return true;
		}
		return false;
	}

	protected void prepareMethods(final DecoratedClassDeclaration clazz,
			final DecoratedDAO dao) {
		if (VulpeConfigHelper.get(VulpeDomains.class).useDB4O()) {
			final SODAQueries sodaQueries = clazz
					.getAnnotation(SODAQueries.class);
			if (sodaQueries != null) {
				for (SODAQuery sq : sodaQueries.value()) {
					putMethod(dao, sq.name(), sq.unique(), sq.attributes());
				}
			}
			final SODAQuery sodaQuery = clazz.getAnnotation(SODAQuery.class);
			if (sodaQuery != null) {
				putMethod(dao, sodaQuery.name(), sodaQuery.unique(), sodaQuery
						.attributes());
			}
		} else {
			final NamedQueries namedQueries = clazz
					.getAnnotation(NamedQueries.class);
			if (namedQueries != null) {
				for (NamedQuery nq : namedQueries.value()) {
					putMethod(clazz, dao, nq.query(), nq.name(), nq.hints());
				}
			}
			final NamedQuery namedQuery = clazz.getAnnotation(NamedQuery.class);
			if (namedQuery != null) {
				putMethod(clazz, dao, namedQuery.query(), namedQuery.name(),
						namedQuery.hints());
			}
			final NamedNativeQueries namedNativeQueries = clazz
					.getAnnotation(NamedNativeQueries.class);
			if (namedNativeQueries != null) {
				for (NamedNativeQuery nnq : namedNativeQueries.value()) {
					putMethod(clazz, dao, nnq.query(), nnq.name(), nnq.hints());
				}
			}
			final NamedNativeQuery namedNativeQuery = clazz
					.getAnnotation(NamedNativeQuery.class);
			if (namedNativeQuery != null) {
				putMethod(clazz, dao, namedNativeQuery.query(),
						namedNativeQuery.name(), namedNativeQuery.hints());
			}
		}
	}

	private void putMethod(final DecoratedClassDeclaration clazz,
			final DecoratedDAO dao, final String query, final String queryName,
			final QueryHint[] hints) {
		if (queryName.equals(dao.getName().concat(".read"))) {
			return;
		}

		final DecoratedDAOMethod method = new DecoratedDAOMethod();

		if (StringUtils.indexOf(queryName, dao.getName().concat(".")) > -1) {
			method.setName(StringUtils.substring(queryName, StringUtils
					.indexOf(queryName, dao.getName().concat("."))
					+ new String(dao.getName().concat(".")).length()));
		} else {
			throw new VulpeSystemException(
					"Name of definition in the query is incorrect. Must be on format: "
							.concat(dao.getName()).concat(".")
							.concat(queryName));
		}

		boolean unique = false;
		String newQuery = StringUtils.replace(query, "\t", " ");
		final char dots = ":".charAt(0);
		final char space = " ".charAt(0);
		while (newQuery.indexOf(dots) > -1) {
			newQuery = newQuery.substring(newQuery.indexOf(dots));
			String param = newQuery.substring(1);
			if (newQuery.indexOf(space) > -1) {
				param = newQuery.substring(1, newQuery.indexOf(space));
			}
			newQuery = newQuery.replace(":".concat(param), "");

			// get parameter type in annotations @Params and @Param
			String type = getType(param, hints);

			if (type == null) {
				final FieldDeclaration field = getField(clazz, param);
				if (field == null) {
					throw new VulpeSystemException("Parameter [".concat(param)
							.concat("] not found on class: ").concat(
									dao.getName()));
				} else {
					if (param.equals("id")) {
						unique = true;
					}
					type = field.getType().toString();
					// verified if is unique column or id
					if (!unique) {
						final Column column = field.getAnnotation(Column.class);
						if (column != null) {
							unique = column.unique();
						}
						if (!unique) {
							final JoinColumn joinColumn = field
									.getAnnotation(JoinColumn.class);
							if (field.getAnnotation(JoinColumn.class) != null) {
								unique = joinColumn.unique();
							}
						}
					}
				}
			}

			final DecoratedDAOParameter parameter = new DecoratedDAOParameter();
			parameter.setName(param);
			parameter.setType(type);
			method.getParameters().add(parameter);
		}

		setupReturn(dao, queryName, hints, method, unique);

		dao.getMethods().add(method);
	}

	private void putMethod(final DecoratedDAO dao, final String queryName,
			final boolean unique, final SODAQueryAttribute[] atributes) {
		if (queryName.equals(dao.getName().concat(".read"))) {
			return;
		}

		final DecoratedDAOMethod method = new DecoratedDAOMethod();
		method.setName(queryName);
		for (SODAQueryAttribute queryAttribute : atributes) {
			final DecoratedDAOParameter parameter = new DecoratedDAOParameter();
			parameter.setName(queryAttribute.name());
			parameter.setType(queryAttribute.type());
			method.getParameters().add(parameter);
		}

		setupReturn(dao, queryName, method, unique);

		dao.getMethods().add(method);
	}

	protected void setupReturn(final DecoratedDAO dao, final String queryName,
			final QueryHint[] hints, final DecoratedDAOMethod method,
			final boolean unique) {
		if (unique || isReturnEntity(hints)) {
			method.setReturnType(dao.getName());
		} else {
			method.setReturnType("java.util.List<".concat(dao.getName())
					.concat(">"));
		}
	}

	protected void setupReturn(final DecoratedDAO dao, final String queryName,
			final DecoratedDAOMethod method, final boolean unique) {
		if (unique) {
			method.setReturnType(dao.getName());
		} else {
			method.setReturnType("java.util.List<".concat(dao.getName())
					.concat(">"));
		}
	}

}