package org.vulpe.controller.struts.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import ognl.NullHandler;
import ognl.Ognl;

import org.apache.log4j.Logger;
import org.vulpe.common.ReflectUtil;
import org.vulpe.common.ReflectUtil.DeclaredType;
import org.vulpe.model.entity.VulpeBaseEntity;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.util.InstantiatingNullHandler;
import com.opensymphony.xwork2.util.OgnlContextState;
import com.opensymphony.xwork2.util.OgnlUtil;

/**
 * Classe utilizada para corrigir problemas ao instanciar tipos genericos e
 * Set's.
 *
 * @author <a href="mailto:fabio.viana@activethread.com.br">F�bio Viana</a>
 */
@SuppressWarnings("unchecked")
public class GenericsNullHandler<ENTITY extends VulpeBaseEntity<ID>, ID extends Serializable & Comparable>
		extends InstantiatingNullHandler implements NullHandler {

	private static final Logger LOG = Logger
			.getLogger(GenericsNullHandler.class);

	/*
	 * (non-Javadoc)
	 *
	 * @seecom.opensymphony.xwork2.conversion.impl.InstantiatingNullHandler#
	 * nullPropertyValue(java.util.Map, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object nullPropertyValue(final Map context, final Object target,
			final Object property) {
		try {
			final boolean createNullObjects = OgnlContextState
					.isCreatingNullObjects(context);
			if (property != null && createNullObjects) {
				final Object realTarget = OgnlUtil.getRealTarget(property
						.toString(), context, target);
				if (realTarget != null) {
					Class clazz = null;
					final Field field = ReflectUtil.getInstance().getField(
							realTarget.getClass(), property.toString());
					if (field != null) {
						if (!field.getType().equals(field.getGenericType())) {
							final DeclaredType declaredType = ReflectUtil
									.getInstance().getDeclaredType(
											realTarget.getClass(),
											field.getGenericType());
							if (!Collection.class.isAssignableFrom(declaredType
									.getType())
									&& declaredType.getType() != Map.class) {
								clazz = declaredType.getType();
							}
						} else if (Set.class.isAssignableFrom(field.getType())) {
							clazz = Set.class;
						}
					}
					if (clazz != null) {
						final ObjectFactory objectFactory = new ObjectFactory();
						final Object param = objectFactory.buildBean(clazz,
								context);
						if (param != null) {
							Ognl.setValue(property.toString(), context,
									realTarget, param);
							return param;
						}
					}
				}
			}
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG
						.debug(
								"N�o foi poss�vel instanciar atributos declarados com Generics.",
								e);
			}
		}
		return super.nullPropertyValue(context, target, property);
	}
}