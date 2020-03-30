/*
 * Dissection servlet. Line by line comments
 */
package edu.pitt.sis.paws.webex;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import edu.pitt.sis.paws.cbum.report.*;
import edu.pitt.sis.paws.cbum.*;
import edu.pitt.sis.paws.core.utils.SQLManager;

// import edu.pitt.sis.paws.um.structures.*;
// import edu.pitt.sis.paws.navex.*;

public class Dissection2 extends HttpServlet
{
	static final long serialVersionUID = -2L;

	// Context parameter names
	// private static final String CTXT_DB_DRIVER = "driver";
	// private static final String CTXT_DB_URL2 = "url2";
	// private static final String CTXT_DB_USER = "user";
	// private static final String CTXT_DB_PASS = "password";
	private static final String CTXT_UMM = "ummanager";

	private static final String CTXT_UM = "um";

	// Request parameter names
	// private static final String REQ_ACTIVITY = "act";
	// private static final String REQ_SUBACTIVITY = "sub";
	// private static final String REQ_SESSION = "sid";
	// private static final String REQ_GROUP = "grp";
	// private static final String REQ_USER = "usr";
	// private static final String REQ_SVC = "svc";
	private static final String REQ_MODE = "mode";

	// private static final String REQ_RESULT = "res";
	private static final String REQ_UM = "um";
	private static final String REQ_UMM = "umm";
	private static final String REQ_NOREPORT = "noreport";
	private static final String REQ_TITLE = "title";
	private static final String REQ_DESCRIPTION = "desc";

	// Context parameter values
	// private static String ctxt_db_driver = null;
	// private static String ctxt_db_url = null;
	// private static String ctxt_db_user = null;
	// private static String ctxt_db_pass = null;
	private static String ctxt_umm = null;

	private static String ctxt_um = null;

	// Request allowed parameter values
	private static final String REQ_MODE_SIMPLE = "sim";

	private static final String REQ_MODE_SOCIAL_NAVIGATION = "sn";

	private static final String REQ_MODE_TEXT_ONLY = "txt";

	// Other constants
	private final static String[] social_icons = { "/system/ic-box.gif",
			"/system/ic-box-check.gif" };

	private final static String[] simple_icons = { "/system/b_emp.gif", // Empty
			// space
			// - no
			// comments
			// at
			// all
			"/system/b_up.gif", // Comment exists
			"/system/b_dn.gif" }; // Comment being viewed

	private final static String[] icon_styles = { " class='webex_bullet_box'",
			" class='webex_bullet_check'" };

	public final static String[] social_progress = { "SocioBG1", "SocioBG2",
			"SocioBG3", "SocioBG4" };

	private SQLManager sqlm = null;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Dissection2()
	{
		super();
		sqlm = new SQLManager("java:comp/env/jdbc/webex");
	}

	public static ArrayList getWebexProfile(String activity, String user,
			String group, HttpServletRequest req) //throws IOException
	{
		ArrayList<ProgressEstimatorReport> um_request = new ArrayList<ProgressEstimatorReport>();
		um_request.add(new ProgressEstimatorReport(activity));

		ObjectOutputStream send_stream = null;
		ObjectInputStream recv_stream = null;
		ArrayList um_response = null;
		try
		{
			URL url = new URL(ctxt_umm + "?typ=act&dir=in&frm=dat&app=3&usr="
					+ user + "&grp=" + group);
			// send
			URLConnection conn = url.openConnection();
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "java-internal/"
					+ req.getClass().getName());
			send_stream = new ObjectOutputStream(conn
					.getOutputStream());
			send_stream.writeObject(um_request);
			send_stream.flush();
			send_stream.close();
			
			// System.out.println("");
			// receive
			
			recv_stream = new ObjectInputStream(conn
					.getInputStream());
			um_response = (ArrayList) recv_stream.readObject();

			recv_stream.close();

		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}

		// System.out.println("UM response="+um_response);
		// if(um_response!=null && um_response.size()>0)
		// {
		// for(int i=0; i<um_response.size(); i++)
		// {
		// ProgressEstimatorReport act =
		// (ProgressEstimatorReport)um_response.get(i);
		// System.out.println("\t" + act);
		// for(int j=0; j<act.subs.size(); j++)
		// {
		// ProgressEstimatorReport sub =
		// (ProgressEstimatorReport)act.subs.get(j);
		// System.out.println("\t\t" + sub);
		// }
		// }
		// }

		return um_response;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		// Read context parameters
		ServletContext context = getServletContext();
		ctxt_umm = context.getInitParameter(CTXT_UMM);
		ctxt_um = context.getInitParameter(CTXT_UM);
		// Read request parameters
		String req_activity = req.getParameter(um_cache2.REQ_ACTIVITY);
		String req_subactivity = req.getParameter(um_cache2.REQ_SUBACTIVITY);
		String req_session = req.getParameter(um_cache2.REQ_SESSION);
		String req_group = req.getParameter(um_cache2.REQ_GROUP);
		String req_user = req.getParameter(um_cache2.REQ_USER);
		String req_svc = req.getParameter(um_cache2.REQ_SVC);
		String req_mode = req.getParameter(REQ_MODE);
		String req_um = req.getParameter(REQ_UM);
		String req_noreport = req.getParameter(REQ_NOREPORT);
		String req_umm = req.getParameter(REQ_UMM);
		String req_title = req.getParameter(REQ_TITLE);
		String req_descr = req.getParameter(REQ_DESCRIPTION);
		
		boolean show_title = (req_title != null && req_title.equals("1"));
		boolean show_desc = (req_descr != null && req_descr.equals("1"));

		req_subactivity = (req_subactivity == null || req_subactivity.length() == 0) ? "0"
				: req_subactivity;

		Random rand = new Random();
		req_session = (req_session == null || req_session.length() == 0) ? "WE"
				+ rand.nextInt(101) : req_session;

		req_um = (req_um != null && req_um.length() > 0) ? URLDecoder.decode(
				req_um, "UTF-8") : null;
		req_umm = (req_umm != null && req_umm.length() > 0) ? URLDecoder
				.decode(req_umm, "UTF-8") : null;
		req_mode = (req_mode != null && req_mode.length() > 0) ? req_mode
				: REQ_MODE_SIMPLE;
		req_svc = (req_svc != null && req_svc.length() > 0) ? req_svc : "";

		// User Model and User Model Manager in request have priority
		if (req_um != null)
			ctxt_um = req_um;
		if (req_umm != null)
			ctxt_umm = req_umm;

		// Report ectivity
		if (req_noreport == null || req_noreport.length() == 0)
		{
			String svc_suffix = ((req_svc != null && req_svc.length() > 0) ? ";"
					: "")
					+ "mode." + req_mode;
			ReportAPI rapi = new ReportAPI(ctxt_um);
			rapi.report(ReportAPI.APPLICATION_WEBEX, req_activity,
					req_subactivity, req_session, /* _res */-1, req_group,
					req_user, req_svc + svc_suffix, req.getRemoteAddr());
		}
		// end of -- Report ectivity

		// Start the HTML
		res.setContentType("text/html;charset=utf-8"); //ContentType/Charset should be set before the getWriter
		PrintWriter out = res.getWriter();
		
		if(!req_mode.equals(REQ_MODE_TEXT_ONLY))
		{
			out.println("<!doctype html>");
			out.println("<html>");
			out.println("<meta charset='utf-8'/>");
			out.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
			out.println("<link href='" + req.getContextPath()
					+ "/system/A2TeAL.css' rel='stylesheet' type='text/css'>");
			// out.println("<link href='" + req.getContextPath() +"/assets/rest.css'
			// rel='stylesheet' type='text/css'>");
			out.println("<head>");
			out.println("<title>WebEx:: " + req_activity + "</title>");
			out.println("</head>");
			out.println("<body>");
		}
		else // plain text
		{
			res.setContentType("text/plain");
		}

		
		// Error if parameters are wrong
		if (req_subactivity == null || req_activity == null
				|| req_session == null
		/* || req_user==null || req_group==null */)
		{
			String result = WebexRSS.getErrorMessageHTML("Invalid parameters.",
					req.getContextPath());
			WebexRSS.WriteHTML(res, result);
			return;
		}

		int subactivity = Integer.parseInt(req_subactivity);

		// Act on the mode
		ArrayList uprlist = null;
		if (req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))
		{
			try
			{
				uprlist = getWebexProfile(req_activity, req_user, req_group,
						req);
			}
			catch (Exception e)
			{
				e.printStackTrace(System.out);
			}
		}
		// Work with database
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		Statement stmt2 = null;
		ResultSet rs2 = null;

		try
		{

			int lineno = -1;
			String code = "", comment = "";

			conn = sqlm.getConnection();

			String sql2 = "";
			String dissection_title = "";
			String dissection_desc = "";
			int dissection_is_jeliot = 0;
			
			if(show_title || show_desc)
			{
				boolean annot_open = false;

				sql2 = "SELECT * FROM ent_dissection WHERE rdfID='" + req_activity + "';";
				stmt2 = conn.prepareStatement(sql2);
				rs2 = stmt2.executeQuery(sql2);
				
				if(rs2.next())
				{
					dissection_title = rs2.getString("Name");
					dissection_desc = rs2.getString("Description");
				}
				
				rs2.close();
				stmt2.close();
				rs2 = null;
				stmt2 = null;
				
				// Show the title/description
				if(show_title)
					out.print("<div style='font-size:1.2em;'>");
				if(show_desc)
				{// show desc
//System.out.println("show desc");					
					boolean annotated = dissection_desc != null && dissection_desc.length() > 0;
					annot_open = (subactivity==-1);
					boolean progress = true;
					boolean social = req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION);
					
					// subactivity="+subactivity);
					int u_click = 0;
					int o_click = 0;
					String social_icon = "";
					String icon_bg_style = "";

//System.out.println("uprlist.get(0) " + uprlist.get(0));					
					if (req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))
					{// if SOCIAL NAVIGATION
//System.out.println("social show desc");						
						if (uprlist != null
								&& uprlist.size() == 1
								&& ((ProgressEstimatorReport) uprlist.get(0)).getSubs()
										.size() > 0 && annotated)
						{// find annotations
//System.out.println("profile");						
							ArrayList subs = ((ProgressEstimatorReport) uprlist
									.get(0)).getSubs();
							String line = Integer.toString(-1);
							for (int i = 0; i < subs.size(); i++)
							{
//System.out.println("\t(ProgressEstimatorReport) subs.get(i)) " + (ProgressEstimatorReport) subs.get(i));					
								if (((ProgressEstimatorReport) subs.get(i)).getId()
										.equals(line))
								{
									u_click = (int) Math
											.ceil(((ProgressEstimatorReport) subs
													.get(i)).getProgress(1/*fudge*/));// (int)upr.u_progr;
									progress = u_click > 0;
									o_click = (int) Math
											.round(((ProgressEstimatorReport) subs
													.get(i)).getGroupProgress(1/*fudge*/) * 4);
									break;
								}
							}

						}
						o_click = (o_click > 3) ? 3 : o_click;

						social_icon = (annotated) ? ("<img src='"
								+ ((annot_open) ?  req.getContextPath() + social_icons[1]
										: ((progress) ? req.getContextPath() + social_icons[u_click]
												: req.getContextPath() + social_icons[0])) + "' border='0'>")
								: "&nbsp;";
						icon_bg_style = (annotated) ? ((social) ? " class='"
								+ social_progress[o_click] + "'" : "")
								: " class='webex_bullet_empty'";

						String _url = "Dissection2?" +
							REQ_MODE + "=" + req_mode + "&" +
							um_cache2.REQ_SUBACTIVITY + "=" + "-1" + "&" +
							um_cache2.REQ_ACTIVITY + "=" + req_activity + "&" +
							um_cache2.REQ_SESSION + "=" + req_session + "&" +
							um_cache2.REQ_USER + "=" + req_user + "&result=-1" + "&" +
							um_cache2.REQ_GROUP + "=" + req_group
							+ ((req_noreport != null) ? ("&" + REQ_NOREPORT
									+ "=" + req_noreport) : "")
							+ ((req_svc.length() > 0) ? "&" + um_cache2.REQ_SVC
									+ "=" + req_svc : "") +
							((show_title)?"&title=1":"") +
							((show_desc)?"&desc=1":""); // + "W" +
								
						String icon_url = (annotated && !annot_open) ? (" href='"
								+ _url + "' title='Click for comment to this line'") : "";

						out.print("<span " + icon_bg_style + ">");
						out.print("<a" + icon_url + ">" + social_icon + "</a>");
						out.println("</span>&nbsp;");
						
					}// end of -- if SOCIAL NAVIGATION
					else if (req_mode.equals(REQ_MODE_SIMPLE))
					{
						social_icon = (annotated) ? ((annot_open) ? req.getContextPath() + simple_icons[2]
								: req.getContextPath() + simple_icons[1])
								: req.getContextPath() + simple_icons[0];
						social_icon = "<img src='" + social_icon
								+ "' border='0'>";
						icon_bg_style = "";
					}				
					
				}// end of -- show desc
				if(show_title)
					out.println(dissection_title + "</div>");
				// Show description
				if (show_desc && annot_open)
				{
					out.println("<div class='webex_annotation'>" + dissection_desc + "</div>");
				}
			}
			
			
			// JELIOT 3
			sql2 = "SELECT * FROM ent_dissection WHERE rdfID='" + req_activity + "';";
			stmt2 = conn.prepareStatement(sql2);
			rs2 = stmt2.executeQuery(sql2);
			
			if(rs2.next())
			{
				dissection_is_jeliot = rs2.getInt("IsForJeliot");
			}
			
			rs2.close();
			stmt2.close();
			rs2 = null;
			stmt2 = null;
			if(!req_mode.equals(REQ_MODE_TEXT_ONLY) && dissection_is_jeliot==1)
				// only if it is simple or social and is for Jeliot
			{
				String webex_txt_url = "http://adapt2.sis.pitt.edu/webex/Dissection2?mode=txt&act=" + req_activity;
				String jeliot_url = /*req.getContextPath() + */"jeliot_webex.jnlp" + 
					"?codebase=" + URLEncoder.encode(JeliotJNLPGenerator.jeliot_codebase,"UTF-8") +
					"&src=" + URLEncoder.encode(webex_txt_url,"UTF-8").replaceAll("&", "&amp;") + 
					"&usr=" + req_user + 
//					"&grp=" + req_group + 
					"&sid=" + req_session + 
					"&q=0" +
					"";

				out.println("<div><a title='Visualize execution of this code in Jeliot' href='" + jeliot_url + "'><img width='52' height='46' border='0' src='"
						+ req.getContextPath() + "/assets/jeliot.gif' /></a></div>");
			}
			// -- end of -- JELIOT 3
			

			
			String sql = "SELECT l.Code AS Code, l.Comment AS Comments, l.LineIndex AS LNo FROM ent_line l "
				+ "JOIN ent_dissection d ON(l.DissectionID=d.DissectionID) "
				+ "WHERE d.rdfID='" + req_activity + "' ORDER BY LNo ASC;";

			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery(sql);
		
			
			
			boolean table_out = false;
			if (req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION)
					|| req_mode.equals(REQ_MODE_SIMPLE))
				table_out = true;
			if (table_out)
				out.println("<table width='100%' cellspacing='2' cellpadding='0'>");
//			else
//				out.println("<code>");

			while (rs.next())
			{// for all lines of code

				lineno = rs.getInt("LNo");
				code = rs.getString("Code");
				// Do tabs and trims
				
				if(!req_mode.equals(REQ_MODE_TEXT_ONLY))
				{
					code = convertString(code);
					code = ((String) code.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")).trim();
//					System.out.println(req_mode);
				}
				
				// count = rs.getAsciiStream("Comments").read(com);
				//				
				// if (count<1) comment="";
				// else comment = (new String(com,0,count)).trim();
				comment = rs.getString("Comments");

				boolean annotated = false;
				boolean annot_open = false;

				// TODO - these parameters can be changed from outside
				boolean progress = true;
				boolean social = req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION);
				String social_svc = "";
				
				if (req_mode.equals(REQ_MODE_TEXT_ONLY))
				{
					out.println(code /*+ "<br />"*/);
				}
				else 
				if ((req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))
						|| (req_mode.equals(REQ_MODE_SIMPLE)))
				{
					if (table_out)
						out.println("<tr>");

					annotated = ((comment!=null) && (comment.length() != 0));
					annot_open = (lineno == subactivity);
					// System.out.print("[webex] lineno="+lineno+"
					// subactivity="+subactivity);
					int u_click = 0;
					int o_click = 0;
					String social_icon = "";
					String icon_bg_style = "";

					if (req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))
					{// if SOCIAL NAVIGATION
						if (uprlist != null
								&& uprlist.size() == 1
								&& ((ProgressEstimatorReport) uprlist.get(0)).getSubs()
										.size() > 0 && annotated)
						{// find annotations
							ArrayList subs = ((ProgressEstimatorReport) uprlist
									.get(0)).getSubs();
							String line = Integer.toString(lineno);
							for (int i = 0; i < subs.size(); i++)
							{
								if (((ProgressEstimatorReport) subs.get(i)).getId()
										.equals(line))
								{
									u_click = (int) Math
											.ceil(((ProgressEstimatorReport) subs
													.get(i)).getProgress(1/*fudge*/));// (int)upr.u_progr;
									// //SocialNavigation.getClickLevel(upr.u_click);
									progress = u_click > 0;
									o_click = (int) Math
											.round(((ProgressEstimatorReport) subs
													.get(i)).getGroupProgress(1/*fudge*/) * 4);
									
									social_svc = "Wu" + u_click + "|g" + o_click;
									
									// System.out.println("id=" +
									// ((ProgressEstimatorReport)subs.get(i)).id
									// + " line=" + line + " u.click=" + u_click
									// + " o.click=" + o_click + " progress=" +
									// progress + " " +
									// ((ProgressEstimatorReport)subs.get(i)).progress);
									break;
									// u_click = (u_click>0)?1:0;
								}
							}

						}
						// UserProfileRecord upr = uprlist.findByAcivity((new
						// Integer(lineno)).toString());
						// if(upr!=null)
						// {
						// u_click = (int)upr.u_progr;
						// //SocialNavigation.getClickLevel(upr.u_click);
						//	
						// o_click =
						// SocialNavigation.getClickLevel((int)upr.o_click);
						//	
						// u_click = (u_click>0)?1:0;
						// }
						// System.out.println(" annotated="+annotated+"
						// annot_open="+annot_open+" u_click="+u_click);

						// if((u_click<0) || (u_click>1) || (o_click<0) ||
						// (o_click>3)) System.out.println("[webex] array error
						// possible u_click="+u_click+" o_click="+o_click);

						// If the group has done the max - avoid array error
						o_click = (o_click > 3) ? 3 : o_click;

						social_icon = (annotated) ? ("<img src='"
								+ ((annot_open) ? req.getContextPath() + social_icons[1]
										: ((progress) ? req.getContextPath() + social_icons[u_click]
												: req.getContextPath() + social_icons[0])) + "' border='0'>")
								: "&nbsp;";
						icon_bg_style = (annotated) ? ((social) ? " class='"
								+ social_progress[o_click] + "'" : "")
								: " class='webex_bullet_empty'";
					}// end of -- if SOCIAL NAVIGATION
					else if (req_mode.equals(REQ_MODE_SIMPLE))
					{
						social_icon = (annotated) ? ((annot_open) ? req.getContextPath() +simple_icons[2]
								: req.getContextPath() + simple_icons[1])
								: req.getContextPath() + simple_icons[0];
						social_icon = "<img src='" + social_icon
								+ "' border='0'>";
						icon_bg_style = "";
					}

					String _url = "Dissection2?"
							+ REQ_MODE + "=" + req_mode + "&" 
							+ um_cache2.REQ_SUBACTIVITY + "=" + lineno + "&"
							+ um_cache2.REQ_ACTIVITY + "=" + req_activity + "&"
							+ um_cache2.REQ_SESSION + "=" + req_session + "&"
							+ um_cache2.REQ_USER + "=" + req_user + "&"
							+ um_cache2.REQ_RESULT + "=-1" + "&"
							+ um_cache2.REQ_GROUP
							+ "="
							+ req_group
							+ ((req_noreport != null) ? ("&" + REQ_NOREPORT
									+ "=" + req_noreport) : "")
							+ ((req_svc.length() > 0) ? "&" + um_cache2.REQ_SVC
									+ "=" + req_svc + social_svc : "") +
							((show_title)?"&title=1":"") +
							((show_desc)?"&desc=1":""); // + "W" +
					// ((req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))?(u_click +
					// "" + o_click):"") + "#pos";

					String icon_url = (annotated && !annot_open) ? (" href='"
							+ _url + "' title='Click for comment to this line'") : "";

					out.print("<td width='16' " + icon_bg_style + ">");
					out.print("<a" + icon_url + ">" + social_icon + "</a>");
					out.println("</td>");

					// Code if any
					String code_style = " class="
							+ ((annotated && annot_open) ? "webex_code_annot_shadow"
									: ((annotated) ? "webex_code_annot"
											: "webex_code"));
					String anchor = (annot_open) ? " name='pos'" : "";

					String onClick = "";// (annotated && !annot_open)?("
					// onClick='window.document.location=\""+_url
					// +"\";'"):"";;
					out.print("<td" + onClick + ">");
					out.print("<a" + code_style + anchor + icon_url + ">");
					out.print(code);
					out.print("</a>");
				}

				if (table_out)
				{
					out.println("</td>");
					out.println("</tr>");
				}

				if (annot_open)
				{
					out.println("<tr height='16'>");
					out.println("<td width='16'>&nbsp;</td>");
					out.println("<td class=webex_annotation>" + comment
							+ "</td>");
					out.println("</tr>");
				}
			}// end of -- for all lines of code
			rs.close();
			stmt.close();
			
			// Close table / code fragment
			if (table_out)
				out.println("</table>");
//			else
//				out.println("</code>");
			
			sqlm.recycleObjects(conn, (PreparedStatement)stmt, rs);
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.out);
			e.printStackTrace(out);
		}

		if(!req_mode.equals(REQ_MODE_TEXT_ONLY))
		{
			out.println("</body>");
			out.println("</html>");
		}
		out.close();
	}

	private String convertString(String str)
	{
		StringBuffer sbuf = new StringBuffer();
		if (str == null)
			return "";
		if (str.length() == 0)
			return "";
		int i = 0;
		char c;
		while (i < str.length() && (c = str.charAt(i++)) == ' ')
			sbuf.append("&nbsp;");
		i--;
		for (; i < str.length(); i++)
		{
			c = str.charAt(i);
			if (c == '<')
				sbuf.append("&lt;");
			else if (c == '>')
				sbuf.append("&gt;");
			else if (c == '&')
				sbuf.append("&amp;");
			else if (c == '"')
				sbuf.append("&quot;");
			else
				sbuf.append(c);
		}
		return sbuf.toString();
	}

}






