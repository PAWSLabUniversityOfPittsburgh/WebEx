<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'> 
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel='StyleSheet' href='assets/rest.css' type='text/css' /> 
<title>WebEx - Examples on the Web</title>
</head>
<style>
h3 { border-bottom:1px solid black;}
</style>
<body>

<%!
	String full_context_path = "";
	String rss_link_1 = "";
	String rss_link_2 = "";
	String rss1_valid_icon = "";
	String rss2_valid_icon = "";
%>
<%
	full_context_path = "http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath();
%>


<table cellpadding='2px' cellspacing='0px' class='dkcyan_table' width='550px'>
	<caption class='dkcyan_table_caption'>WebEx</caption>
	<tr>
		<td>
			<div><img src="<%=request.getContextPath()%>/assets/webex_rss_image.gif" width="90" height="30" /></div>
			<h3>Overview</h3>
			<div>Web Examples (WebEx) is an online tool for exploring code examples. Each example is dissected into lines. Each line is supplied with explanatory annotation. WebEx serves both in plain view mode and in disected mode. More information can be found <a target="_blank" href="http://www.sis.pitt.edu/~taler/WebEx.html">here</a>.</div>
			<h3>Use WebEx</h3>
			<div>Try WebEx and other tools at our <a href="http://adapt2.sis.pitt.edu/cbum">Community Portal <img style="vertical-align:bottom;" src="<%=request.getContextPath()%>/assets/CommunityPortalIcon_24.gif" border="0" /></a></div>
			<div>Registered users access WebEx via <a href="http://adapt2.sis.pitt.edu/kt/">Knowledge Tree Portal <img style="vertical-align:bottom;" src="<%=request.getContextPath()%>/assets/KnowledgeTreeIcon_24.gif" border="0"/></a></div>
			<h3>Syndicate WebEx</h3>
			<h4>Open Feeds (no user modeling support or adaptation)</h4>
			<%
//				rss_link_1 = URLEncoder.encode(full_context_path + "/feed?scope=is12&format=rss1.0");
//				rss_link_2 = URLEncoder.encode(full_context_path + "/feed?scope=is12&format=rss2.0");
//				rss1_valid_icon = "<a href='http://feedvalidator.org/check.cgi?url=" + rss_link_1+ "'><img width=44 height=16 border='0' style='vertical-align:bottom;' src='assets/valid-rss.png' alt='[Valid RSS]' title='Validate my RSS feed' /></a>";
//				rss2_valid_icon = "<a href='http://feedvalidator.org/check.cgi?url=" + rss_link_2+ "'><img width=44 height=16 border='0' style='vertical-align:bottom;' src='assets/valid-rss.png' alt='[Valid RSS]' title='Validate my RSS feed' /></a>";
			%>
			<div>IS0012 (Introduction to Programming) C <a target="_blank" href="<%=full_context_path%>/feed?scope=is12&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=is12&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=is12&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			<div>IS0017 (Object Oriented Programming) Java <a target="_blank" href="<%=full_context_path%>/feed?scope=is17&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=is17&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=is17&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			<div>IS2470 (Interactive Systems Design) Java <a target="_blank" href="<%=full_context_path%>/feed?scope=isd&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=isd&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=isd&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			<div>IS2710 (Database Management Systems) SQL <a target="_blank" href="<%=full_context_path%>/feed?scope=sql&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=sql&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=sql&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			<div>IFMG230 (IBack-End Business Applications) VB.NET <a target="_blank" href="<%=full_context_path%>/feed?scope=vb.net&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=vb.net&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=vb.net&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			
			<!-- 
			<h4>Feeds with Authentication. With user modeling and adaptation.</h4>
			<div>IS0012 (Introduction to Programming) Dissections <a target="_blank" href="<%=full_context_path%>/restricted/feed?scope=is12&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=is12&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=is12&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			<div>IS2470 (Interactive Systems Design) Dissections <a target="_blank" href="<%=full_context_path%>/restricted/feed?scope=isd&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=isd&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=isd&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			<div>IS2710 (Database Management Systems) SQL Dissections <a target="_blank" href="<%=full_context_path%>/restricted/feed?scope=sql&format=rss1.0">RSS1.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>, <a target="_blank" href="<%=full_context_path%>/feed?scope=sql&format=rss2.0">RSS2.0</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/>,  <a target="_blank" href="<%=full_context_path%>/feed?scope=sql&format=atom">Atom</a>&nbsp;<img src="<%=full_context_path%>/assets/rss.gif"/></div>
			-->
			<h3>WebEx in RDF</h3>
			<div>All avaliable Examples <a target="_blank" href="<%=full_context_path%>/webex.rdf">RDF</a>&nbsp;<img src="<%=full_context_path%>/assets/rdf.gif"/></div>
			<div>IS0012 (Introduction to Programming) C <a target="_blank" href="<%=full_context_path%>/webex.rdf?scope=is12">RDF</a>&nbsp;<img src="<%=full_context_path%>/assets/rdf.gif"/></div>
			<div>IS0017 (Object Oriented Programming) Java <a target="_blank" href="<%=full_context_path%>/webex.rdf?scope=is17">RDF</a>&nbsp;<img src="<%=full_context_path%>/assets/rdf.gif"/></div>
			<div>IS2470 (Interactive Systems Design) Java <a target="_blank" href="<%=full_context_path%>/webex.rdf?scope=isd">RDF</a>&nbsp;<img src="<%=full_context_path%>/assets/rdf.gif"/></div>
			<div>IS2710 (Database Management Systems) SQL <a target="_blank" href="<%=full_context_path%>/webex.rdf?scope=sql">RDF</a>&nbsp;<img src="<%=full_context_path%>/assets/rdf.gif"/></div>
			<div>IFMG300 (Back-End Business Applications) VB.NET <a target="_blank" href="<%=full_context_path%>/webex.rdf?scope=vb.net">RDF</a>&nbsp;<img src="<%=full_context_path%>/assets/rdf.gif"/></div>
		</td>
	</tr>

	<tr>
	  <td>&nbsp;</td>
    </tr>
	<tr>
	  <td class='dkcyan_table_footer'>Michael V. Yudelson &copy; 2007-2008</td>
    </tr>
</table>

</body>
</html>
