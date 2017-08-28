package org.entirej;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.entirej.applicationframework.rwt.spring.ext.AbstractTemplateServlet;

@WebServlet("/403")
public class AccessDeniedServlet extends AbstractTemplateServlet
{

    protected Map<String, String> getVariables(HttpServletRequest request)
    {
        Map<String, String> variables = new HashMap<String, String>();

        variables.put("logout_path", request.getContextPath() + "/logout");

        return variables;
    }

    @Override
    protected String getTemplatePath()
    {
        return "403.html";
    }

}
