/*
 * Dissection servlet. Line by line comments
 */
package edu.pitt.sis.paws.webex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.paws.cbum.um_cache2;
import edu.pitt.sis.paws.cbum.report.ProgressEstimatorReport;
import edu.pitt.sis.paws.cbum.report.ReportAPI;
import edu.pitt.sis.paws.core.utils.SQLManager;

// import edu.pitt.sis.paws.um.structures.*;
// import edu.pitt.sis.paws.navex.*;

public class DissectionAquaAJAX extends HttpServlet
{
	static final long serialVersionUID = -2L;

	// Context parameter names
	private static final String CTXT_UMM = "ummanager";

	private static final String CTXT_UM = "um";

	// Request parameter names
	private static final String REQ_MODE = "mode";

	private static final String REQ_UM = "um";
	private static final String REQ_UMM = "umm";
	private static final String REQ_NOREPORT = "noreport";
	private static final String REQ_TITLE = "title";
	private static final String REQ_DESCRIPTION = "desc";

	// Context parameter values
	private static String ctxt_umm = null;

	private static String ctxt_um = null;

	// Request allowed parameter values
	public static final String REQ_MODE_SIMPLE = "sim";

	public static final String REQ_MODE_SOCIAL_NAVIGATION = "sn";

	public static final String REQ_MODE_TEXT_ONLY = "txt";

	public final static String[][] icons =
	{
		{ "webex_neo_bullet_white.gif", "webex_neo_bullet_green1.gif", "webex_neo_bullet_green2.gif", "webex_neo_bullet_green3.gif" },
		{ "webex_neo_bullet_white_chk.gif", "webex_neo_bullet_green1_chk.gif", "webex_neo_bullet_green2_chk.gif", "webex_neo_bullet_green3_chk.gif" }
	};

	private SQLManager sqlm = null;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public DissectionAquaAJAX()
	{
		super();
		sqlm = new SQLManager("java:comp/env/jdbc/webex");
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
		
		boolean show_title = "1".equals(req_title); // (req_title != null && req_title.equals("1"));
		boolean show_desc = "1".equals(req_descr); //(req_descr != null && req_descr.equals("1"));

		req_subactivity = (req_subactivity == null || req_subactivity.length() == 0) ? "0" : req_subactivity;
		int i_subactivity = Integer.parseInt(req_subactivity);

		Random rand = new Random();
		req_session = (req_session == null || req_session.length() == 0) ? "WE"
				+ rand.nextInt(101) : req_session;

		req_um = (req_um != null && req_um.length() > 0) ? URLDecoder.decode(req_um, "UTF-8") : null;
		req_umm = (req_umm != null && req_umm.length() > 0) ? URLDecoder.decode(req_umm, "UTF-8") : null;
		req_mode = (req_mode != null && req_mode.length() > 0) ? req_mode: REQ_MODE_SIMPLE;
		req_svc = (req_svc != null && req_svc.length() > 0) ? req_svc : "";

		// User Model and User Model Manager in request have priority
		if (req_um != null)
			ctxt_um = req_um;
		if (req_umm != null)
			ctxt_umm = req_umm;

		// update svc param
		req_svc += ((req_svc != null && req_svc.length() > 0) ? ";" : "") + "mode." + req_mode;

		// Report activity
		if (req_noreport == null || req_noreport.length() == 0)
		{
			ReportAPI rapi = new ReportAPI(ctxt_um);
			rapi.report(ReportAPI.APPLICATION_WEBEX, req_activity,
					req_subactivity, req_session, /* _res */-1, req_group,
					req_user, req_svc, req.getRemoteAddr());
		}
		// end of -- Report ectivity

		// Error if parameters are wrong
		if (req_subactivity == null || req_activity == null
				|| req_session == null
		/* || req_user==null || req_group==null */)
		{
			String result = WebexRSS.getErrorMessageHTML("Invalid parameters!", req.getContextPath());
			WebexRSS.WriteHTML(res, result);
			return;
		}

		// Retrieve Data
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		Statement stmt2 = null;
		ResultSet rs2 = null;
		
		String dissection_title = "";
		String dissection_desc = "";
		
		ArrayList<WebexLine> webex_lines = new ArrayList<WebexLine>();
		
		WebexLineComparator webex_cmp = new WebexLineComparator<WebexLine>();

		try
		{
			conn = sqlm.getConnection();
			
			//	Retrieve title and desc if necessary
			String sql2 = "SELECT * FROM ent_dissection WHERE rdfID='" + req_activity + "';";
			stmt2 = conn.prepareStatement(sql2);
			rs2 = stmt2.executeQuery(sql2);
			
			if(rs2.next())
			{
				dissection_title = rs2.getString("Name");
				dissection_desc = rs2.getString("Description");
				
				dissection_title = (dissection_title == null)?"":dissection_title;
				dissection_desc = (dissection_desc == null)?"":dissection_desc;
			}
			rs2.close();
			stmt2.close();
			rs2 = null;
			stmt2 = null;
			//	end of -- Retrieve title and desc if necessary
			
			//	Retrieve code
			String sql = "SELECT l.Code AS Code, l.Comment AS Comments, l.LineIndex AS LNo FROM ent_line l "
				+ "JOIN ent_dissection d ON(l.DissectionID=d.DissectionID) "
				+ "WHERE d.rdfID='" + req_activity + "' ORDER BY LNo ASC;";

			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery(sql);
			
			while(rs.next())
			{
				WebexLine wl = new WebexLine(rs.getInt("LNo"), convertString(rs.getString("Code")), rs.getString("Comments"));
				webex_lines.add(wl);
			}
			Collections.sort (webex_lines, webex_cmp); 
			//	end of -- Retrieve code
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.out);
			String result = WebexRSS.getErrorMessageHTML("Error retrieving data from the database!", req.getContextPath());
			WebexRSS.WriteHTML(res, result);
			return;
		}
		
		String global_error = "";
		WebexLine wl_title = new WebexLine(-1,dissection_title,dissection_desc);
		// get and upload the progress if necessary
		
		ArrayList uprlist = null;
		if (req_mode.equals(REQ_MODE_SOCIAL_NAVIGATION))
		{
			try
			{
				uprlist = getWebexProfile(req_activity, req_user, req_group, req);
				
				// now upload
				for(int i=0; i<((ProgressEstimatorReport)uprlist.get(0)).getSubs().size(); i++)
				{
					ProgressEstimatorReport per = (ProgressEstimatorReport)((ProgressEstimatorReport)uprlist.get(0)).getSubs().get(i);
					
					int um_line_idx = Integer.parseInt(per.getId());
					WebexLine um_line = new WebexLine(um_line_idx,"","");
					
					int wx_idx = Collections.binarySearch( webex_lines, um_line, webex_cmp);
//System.out.println("um_line_idx = " + um_line_idx);					
//System.out.println("wx_idx = " + wx_idx);		
					if(wx_idx>-1)
					{
						webex_lines.get(wx_idx).u_progress = per.getProgress(1 /*fudge*/);
						webex_lines.get(wx_idx).o_progress = per.getGroupProgress(1 /*fudge*/);
//System.out.println("u_progress/o_progress = " + webex_lines.get(wx_idx).u_progress + "/" + webex_lines.get(wx_idx).o_progress);		
					}
					if(um_line_idx==-1)
					{
						wl_title.u_progress = per.getProgress(1 /*fudge*/);
						wl_title.o_progress = per.getGroupProgress(1 /*fudge*/);
//System.out.println("titlw u/o " + wl_title.u_progress + "/" + wl_title.o_progress);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace(System.out);
				global_error = "Unable to retrieve user progress!";
				req_mode = REQ_MODE_SIMPLE;
			}
		}
		
		// Now visualize
		
		// Start the HTML
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>\n" +
			"<html>\n" +
			"<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n" +
			"<link href='" + req.getContextPath() + "/system/webex.css' rel='stylesheet' type='text/css'>\n" +
			"<head>\n" +
			"<script language='JavaScript' src='" + req.getContextPath() + "/system/prototype.js'></script>\n" +
			"<script language='JavaScript' type='text/javascript'>\n" +
			
			"function revealOne(anchor, line, svc)\n" + 
			"{\n" + 
			"	var name = anchor.id;\n" + 
			"\n" + 
			"	var to_zero = null;\n" + 
			"	to_zero = $A(document.getElementsByName('line'));\n" + 
			"	to_zero.each(\n" + 
			"		function(node){node.title='click to see explanation';}\n" + 
			"	);\n" + 
			"	to_zero = $A(document.getElementsByName('linecbx'));\n" + 
			"	to_zero.each(\n" + 
			"		function(node){node.className=node.className.split('_selected')[0]; node.onClick = null; }\n" + 
			"	);\n" + 
			"	to_zero = $A(document.getElementsByName('annot'));\n" + 
			"	to_zero.each(\n" + 
			"		function(node){node.className='wx_annot_row';}\n" + 
			"	);\n" + 
			"\n" + 
			"	$(name).title='';\n" + 
			"	$(name+'cbx').className+='_selected';\n" + 
			"	$(name+'cbx').onClick = 'revealOne(this)';\n" + 
			"	$(name+'annot').className='wx_annot_row_shown';\n" + 
			"	var gif_file = $(name+'icon').src;\n" + 
			"	if(gif_file.indexOf('_chk')==-1)\n" + 
			"		$(name+'icon').src = gif_file.split('.gif')[0] + '_chk.gif';\n" + 
			"	um_update(line, svc);\n" + 
			"}\n" + 
			"function um_update(line, svc)\n" + 
			"{ \n" + 
			"	var params = $H({usr:'" + req_user + "', grp:'" + req_group + "', sid:'" + req_session + "', app:'3', act:'" + req_activity + "', res:'-1', svc:'" + req_svc + "' + svc, sub:line});\n" + 
			"	$('notify_pane').innerHTML = '<img src=\"" + req.getContextPath() + "/system/progress20.gif\" width=\"20\" height=\"20\" border=\"0\"/>'\n" + 
			"	mvy_req = new Ajax.Request(\n" + 
			"		'" + req.getContextPath() + "/cumulateproxy',\n" +  
			"		{ \n" + 
			"			method: 'post', \n" + 
			"			parameters: params,\n" + 
			"			onSuccess: function(){ $('notify_pane').innerHTML='<img src=\"" + req.getContextPath() + "/system/empty_notification.gif\" width=\"20\" height=\"20\" border=\"0\"/>'; },\n" + 
			"			onFailure: function(){ $('notify_pane').innerHTML='<img src=\"" + req.getContextPath() + "/system/warning.gif\"/> Warning! there is no connection to the server. Your progress might be lost.';}\n" + 
			"		}\n" +  
			"	);\n" + 
			"}\n" +
			
			"</script>\n" +
			"<title>WebEx:: " + dissection_title + "</title>\n" +
			"</head>\n" +
			"<body>\n" +
			"<table width='100%' border='0' cellpadding='2' cellspacing='0'>");

		// Help
		if(REQ_MODE_SIMPLE.equals(req_mode))
			out.println("	<tr class='wx_help_row'><td colspan='2'><strong>Tip:</strong> To see explanations click on the text of those lines that have a bullet (<img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_white.gif' />) next to them</td></tr>");
		if(REQ_MODE_SOCIAL_NAVIGATION.equals(req_mode))
			out.println("	<tr class='wx_help_row'><td colspan='2'><strong>Tip:</strong> To see explanations click on the text of lines that have a bullet (<img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_white.gif' />) next to them. Checkmark (<img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_white_chk.gif' />) means you have alreadfy seen the explanations to this line before. Color denotes whether none, few, good number of, or a lot of (<img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_white.gif' /> <img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_green1.gif' /> <img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_green2.gif' /> <img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_green3.gif' />) your classmates viewed the explanation.</td></tr>");
		out.println("	<tr><td height='20px' colspan='2' id='notify_pane' style='margin-top:10px;margin-bottom:10px;font-size:10pt;color:#990000'><img src='" + req.getContextPath() + "/system/empty_notification.gif'  width='20' height='20' border='0' /></td></tr>");

		// Title if there
		if(show_title)
		{
			boolean desc_open = i_subactivity==-1;
			boolean desc_exists = dissection_desc.length()>0;

//System.out.println("dissection_desc = |" + dissection_desc + "|");
//System.out.println("show_desc = " + show_desc);
//System.out.println("desc_exists = " + desc_exists);
			
			out.println("	<tr class='wx_title_row'>\n" +
						"		<td class='wx_icon_cell'>" + ((show_desc)?wl_title.getIcon(req_mode, req.getContextPath()):"&nbsp;") /*((show_desc && desc_exists)?"<img class='wx_icon' src='" + req.getContextPath() + "/system/webex_neo_bullet_white.gif' id='line-1icon' name='icon' />":"&nbsp") */ + "</td>\n" + 
						"		<td class='wx_title_cell" + ((show_desc && desc_exists && desc_open)?"_selected":"") + "' id='line-1cbx' name='linecbx'><a id='line-1' name='line' title='click to see explanation' " + ((show_desc && !desc_open && desc_exists)?"href='javascript:void(0)' onClick='revealOne(this,-1,\"" + wl_title.getSVCSuffix(req_mode) + "\");'":"") + ">" + dissection_title + "</a></td>\n" + 
						"	</tr>\n" + 
						((show_desc && desc_exists)?
						"	<tr class='wx_annot_row" + ((desc_open)?"_shown":"") + "' id='line-1annot' name='annot'>\n" + 
						"		<td>&nbsp;</td>\n" + 
						"		<td class='wx_annot_cell'>" + dissection_desc + "</td>\n" + 
						"	</tr>\n":"") + 
						"	<tr>\n" + 
						"		<td>&nbsp;</td>\n" + 
						"		<td>&nbsp;</td>\n" + 
						"	</tr>\n");
		}
		
		// Code
		for(Iterator<WebexLine> it=webex_lines.iterator(); it.hasNext(); )
		{
			WebexLine wl = it.next();
			boolean has_annot = wl.annot.length() > 0;
			boolean annot_open = wl.line == i_subactivity;
			out.println(
				"	<tr class='wx_code_row'>\n" +
				"		<td class='wx_icon_cell'>" + wl.getIcon(req_mode, req.getContextPath()) + "</td>\n" +
				"		<td class='wx_code_cell" + ((has_annot && annot_open)?"_selected":"") + "' id='line" + wl.line + "cbx' name='linecbx'><a id='line" + wl.line + "' name='line'" + ((has_annot && !annot_open)? " title='click to see explanation' href='javascript:void(0)' onClick='revealOne(this," + wl.line + ",\"" + wl.getSVCSuffix(req_mode) + "\");'":"") + ">" + ((wl.code.length()==0)?"    ":"") + wl.code + "</a></td>\n" +
				"	</tr>\n" +
				((has_annot)?
				"	<tr class='wx_annot_row" + ((annot_open)?"_shown":"") + "' id='line" + wl.line + "annot' name='annot'>\n" +
				"		<td>&nbsp;</td>\n" +
				"		<td class='wx_annot_cell'>" + wl.annot + "</td>\n" +
				"	</tr>\n":"")
			);
		}
		out.println("</table>");
		
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
//		while (i < str.length() && (c = str.charAt(i++)) == ' ')
//			sbuf.append("&nbsp;");
//		i--;
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
}

class WebexLine 
{
	public String code;
	public String annot;
	public int line;
	public double u_progress;
	public double o_progress;
	
	public static WebexLine comparator = new WebexLine(0,"","");
	
	public WebexLine(int _l, String _c, String _a)
	{
		code = _c;
		annot = _a;
		line = _l;
	}	
	
	public String getIcon(String req_mode, String context_path)
	{
		String result = "&nbsp;";
		
		if(annot.length()>0)
		{
			if(req_mode.equals(DissectionAquaAJAX.REQ_MODE_SOCIAL_NAVIGATION))
			{
				int social_index = (int) Math.round(o_progress * 4);
				social_index = (social_index>3)?3:social_index;
				int pers_index  = (u_progress>0)?1:0;
				
				result = "<img class='wx_icon' src='" + context_path + "/system/" + DissectionAquaAJAX.icons[pers_index][social_index] + "' id='line" + this.line + "icon' name='icon' />";
			}
			if(req_mode.equals(DissectionAquaAJAX.REQ_MODE_SIMPLE))
			{
				result = "<img class='wx_icon' src='" + context_path + "/system/webex_neo_bullet_white.gif' id='line" + this.line + "icon' name='icon' />";
			}
		}		
		return result;
	}
	
	public String getSVCSuffix(String req_mode)
	{
		String result = "";
		
		if(annot.length()>0)
		{
			if(req_mode.equals(DissectionAquaAJAX.REQ_MODE_SOCIAL_NAVIGATION))
			{
				int social_index = (int) Math.round(o_progress * 4);
				social_index = (social_index>3)?3:social_index;
				int pers_index  = (u_progress>0)?1:0;
				
				result = ";Wu" + pers_index + "|g" + social_index;
			}
			if(req_mode.equals(DissectionAquaAJAX.REQ_MODE_SIMPLE))
			{
				result = "";
			}
		}		
		return result;
	}
	
}

class WebexLineComparator<E extends WebexLine> implements Comparator<E>
{
	public int compare(WebexLine i1, WebexLine i2)
	{
		return (i1.line - i2.line);
	}

}


