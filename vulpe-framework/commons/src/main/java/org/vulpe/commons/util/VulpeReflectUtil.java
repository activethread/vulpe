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
package org.vulpe.commons.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vulpe.commons.helper.VulpeCacheHelper;
import org.vulpe.exception.VulpeSystemException;
import org.vulpe.model.entity.VulpeEntity;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VulpeReflectUtil {

	private static final Logger LOG = LoggerFactory.getLogger(VulpeReflectUtil.class);

	protected VulpeReflectUtil() {
		// default constructor
	}

	/**
	 * Returns list of fields in class or superclass.
	 *
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFields(final Class<?> clazz) {
		if (VulpeCacheHelper.getInstance().contains(clazz.getName().concat(".fields"))) {
			return VulpeCacheHelper.getInstance().get(clazz.getName().concat(".fields"));
		}
		Class<?> baseClass = clazz;
		final List<Field> list = new ArrayList<Field>();
		while (!baseClass.equals(Object.class)) {
			for (Field field : baseClass.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
					list.add(field);
				}
			}
			baseClass = baseClass.getSuperclass();
		}
		VulpeCacheHelper.getInstance().put(clazz.getName().concat(".fields"), list);
		return list;
	}

	/**
	 * Returns list of fields noted by <code>annotationClass</code>.
	 *
	 * @param clazz
	 * @param annotationClass
	 * @return
	 */
	public static List<Field> getFieldsWithAnnotation(final Class<?> clazz,
			final Class<? extends Annotation> annotationClass) {
		final List<Field> filtredFields = new ArrayList<Field>();
		final List<Field> fields = getFields(clazz);
		for (Field field : fields) {
			if (isAnnotationInField(annotationClass, clazz, field)) {
				filtredFields.add(field);
			}
		}
		return filtredFields;
	}

	/**
	 * Returns field of class or superclass.
	 *
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static Field getField(final Class<?> clazz, final String fieldName) {
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		if (VulpeCacheHelper.getInstance().contains(clazz.getName().concat(".fieldMap"))) {
			fieldMap = VulpeCacheHelper.getInstance().get(clazz.getName().concat(".fieldMap"));
		} else {
			final List<Field> fields = getFields(clazz);
			for (Field field : fields) {
				fieldMap.put(field.getName(), field);
			}
			VulpeCacheHelper.getInstance().put(clazz.getName().concat(".fieldMap"), fieldMap);
		}
		return fieldMap.get(fieldName);
	}

	public static Class<?> getFieldClass(final Class<?> clazz, final String fieldName) {
		final Field field = getField(clazz, fieldName);
		return field == null ? null : getDeclaredType(clazz, field.getGenericType()).getType();
	}

	/**
	 * Copy attributes from <code>origin</code> to <code>destination</code>.
	 *
	 * @param destination
	 * @param origin
	 */
	public static void copy(final Object destination, final Object origin) {
		copy(destination, origin, false);
	}

	/**
	 * Copy attributes from <code>origin</code> to <code>destination</code>.
	 *
	 * @param destination
	 * @param origin
	 * @param ignoreTransient
	 */
	public static void copy(final Object destination, final Object origin, boolean ignoreTransient) {
		final List<Field> fields = getFields(origin.getClass());
		for (final Field field : fields) {
			if (ignoreTransient && Modifier.isTransient(field.getModifiers())) {
				continue;
			}
			try {
				final Object value = PropertyUtils.getProperty(origin, field.getName());
				if (Collection.class.isAssignableFrom(field.getType())) {
					final Collection valueDes = (Collection) PropertyUtils.getProperty(destination,
							field.getName());
					if (value == null) {
						if (valueDes != null) {
							valueDes.clear();
						}
					} else {
						if (valueDes == null) {
							PropertyUtils.setProperty(destination, field.getName(), value);
						} else {
							valueDes.clear();
							valueDes.addAll((Collection) value);
						}
					}
				} else {
					PropertyUtils.setProperty(destination, field.getName(), value);
				}
			} catch (NoSuchMethodException e) {
				LOG.debug("Method not found.", e);
			} catch (Exception e) {
				throw new VulpeSystemException(e);
			}
		}
	}

	/**
	 * Copy transient attributes from <code>origin</code> to
	 * <code>destination</code>.
	 *
	 * @param destination
	 * @param origin
	 */
	public static void copyOnlyTransient(final Object destination, final Object origin) {
		final List<Field> fields = getFields(origin.getClass());
		for (final Field field : fields) {
			if (!Modifier.isTransient(field.getModifiers())) {
				continue;
			}
			try {
				final Object value = PropertyUtils.getProperty(origin, field.getName());
				if (Collection.class.isAssignableFrom(field.getType())) {
					final Collection valueDes = (Collection) PropertyUtils.getProperty(destination,
							field.getName());
					if (value == null) {
						if (valueDes != null) {
							valueDes.clear();
						}
					} else {
						if (valueDes == null) {
							PropertyUtils.setProperty(destination, field.getName(), value);
						} else {
							valueDes.clear();
							valueDes.addAll((Collection) value);
						}
					}
				} else {
					PropertyUtils.setProperty(destination, field.getName(), value);
				}
			} catch (NoSuchMethodException e) {
				LOG.debug("Method not found.", e);
			} catch (Exception e) {
				throw new VulpeSystemException(e);
			}
		}
	}

	/**
	 * Returns list of methods in class or superclass.
	 *
	 * @param clazz
	 * @return
	 */
	public static List<Method> getMethods(final Class<?> clazz) {
		if (VulpeCacheHelper.getInstance().contains(clazz.getName().concat(".methods"))) {
			return VulpeCacheHelper.getInstance().get(clazz.getName().concat(".methods"));
		}
		Class<?> baseClass = clazz;
		final List<Method> list = new ArrayList<Method>();
		while (!baseClass.equals(Object.class)) {
			for (Method method : baseClass.getDeclaredMethods()) {
				if (!method.isSynthetic() && !method.isBridge()
						&& !Modifier.isStatic(method.getModifiers())) {
					list.add(method);
				}
			}
			baseClass = baseClass.getSuperclass();
		}
		VulpeCacheHelper.getInstance().put(clazz.getName().concat(".methods"), list);
		return list;
	}

	/**
	 * Returns method of class or superclass.
	 *
	 * @param clazz
	 * @param methodName
	 * @param typeParams
	 * @return
	 */
	public static Method getMethod(final Class<?> clazz, final String methodName,
			final Class<?>... typeParams) {
		final List<Method> methods = getMethods(clazz);
		Method methodFirst = null;
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				if ((typeParams == null && method.getParameterTypes() == null)
						|| (typeParams != null && method.getParameterTypes() != null && Arrays
								.equals(typeParams, method.getParameterTypes()))) {
					return method;
				}
				if (typeParams == null && methodFirst == null) {
					methodFirst = method;
				}
			}
		}
		return methodFirst;
	}

	/**
	 * Returns class of Type.
	 *
	 * @param clazz
	 * @param type
	 * @return
	 */
	public static DeclaredType getDeclaredType(final Class<?> clazz, final Type type) {
		if (type == null) {
			return null;
		}

		DeclaredType declaredType = null;
		if (type instanceof Class) {
			declaredType = new DeclaredType();
			declaredType.setType((Class<?>) type);
		} else if (type instanceof ParameterizedType) {
			declaredType = new DeclaredType();
			final ParameterizedType parameterizedType = (ParameterizedType) type;
			final DeclaredType rawType = getDeclaredType(clazz, parameterizedType.getRawType());
			declaredType.setType(rawType.getType());
			for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
				declaredType.getItems().add(
						getDeclaredType(clazz, parameterizedType.getActualTypeArguments()[i]));
			}
		} else if (type instanceof TypeVariable) {
			return getDeclaredTypeVariable(clazz, (TypeVariable<?>) type);
		} else if (type instanceof WildcardType) {
			declaredType = new DeclaredType();
			final WildcardType wildcardType = (WildcardType) type;
			if (wildcardType.getLowerBounds().length > 0) {
				declaredType.setType(getDeclaredType(clazz, wildcardType.getLowerBounds()[0])
						.getType());
			} else {
				declaredType.setType(null);
			}

			for (int i = 0; i < wildcardType.getUpperBounds().length; i++) {
				declaredType.getTypeItems().add(
						getDeclaredType(clazz, wildcardType.getUpperBounds()[i]));
			}

			return declaredType;
		}
		return declaredType;
	}

	/**
	 * Returns class of TypeVariable.
	 *
	 * @param clazz
	 * @param typeVariable
	 * @return
	 */
	private static DeclaredType getDeclaredTypeVariable(final Class<?> clazz,
			final TypeVariable<?> typeVariable) {
		final int index = getIndexTypeVariable(typeVariable);
		final VariableInfo info = new VariableInfo(index);
		DeclaredType declaredType = getDeclaredTypeVariableDeclared(clazz, clazz.getSuperclass(),
				typeVariable, info);
		if (declaredType == null) {
			if (info.isInSuperclass()) {
				final ParameterizedType pType = (ParameterizedType) info.getFirstClass()
						.getGenericSuperclass();
				final Type type = pType.getActualTypeArguments()[info.getIndex()];
				if (type instanceof TypeVariable) {
					declaredType = getDeclaredType(clazz, ((TypeVariable<?>) type).getBounds()[0]);
				} else {
					declaredType = getDeclaredType(clazz, type);
				}
			} else {
				declaredType = getDeclaredType(clazz,
						info.getFirstClass().getTypeParameters()[info.getIndex()].getBounds()[0]);
			}
			return declaredType;
		}
		return declaredType;
	}

	/**
	 * Returns class of TypeVariable.
	 *
	 * @param clazz
	 * @param superClass
	 * @param typeVariable
	 * @param info
	 * @return
	 */
	private static DeclaredType getDeclaredTypeVariableDeclared(final Class<?> clazz,
			final Class<?> superClass, final TypeVariable<?> typeVariable, final VariableInfo info) {
		if (clazz.equals(Object.class)) {
			return null;
		}

		if (typeVariable.getGenericDeclaration().equals(superClass)
				|| typeVariable.getGenericDeclaration().equals(clazz)) {
			final int index = getIndexTypeVariable(typeVariable);
			if (typeVariable.getGenericDeclaration().equals(clazz)) {
				info.setIndex(index);
				info.setFirstClass((Class<?>) typeVariable.getGenericDeclaration());
				info.setInSuperclass(false);
				return null;
			} else {
				final ParameterizedType pType = (ParameterizedType) clazz.getGenericSuperclass();
				final Type type = pType.getActualTypeArguments()[index];
				if (type instanceof TypeVariable) {
					info.setIndex(getIndexTypeVariable((TypeVariable<?>) type));
					info.setFirstClass(clazz);
					info.setInSuperclass(true);
					return null;
				}
				return getDeclaredType(clazz, type);
			}
		} else {
			final DeclaredType declaredType = getDeclaredTypeVariableDeclared(
					clazz.getSuperclass(), superClass.getSuperclass(), typeVariable, info);
			if (declaredType == null) {
				ParameterizedType parameterizedType = null;
				if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
					parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
					info.setInSuperclass(true);
					info.setFirstClass(clazz);
				} else {
					parameterizedType = (ParameterizedType) superClass.getGenericSuperclass();
					info.setInSuperclass(false);
					info.setFirstClass(superClass);
				}
				final Type type = parameterizedType.getActualTypeArguments()[info.getIndex()];
				if (type instanceof TypeVariable) {
					info.setIndex(getIndexTypeVariable((TypeVariable<?>) type));
					return null;
				}
				return getDeclaredType(clazz, type);
			} else {
				return declaredType;
			}
		}
	}

	/**
	 * Returns position of TypeVariable in list of getTypeParameters on class.
	 *
	 * @param typeVariable
	 * @return
	 */
	private static int getIndexTypeVariable(final TypeVariable<?> typeVariable) {
		int index = -1;
		for (final TypeVariable<?> typeVariable2 : typeVariable.getGenericDeclaration()
				.getTypeParameters()) {
			++index;
			if (typeVariable.getName().equals(typeVariable2.getName())) {
				break;
			}
		}
		return index;
	}

	/**
	 * Auxiliary Class to locate class of TypeVariable
	 */
	private static class VariableInfo {

		private int index;
		private boolean inSuperclass;
		private Class<?> firstClass;

		public Class<?> getFirstClass() {
			return firstClass;
		}

		public void setFirstClass(final Class<?> firstClass) {
			this.firstClass = firstClass;
		}

		public VariableInfo(final int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(final int index) {
			this.index = index;
		}

		public boolean isInSuperclass() {
			return inSuperclass;
		}

		public void setInSuperclass(final boolean inSuperclass) {
			this.inSuperclass = inSuperclass;
		}
	}

	/**
	 * Class to store info of Type
	 */
	public static class DeclaredType {
		/**
		 * Generic items classes
		 */
		private List<DeclaredType> items = new ArrayList<DeclaredType>();
		/**
		 * Class of Type
		 */
		private Class<?> type;

		private List<DeclaredType> typeItems = new ArrayList<DeclaredType>();

		public DeclaredType() {
			// default constructor
		}

		public DeclaredType(final Class<?> type) {
			this.type = type;
		}

		public String getAssign() {
			final StringBuilder stringBuilder = new StringBuilder();
			if (isTypeItems()) {
				stringBuilder.append("? extends ");
				for (int i = 0; i < getTypeItems().size(); i++) {
					stringBuilder.append(getTypeItems().get(i).getAssign());
					if (i + 1 < getTypeItems().size()) {
						stringBuilder.append("& ");
					}
				}
			} else {
				stringBuilder.append(getType().getName());
			}

			if (getItems() != null && !getItems().isEmpty()) {
				stringBuilder.append("<");
				for (int i = 0; i < getItems().size(); i++) {
					final DeclaredType item = getItems().get(i);
					stringBuilder.append(item.getAssign());

					if (i + 1 < getItems().size()) {
						stringBuilder.append(",");
					}
				}
				stringBuilder.append(">");
			}
			return stringBuilder.toString();
		}

		public Class<?> getType() {
			return type;
		}

		public void setType(final Class<?> type) {
			this.type = type;
		}

		public List<DeclaredType> getItems() {
			return items;
		}

		public void setItems(final List<DeclaredType> item) {
			this.items = item;
		}

		public List<DeclaredType> getTypeItems() {
			return typeItems;
		}

		public void setTypeItems(final List<DeclaredType> typeItems) {
			this.typeItems = typeItems;
		}

		public boolean isTypeItems() {
			return (getTypeItems() != null && getTypeItems().size() > 0);
		}

		@Override
		public String toString() {
			return getAssign();
		}
	}

	/**
	 * Returns class on index in the parameterized type on <code>clazz</code> or
	 * <code>super</code>.
	 *
	 * @param clazz
	 * @param index
	 * @return
	 */
	public static Class<?> getIndexClass(final Class<?> clazz, final int index) {
		if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
			final ParameterizedType type = (ParameterizedType) clazz.getGenericSuperclass();
			DeclaredType declaredType = null;
			if (type.getActualTypeArguments().length > index + 1) {
				declaredType = getDeclaredType(clazz, type.getActualTypeArguments()[index]);
			} else {
				declaredType = getDeclaredType(clazz,
						type.getActualTypeArguments()[type.getActualTypeArguments().length - 1]);
			}
			return (Class<?>) declaredType.getType();
		} else if (clazz.getGenericSuperclass() instanceof Class) {
			return getIndexClass(clazz.getSuperclass(), index);
		} else {
			return null;
		}
	}

	/**
	 * Checks if class is noted by <code>annotationClass</code>.
	 *
	 * @param <T>
	 * @param annotationClass
	 * @param clazz
	 * @return
	 */
	public static <T extends Annotation> T getAnnotationInClass(final Class<T> annotationClass,
			final Class<?> clazz) {
		Class<?> baseClass = clazz;
		while (baseClass != null && !baseClass.equals(Object.class)) {
			if (baseClass.isAnnotationPresent(annotationClass)) {
				return baseClass.getAnnotation(annotationClass);
			}

			for (Class<?> iClass : baseClass.getInterfaces()) {
				final T tAnnotation = getAnnotationInClass(annotationClass, iClass);
				if (tAnnotation != null) {
					return tAnnotation;
				}
			}

			baseClass = baseClass.getSuperclass();
		}

		return null;
	}

	/**
	 * Checks if class is noted by <code>annotationClass</code>.
	 *
	 * @param <T>
	 * @param annotationClass
	 * @param clazz
	 * @return
	 */
	public static <T extends Annotation> boolean isAnnotationInClass(
			final Class<T> annotationClass, final Class<?> clazz) {
		return getAnnotationInClass(annotationClass, clazz) != null;
	}

	/**
	 * Checks if field is noted by <code>annotationClass</code>.
	 *
	 * @param <T>
	 * @param annotationClass
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static <T extends Annotation> T getAnnotationInField(final Class<T> annotationClass,
			final Class<?> clazz, final String fieldName) {
		final Field field = getField(clazz, fieldName);
		if (field != null && field.isAnnotationPresent(annotationClass)) {
			return field.getAnnotation(annotationClass);
		}
		final String name = VulpeStringUtil.upperCaseFirst(fieldName);
		Method method = getMethod(clazz, "get".concat(name));
		if (method != null && method.isAnnotationPresent(annotationClass)) {
			return method.getAnnotation(annotationClass);
		}
		method = getMethod(clazz, "set".concat(name));
		if (method != null && method.isAnnotationPresent(annotationClass)) {
			return method.getAnnotation(annotationClass);
		}
		return null;
	}

	/**
	 * Checks if field is noted by <code>annotationClass</code>.
	 *
	 * @param <T>
	 * @param annotationClass
	 * @param clazz
	 * @param field
	 * @return
	 */
	public static <T extends Annotation> T getAnnotationInField(final Class<T> annotationClass,
			final Class<?> clazz, final Field field) {
		if (field.isAnnotationPresent(annotationClass)) {
			return field.getAnnotation(annotationClass);
		}
		final String name = VulpeStringUtil.upperCaseFirst(field.getName());
		Method method = getMethod(clazz, "get".concat(name));
		if (method != null && method.isAnnotationPresent(annotationClass)) {
			return method.getAnnotation(annotationClass);
		}
		method = getMethod(clazz, "set".concat(name));
		if (method != null && method.isAnnotationPresent(annotationClass)) {
			return method.getAnnotation(annotationClass);
		}
		return null;
	}

	/**
	 * Checks if field is noted by <code>annotationClass</code>.
	 *
	 * @param <T>
	 * @param annotationClass
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static <T extends Annotation> boolean isAnnotationInField(
			final Class<T> annotationClass, final Class<?> clazz, final String fieldName) {
		return getAnnotationInField(annotationClass, clazz, fieldName) != null;
	}

	/**
	 * Checks if field is noted by <code>annotationClass</code>.
	 *
	 * @param <T>
	 * @param annotationClass
	 * @param clazz
	 * @param field
	 * @return
	 */
	public static <T extends Annotation> boolean isAnnotationInField(
			final Class<T> annotationClass, final Class<?> clazz, final Field field) {
		return getAnnotationInField(annotationClass, clazz, field) != null;
	}

	/**
	 * Sets field value in object.
	 *
	 * @param object
	 * @param fieldName
	 * @param value
	 */
	public static void setFieldValue(final Object object, final String fieldName, final Object value) {
		try {
			final String name = VulpeStringUtil.upperCaseFirst(fieldName);
			Method method = null;
			Class<?> classField = (value == null ? getFieldClass(object.getClass(), fieldName)
					: value.getClass());
			while (classField != null && !classField.equals(Object.class)) {
				method = getMethod(object.getClass(), "set".concat(name), classField);
				if (method != null) {
					break;
				}
				classField = classField.getSuperclass();
			}
			if (method != null) {
				synchronized (method) {
					boolean setted = false;
					if (!method.isAccessible()) {
						method.setAccessible(true);
						setted = true;
					}
					try {
						method.invoke(object, value);
					} catch (Exception e) {
						LOG.error(e.getMessage());
					} finally {
						if (setted) {
							method.setAccessible(false);
						}
					}
				}
			} else {
				final Field field = getField(object.getClass(), fieldName);
				if (field != null) {
					synchronized (field) {
						boolean setted = false;
						if (!field.isAccessible()) {
							field.setAccessible(true);
							setted = true;
						}
						try {
							if (field.getType().isAssignableFrom(value.getClass())) {
								field.set(object, value);
							} else {
								Object newValue = value;
								if (Long.class.isAssignableFrom(field.getType())) {
									newValue = new Long(value.toString());
								}
								field.set(object, newValue);
							}
						} finally {
							if (setted) {
								field.setAccessible(false);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new VulpeSystemException(e);
		}
	}

	/**
	 * Checks if field exists in class.
	 *
	 * @param object
	 * @param fieldName
	 * @return
	 */
	public static boolean fieldExists(final Class<?> clazz, final String fieldName) {
		boolean exists = false;
		final List<Method> methods = getMethods(clazz);
		for (Method method : methods) {
			final String name = VulpeStringUtil.upperCaseFirst(fieldName);
			if ("get".concat(name).equals(method.getName())) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			final List<Field> fields = getFields(clazz);
			for (Field field : fields) {
				if (fieldName.equals(field.getName())) {
					exists = true;
					break;
				}
			}
		}
		return exists;
	}

	/**
	 * Returns field value from object.
	 *
	 * @param <T>
	 * @param object
	 * @param fieldName
	 * @return
	 */
	public static <T> T getFieldValue(final Object object, final String fieldName) {
		try {
			final String name = VulpeStringUtil.upperCaseFirst(fieldName);
			final Method method = getMethod(object.getClass(), "get".concat(name));
			if (method != null) {
				synchronized (method) {
					boolean setted = false;
					if (!method.isAccessible()) {
						method.setAccessible(true);
						setted = true;
					}
					try {
						return (T) method.invoke(object);
					} catch (Exception e) {
						LOG.error(e.getMessage());
					} finally {
						if (setted) {
							method.setAccessible(false);
						}
					}
				}
			}
			final Field field = getField(object.getClass(), fieldName);
			if (field != null) {
				synchronized (field) {
					boolean setted = false;
					if (!field.isAccessible()) {
						field.setAccessible(true);
						setted = true;
					}
					try {
						return (T) field.get(object);
					} finally {
						if (setted) {
							field.setAccessible(false);
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new VulpeSystemException(e);
		}
	}

	public static <T> T getExpressionValue(Object object, String expression) {
		final String[] expressionParts = expression.split("\\.");
		Object fieldValue = null;
		if (expressionParts.length > 1) {
			int count = 1;
			for (final String part : expressionParts) {
				if (count == expressionParts.length) {
					fieldValue = getFieldValue(object, part);
				} else {
					object = getFieldValue(object, part);
				}
				++count;
			}
		} else {
			fieldValue = getFieldValue(object, expression);
		}
		return (T) fieldValue;
	}

	public static void instanciate(Object object, String expression, Object value) {
		final String[] expressionParts = expression.split("\\.");
		if (expressionParts.length > 1) {
			for (final String part : expressionParts) {
				Object fieldValue = getFieldValue(object, part);
				final Field field = getField(object.getClass(), part);
				if (fieldValue == null) {
					try {
						final Class<?> type = field.getType();
						if (VulpeEntity.class.isAssignableFrom(type)) {
							final Object instance = type.newInstance();
							setFieldValue(object, part, instance);
							object = instance;
						} else {
							setFieldValue(object, part, value);
						}
					} catch (InstantiationException e) {
						LOG.error(e.getMessage());
					} catch (IllegalAccessException e) {
						LOG.error(e.getMessage());
					}
				} else {
					object = fieldValue;
					final Class<?> type = field.getType();
					if (!VulpeEntity.class.isAssignableFrom(type)) {
						setFieldValue(object, part, value);
					}
				}
			}
		} else {
			setFieldValue(object, expression, value);
		}
	}

	public static void fixToJson(Object object) {
		if (object instanceof Collection) {
			final List<?> list = (List<?>) object;
			for (final Object object2 : list) {
				fixObjectToJson(object2, null);
			}
		} else {
			fixObjectToJson(object, null);
		}
	}

	private static void fixObjectToJson(Object object, String parent) {
		final List<Field> fields = getFields(object.getClass());
		final String name = VulpeStringUtil.getAttributeName(object.getClass().getSimpleName());
		for (final Field field : fields) {
			final Class<?> type = field.getType();
			final Object value = getFieldValue(object, field.getName());
			if (value != null) {
				if (VulpeEntity.class.isAssignableFrom(type)) {
					fixEntityToJson(value, parent == null ? name : parent);
				} else if (value instanceof Collection) {
					final List<?> list = (List<?>) value;
					for (final Object object2 : list) {
						if (object2 != null) {
							fixObjectToJson(object2, name);
						}
					}
				}
			}
		}
	}

	private static void fixEntityToJson(Object value, String name) {
		final List<Field> relationFields = getFields(value.getClass());
		for (final Field field : relationFields) {
			final String fieldName = field.getName();
			final Object fieldValue = getFieldValue(value, fieldName);
			if (fieldValue != null) {
				if (fieldName.startsWith(name) && fieldValue instanceof Collection) {
					final List<?> list = (List<?>) fieldValue;
					for (final Object object2 : list) {
						if (object2 != null) {
							fixEntityToJson(object2, name);
						}
					}
				} else if (fieldName.equals(name)) {
					final VulpeEntity entity = getFieldValue(value, fieldName);
					try {
						final VulpeEntity simple = (VulpeEntity) entity.getClass().newInstance();
						simple.setId(entity.getId());
						setFieldValue(value, fieldName, simple);
					} catch (Exception e) {
						LOG.error(e.getMessage());
					}
					fixObjectToJson(value,
							VulpeStringUtil.getAttributeName(value.getClass().getSimpleName()));
				}
			}
		}
	}
}