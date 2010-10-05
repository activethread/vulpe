package org.vulpe.portal.controller;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.vulpe.commons.VulpeConstants.Controller.URI;
import org.vulpe.commons.util.VulpeReflectUtil;
import org.vulpe.commons.util.VulpeValidationUtil;
import org.vulpe.controller.struts.VulpeStrutsController;
import org.vulpe.model.entity.VulpeEntity;
import org.vulpe.portal.commons.ApplicationConstants.Core;
import org.vulpe.portal.commons.model.entity.Status;
import org.vulpe.portal.commons.model.entity.TextTranslate;
import org.vulpe.portal.commons.model.entity.TextTranslateLanguage;
import org.vulpe.portal.core.controller.PortalController;
import org.vulpe.portal.core.model.entity.Portal;

@SuppressWarnings( { "serial", "unchecked" })
public class ApplicationBaseController<ENTITY extends VulpeEntity<ID>, ID extends Serializable & Comparable>
		extends VulpeStrutsController<ENTITY, ID> {

	protected static final Logger LOG = Logger.getLogger(ApplicationBaseController.class);

	@Override
	protected void postConstruct() {
		super.postConstruct();
		final List<Portal> portalList = getCachedClass().getSelf(Portal.class.getSimpleName());
		if (portalList != null) {
			for (Portal portal : portalList) {
				if (portal.getStatus().equals(Status.ACTIVE)) {
					setSessionAttribute(Core.VULPE_PORTAL, portal);
				}
			}
		}
	}

	@Override
	protected ENTITY prepareEntity(Operation operation) {
		final ENTITY entity = super.prepareEntity(operation);
		final List<Field> fields = VulpeReflectUtil.getInstance().getFields(entity.getClass());
		for (final Field field : fields) {
			if (field.getType().getName().equals(TextTranslate.class.getName())) {
				final TextTranslate textTranslate = VulpeReflectUtil.getInstance().getFieldValue(
						entity, field.getName());
				if (textTranslate != null
						&& VulpeValidationUtil.isNotEmpty(textTranslate.getLanguages())) {
					for (final Iterator<TextTranslateLanguage> iterator = textTranslate
							.getLanguages().iterator(); iterator.hasNext();) {
						final TextTranslateLanguage textTranslateLanguage = iterator.next();
						if (StringUtils.isEmpty(textTranslateLanguage.getText())) {
							iterator.remove();
						}
					}
				}
			}
		}
		return entity;
	}

	@Override
	public String select() {
		final String redirecionar = validarCongregacaoSelecionada();
		if (StringUtils.isNotEmpty(redirecionar)) {
			return redirecionar;
		}
		return StringUtils.isNotEmpty(redirecionar) ? redirecionar : super.select();
	}

	@Override
	public String create() {
		final String redirecionar = validarCongregacaoSelecionada();
		return StringUtils.isNotEmpty(redirecionar) ? redirecionar : super.create();
	}

	@Override
	public String update() {
		final String redirecionar = validarCongregacaoSelecionada();
		return StringUtils.isNotEmpty(redirecionar) ? redirecionar : super.update();
	}

	@Override
	public String tabular() {
		final String redirecionar = validarCongregacaoSelecionada();
		return StringUtils.isNotEmpty(redirecionar) ? redirecionar : super.tabular();
	}

	private String validarCongregacaoSelecionada() {
		if (getSessionAttribute(Core.VULPE_PORTAL) == null
				&& !this.getClass().getName().equals(PortalController.class.getName())) {
			if (getRequest().getRequestURI().endsWith(URI.AJAX)) {
				setAjax(true);
			}
			return redirectTo("/core/Portal/select", isAjax());
		}
		return null;
	}
}