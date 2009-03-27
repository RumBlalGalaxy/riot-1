<@template.set stylesheets=[
	"style/group.css", 
	"style/group-custom.css"
] />
<@template.extend file="screen.ftl">
	<@template.block name="content" cache=false>
	
		<div id="body-wrapper">
			<div id="wrapper">
				<div id="editors" class="main">
					<div class="box-title"><span class="label">${context.title!}</span></div>
					<#list context.screen.childScreens as child>
						<a class="editor" href="${c.urlForHandler(child.id, context)}">
							<span class="icon"<#if child.icon??> style="background-image:url(${riot.resource("style/icons/editors/" + child.icon + ".gif")})"</#if>></span>
							<span class="text">
								<div>
									<div class="label">${child.id}</div>
									<div class="description"></div>
								</div>
							</span>
						</a>
					</#list>
				</div>
			</div>
			<div class="extra">
			</div>
		</div>
	</@template.block>
</@template.extend>