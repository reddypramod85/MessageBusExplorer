package com.hpe.servlet;

import java.io.*;
import java.net.SocketException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hpe.ReceiveMsgsSCMB;
import com.rabbitmq.client.AlreadyClosedException;

/*
 * Created by: pramod-reddy.sareddy@hpe.com
 * Class Name: ReceiveMessageServlet
 * Description: To receive the request parameters and call the getMyMethod method of ReceiveMsgsSCMB class
 * Date Created: 12-MAR-2016
 */
public class ReceiveMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReceiveMessageServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//messageVO vo = new messageVO();
		System.out.println("IN ReceiveMessageServlet");
		String host = request.getParameter("host");
		String exchange = request.getParameter("exchange");
		String key = request.getParameter("key");
		String flag = request.getParameter("flag");
		request.setAttribute("errorMessage", "");
		
		try {
			response.getWriter().write(ReceiveMsgsSCMB.getInstance().getMyMethod(host,exchange,key,flag,request));
			
			/*if(resp!=null && resp!="" && resp.equalsIgnoreCase("Metric configuration was automatically set to 300s interval because no configuration was found!")){
				request.setAttribute("msmbStatus", resp);
				System.out.println("resp"+resp);
				response.getWriter().write("");
			}else{
				System.out.println("resp"+resp);
				response.getWriter().write(resp);
			}*/
			request.setAttribute("host",host);
			request.setAttribute("exchange",exchange);
			request.setAttribute("key",key);
			request.setAttribute("flag",2);
			System.out.println("END OF ReceiveMessageServlet");
		}catch(AlreadyClosedException e){
			System.out.println("+++++In Catch ReceiveMessageServlet AlreadyClosedException ReceiveMsgsSCMB+++++");
			request.setAttribute("errorMessage", "AlreadyClosedException");
			System.out.println(e);
		}
		catch(SocketException e){
			System.out.println("+++++In Catch ReceiveMessageServlet SocketException ReceiveMsgsSCMB+++++");
			request.setAttribute("errorMessage", "SocketException");
			System.out.println(e);
		}catch(IOException e){
			System.out.println("+++++In Catch ReceiveMessageServlet AlreadyClosedException ReceiveMsgsSCMB+++++");
			request.setAttribute("errorMessage", "IOException");
			System.out.println(e);
			} catch (Exception e) {
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
