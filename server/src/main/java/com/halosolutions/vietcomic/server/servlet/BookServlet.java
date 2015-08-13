package com.halosolutions.vietcomic.server.servlet;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.server.service.BookManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by cmg on 12/08/15.
 */
public class BookServlet extends HttpServlet {

    public static class ResponseData {
        int version;
        String data;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String v = req.getParameter("v");
        int version = 0;
        try {
            version = Integer.parseInt(v);
        } catch (Exception e) {}
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        ResponseData responseData = new ResponseData();
        if (version < BookManager.DATA_VERSION || BookManager.DATA_VERSION == -1) {
            responseData.data =BookManager.getBookDataJson();
        }
        responseData.version = BookManager.DATA_VERSION;
        resp.getWriter().println(BookManager.getBookDataJson());
    }
}
