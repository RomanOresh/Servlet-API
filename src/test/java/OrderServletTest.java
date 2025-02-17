import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServletTest {
    private OrderServlet orderServlet;
    private ObjectMapper objectMapper;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;
    private PrintWriter printWriter;
    private ByteArrayOutputStream responseStream;

    @BeforeEach
    void setUp() throws IOException {
        orderServlet = new OrderServlet();
        objectMapper = new ObjectMapper();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        // Настройка Mock OutputStream для response
        responseStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) {
                responseStream.write(b);
            }
        };

        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @Test
    void testDoPost_CreatesOrder() throws IOException, ServletException {
        List<Product> products = List.of(new Product(1, "Laptop", 1200.0));
        Order order = new Order(1, new Date(), 1200.0, products);
        String jsonRequest = objectMapper.writeValueAsString(order);

        when(request.getInputStream()).thenReturn(new ServletInputStreamMock(jsonRequest));

        orderServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        assertTrue(orderServlet.orderDatabase.containsKey(1));

        // Проверяем содержимое JSON-ответа
        String jsonResponse = responseStream.toString().trim();
        assertEquals(jsonRequest, jsonResponse);
    }

    @Test
    void testDoPut_UpdatesOrder() throws IOException, ServletException {
        // Создаём заказ
        Order originalOrder = new Order(3, new Date(), 500.0, List.of(new Product(3, "Tablet", 500.0)));
        orderServlet.orderDatabase.put(3, originalOrder);

        // Обновляем заказ
        Order updatedOrder = new Order(3, new Date(), 600.0, List.of(new Product(3, "Tablet", 600.0)));
        String jsonRequest = objectMapper.writeValueAsString(updatedOrder);

        // Настройка запроса
        when(request.getParameter("id")).thenReturn("3");
        when(request.getInputStream()).thenReturn(new ServletInputStreamMock(jsonRequest));

        // Вызов метода doPut
        orderServlet.doPut(request, response);

        // Проверяем, что заказ обновился
        assertEquals(600.0, orderServlet.orderDatabase.get(3).getCost());

    }

    @Test
    void testDoPut_OrderNotFound() throws IOException, ServletException {
        // Обновляем несуществующий заказ
        Order updatedOrder = new Order(99, new Date(), 700.0, List.of(new Product(4, "Monitor", 700.0)));
        String jsonRequest = objectMapper.writeValueAsString(updatedOrder);

        // Настройка запроса
        when(request.getParameter("id")).thenReturn("99");
        when(request.getInputStream()).thenReturn(new ServletInputStreamMock(jsonRequest));

        // Вызов метода doPut
        orderServlet.doPut(request, response);

        // Проверяем, что вернулся 404
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoDelete_RemovesOrder() throws IOException, ServletException {
        // Создаём заказ
        orderServlet.orderDatabase.put(4, new Order(4, new Date(), 300.0, List.of()));

        // Настройка запроса
        when(request.getParameter("id")).thenReturn("4");

        // Вызов метода doDelete
        orderServlet.doDelete(request, response);

        // Проверяем, что заказ удалён
        assertFalse(orderServlet.orderDatabase.containsKey(4));

        // Проверяем статус ответа
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void testDoDelete_OrderNotFound() throws IOException, ServletException {
        // Настройка запроса
        when(request.getParameter("id")).thenReturn("99");

        // Вызов метода doDelete
        orderServlet.doDelete(request, response);

        // Проверяем, что вернулся 404
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }


    private static class ServletInputStreamMock extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public ServletInputStreamMock(String json) {
            this.inputStream = new ByteArrayInputStream(json.getBytes());
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {}
    }
}
