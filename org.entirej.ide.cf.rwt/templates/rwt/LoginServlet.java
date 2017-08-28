package org.entirej;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.entirej.applicationframework.rwt.spring.ext.AbstractTemplateServlet;

@WebServlet("/login")
public class LoginServlet extends AbstractTemplateServlet
{

    protected Map<String, String> getVariables(HttpServletRequest request)
    {
        Map<String, String> variables = new HashMap<String, String>();

        variables.put("login_path", request.getContextPath() + "/login");

        return variables;
    }

    @Override
    protected String getTemplatePath()
    {
        return "login.html";
    }

}
