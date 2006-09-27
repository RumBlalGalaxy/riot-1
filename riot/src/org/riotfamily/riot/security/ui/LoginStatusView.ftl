<#import "/spring.ftl" as spring />
<?xml version="1.0" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
		"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title></title>
		<link rel="stylesheet" href="${request.contextPath}${resourcePath}/style/status.css" type="text/css" />
		<script type="text/javascript" language="JavaScript" src="${request.contextPath}${resourcePath}/status.js"></script>
		<script type="text/javascript" language="JavaScript" src="${request.contextPath}${resourcePath}/prototype/prototype.js"></script>
		<script type="text/javascript" language="JavaScript" src="${request.contextPath}${resourcePath}/style/tweak.js"></script>
	</head>
	<body onload="TweakStyle.status()">
		<div id="panel">
			<div id="status">
				<span class="label"><@spring.messageText "label.status.username", "User" />:</span> <span class="value">${sessionData.username?if_exists}</span>
				<span class="label"><@spring.messageText "label.status.lastLogin", "Last login" />: </span><span class="value">${sessionData.lastLoginData?if_exists} [${sessionData.lastLoginIP?if_exists}]</span>
				<a href="${url(servletPrefix + '/logout')}"><@spring.messageText "label.status.logout", "Logout" /></a>
			</div>
		</div>
	</body>
</html>