<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java" 
	import="java.util.*,edu.pitt.sis.paws.kt2.*" 
	errorPage="" %>
	
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon">
<link rel="shortcut icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon"> 
<title>Portal for Adaptive Teaching and Learning. Authentication</title>
<script type="text/javascript">
<!--
	if (top != self) 
		top.location.href = self.location.href;
-->
</script>
</head>

<body>
<%
	//	ClientDaemon cd = (ClientDaemon)ClientDaemon.getInstance(application);
	Enumeration enu = session.getAttributeNames();
	for(;enu.hasMoreElements();)
		session.removeAttribute((String)enu.nextElement());
%>
<CENTER><img src="<%=request.getContextPath()%>/assets/KnowledgeTreeLogo2.gif" alt="Knowledge Tree Logo" align="middle"></CENTER>
<form action="j_security_check" method="post">
	<table border="0" cellspacing="2" cellpadding="2" align="center">
		<tr> 
			<td width="50">Login</td>
			<td width="150"><input id="j_username" name="j_username" type="text" value="" size="25" maxlength="15"></td>
		</tr>
		<tr> 
			<td width="50">Password</td>
			<td width="150"><input id="j_password" name="j_password" type="password" value="" size="25" maxlength="15"></td>
		</tr>
		<tr> 
			<td><input type="reset" value="Reset"></td>
			<td align="right"><input type="Submit" value="Login"></td>
		</tr>
	</table>
</form>

<script type="text/javascript">
	document.getElementById("j_username").focus();
</script>
</body>
</html>


