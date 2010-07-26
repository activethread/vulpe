<%@include file="/WEB-INF/protected-jsp/commons/tags/tagAttributes.jsp"%>
<%@ attribute name="property" required="false" rtexprvalue="true"%>
<%@ attribute name="mask" required="false" rtexprvalue="true"%>
<%@ attribute name="upperCase" required="false" rtexprvalue="true" type="java.lang.Boolean"%>
<%@ attribute name="maxlength" required="false" rtexprvalue="true"%>
<%@ attribute name="readonly" required="false" rtexprvalue="true"%>
<%@ attribute name="size" required="false" rtexprvalue="true"%>
<%@ attribute name="onselect" required="false" rtexprvalue="true"%>

<%@include file="/WEB-INF/protected-jsp/commons/taglibs.jsp"%>

<c:set var="show" value="${true}" />
<c:if test="${not empty logged && logged eq true && util:isLogged(pageContext) eq false}">
	<c:set var="show" value="${false}" />
</c:if>
<c:if test="${not empty role && util:isRole(pageContext, role) eq false}">
	<c:set var="show" value="${false}" />
</c:if>

<c:if test="${show eq true}">
	<%@include file="/WEB-INF/protected-jsp/commons/tags/beginTag.jsp"%>

	<c:if test="${not empty property && empty name}">
		<c:set var="name" value="${targetName}.${property}" />
	</c:if>

	<c:if test="${not empty property && empty value}">
		<c:set var="valueEL" value="${'${'}targetValue.${property}${'}'}" />
		<c:set var="value" value="${util:eval(pageContext, valueEL)}" />
	</c:if>

	<c:if test="${not empty mask}">
		<c:choose>
			<c:when test="${mask eq 'N'}">
				<script type="text/javascript">
			jQuery(function($){
				vulpe.util.get('${elementId}').maskMoney({symbol:"R$",decimal:",",thousands:"."});
			});
		</script>
				<c:set var="style" value="text-align:right; ${style}" />
			</c:when>
			<c:when test="${mask eq 'I'}">
				<script type="text/javascript">
			jQuery(function($){
				vulpe.util.get('${elementId}').numeric();
			});
		</script>
				<c:set var="style" value="text-align:right; ${style}" />
			</c:when>
			<c:otherwise>
				<script type="text/javascript">
			jQuery(function($){
				vulpe.util.get('${elementId}').mask("${mask}");
			});
		</script>
				<c:if test="${empty size}">
					<c:set var="size" value="${fn:length(mask)}" />
				</c:if>
				<c:if test="${empty maxlength}">
					<c:set var="maxlength" value="${size}" />
				</c:if>
			</c:otherwise>
		</c:choose>
	</c:if>

	<c:if test="${empty size && not empty maxlength}">
		<c:set var="size" value="${maxlength}" />
	</c:if>

	<c:if test="${not empty property && util:isFieldInValidator(targetValue, property)}">
		<c:set var="onblur"
			value="validate${fn:toUpperCase(fn:substring(property, 0, 1))}${fn:substring(property, 1, -1)}(); ${onblur}" />
	</c:if>

	<c:if test="${onlyToSee}">
		<c:set var="showAsText" value="${true}" />
	</c:if>
	<c:choose>
		<c:when test="${showAsText}">
			<span onclick="${onclick}" ondblclick="${ondblclick}" onkeydown="${onkeydown}" onkeypress="${onkeypress}" onkeyup="${onkeyup}" onmousedown="${onmousedown}" onmousemove="${onmousemove}" onmouseout="${onmouseout}" onmouseover="${onmouseover}" onmouseup="${onmouseup}" style="${style}" class="vulpeSimpleLabel ${styleClass}" id="${elementId}" title="${title}" >${value}</span>
		</c:when>
		<c:otherwise>
			<input type="text" name="${name}" <c:if test="${not empty accesskey}"> accesskey="${accesskey}"</c:if><c:if test="${disabled}">disabled="${disabled}"</c:if><c:if test="${not empty maxlength && maxlength > size}">maxlength="${maxlength}"</c:if><c:if test="${not empty onblur}"> onblur="${onblur}"</c:if><c:if test="${not empty onchange}"> onchange="${onchange}"</c:if><c:if test="${not empty onclick}"> onclick="${onclick}"</c:if><c:if test="${not empty ondblclick}"> ondblclick="${ondblclick}"</c:if><c:if test="${not empty onfocus}"> onfocus="${onfocus}"</c:if><c:if test="${not empty onkeydown}"> onkeydown="${onkeydown}"</c:if><c:if test="${not empty onkeypress}"> onkeypress="${onkeypress}"</c:if><c:if test="${not empty onkeyup}"> onkeyup="${onkeyup}"</c:if><c:if test="${not empty onmousedown}"> onmousedown="${onmousedown}"</c:if><c:if test="${not empty onmousemove}"> onmousemove="${onmousemove}"</c:if><c:if test="${not empty onmouseout}"> onmouseout="${onmouseout}"</c:if><c:if test="${not empty onmouseover}"> onmouseover="${onmouseover}"</c:if><c:if test="${not empty onmouseup}"> onmouseup="${onmouseup}"</c:if><c:if test="${not empty onselect}"> onselect="${onselect}"</c:if><c:if test="${readonly}"> readonly="${readonly}"</c:if> size="${size}"<c:if test="${not empty style}"> style="${style}"</c:if><c:if test="${not empty styleClass}"> class="${styleClass}"</c:if> id="${elementId}" tabindex="${tabindex}" <c:if test="${not empty title}">title="${title}"</c:if> value="${value}" />
		</c:otherwise>
	</c:choose>
	<jsp:doBody />
	<%@include file="/WEB-INF/protected-jsp/commons/tags/endTag.jsp"%>
	<script type="text/javascript">
		jQuery(function($){
			<c:if test="${upperCase && empty mask}">
			vulpe.util.get('${elementId}').bestupper();
			</c:if>
			vulpe.util.get('${elementId}').focus(function() {
				$(this).effect("highlight");
			});
		});
	</script>
</c:if>