package edu.pitt.sis.paws.webex;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: JeliotJNLPGenerator
 * 
 */
public class JeliotJNLPGenerator extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
{
	static final long serialVersionUID = 111L;

	public static final String jeliot_codebase = "http://adapt2.sis.pitt.edu/webex/";
	private static final String REQ_SOURCE = "src";
	private static final String REQ_USERID = "usr";
	private static final String REQ_GROUPID = "grp";
	private static final String REQ_SESSIONID = "sid";
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String req_source = request.getParameter(REQ_SOURCE);
		String req_userid = request.getParameter(REQ_USERID);
		String req_groupid = request.getParameter(REQ_GROUPID);
		String req_sessionid = request.getParameter(REQ_SESSIONID);
		// Start the HTML
		response.setContentType("application/x-java-jnlp-file");
		PrintWriter out = response.getWriter();
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
		
		out.println("<jnlp spec='1.0+'");
		out.println("	codebase='" + jeliot_codebase + "'");
		out.println("	href='jeliot_webex.jnlp" + 
				"?codebase=" + URLEncoder.encode(jeliot_codebase, "UTF-8") +
				"&" + REQ_SOURCE + "=" + URLEncoder.encode(req_source, "UTF-8").replaceAll("&", "&amp;") + 
				"&" + REQ_USERID + "=" + req_userid + 
//				"&" + REQ_GROUPID + "=" + req_groupid + 
				"&" + REQ_SESSIONID + "=" + req_sessionid + 
				"&q=0'"+ 
				">");
		out.println("	<information>");
		out.println("		<title>Jeliot 3</title>");
		out.println("		<vendor>University of Joensuu</vendor>");
		out.println("		<description>Jeliot 3, the program animation tool</description>");
		out.println("		<offline-allowed />");
		out.println("	</information>");
		out.println("	<security>");
		out.println("		<all-permissions />");
		out.println("	</security>");
		out.println("	<resources>");
		out.println("		<j2se version='1.4+' />");
		out.println("		<jar href='jeliot.jar' />");
		out.println("	</resources>");
		out.println("	<application-desc main-class='jeliot.AdaptJeliot'>");
		out.println("		<argument>" + req_source.replaceAll("&", "&amp;") + "</argument>"); // source
		out.println("		<argument>" + req_userid + "</argument>"); // userid
//		out.println("		<argument>" + req_groupid + "</argument>"); // groupid
		out.println("		<argument>" + req_sessionid + "</argument>"); // sessionid
		out.println("		<argument>0</argument>");
		out.println("	</application-desc>");
		out.println("</jnlp>");
		out.close();

	}
}