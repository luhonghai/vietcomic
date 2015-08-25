package com.halosolutions.vietcomic.server.servlet;

import com.google.gson.Gson;
import com.halosolutions.vietcomic.server.data.ComicBook;
import com.halosolutions.vietcomic.server.service.BookService;
import com.halosolutions.vietcomic.server.service.VechaiBookService;
import com.halosolutions.vietcomic.server.service.VietcomicBookService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmg on 12/08/15.
 */
public class BookServlet extends HttpServlet {

    private static final List<BookService> SERVICES;

    static {
        SERVICES = new ArrayList<BookService>();
        SERVICES.add(new VechaiBookService());
        SERVICES.add(new VietcomicBookService());
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        System.out.println("Start");
        long start = System.currentTimeMillis();
        resp.getWriter().flush();

        List<ComicBook> comicBookList = new ArrayList<ComicBook>();
        for (BookService bookService : SERVICES) {
            bookService.load(true);
            comicBookList.addAll(bookService.getBookData().values());
        }
        Gson gson = new Gson();
        //File output = new File("comic.json");

        resp.getWriter().println(gson.toJson(comicBookList));

        //System.out.println("Write to file: " + output.getAbsolutePath());
        System.out.println("Done. Execution time: " + (System.currentTimeMillis() - start) + "ms");
        //resp.getWriter().flush();
    }
}
