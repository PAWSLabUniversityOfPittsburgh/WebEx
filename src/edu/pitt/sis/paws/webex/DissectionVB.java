/* 
 * Dissection servlet. Line by line comments
 */
package edu.pitt.sis.paws.webex;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;
import edu.pitt.sis.paws.cbum.report.ProgressEstimatorReport;

// import edu.pitt.sis.paws.um.structures.*;
// import edu.pitt.sis.paws.navex.*;

public class DissectionVB extends HttpServlet
{
	static final long serialVersionUID = -2L;

	// Context parameter names
	private static final String CTXT_DB_DRIVER = "driver";

	private static final String CTXT_DB_URLVB = "urlvb";

	private static final String CTXT_DB_USER = "user";

	private static final String CTXT_DB_PASS = "password";

	private static final String CTXT_UMM = "ummanager";

	private static final String CTXT_UM = "um";

	// Request parameter names
	private static final String REQ_ACTIVITY = "act";

	private static final String REQ_SUBACTIVITY = "sub";

	private static final String REQ_SESSION = "sid";

	private static final String REQ_GROUP = "grp";

	private static final String REQ_USER = "usr";

	private static final String REQ_SVC = "svc";

	private static final String REQ_MODE = "mode";

	private static final String REQ_RESULT = "res";

	private static final String REQ_UM = "um";

	private static final String REQ_UMM = "umm";

	private static final String REQ_NOREPORT = "noreport";

	// Context parameter values
	private static String ctxt_db_driver = null;

	private static String ctxt_db_url = null;

	private static String ctxt_db_user = null;

	private static String ctxt_db_pass = null;

	private static String ctxt_umm = null;

	private static String ctxt_um = null;

	// Request parameter values
	private static String req_activity = null;

	private static String req_subactivity = null;

	private static String req_session = null;

	private static String req_group = null;

	private static String req_user = null;

	private static String req_svc = null;

	private static String req_mode = null;

	private static String req_um = null;

	private static String req_umm = null;

	private static String req_noreport = null;

	// Request allowed parameter values
	private static final String REQ_MODE_SIMPLE = "sim";

	private static final String REQ_MODE_SOCIAL_NAVIGATION = "sn";

	private static final String REQ_MODE_TEXT_ONLY = "txt";

	// Other constants
	private final static String[] social_icons = { "/webex/system/ic-box.gif",
			"/webex/system/ic-box-check.gif" };

	private final static String[] simple_icons = { "/webex/system/b_emp.gif", // Empty
			// space
			// - no
			// comments
			// at
			// all
			"/webex/system/b_up.gif", // Comment exists
			"/webex/system/b_dn.gif" }; // Comment being viewed

	private final static String[] icon_styles = { " class='webex_bullet_box'",
			" class='webex_bullet_check'" };

	public final static String[] social_progress = { "SocioBG1", "SocioBG2",
			"SocioBG3", "SocioBG4" };

	public static ArrayList getWebexProfile(String activity, String user,
			String group, HttpServletRequest req) throws IOException
	{
		ArrayList um_request = new ArrayList();
		um_request.add(new ProgressEstimatorReport(activity));

		URL url = new URL(ctxt_umm + "?typ=act&dir=in&frm=dat&app=22&usr="
				+ user + "&grp=" + group);
		// send
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		conn.setDefaultUseCaches(false);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "java-internal/"
				+ req.getClass().getName());
		ObjectOutputStream send_strean = new ObjectOutputStream(conn
				.getOutputStream());
		send_strean.writeObject(um_request);
		send_strean.flush();
		send_strean.close();
		// System.out.println("");
		// receive
		ArrayList um_response = null;
		ObjectInputStream recv_stream = new ObjectInputStream(conn
				.getInputStream());
		try
		{
			um_response = (ArrayList) recv_stream.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
		finally
		{
			recv_stream.close();
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
		ctxt_db_driver = context.getInitParameter(CTXT_DB_DRIVER);
		ctxt_db_url = context.getInitParameter(CTXT_DB_URLVB);
		ctxt_db_user = context.getInitParameter(CTXT_DB_USER);
		ctxt_db_pass = context.getInitParameter(CTXT_DB_PASS);
		ctxt_umm = context.getInitParameter(CTXT_UMM);
		ctxt_um = context.getInitParameter(CTXT_UM);
		// System.out.println("[webex] doGet ctxt_um=" + ctxt_um); /// DEBUG
		// Read request parameters
		req_activity = req.getParameter(REQ_ACTIVITY);
		req_subactivity = req.getParameter(REQ_SUBACTIVITY);
		req_session = req.getParameter(REQ_SESSION);
		req_group = req.getParameter(REQ_GROUP);
		req_user = req.getParameter(REQ_USER);
		req_svc = req.getParameter(REQ_SVC);
		req_mode = req.getParameter(REQ_MODE);
		req_um = req.getParameter(REQ_UM);
		req_noreport = req.getParameter(REQ_NOREPORT);
		req_um = (req_um != null) ? URLDecoder.decode(req_um, "UTF-8") : null;
		req_umm = req.getParameter(REQ_UMM);
		req_umm = (req_umm != null) ? URLDecoder.decode(req_umm, "UTF-8")
				: null;
		req_mode = (req_mode != null) ? req_mode : REQ_MODE_SIMPLE;

		// User Model and User Model Manager in request have priority
		if (req_um != null)
			ctxt_um = req_um;
		if (req_umm != null)
			ctxt_umm = req_umm;

		// Start the HTML
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out
				.println("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>");
		out.println("<html>");
		out
				.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out
				.println("<link href='/webex/system/A2TeAL.css' rel='stylesheet' type='text/css'>");
		out.println("<head>");
		out.println("<title> Example " + req_activity + "</title>");
		out.println("</head>");
		out.println("<body>");

		if (req_subactivity == null || req_activity == null
				|| req_session == null || req_user == null)
		{
			out.println("<H3>Error. Invalid parameter(s) used.</H3>");
			out.println("</body>");
			out.println("</html>");
			return;
		}
		int subactivity = Integer.parseInt(req_subactivity);

		// Report the click
		if (req_noreport == null)
		{
			// if (subactivity>0)
			{
				String report_url = ctxt_um + "?usr=" + req_user + "&sid="
						+ req_session + "&app=22&act=" + req_activity + "&sub="
						+ subactivity + "&res=-1&grp=" + req_group
						+ "&svc=mode." + req_mode;
				// System.out.println("[webex] report to: " + report_url); ///
				// DEBUG
				URLConnection dbpc = (new URL(report_url)).openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						dbpc.getInputStream()));
			}
			// System.out.println("[webex] report section done!"); /// DEBUG
		}

		// System.out.println("[webex] mode="+req_mode); /// DEBUG
		// Act on the mode
		ArrayList uprlist = null;
		if (req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))
		{
			// System.out.println("[webex] social"); /// DEBUG
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
		// REQ_MODE_SIMPLE:
		// REQ_MODE_TEXT_ONLY:
		// Work with database
		try
		{

			// int offset=5,i=0;
			int lineno = -1;
			String code = "", comment = "", tmp = "", display = "";

			Class.forName(ctxt_db_driver);
			Connection conn = DriverManager.getConnection(ctxt_db_url,
					ctxt_db_user, ctxt_db_pass);

			String sql = "SELECT l.Code AS Code, l.Comment AS Comments, l.LineIndex AS LNo FROM ent_line l "
					+ "JOIN ent_dissection d ON(l.DissectionID=d.DissectionID) "
					+ "WHERE d.Name='" + req_activity + "' ORDER BY LNo ASC;";
			// System.out.println(sql);
			// out.println(sql);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			byte[] com = new byte[4096];
			int count;
			boolean table_out = false;
			if (req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION)
					|| req_mode.equals(REQ_MODE_SIMPLE))
				table_out = true;
			if (table_out)
				out
						.println("<table width='100%' cellspacing='2' cellpadding='0'>");
			else
				out.println("<code>");
			// System.out.println("[webex] DB done, Before going through
			// code."); /// DEBUG
			while (rs.next())
			{// for all lines of code

				lineno = rs.getInt("LNo");
				code = convertString(rs.getString("Code"));
				// Do tabs and trims
				code = ((String) code.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"))
						.trim();
				// System.out.println("[webex] retrieveing comments ------");
				// /// DEBUG
				// System.out.println("\t (rs==null):"+(rs==null)); /// DEBUG
				// System.out.println("\t
				// (rs.getAsciiStream(\"Comments\")==null):"+(rs.getAsciiStream("Comments")==null));
				// /// DEBUG
				count = rs.getAsciiStream("Comments").read(com);

				if (count < 1)
					comment = "";
				else
					comment = (new String(com, 0, count)).trim();

				boolean annotated = false;
				boolean annot_open = false;

				// TODO - these parameters can be changed from outside
				boolean progress = true;
				boolean social = req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION);

				if (req_mode.equals(REQ_MODE_TEXT_ONLY))
				{
					out.println(code + "<br />");
				}
				else if ((req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))
						|| (req_mode.equals(REQ_MODE_SIMPLE)))
				{
					if (table_out)
						out.println("<tr>");

					annotated = (comment.length() != 0);
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
								+ ((annot_open) ? social_icons[1]
										: ((progress) ? social_icons[u_click]
												: social_icons[0])) + "' border='0'>")
								: "&nbsp;";
						icon_bg_style = (annotated) ? ((social) ? " class='"
								+ social_progress[o_click] + "'" : "")
								: " class='webex_bullet_empty'";
					}// end of -- if SOCIAL NAVIGATION
					else if (req_mode.equals(REQ_MODE_SIMPLE))
					{
						social_icon = (annotated) ? ((annot_open) ? simple_icons[2]
								: simple_icons[1])
								: simple_icons[0];
						social_icon = "<img src='" + social_icon
								+ "' border='0'>";
						icon_bg_style = "";
					}

					String _url = "DissectionVB?"
							+ REQ_MODE
							+ "="
							+ req_mode
							+ "&"
							+ REQ_SUBACTIVITY
							+ "="
							+ lineno
							+ "&"
							+ REQ_ACTIVITY
							+ "="
							+ req_activity
							+ "&"
							+ REQ_SESSION
							+ "="
							+ req_session
							+ "&"
							+ REQ_USER
							+ "="
							+ req_user
							+ "&result=-1"
							+ "&"
							+ REQ_GROUP
							+ "="
							+ req_group
							+ ((req_noreport != null) ? ("&" + REQ_NOREPORT
									+ "=" + req_noreport) : "")
							+ "&"
							+ REQ_SVC
							+ "="
							+ req_svc
							+ "W"
							+ ((req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION)) ? (u_click
									+ "" + o_click)
									: "") + "#pos";

					String icon_url = (annotated && !annot_open) ? (" href='"
							+ _url + "'") : "";

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

			// Close table / code fragment
			if (table_out)
				out.println("</table>");
			else
				out.println("</code>");

			rs.close();
			stmt.close();

		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace(System.out);
			e.printStackTrace(out);
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.out);
			e.printStackTrace(out);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
			e.printStackTrace(out);
		}

		out.println("</body>");

		out.println("</html>");
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
