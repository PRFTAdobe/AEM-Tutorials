package com.sample.core.servlets;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Objects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.Constants;
import org.apache.sling.api.servlets.ServletResolverConstants;

import com.sample.core.services.NavigationService;

@Component(service = Servlet.class,
        property = {
                 Constants.SERVICE_DESCRIPTION + "=Navigation JSON Update Servlet",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/navigationJsonUpdateServlet",
                "sling.auth.requirements=/bin/navigationJsonUpdateServlet",
                "sling.servlet.methods=" + HttpConstants.METHOD_POST
        })
public class NavigationJsonUpdateServlet extends SlingAllMethodsServlet {
    @Reference
    private NavigationService NavigationService;
    
    private static final String LOG_PREFIX = "Perficient PoC: ";
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String brandLogo = "";
        if (Objects.nonNull(request.getParameter("brandLogo"))) {
            brandLogo = request.getParameter("brandLogo");
        }
        String startPath = request.getParameter("startPath");
        ArrayNode navigationJsonArray =  NavigationService.getNavigationAsJson(request, startPath);

        ResourceResolver resourceResolver = request.getResourceResolver();
        String navFragmentPath = "/content/dam/perficientpocs/navigation-fragment-demo/jcr:content/data/master";
        Resource navFragmentResource = resourceResolver.getResource(navFragmentPath);

        try {
            if (Objects.nonNull(navFragmentResource)) {
                ModifiableValueMap map = navFragmentResource.adaptTo(ModifiableValueMap.class);
                map.put("brandLogo", brandLogo);
                map.put("generatedNavigationList", navigationJsonArray.toString());
                resourceResolver.commit();
            }

        } catch (Exception e) {
            log.error("{} The Navigation Servlet was unable to write JSON to the navigation content fragment.", LOG_PREFIX);
            log.error(e.getMessage(), e);
        }
        
    }
}