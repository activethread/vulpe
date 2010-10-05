package br.gov.pbh.prodabel.transfere.core.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vulpe.commons.VulpeConstants.Controller.Forward;
import org.vulpe.commons.annotations.DetailConfig;
import org.vulpe.commons.annotations.Quantity;
import org.vulpe.commons.annotations.Quantity.QuantityType;
import org.vulpe.commons.beans.Tab;
import org.vulpe.commons.beans.ValueBean;
import org.vulpe.controller.annotations.Controller;
import org.vulpe.controller.annotations.Select;
import org.vulpe.controller.commons.VulpeControllerConfig.ControllerType;
import org.vulpe.exception.VulpeApplicationException;

import br.gov.pbh.prodabel.transfere.commons.ApplicationConstants.Now;
import br.gov.pbh.prodabel.transfere.commons.ApplicationConstants.Sessao;
import br.gov.pbh.prodabel.transfere.core.model.entity.Ambiente;
import br.gov.pbh.prodabel.transfere.core.model.entity.Objeto;
import br.gov.pbh.prodabel.transfere.core.model.entity.Sistema;
import br.gov.pbh.prodabel.transfere.core.model.entity.SistemaUsuario;
import br.gov.pbh.prodabel.transfere.core.model.entity.Status;
import br.gov.pbh.prodabel.transfere.core.model.services.CoreService;

/**
 * Controller implementation of Objeto
 */
@Component("core.ObjetoController")
@SuppressWarnings("serial")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller(serviceClass = CoreService.class, detailsConfig = { @DetailConfig(name = "objetoItens", propertyName = "entity.objetoItens", despiseFields = "nomeObjeto", startNewDetails = 5, newDetails = 1, quantity = @Quantity(type = QuantityType.ONE_OR_MORE)) }, select = @Select(pageSize = 5, requireOneFilter = true))
public class ObjetoController extends ObjetoBaseController {

	private static final List<ValueBean> origem = new ArrayList<ValueBean>();
	private static final List<ValueBean> destino = new ArrayList<ValueBean>();

	@Override
	public void postConstruct() {
		super.postConstruct();
		final String keyPrefix = Ambiente.class.getName();
		if (origem.isEmpty()) {
			origem.add(new ValueBean(Ambiente.H.name(), getText(keyPrefix.concat(".").concat(
					Ambiente.H.name()))));
			origem.add(new ValueBean(Ambiente.P.name(), getText(keyPrefix.concat(".").concat(
					Ambiente.P.name()))));
		}
		now.put(Now.ORIGEM, origem);
		if (destino.isEmpty()) {
			destino.add(new ValueBean(Ambiente.D.name(), getText(keyPrefix.concat(".").concat(
					Ambiente.D.name()))));
			destino.add(new ValueBean(Ambiente.H.name(), getText(keyPrefix.concat(".").concat(
					Ambiente.H.name()))));
		}
		now.put(Now.DESTINO, destino);
		if (getControllerType().equals(ControllerType.CRUD)) {
			getTabs().put(getControllerConfig().getMasterTitleKey(), new Tab("Atualizar de"));
			getTabs().put(getControllerConfig().getDetail("objetoItens").getTitleKey(),
					new Tab("Objetos a serem Transferidos"));
		}
	}

	@Override
	public void showButtons(Operation operation) {
		super.showButtons(operation);
		if (getControllerType().equals(ControllerType.CRUD)) {
			if (getEntity() != null && getEntity().getId() != null
					&& getEntity().getStatus().equals(Status.N)) {
				final SistemaUsuario sistemaUsuario = getSessionAttribute(Sessao.SISTEMA_USUARIO_SELECIONADO);
				if (sistemaUsuario != null && !sistemaUsuario.getAdministrador()
						&& !getEntity().getUsuario().equals(getUserAuthenticated())) {
					setOnlyToSee(true);
				} else {
					getButtons().put("transferir", true);
				}
			}
		}
	}

	protected void atualizarDadosObjeto() {
		super.atualizarDadosObjeto();
		final Objeto objeto = getEntity();
		objeto.setDescricao("ATUALIZAÇÃO AMBIENTE");
		for (Sistema sistema : sistemas) {
			if (sistema.getId().equals(objeto.getSistema().getId())) {
				objeto.setSistema(sistema);
				break;
			}
		}
		if (objeto.getOrigem().equals(Ambiente.H)) {
			objeto.setOwnerOrigem(objeto.getSistema().getOwnerHomologacao());
		} else {
			objeto.setOwnerOrigem(objeto.getSistema().getOwnerProducao());
		}
		if (objeto.getDestino().equals(Ambiente.D)) {
			objeto.setOwnerDestino("LEGADO");
		} else {
			objeto.setOwnerDestino(objeto.getSistema().getOwnerHomologacao());
		}
	}

	public String transferir() {
		try {
			final String retorno = getService(CoreService.class).transferir(getEntity());
			if (StringUtils.isNotEmpty(retorno) && "OK".equals(retorno.toUpperCase())) {
				setId(getEntity().getId());
				super.update();
				addActionMessage("Transferência realizada com sucesso!");
			}
		} catch (VulpeApplicationException e) {
			LOG.error(e);
		}
		return Forward.SUCCESS;
	}

	@Override
	public String read() {
		final Sistema sistema = getSessionAttribute(Sessao.SISTEMA_SELECIONADO);
		getEntitySelect().setSistema(sistema);
		Calendar calendar = Calendar.getInstance();
		if (getEntitySelect().getDataInicial() != null) {
			calendar.setTime(getEntitySelect().getDataInicial());
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			getEntitySelect().setDataInicial(calendar.getTime());
		}
		if (getEntitySelect().getDataFinal() != null) {
			calendar.setTime(getEntitySelect().getDataFinal());
			calendar.set(Calendar.HOUR, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 99);
			getEntitySelect().setDataFinal(calendar.getTime());
		}
		return super.read();
	}

	@Override
	protected void createPostAfter(Objeto entity) {
		super.createPostAfter(entity);
		getButtons().put("transferir", true);
	}
}
