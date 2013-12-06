package edu.pitt.sis.paws.webex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: CUMULATEProxy
 * 
 */
public class CUMULATEProxy extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
{
	static final long serialVersionUID = 1L;

	public static final String REQ_ACTIVITY = "act";

	public static final String REQ_SUBACTIVITY = "sub";

	public static final String REQ_SESSION = "sid";

	public static final String REQ_GROUP = "grp";

	public static final String REQ_USER = "usr";

	public static final String REQ_SVC = "svc";

	public static final String REQ_MODE = "mode";

	public static final String REQ_RESULT = "res";

	private static String ctxt_um = null;
	private static final String CTXT_UM = "um";

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		// Read context parameters
		ServletContext context = getServletContext();
		ctxt_um = context.getInitParameter(CTXT_UM);
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
//		RequestDispatcher disp;
//		disp = request.getRequestDispatcher(ctxt_um);
//		disp.forward	(request, response);
		
		String req_activity = request.getParameter(REQ_ACTIVITY);
		String req_subactivity = request.getParameter(REQ_SUBACTIVITY);
		String req_session = request.getParameter(REQ_SESSION);
		String req_group = request.getParameter(REQ_GROUP);
		String req_user = request.getParameter(REQ_USER);
		String req_svc = request.getParameter(REQ_SVC);
		String req_mode = request.getParameter(REQ_MODE);

		String report_url = ctxt_um + "?usr=" + req_user + "&sid="
			+ req_session + "&app=3&act=" + req_activity + "&sub="
			+ req_subactivity + "&res=-1&grp=" + req_group
			+ "&svc=" + req_svc;
		
		URLConnection dbpc = (new URL(report_url)).openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				dbpc.getInputStream()));
		
//		PrintWriter out = response.getWriter();
//		out.println(in.readLine());
//		out.close();
		
		in.close();
		

//		response.sendRedirect(ctxt_um);
	}
}