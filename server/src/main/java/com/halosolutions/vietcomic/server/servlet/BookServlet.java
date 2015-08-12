package com.halosolutions.vietcomic.server.servlet;

import com.halosolutions.vietcomic.server.service.BookManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by cmg on 12/08/15.
 */
public class BookServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().println(BookManager.getBookDataJson());
    }
}
