<%@include file="/WEB-INF/protected-jsp/commons/common.jsp"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="v"%>
<v:table>
	<jsp:attribute name="tableBody">
		<v:row>
			<v:column>
			<table width="100%" cellpadding="0" cellspacing="0" style="font-size: 10px;">
				<tr>
					<td width="90%">
						<v:label key="label.gmn.publicacoes.Pedido.crud.publicacoes.publicacao"/>
					</td>
					<td>
						<v:label key="label.gmn.publicacoes.Pedido.crud.publicacoes.quantidade"/>
					</td>
				</tr>
				<tr>
					<td>
						<v:selectPopup property="publicacao"
							identifier="id" description="nome"
							action="/publicacoes/Publicacao/select" popupId="publicacaoSelectPopup"
							popupProperties="publicacao.id=id,publicacao.nome=nome"
							size="30" popupWidth="420px" autocomplete="true" autocompleteMinLength="1"
						/>
					</td>
					<td>
						<v:text property="quantidade"
							mask="I"
							size="5"
							maxlength="5"
						/>
					</td>
				</tr>
				<tr>
					<td>
						<v:label key="label.gmn.publicacoes.Pedido.crud.publicacoes.entregue"/>
					</td>
					<td>
						<v:label key="label.gmn.publicacoes.Pedido.crud.publicacoes.quantidadeEntregue"/>
					</td>
				</tr>
				<tr>
					<td>
						<v:checkbox property="entregue"
							fieldValue="true" onclick="return app.publicacoes.preencherQuantidadeEntregue(this)"
						/>
					</td>
					<td>
						<v:text property="quantidadeEntregue"
							mask="I"
							size="5"
							maxlength="5"
						/>
					</td>
				</tr>
			</table>
			</v:column>
		</v:row>
	</jsp:attribute>
</v:table>