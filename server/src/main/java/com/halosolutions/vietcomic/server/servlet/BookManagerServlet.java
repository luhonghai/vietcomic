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
public class BookManagerServlet extends HttpServlet {

    public static class ResponseData {
        int latestVersion;
        int oldVersion;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        ResponseData responseData = new ResponseData();
        responseData.oldVersion = BookManager.DATA_VERSION;
        BookManager.load(true);
        responseData.latestVersion = BookManager.DATA_VERSION;
        resp.getWriter().println(new Gson().toJson(responseData));
    }
}
