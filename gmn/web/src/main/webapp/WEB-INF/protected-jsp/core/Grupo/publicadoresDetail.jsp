<%@include file="/WEB-INF/protected-jsp/commons/common.jsp"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="v" %>
<v:table>
	<jsp:attribute name="tableBody">
		<v:row>
			<v:column>
			<table width="100%" cellpadding="0" cellspacing="0">
				<tr>
					<td class="titulo" colspan="2">
						<v:label key="label.gmn.core.Grupo.crud.publicadores.nome" />
					</td>
					<td class="titulo">
						<v:label key="label.gmn.core.Grupo.crud.publicadores.sexo" />
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<v:text property="nome" size="40" maxlength="60" required="true" />
					</td>
					<td>
						<v:select property="sexo" showBlank="true" autoLoad="false" required="true" />
					</td>
				</tr>
				<tr>
					<td class="titulo">
						<v:label key="label.gmn.core.Grupo.crud.publicadores.indicador" />
					</td>
					<td class="titulo">
						<v:label key="label.gmn.core.Grupo.crud.publicadores.leitor" />
					</td>
					<td class="titulo">
						<v:label key="label.gmn.core.Grupo.crud.publicadores.microfone" />
					</td>
				</tr>
				<tr>
				<td>
				<v:checkbox property="indicador" fieldValue="true" />
				</td>
				<td>
				<v:checkbox property="leitor" fieldValue="true" />
				</td>
				<td>
				<v:checkbox property="microfone" fieldValue="true" />
			</td>
				</tr>
			</table>
			</v:column>
		</v:row>
	</jsp:attribute>
</v:table>