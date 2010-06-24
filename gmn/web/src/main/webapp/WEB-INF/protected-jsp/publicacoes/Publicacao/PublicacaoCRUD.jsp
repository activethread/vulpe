<%@include file="/WEB-INF/protected-jsp/commons/common.jsp" %>

<v:hidden property="id"/>
<v:text
	labelKey="label.gmn.publicacoes.Publicacao.crud.codigo"
	property="codigo"
	mask="I"
	size="10"
	maxlength="10"
/>
<v:text
	labelKey="label.gmn.publicacoes.Publicacao.crud.nome"
	property="nome"
	size="40"
	maxlength="40"
	required="true"
/>
<v:select
	labelKey="label.gmn.publicacoes.Publicacao.crud.tipoPublicacao"
	property="tipoPublicacao.id"
	items="TipoPublicacao"
	itemKey="id"
	itemLabel="descricao"
	showBlank="true" autoLoad="true"
	required="true"
/>
<v:select
	labelKey="label.gmn.publicacoes.Publicacao.crud.classificacaoPublicacao"
	property="classificacaoPublicacao"
	showBlank="true" autoLoad="false"
	required="true"
/>