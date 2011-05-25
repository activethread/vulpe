<%@include file="/WEB-INF/protected-jsp/commons/common.jsp"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="v"%>
<v:hidden property="id" />
<v:hidden property="data" />
<table width="100%" cellpadding="0" cellspacing="0">
	<tr>
		<td><v:select labelKey="label.gmn.ministerio.Relatorio.crud.mes" property="mes"
			showBlank="true" autoLoad="false" required="true" /></td>
		<td><v:select labelKey="label.gmn.ministerio.Relatorio.crud.tipoMinisterio"
			property="tipoMinisterio" autoLoad="false" required="true" /></td>
		<td><v:selectPopup labelKey="label.gmn.ministerio.Relatorio.crud.publicador"
			autocompleteMinLength="1" property="publicador" identifier="id" description="nome"
			action="/core/Publicador/select" popupId="publicadorSelectPopup"
			popupProperties="publicador.id=id,publicador.nome=nome" size="40" popupWidth="420px"
			autocomplete="true" required="true" /></td>
	</tr>
	<tr>
		<td colspan="3">
		<table width="100%" cellpadding="0" cellspacing="0" style="margin-left: 0px;">
			<tr>
				<td><v:text labelKey="label.gmn.ministerio.Relatorio.crud.livros" property="livros"
					mask="I" size="8" maxlength="8" /></td>
				<td><v:text labelKey="label.gmn.ministerio.Relatorio.crud.brochuras" property="brochuras"
					mask="I" size="8" maxlength="8" /></td>
				<td><v:text labelKey="label.gmn.ministerio.Relatorio.crud.horas" property="horas" mask="I"
					size="8" maxlength="10" style="background-color: #FFFFDD" /></td>
				<td><v:text labelKey="label.gmn.ministerio.Relatorio.crud.revistas" property="revistas"
					mask="I" size="8" maxlength="8" /></td>
				<td><v:text labelKey="label.gmn.ministerio.Relatorio.crud.revisitas" property="revisitas"
					mask="I" size="8" maxlength="8" /></td>
				<td><v:text labelKey="label.gmn.ministerio.Relatorio.crud.estudos" property="estudos"
					mask="I" size="8" maxlength="8" /></td>
			</tr>
		</table>
		</td>
	</tr>
</table>