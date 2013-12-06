package edu.pitt.sis.paws.webex;

import java.io.IOException;
import java.io.PrintWriter;
//import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
//import java.net.*;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.paws.core.utils.*;
import edu.pitt.sis.paws.cbum.*;
import edu.pitt.sis.paws.kt.rest.*;

/**
 * Servlet implementation class for Servlet: WebexRSS
 * 
 */
public class WebexRSS extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet
{
	static final long serialVersionUID = -2L;

	static final String WEBEX_RSS_SCOPE = "scope";

	static final String WEBEX_RSS_MODE = "mode";

	static final String WEBEX_RSS_MODE_TXT = "txt";

	static final String WEBEX_RSS_VERSION = "format";

	static final String WEBEX_RSS_VERSION_RSS1 = "rss1.0";

	static final String WEBEX_RSS_VERSION_RSS2 = "rss2.0";

	static final String WEBEX_RSS_VERSION_ATOM = "atom";

	private SQLManager sqlm = null;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public WebexRSS()
	{
		super();
		sqlm = new SQLManager("java:comp/env/jdbc/webex");
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
//System.out.println("request.getRemoteUser(): " + request.getRemoteUser());		
//System.out.println("request.isUserInRole('user'): " + request.isUserInRole("user"));		
//System.out.println("request.isUserInRole('admin'): " + request.isUserInRole("admin"));		
//System.out.println("request.getUserPrincipal().toString(): " + ((request.getUserPrincipal()!=null)?request.getUserPrincipal().toString():"none"));		
//System.out.println("request.getSession().getId(): " + request.getSession().getId());
//System.out.println("");


		
		String _scope = request.getParameter(WEBEX_RSS_SCOPE);
		String _version = request.getParameter(WEBEX_RSS_VERSION);
		String _mode = request.getParameter(WEBEX_RSS_MODE);

		Map<String, String> params = new HashMap<String, String>();
		params.put(WEBEX_RSS_SCOPE, _scope);
		params.put(WEBEX_RSS_VERSION, _version);
		params.put(WEBEX_RSS_MODE, _mode);
		params.put(RestDataRobot.REST_CONTEXT_PATH, "http://"
				+ request.getServerName()
				+ ((request.getLocalPort() != 80) ? ":"
						+ request.getLocalPort() : "")
				+ request.getContextPath());
		// System.out.println("path="+"http://" + request.getServerName() +
		// request.getContextPath());

		params = getRSS(params, request);

		String status = params.get(RestDataRobot.REST_STATUS);
		PrintWriter out = response.getWriter();

		if (status.equals(RestDataRobot.REST_STATUS_OK))
		{
			String mime = "application/xml; charset=utf-8";
			// if(_version!=null && _version.equals(WEBEX_RSS_VERSION_RSS1))
			// mime = "application/rdf+xml; charset=utf-8";
			// else if(_version!=null &&
			// _version.equals(WEBEX_RSS_VERSION_RSS2))
			// mime = "application/rss+xml; charset=utf-8";
			// else if(_version!=null &&
			// _version.equals(WEBEX_RSS_VERSION_ATOM))
			// mime = "application/atom+xml; charset=utf-8";

			response.setContentType(mime);
			// System.out.println("MIME="+mime);
		}
		else if (status.equals(RestDataRobot.REST_STATUS_ERROR))
			response.setContentType("text/html; charset=utf-8");

		out.println(params.get(RestDataRobot.REST_RESULT));
	}

	private Map<String, String> getRSS(Map<String, String> _parameters,
			HttpServletRequest _request) throws UnsupportedEncodingException

	{
		String result = "";

		String _scope = _parameters.get(WEBEX_RSS_SCOPE);
		String _mode = _parameters.get(WEBEX_RSS_MODE);
		String _format = _parameters.get(WEBEX_RSS_VERSION);
		String _context_path = _parameters.get(RestDataRobot.REST_CONTEXT_PATH);
		
		boolean is_txt_mode = (_mode != null && _mode.length() > 0)?_mode.equals(WEBEX_RSS_MODE_TXT):false;

		String qry = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		if (_scope == null || _scope.length() == 0)
		{
			_parameters.put(RestDataRobot.REST_STATUS,
					RestDataRobot.REST_STATUS_ERROR);
			_parameters.put(RestDataRobot.REST_RESULT, getErrorMessageHTML(
					"Scope of visivility is specified incorretly.",
					_context_path));
		}
		else if (_format == null
				|| (!_format.equals(WEBEX_RSS_VERSION_RSS1)
						&& !_format.equals(WEBEX_RSS_VERSION_RSS2) && !_format
						.equals(WEBEX_RSS_VERSION_ATOM)))
		{
			_parameters.put(RestDataRobot.REST_STATUS, RestDataRobot.REST_STATUS_ERROR);
			_parameters.put(RestDataRobot.REST_RESULT, getErrorMessageHTML(
					"Format of feed is specified incorrectly.", _context_path));
		}
		else
		{
			try
			{// retrieve user by login from db
				conn = sqlm.getConnection();
				int found_scope_id = 0;
				String scope_name = "";
				String scope_description = "";

				qry = "SELECT * FROM ent_scope WHERE rdfID='" + _scope + "';";
				stmt = conn.prepareStatement(qry);
				rs = stmt.executeQuery();
				if (rs.next())
				{
					found_scope_id = rs.getInt("ScopeID");
					scope_name = rs.getString("Name");
					scope_description = rs.getString("Description");
				}
				rs.close();
				stmt.close();

				if (found_scope_id == 0)
				{
					_parameters.put(RestDataRobot.REST_STATUS,
							RestDataRobot.REST_STATUS_ERROR);
					_parameters.put(RestDataRobot.REST_RESULT,
							getErrorMessageHTML(
									"The specified feed is not found.",
									_context_path));
					return _parameters;
				}

				qry = "SELECT d.* FROM ent_dissection d JOIN rel_scope_dissection sd ON (d.DissectionID=sd.DissectionID) "
						+
						// "JOIN ent_scope s ON(sd.ScopeID=s.ScopeID) " +
						"WHERE sd.ScopeID=" + found_scope_id + ";";
				stmt = conn.prepareStatement(qry);
				rs = stmt.executeQuery();

				result += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
				String rss_link = _context_path + _request.getServletPath()
						+ "?" + WEBEX_RSS_SCOPE + "=" + _scope + "&amp;"
						+ WEBEX_RSS_VERSION + "=" + _format;

				// String _icon = ""; //"<img src='" + _context_path +
				// "/assets/CommunityPortalIcon_24.gif' />&nbsp;";
				// _icon.replaceAll("<", "&lt;").replaceAll(">",
				// "&gt;").replaceAll("&", "&amp;");
				// _icon = java.net.URLEncoder.encode(_icon);

				if (_format.equals(WEBEX_RSS_VERSION_RSS2))
				{// RSS2.0
					result += "<rss version=\"2.0\">\n" + "	<channel>\n"
							+ "		<title>WebEx:: " + scope_name.replaceAll("&", "&amp;") + "</title>\n"
							+ "		<link>" + rss_link + "</link>\n"
							+ "		<description rdf:parseType='Literal'>" + scope_description.replaceAll("&", "&amp;")
							+ "</description>\n" + "		<image>\n" + "			<url>"
							+ _context_path
							+ "/assets/webex_rss_image.gif</url>\n"
							+ "			<title>WebEx:: " + scope_name.replaceAll("&", "&amp;") + "</title>\n"
							+ "			<link>" + rss_link + "</link>\n"
							+ "			<width>90</width>\n"
							+ "			<height>30</height>\n" + "		</image>\n";

					while (rs.next())
					{
						String _rdfID = rs.getString("rdfID");
						String _name = rs.getString("Name");
						String _description = rs.getString("Description");
						_description = ((_description == null || _description
								.length() == 0) ? "" : _description);

						_description = _description.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

						String item_link = _context_path + "/Dissection2?"
						+ um_cache2.REQ_ACTIVITY + "=" + _rdfID;

						String item_uri = _context_path + "/webex.rdf#" + _rdfID;

						result += "		<item>\n"
								+ "			<title>"
								+ _name.replaceAll("&", "&amp;")
								+ "</title>\n"
								+ "			<link>"
								+ item_link
								+ "</link>\n"
								+ ((_description.length() > 0) ? "			<description rdf:parseType='Literal'>"
										+ _description.replaceAll("&", "&amp;") + "</description>\n"
										: "") + "			<guid isPermaLink=\"true\">"
								+ item_uri + "</guid>\n" + 
								"		</item>\n";
					}
					result += "	</channel>\n" + "</rss>";
				}// end of -- RSS2.0
				else if (_format.equals(WEBEX_RSS_VERSION_RSS1))
				{// RSS1.0
					String item_list_full = "";
					String item_list_short = "";

					while (rs.next())
					{
						String _rdfID = rs.getString("rdfID");
						String _name = rs.getString("Name");
						String _description = rs.getString("Description");

						_description = ((_description == null || _description
								.length() == 0) ? "" : _description);

						_description = _description.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

						String item_link = _context_path + "/Dissection2?"
								+ um_cache2.REQ_ACTIVITY + "=" + _rdfID + ((is_txt_mode)?"&amp;mode=txt":"");

						String item_uri = _context_path + "/webex" + ((is_txt_mode)?"_txt":"") + ".rdf#" + _rdfID;

						item_list_short += "				<rdf:li rdf:resource=\"" + item_uri
								+ "\"/>\n"; 

						item_list_full += "	<item rdf:about=\""
								+ item_uri
								+ "\">\n" +
								"		<title>"
								+ _name.replaceAll("&", "&amp;")
								+ "</title>\n"
								+ "		<link>"
								+ item_link
								+ "</link>\n"
								+ ((_description.length() > 0) ? "		<description rdf:parseType='Literal'>"
										+ _description.replaceAll("&", "&amp;") + "</description>\n"
										: "") + "	</item>\n";
					}

					result += "<rdf:RDF\n"
							+ "	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
							+ "	xmlns=\"http://purl.org/rss/1.0/\"\n"
							+
							// " xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"
							// +
							">\n" + "	<channel rdf:about=\"" + rss_link + "\">\n"
							+ "		<title>WebEx:: " + scope_name.replaceAll("&", "&amp;") + "</title>\n"
							+ "		<link>" + rss_link + "</link>\n"
							+ "		<description rdf:parseType='Literal'>" + scope_description.replaceAll("&", "&amp;")
							+ "</description>\n" + "		<image rdf:resource=\""
							+ _context_path
							+ "/assets/webex_rss_image.gif\" />\n"
							+ "		<items>\n" + "			<rdf:Seq>\n"
							+ item_list_short + "			</rdf:Seq>\n"
							+ "		</items>\n" + "	</channel>\n"
							+ "	<image rdf:about=\"" + _context_path
							+ "/assets/webex_rss_image.gif\">\n" + "		<url>"
							+ _context_path
							+ "/assets/webex_rss_image.gif</url>\n"
							+ "		<title>WebEx:: " + scope_name.replaceAll("&", "&amp;") + "</title>\n"
							+ "		<link>" + rss_link + "</link>\n"
							+ "	</image>\n" + item_list_full + "</rdf:RDF>";
				}// end of -- RSS1.0
				else if (_format.equals(WEBEX_RSS_VERSION_ATOM))
				{// Atom
					result = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
							+ "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n"
							+ "	<title>" + scope_name.replaceAll("&", "&amp;") + "</title> \n"
							+ "	<link rel=\"self\" href=\"" + rss_link
							+ "\"/>\n"
							+ "	<updated>2007-04-23T18:30:02Z</updated>\n"
							+ // TODO think about time
							"	<author> \n"
							+ "		<name>Michael V. Yudelson</name>\n"
							+ "	</author> \n" + "	<id>" + rss_link + "</id>\n"; // TODO
					// what
					// else
					// can
					// be
					// used
					// as
					// id

					while (rs.next())
					{
						String _rdfID = rs.getString("rdfID");
						String _name = rs.getString("Name");
						String _description = rs.getString("Description");

						_description = (((_description == null || _description
								.length() == 0) ? "" : _description));
						_description = _description.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
						
						String item_link = _context_path + "/Dissection2?"
								+ um_cache2.REQ_ACTIVITY + "=" + _rdfID;
						
						String item_uri = _context_path + "/webex.rdf#" + _rdfID;

						result += "	<entry>\n"
								+ "		<title>"
								+ _name.replaceAll("&", "&amp;")
								+ "</title>\n"
								+ "		<link href=\""
								+ item_link
								+ "\"/>\n"
								+ "		<id>"
								+ item_uri
								+ "</id>\n" +
								"		<updated>2007-04-23T18:30:02Z</updated>\n"
								+ // TODO think about time
								/* ((_description.length()>0)? */"		<summary>Description: "
								+ _description.replaceAll("&", "&amp;") + "</summary>\n"/* :"") */
								+ "	</entry>\n";
					}

					result += "</feed>";
				}// end of -- Atom
				else
				{
					_parameters.put(RestDataRobot.REST_STATUS,
							RestDataRobot.REST_STATUS_ERROR);
					_parameters.put(RestDataRobot.REST_RESULT,
							getErrorMessageHTML(
									"Format of feed is not supported yet",
									_context_path));
				}

				rs.close();
				stmt.close();

				_parameters.put(RestDataRobot.REST_STATUS,
						RestDataRobot.REST_STATUS_OK);
				_parameters.put(RestDataRobot.REST_RESULT, result);

			}// end of -- retrieve user by login from db
			catch (SQLException sqle)
			{
				sqle.printStackTrace(System.out);
			}
			finally
			{
				sqlm.recycleObjects(conn, stmt, rs);
			}
		}

		return _parameters;
	}

	private static String getPageHeaderHTML(String _title, String _context_path)
	{
		String result = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n"
				+ "<html><head>\n"
				+ "<title>"
				+ _title.replaceAll("&", "&amp;")
				+ "</title>\n"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
				+ "<link rel=\"StyleSheet\" href=\""
				+ _context_path
				+ "/assets/rest.css\" type=\"text/css\" />\n" + "</head><body>\n";

		return result;
	}

	public static String getErrorMessageHTML(String _message,
			String _context_path)
	{
		String result = getPageHeaderHTML("Knowledge Tree - Error",
				_context_path)
				+ "<table cellpadding=\"0px\" cellspacing=\"0px\">"
				+ "<tr>"
				+ "	<td class=\"error_table_caption\">Error</td>"
				+ "</tr>"
				+ "<tr>"
				+ "	<td class=\"error_table_message\">"
				+ _message
				+ "</td>" + "</tr>" + "</table></body></html>";

		return result;
	}

	public static void WriteHTML(HttpServletResponse response, String result)
			throws IOException
	{
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(result);
		out.close();
	}

}