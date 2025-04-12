package controller;

import model.OrderItem;
import model.Product;
import model.User;
import util.HibernateUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.hibernate.Session;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/products/*")
public class ProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();

        out.println("<html><head><title>Online Grocery Shop - Products</title>");
        out.println("<link rel='stylesheet' href='/static/css/style.css'></head><body>");
        out.println("<header><nav>" + getNavBar(user) + "</nav></header>");
        out.println("<div class='container'>");

        try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {

            if ("/add".equals(pathInfo) && isAdmin(user)) {
                out.println("<h1>Add New Product</h1>");
                out.println("<form method='post' action='/products/add'>");
                out.println("<input type='text' name='productname' placeholder='Product Name' required>");
                out.println("<input type='number' name='price' placeholder='Price' step='0.01' required>");
                out.println("<input type='number' name='quantity' placeholder='Quantity' step='1' required>");
                out.println("<input type='number' name='stock' placeholder='Stock' required>");
                out.println("<button type='submit'>Add Product</button>");
                out.println("</form>");

            } else if ("/edit".equals(pathInfo) && isAdmin(user)) {
                String idStr = req.getParameter("id");
                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    Product product = dbSession.get(Product.class, id);
                    if (product != null) {
                        out.println("<h1>Edit Product</h1>");
                        out.println("<form method='post' action='/products/edit'>");
                        out.println("<input type='hidden' name='id' value='" + product.getProductId() + "'>");
                        out.println("<input type='text' name='productname' value='" + product.getProductname() + "' required>");
                        out.println("<input type='number' name='price' value='" + product.getPrice() + "' step='0.01' required>");
                        out.println("<input type='number' name='quantity' value='" + product.getQuantity() + "' step='1' required>");
                        out.println("<input type='number' name='stock' value='" + product.getStock() + "' required>");
                        out.println("<button type='submit'>Update Product</button>");
                        out.println("</form>");
                    } else {
                        out.println("<p>Product not found.</p>");
                    }
                }

            } else {
                out.println("<h1>Our Products</h1>");
                List<Product> products = dbSession.createQuery("FROM Product", Product.class).list();
                out.println("<div class='product-grid'>");
                for (Product product : products) {
                    out.println("<div class='product-card'>");
                    out.println("<h3>" + product.getProductname() + "</h3>");
                    out.println("<p class='price'>$" + product.getPrice() + "</p>");
                    out.println("<p>Stock: " + product.getStock() + "</p>");
                    out.println("<p>Quantity: " + product.getQuantity() + "</p>");
                    if (user != null) {
                        out.println("<form method='post' action='/cart/add'>");
                        out.println("<input type='hidden' name='productId' value='" + product.getProductId() + "'>");
                        out.println("<button type='submit'>Add to Cart</button>");
                        out.println("</form>");
                    }
                    if (isAdmin(user)) {
                        out.println("<form method='get' action='/products/edit'>");
                        out.println("<input type='hidden' name='id' value='" + product.getProductId() + "'>");
                        out.println("<button type='submit'>Edit</button>");
                        out.println("</form>");

                        out.println("<form method='post' action='/products/delete'>");
                        out.println("<input type='hidden' name='id' value='" + product.getProductId() + "'>");
                        out.println("<button type='submit' onclick='return confirm(\"Are you sure?\")'>Delete</button>");
                        out.println("</form>");
                    }
                    out.println("</div>");
                }
                out.println("</div>");
            }

        }
        out.println("</div></body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        String pathInfo = req.getPathInfo();

        if (user == null || !isAdmin(user)) {
            resp.sendRedirect("/auth/login");
            return;
        }

        out.println("<html><head><title>Online Grocery Shop</title>");
        out.println("<link rel='stylesheet' href='/static/css/style.css'></head><body>");
        out.println("<header><nav>" + getNavBar(user) + "</nav></header>");
        out.println("<div class='container'>");

        try (Session dbSession = HibernateUtil.getSessionFactory().openSession()) {

            if ("/add".equals(pathInfo)) {
                Product product = new Product();
                product.setProductname(req.getParameter("productname"));
                product.setPrice(new BigDecimal(req.getParameter("price")));
                product.setQuantity(Integer.parseInt(req.getParameter("quantity")));
                product.setStock(Integer.parseInt(req.getParameter("stock")));

                dbSession.beginTransaction();
                dbSession.persist(product);
                dbSession.getTransaction().commit();

                out.println("<h1>Success</h1><p>Product added! <a href='/products'>Back to Products</a>.</p>");

            } else if ("/edit".equals(pathInfo)) {
                int id = Integer.parseInt(req.getParameter("id"));
                dbSession.beginTransaction();
                Product product = dbSession.get(Product.class, id);
                if (product != null) {
                    product.setProductname(req.getParameter("productname"));
                    product.setPrice(new BigDecimal(req.getParameter("price")));
                    product.setQuantity(Integer.parseInt(req.getParameter("quantity")));
                    product.setStock(Integer.parseInt(req.getParameter("stock")));
                    dbSession.update(product);
                    dbSession.getTransaction().commit();
                    out.println("<h1>Updated</h1><p>Product updated. <a href='/products'>Back</a></p>");
                } else {
                    out.println("<h1>Error</h1><p>Product not found.</p>");
                }

             } else if ("/delete".equals(pathInfo)) {
            	    int id = Integer.parseInt(req.getParameter("id"));
            	    dbSession.beginTransaction();
            	    Product product = dbSession.get(Product.class, id);
            	    if (product != null) {
            	        // Retrieve all OrderItem entities associated with this Product
            	        List<OrderItem> orderItems = dbSession.createQuery(
            	            "FROM OrderItem WHERE product.productId = :productId", OrderItem.class)
            	            .setParameter("productId", id)
            	            .list();

            	        // Delete all associated OrderItem entities
            	        for (OrderItem orderItem : orderItems) {
            	            dbSession.remove(orderItem);
            	        }

            	        // Now, delete the Product
            	        dbSession.remove(product);
            	        dbSession.getTransaction().commit();
            	        out.println("<h1>Deleted</h1><p>Product and its associated order items deleted. <a href='/products'>Back</a></p>");
            	    } else {
            	        out.println("<h1>Error</h1><p>Product not found.</p>");
            	        dbSession.getTransaction().rollback();
            	    }
            	}


        } catch (Exception e) {
            out.println("<h1>Error</h1><p>" + e.getMessage() + "</p>");
        }

        out.println("</div></body></html>");
    }

    private String getNavBar(User user) {
        StringBuilder nav = new StringBuilder("<ul class='nav-list'>");
        nav.append("<li><a href='/auth'>Home</a></li>");
        nav.append("<li><a href='/products'>Products</a></li>");
        nav.append("<li><a href='/cart'>Cart</a></li>");
        if (user != null) {
            nav.append("<li><a href='/orders'>Orders</a></li>");
            if (isAdmin(user)) {
                nav.append("<li><a href='/products/add'>Add Product</a></li>");
                nav.append("<li><a href='/products'>Manage Products</a></li>"); 
            }
            nav.append("<li><a href='/auth/logout'>Logout (" + user.getUsername() + ")</a></li>");
        } else {
            nav.append("<li><a href='/auth/login'>Login</a></li>");
            nav.append("<li><a href='/auth/register'>Register</a></li>");
        }
        nav.append("</ul>");
        return nav.toString();
    }

    private boolean isAdmin(User user) {
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }
}
