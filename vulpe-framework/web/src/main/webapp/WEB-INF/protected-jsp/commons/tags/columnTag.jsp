<%@include file="/WEB-INF/protected-jsp/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/protected-jsp/commons/tags/headerTag.jsp" %>
<c:if test="${empty targetName}">
	<c:if test="${empty targetConfig}">
		<c:set var="targetName" value="entity"/>
	</c:if>
	<c:if test="${not empty targetConfig}">
		<c:set var="targetName" value="${targetConfigPropertyName}[${currentStatus.index}]"/>
	</c:if>
</c:if>
<c:if test="${empty targetValue}">
	<c:if test="${empty targetConfig}">
		<c:set var="targetValueEL" value="${'${'}${targetName}${'}'}"/>
		<c:set var="targetValue" value="${util:eval(pageContext, targetValueEL)}"/>
	</c:if>
	<c:if test="${not empty targetConfig}">
		<c:set var="targetValue" value="${currentItem}"/>
	</c:if>
</c:if>
<c:if test="${show eq true}">
	<c:if test="${not empty elementId}">
		<c:set var="elementId">id="${elementId}" </c:set>
	</c:if>
	<c:if test="${not empty styleClass}">
		<c:set var="styleClass"> class="${styleClass}"</c:set>
	</c:if>
	<c:if test="${empty isHeaderTableTag || isHeaderTableTag}">
		<c:if test="${not empty labelKey}">
			<fmt:message key="${labelKey}" var="label"/>
		</c:if>
		<c:if test="${not empty width}">
			<c:set var="width">width="${width}" </c:set>
		</c:if>
		<c:if test="${not empty labelAlign}">
			<c:set var="labelStyle" value="text-align: ${labelAlign};${labelStyle}"/>
		</c:if>
		<c:if test="${sort eq true && not empty sortPropertyInfoTableTag && not empty property}">
			<c:if test="${empty alias}">
				<c:set var="alias" value="obj"/>
			</c:if>
			<c:choose>
			<c:when test="${global['useDB4O']}">
				<c:set var="elementId">id="${sortPropertyInfoTableTag}-${property}" </c:set>
				<c:set var="label"><a href="javascript:void(0);" onclick="javascript:vulpe.view.sortTable('${vulpeFormName}', '${sortPropertyInfoTableTag}', '${property}');">${label}</a></c:set>
			</c:when>
			<c:otherwise>
				<c:set var="elementId">id="${sortPropertyInfoTableTag}-${alias}-${property}" </c:set>
				<c:set var="label"><a href="javascript:void(0);" onclick="javascript:vulpe.view.sortTable('${vulpeFormName}', '${sortPropertyInfoTableTag}', '${alias}.${property}');">${label}</a></c:set>
			</c:otherwise>
			</c:choose>
		</c:if>
		<c:if test="${not empty onclick}">
			<c:set var="onclick">onclick="${onclick}" </c:set>
		</c:if>
		<c:if test="${not empty onmouseover}">
			<c:set var="onmouseover">onmouseover="${onmouseover}" </c:set>
		</c:if>
		<c:if test="${not empty onmouseout}">
			<c:set var="onmouseout">onmouseout="${onmouseout}" </c:set>
		</c:if>
		<c:if test="${not empty labelStyle}">
			<c:set var="labelStyle"> style="${labelStyle}"</c:set>
		</c:if>
		<c:if test="${empty styleClass}">
			<c:set var="styleClass"> class="vulpeColumnHeader"</c:set>
		</c:if>
		<th ${elementId}${onclick}${onmouseover}${onmouseout}colspan="${colspan}" ${width}scope="col"${labelStyle}${styleClass}>
			${label}
			<c:if test="${showBodyInHeader}">
				<jsp:doBody/>
			</c:if>
		</th>
	</c:if>
	<c:if test="${empty listName}">
		<c:set var="listName" value="entities"/>
	</c:if>
	<c:if test="${!isHeaderTableTag}">
		<c:if test="${not empty align}">
			<c:set var="style" value="text-align: ${align};${style}"/>
		</c:if>
		<c:if test="${empty value && not empty property && not empty currentItem}">
			<c:set var="valueEL" value="${'${'}currentItem.${property}${'}'}"/>
			<c:set var="value" value="${util:eval(pageContext, valueEL)}"/>
			<c:set var="propertyValue" value="${util:enumInField(targetValue, property, value)}"/>
			<c:if test="${not empty propertyValue}">
				<c:set var="value" value="${propertyValue}"/>
			</c:if>
		</c:if>
		<c:if test="${not empty onclick}">
			<c:set var="onclick">onclick="${onclick}" </c:set>
		</c:if>
		<c:if test="${not empty onmouseover}">
			<c:set var="onmouseover">onmouseover="${onmouseover}" </c:set>
		</c:if>
		<c:if test="${not empty onmouseout}">
			<c:set var="onmouseout">onmouseout="${onmouseout}" </c:set>
		</c:if>
		<c:if test="${not empty selectCheckOff && empty onclick}">
			<c:set var="onclick">onclick="${selectCheckOff}" </c:set>
		</c:if>
		<c:if test="${not empty colspan}">
			<c:set var="colspan">colspan="${colspan}" </c:set>
		</c:if>
		<c:if test="${not empty style}">
			<c:set var="style">style="${style}"</c:set>
		</c:if>
		<c:if test="${empty styleClass}">
			<c:set var="styleClass"> class="vulpeColumn"</c:set>
		</c:if>
		<td ${elementId}${onclick}${onmouseover}${onmouseout}${colspan}${style}${styleClass}>
			<c:if test="${not empty value}">
				<c:choose>
				<c:when test="${!isImage}">
					<c:choose>
					<c:when test="${not empty booleanTo}">
						${util:booleanTo(value, booleanTo)}
					</c:when>
					<c:when test="${not empty enumType}">
						<c:set var="enumerationEL" value="${'${'}cachedEnumArray['${enumType}']${'}'}"/>
						<c:set var="enumeration" value="${util:eval(pageContext, enumerationEL)}"/>
						<c:set var="items" value="${fn:replace(enumeration, '#{', '')}"/>
						<c:set var="items" value="${fn:replace(items, '}', '')}"/>
						<c:set var="items" value="${fn:replace(items, '\\'', '')}"/>
						<c:set var="items" value="${fn:replace(items, ' ', '')}"/>
						<c:set var="enumeration" value=""/>
						<c:forEach var="v" items="${value}" varStatus="status">
							<c:if test="${status.index > 0}">
								<c:set var="enumeration" value="${enumeration}, "/>
							</c:if>
							<c:forEach var="item" items="${fn:split(items, ',')}">
								<c:set var="itemValue" value="${fn:split(item, ':')}"/>
								<c:if test="${v == itemValue[0]}">
								<c:set var="description"><fmt:message key="${itemValue[1]}"/></c:set>
								<c:set var="enumeration" value="${enumeration}${description}"/>
								</c:if>
							</c:forEach>
						</c:forEach>
						${enumeration}
					</c:when>
					<c:otherwise>
						${util:toString(value)}
					</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<c:if test="${empty imageWidth}">
						<c:set var="imageWidth" value="50"/>
					</c:if>
					<c:if test="${empty imageHeight}">
						<c:set var="imageHeight" value="38"/>
					</c:if>
					<c:set var="key" value="${listName}[${currentStatus.index}].${property}"/>
					<center>
						<img border="0" src="${util:linkImage(pageContext, key, 'image/jpeg', '', imageWidth, null)}" width="${imageWidth}" height="${imageHeight}" class="vulpeThumb"/>
					</center>
				</c:otherwise>
				</c:choose>
			</c:if>
			<jsp:doBody/>
		</td>
	</c:if>
</c:if>