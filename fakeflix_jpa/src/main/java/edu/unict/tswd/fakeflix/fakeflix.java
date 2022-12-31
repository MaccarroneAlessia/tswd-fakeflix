package edu.unict.tswd.fakeflix;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;

public class fakeflix extends HttpServlet {
    EntityManager em;

    public void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        this.em = emf.createEntityManager();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String randomMovie="";
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
        // Do Query
        try {
            Query q = em.createNativeQuery("SELECT titolo from flist ORDER BY RAND() LIMIT 1");
            randomMovie= (String) q.getSingleResult();
            if (!randomMovie.equals("")) {
                out.write("<h1>Titolo Consigliato</h1>");
                out.write("<b>"+randomMovie+"</b>");
            }
        } catch (Exception e) {
            System.out.println("Exception "+e);
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
        String title = "";
        String director = "";
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
                List<flist> flists = em
                        .createQuery("Select m from flist m where m.titolo = :title or m.regista = :director", flist.class)
                        .setParameter("title",title)
                        .setParameter("director",director)
                        .getResultList();

                hasNOTResult=true;
                for (flist s: flists) {
                    if (hasNOTResult) {
                        out.write("<h1>Titoli richiesti</h1>");
                        hasNOTResult=false;
                    }
                    out.write("<li>" +s.getId()+" "+s.getTitolo() +"</li>");
                }

                if (hasNOTResult) {
                    out.write("<h1>No result in available movies</h1>");
                    List<wlist> wlists = em
                            .createQuery("Select m from wlist m where m.titolo = :title or m.regista = :director", wlist.class)
                            .setParameter("title",title)
                            .setParameter("director",director)
                            .getResultList();
                    hasNOTResult = true;
                    for (wlist s: wlists) {
                        if (hasNOTResult) {
                            out.write("<h1>Titoli già presente in whishlist</h1>");
                            hasNOTResult=false;
                        }
                        out.write("<li>" +s.getId()+" "+s.getTitolo() +"</li>");
                    }
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
                break;
            }
            case "addToWhishList": {
                out.write("Adding to Whishlist "+title+","+director);
                wlist item=new wlist(title,director);
                EntityTransaction transaction = this.em.getTransaction();
                transaction.begin();
                this.em.persist(item);
                transaction.commit();
                out.write("<p>Element Inserted</p>");
                out.write("<a href=\"/fakeflix\">Back</a>");
                break;
            }
            case "returnToHomePage": {
                doGet(request,response);
                break;
            }
            case "showWishList": {
                out.write("<h1>Show Wish List</h1>");
                List<wlist> wlists = em
                        .createQuery("Select m from wlist m", wlist.class)
                        .getResultList();
                hasNOTResult = true;
                for (wlist s: wlists) {
                    if (hasNOTResult) {
                        out.write("<h1>Titoli già presenti in whishlist</h1>");
                        hasNOTResult=false;
                    }
                    out.write("<li>" +s.getId()+" "+s.getTitolo() +"</li>");
                }
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
                break;
            }
            case "deleteWishList": {
                String stringQuery = "DELETE FROM wlist";
                EntityTransaction transaction = this.em.getTransaction();
                transaction.begin();
                Query queryDelete = this.em.createQuery(stringQuery);
                numrows=queryDelete.executeUpdate();
                transaction.commit();
                out.write("<p>Element Deleted: "+numrows+"</p>");
                out.write("<a href=\"/fakeflix\">Back</a>");
                break;
            }
        }
    }

    public void destroy() {
        this.em.close();
    }
}