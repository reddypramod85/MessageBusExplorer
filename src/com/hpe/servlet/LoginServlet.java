package com.hpe.servlet;

import java.io.*;
import java.net.ConnectException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hpe.ConnectToOneView;

/*
 * Created by: pramod-reddy.sareddy@hpe.com
 * Class Name: LoginServlet
 * Description: LoginServlet to receive the request parameters and call the getConnOV method of ConnectToOneView class
 * Date Created: 12-MAR-2016
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String host = request.getParameter("host");
		String userName = request.getParameter("UsrName");
		String password = request.getParameter("pswd");
		request.setAttribute("errormsg", "");
		System.out.println("IN LOGIN SERVLET"+host+"userName"+userName+"password"+password);
		ConnectToOneView connOV = new ConnectToOneView();
		try {
			//response.getWriter().write(ConnectToOneView.getInstance().getConnOV(host,userName,password));
			connOV.getConnOV(host,userName,password,request);
			if(request.getAttribute("errormsg")!=null && request.getAttribute("errormsg")==""){
				request.getRequestDispatcher("/HPEOVMsgBus.jsp").forward(request, response);
			}
			else{
				request.setAttribute("host", host);
				request.setAttribute("UsrName", userName);
				request.getRequestDispatcher("/Login.jsp").forward(request, response);
			}
			System.out.println("END OF LOGIN SERVLET");
		}catch (ConnectException e) {
			e.printStackTrace();
			System.out.println("catched the connection timed out exception in LOGIN servlet");
			try {
				request.getRequestDispatcher("/Login.jsp").forward(request, response);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("catched the exception in servlet");
			/*request.setAttribute("errormsg", "Please enter valid credentials");
			request.setAttribute("host", host);
			request.setAttribute("UsrName", userName);*/
			try {
				//response.sendRedirect(redirect+"/Login.jsp");
				request.getRequestDispatcher("/Login.jsp").forward(request, response);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
