package edu.unict.tswd.fakeflix;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.*;
import java.util.Map;

public class fakeflix extends HttpServlet {
    Connection dbConnection=null;

    static final String DATABASE_URL = "jdbc:mysql://localhost/myDb";
    public void init() {
        try {
            // establish connection to database
            dbConnection = DriverManager.getConnection(DATABASE_URL, "username", "password");
            System.out.println("Connected:"+dbConnection.toString());
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } // end catch
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Statement aStatement = null;
        ResultSet aResultSet = null;
        String randomTitle; 
        String query;
       
        // Do Query
        try {
            aStatement = this.dbConnection.createStatement();
            query = "SELECT titolo from flist ORDER BY RAND() LIMIT 1";
            aResultSet = aStatement.executeQuery(query);
            aResultSet.next();
            randomTitle=aResultSet.getString("titolo");
        } catch (Exception e) {
            System.out.println("Exception "+e);
            randomTitle="";
        }

        // Render Page
        response.setContentType("text/html");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.write("<html>");
        out.write("<head><title></title></head>");
        out.write("<body>");
        // Print suggested title
        if (!randomTitle.equals("")) {
            out.write("<h1>Titolo Consigliato</h1>");
            out.write("<b>"+randomTitle+"</b>");
        }
        // Print Form
        out.write("<h1>Search a movie</h1>");
        out.write("<form action=\"/fakeflix\" method=\"POST\">"+
        "<label for=\"title\">Title</label>"+
        "<input type=\"text\" name=\"title\">"+
        "<label for=\"director\">Director</label>"+
        "<input type=\"text\" name=\"director\">"+
        "<input type=\"submit\" name=\"action\" value=\"search\">"+
        "<input type=\"submit\" name=\"action\" value=\"showWishList\">"+
        "</form>");
        out.write("</body></html>");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = "";
        String query;
        String query2;
        String title = "";
        String director = "";
        PreparedStatement aStatement; // Query to search in flist
        PreparedStatement aStatement2; // Query to search in wlist
        Statement cStatement; // Query to search wList
        ResultSet aResultSet;
        boolean hasNOTResult;
        int numrows = 0;
        // Render Page
        response.setContentType("text/html");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        action = request.getParameter("action");
        title = request.getParameter("title");
        director = request.getParameter("director");

        switch (action) {
            case "search": {
                if (title.equals("") && director.equals("")) {
                    out.write("<h2>Missing Parameters</h2>");
                    out.write("<a href=\"/fakeflix\">Back</a>");
                    break;
                }
                try {
                    if (!title.equals("") && director.equals("")) {
                        query = "select titolo from flist where titolo = ?";
                        query2 = "select titolo from wlist where titolo = ?";
                        aStatement = this.dbConnection.prepareStatement(query);
                        aStatement2 = this.dbConnection.prepareStatement(query2);
                        aStatement.setString(1, title);
                        aStatement2.setString(1, title);
                    } else if (title.equals("") && !director.equals("")) {
                        query = "select titolo from flist where regista = ?";
                        aStatement = this.dbConnection.prepareStatement(query);
                        aStatement.setString(1, director);
                        query2 = "select titolo from wlist where regista = ?";
                        aStatement2 = this.dbConnection.prepareStatement(query);
                        aStatement2.setString(1, director);
                    } else {
                        query = "select titolo from flist where titolo = ? or regista = ?";
                        aStatement = this.dbConnection.prepareStatement(query);
                        aStatement.setString(1, title);
                        aStatement.setString(2, director);
                        query2 = "select titolo from flist where titolo = ? or regista = ?";
                        aStatement2 = this.dbConnection.prepareStatement(query);
                        aStatement2.setString(1, title);
                        aStatement2.setString(2, director);
                    }
                    aResultSet = aStatement.executeQuery();
                    hasNOTResult=true;
                    while (aResultSet.next()) {
                        if (hasNOTResult) out.write("<h1>Titoli richiesti</h1>");
                        hasNOTResult=false;
                        out.write("<p>" + aResultSet.getString("titolo") + "</p>");
                    }

                    aResultSet.close();
                    if (hasNOTResult) {
                        out.write("<h1>No result in available movies</h1>");
                        // Search in wishlist
                        aResultSet = aStatement2.executeQuery();
                        hasNOTResult = true;
                        while (aResultSet.next()) {
                            if (hasNOTResult) out.write("<h1>Titoli già presente in whishlist</h1>");
                            hasNOTResult = false;
                            out.write("<p>" + aResultSet.getString("titolo") + "</p>");
                            out.write("<a href=\"/fakeflix\">Back</a>");
                        }
                        aStatement2.close();
                        if (hasNOTResult) { // Siamo nel caso in cui il titolo non e' presente neanche nella whishlist
                            out.write("<h1>Do you want to add to wishlist ? </h1>");
                            out.write("<form action=\"/fakeflix\" method=\"POST\">" +
                                    "<input type=\"hidden\" name=\"title\" value=\"" + title + "\">" +
                                    "<input type=\"hidden\" name=\"director\" value=\"" + director + "\">" +
                                    "<input type=\"submit\" name=\"action\" value=\"addToWhishList\">" +
                                    "<input type=\"submit\" name=\"action\" value=\"returnToHomePage\">" +
                                    "</form>");
                        }
                    } else {
                        out.write("<a href=\"/fakeflix\">Back</a>");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "addToWhishList": {
                out.write("Adding to Whishlist");
                try {
                if (!title.equals("") && director.equals("")) {
                    query = "insert into wlist(titolo) values (?)";
                    aStatement = this.dbConnection.prepareStatement(query);
                    aStatement.setString(1, title);
                } else if (title.equals("") && !director.equals("")) {
                    query = "insert into wlist(regista) values (?)";
                    aStatement = this.dbConnection.prepareStatement(query);
                    aStatement.setString(1, director);
                } else {
                    query = "insert into wlist(titolo,regista) values (?,?)";
                    aStatement = this.dbConnection.prepareStatement(query);
                    aStatement.setString(1, title);
                    aStatement.setString(2, director);
                }
                numrows=aStatement.executeUpdate();
                out.write("<p>Element Inserted: "+numrows+"</p>");
                out.write("<a href=\"/fakeflix\">Back</a>");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "returnToHomePage": {
                doGet(request,response);
                break;
            }
            case "showWishList": {
                cStatement=null;
                out.write("<h1>Show Wish List</h1>");
                // Do Query
                try {
                    cStatement = this.dbConnection.createStatement();
                    query = "SELECT titolo,regista from wlist";
                    aResultSet = cStatement.executeQuery(query);
                    hasNOTResult=true;
                    while (aResultSet.next()) {
                        hasNOTResult=false;
                        out.write("<p>" + aResultSet.getString("titolo") + " "+ aResultSet.getString("regista") + "</p>");
                    }
                    aResultSet.close();
                    if (hasNOTResult) {
                        out.write("<p>WishList Empty</p>");
                        out.write("<a href=\"/fakeflix\">Back</a>");
                    } else {
                        // Print Form
                        out.write("<h1>Do you want to delete whitelist ?</h1>");
                        out.write("<form action=\"/fakeflix\" method=\"POST\">" +
                                "<input type=\"submit\" name=\"action\" value=\"deleteWishList\">" +
                                "<input type=\"submit\" name=\"action\" value=\"returnToHomePage\">" +
                                "</form>");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "deleteWishList": {
                aStatement = null;
                out.write("<h1>Delete WishList</h1>");
                query="delete  from wlist";
                try {
                    aStatement = this.dbConnection.prepareStatement(query);
                    numrows=aStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                out.write("<p>Element Deleted: "+numrows+"</p>");
                out.write("<a href=\"/fakeflix\">Back</a>");
                break;
            }
        }
    }
    public void destroy() {
        try {
            dbConnection.close();
        } catch (Exception exception) {
                exception.printStackTrace();
        }
    }
}