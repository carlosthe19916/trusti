package org.trusti.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Optional;

@RegisterForReflection
@WebServlet("/")
public class UIServer extends HttpServlet {

    @Location("index.html.ejs")
    Template indexHtmlTemplate;

    @Inject
    ObjectMapper objectMapper;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BrandingStrings brandingStrings = new BrandingStrings(
                new BrandingStrings.Application(
                        "Trusti",
                        Optional.empty(),
                        Optional.empty()
                ),
                new BrandingStrings.About(
                        "Trusti UI",
                        Optional.empty(),
                        Optional.empty()
                ),
                new BrandingStrings.Masthead(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                )
        );

        UiEnv env = new UiEnv(
          "development",
          "99.0.0",
          "false",
          "false",
          "",
          "",
          "",
          "500m",
          "false",
          ""
        );
        String encodedEnv = Base64.getEncoder().encodeToString(objectMapper.writer().writeValueAsString(env).getBytes());

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println(indexHtmlTemplate
                .data("branding", brandingStrings)
                .data("_env", encodedEnv)
                .render()
        );
    }

}
