import com.fasterxml.jackson.databind.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    public final Map<Integer, Order> orderDatabase = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Order order = objectMapper.readValue(req.getInputStream(), Order.class);
        orderDatabase.put(order.getId(), order);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(resp.getOutputStream(), order);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        Order order = orderDatabase.get(id);

        if (order != null) {
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getOutputStream(), order);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        Order updatedOrder = objectMapper.readValue(req.getInputStream(), Order.class);

        if (orderDatabase.containsKey(id)) {
            orderDatabase.put(id, updatedOrder);
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getOutputStream(), updatedOrder);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));

        if (orderDatabase.remove(id) != null) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}