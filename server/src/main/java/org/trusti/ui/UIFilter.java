package org.trusti.ui;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@RegisterForReflection
@WebFilter(urlPatterns = "/*")
public class UIFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String path = request.getServletPath();

        if (path.equals("/")) {
            chain.doFilter(request, response);
            return;
        }

        request.getRequestDispatcher("/").forward(request, response);
    }
}
