package edu.pitt.sis.paws.webex;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.paws.cbum.um_cache2;
import edu.pitt.sis.paws.core.utils.SQLManager;
import edu.pitt.sis.paws.kt.rest.RestDataRobot;

import org.apache.commons.lang.StringUtils;

/**
 * Servlet implementation class for Servlet: WebexRDF
 * 
 */
public class WebexRDF extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet
{
	static final long serialVersionUID = -2L;

	private SQLManager sqlm = null;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public WebexRDF()
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
		// read _scope
		String _scope = request.getParameter(WebexRSS.WEBEX_RSS_SCOPE);
		String _mode = request.getParameter(WebexRSS.WEBEX_RSS_MODE);

		Map<String, String> params = new HashMap<String, String>();
		params.put(WebexRSS.WEBEX_RSS_SCOPE, _scope);
		params.put(WebexRSS.WEBEX_RSS_MODE, _mode);
		params.put(RestDataRobot.REST_CONTEXT_PATH, "http://"
				+ request.getServerName()
				+ ((request.getLocalPort() != 80) ? ":"
						+ request.getLocalPort() : "")
				+ request.getContextPath());

		params = getRDF(params, request);

		String status = params.get(RestDataRobot.REST_STATUS);
		PrintWriter out = response.getWriter();

		if (status.equals(RestDataRobot.REST_STATUS_OK))
		{
			String mime = "application/rdf+xml; charset=utf-8";
			response.setContentType(mime);
			// System.out.println("MIME="+mime);
		}
		else if (status.equals(RestDataRobot.REST_STATUS_ERROR))
			response.setContentType("text/html; charset=utf-8");

		out.println(params.get(RestDataRobot.REST_RESULT));
	}

	private Map<String, String> getRDF(Map<String, String> _parameters,
			HttpServletRequest _request)
	{
		String result = "";

		String _scope = _parameters.get(WebexRSS.WEBEX_RSS_SCOPE);
		String _context_path = _parameters.get(RestDataRobot.REST_CONTEXT_PATH);
		String _mode = _parameters.get(WebexRSS.WEBEX_RSS_MODE);

		boolean is_txt_mode = (_mode != null)?_mode.equals(WebexRSS.WEBEX_RSS_MODE_TXT):false;

		String qry = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		boolean scoped = (_scope != null && _scope.length() > 0);
	
		try
		{// work with db
			conn = sqlm.getConnection();
			
			// check existance of the scope
			int found_scope_id = 0;
			boolean scope_found = false;
			if(scoped)
			{
				qry = "SELECT * FROM ent_scope WHERE rdfID='" + _scope + "';";
				stmt = conn.prepareStatement(qry);
				rs = stmt.executeQuery();
				if (rs.next())
				{
					found_scope_id = rs.getInt("ScopeID");
					scope_found = true;
				}
				rs.close();
				stmt.close();
			}
			if(scoped & !scope_found)
			{
				_parameters.put(RestDataRobot.REST_STATUS,
						RestDataRobot.REST_STATUS_ERROR);
				_parameters.put(RestDataRobot.REST_RESULT,
						WebexRSS.getErrorMessageHTML(
						"The specified scope is not found.", _context_path));
				return _parameters;
			}
			// end of -- check existance of the scope
			
			// create RDF
			qry = "SELECT d.* FROM ent_dissection d" + 
				((scoped)?" JOIN rel_scope_dissection sd ON (d.DissectionID=sd.DissectionID) WHERE sd.ScopeID=" + found_scope_id:"") + 
				";";

			stmt = conn.prepareStatement(qry);
			rs = stmt.executeQuery();

			result += "<?xml version='1.0' encoding='utf-8'?>\n" + 
				"<rdf:RDF\n" +
				"	xml:base='" + _context_path + "/webex" + ((is_txt_mode)?"_txt":"") + ".rdf'\n" +
				"	xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
				"	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'\n" +
				"	xmlns:dc='http://purl.org/dc/elements/1.1/'\n" +
				"	xmlns:lom='http://purl.org/lom/terms/'\n" +
				"	xmlns:lom-structure='http://purl.org/lom/voc/Structure/'\n" +
				"	xmlns:lom-lr-type='http://purl.org/lom/voc/LearningResourceType/'>\n\n";
			
		 	 
//			String item_list = "";
			
			while (rs.next())
			{
				String _rdfID = rs.getString("rdfID");
				String _name = rs.getString("Name").replaceAll("&", "&amp;");
				_name = StringUtils.replace(_name, "&", "&amp;");	
				
				String _description = rs.getString("Description");

				_description = ((_description == null || _description
						.length() == 0) ? "" : _description);
				_description = StringUtils.replace(_description, "&", "&amp;");	
				
				String item_link = _context_path + "/Dissection2?"
						+ um_cache2.REQ_ACTIVITY + "=" + _rdfID;

				String item_uri = /*_context_path + "/webex.rdf*/ "#" + _rdfID ;//+ ((is_txt_mode)?"&amp;mode=txt":"");

				result += "	<lom-lr-type:LearningObject rdf:about='" + item_uri + "'>\n" +
						"		<rdf:type rdf:resource='http://purl.org/lom/voc/LearningResourceType/Exercise'/>\n" +
						"		<dc:title xml:lang='en'>" + _name + "</dc:title>\n" + 
						"		<rdfs:isDefinedBy rdf:resource='" + _context_path + "/webex" + ((is_txt_mode)?"_txt":"") + ".rdf'/>\n" + 
						((_description.length()>0)?"		<dc:description xml:lang='en' rdf:parseType='Literal'>" + _description + "</dc:description>\n":"") + 
						"		<dc:identifier rdf:datatype='http://www.w3.org/2001/XMLSchema#anyURI'>" + item_link + "</dc:identifier>\n" + 
						"		<lom:structure rdf:resource='http://purl.org/lom/voc/Structure/linear'/>\n" + 
						"	</lom-lr-type:LearningObject>\n";
			}
			result += "</rdf:RDF>";
			// end of -- create RDF
			
			rs.close();
			stmt.close();

			_parameters.put(RestDataRobot.REST_STATUS,
					RestDataRobot.REST_STATUS_OK);
			_parameters.put(RestDataRobot.REST_RESULT, result);
			
		}// end of -- work with db
		catch (SQLException sqle) { sqle.printStackTrace(System.out); }
		finally { sqlm.recycleObjects(conn, stmt, rs); }

		return _parameters;
	}
}