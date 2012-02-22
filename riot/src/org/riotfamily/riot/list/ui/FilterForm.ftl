<form onsubmit="list.filter(RForm.getValues(this)); return false">
	<#list elements.elements as element>
		<div class="element<#if element.styleClass?exists> ${element.styleClass}-element</#if>">
			<#if element.label?exists>
				<label for="${element.id}">${element.label}</label>
			</#if>
			${element.render()}
		</div>
	</#list>
	<div class="buttons">
		<input type="submit" value="Apply" />
		<input type="button" onclick="window.list.reset(); return false" value="Reset" />
	</div>
</form>
