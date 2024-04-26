package com.sample.core.services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sample.core.pojos.NavigationItem;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = NavigationService.class)
public class NavigationServiceImpl implements NavigationService {

    private static final String LOG_PREFIX = "Perficient PoC: ";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String jcrContent = "jcr:content";
    private String pageTitle = "";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private ResourceResolver resourceResolver;

    @Activate
    protected void activate() {
        log.info("{} Navigation Service Activate: Start.", LOG_PREFIX);
        try {
            log.info("{} Navigation Service Activate: Done.", LOG_PREFIX);
        } catch (Exception e) {
            log.error("{} Navigation Service Activate: Failed.", LOG_PREFIX);
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public List<NavigationItem> getNavigation(SlingHttpServletRequest request, String startPath) {
        List<NavigationItem> navigationItems = new ArrayList<>();
        resourceResolver = request.getResourceResolver();
        Resource startResource = resourceResolver.getResource(startPath);
        if (startResource != null) {
            for (Resource child : startResource.getChildren()) {

                try {
                Node childNode = child.adaptTo(Node.class).getNode("jcr:content");
                pageTitle = childNode.getProperty("jcr:title").getString();
                } catch (ValueFormatException e) {
                    e.printStackTrace();
                } catch (PathNotFoundException e) {
                    e.printStackTrace();
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                
                if (!child.getName().equals(jcrContent)) {
                    navigationItems.add(new NavigationItem(pageTitle, child.getName(), child.getPath()));
                }
            }
        } else {
            log.error("{} The starting path resource is null.", LOG_PREFIX);
        }
        return navigationItems;
    }

    @Override
    public ArrayNode getNavigationAsJson(SlingHttpServletRequest request, String startPath) {
        try {
           // JsonArray navigationArray = new JsonArray();
            ArrayNode navigationArray = JsonNodeFactory.instance.arrayNode();

            resourceResolver = request.getResourceResolver();
            Resource startResource = resourceResolver.getResource(startPath);
            if (startResource != null) {
                for (Resource child : startResource.getChildren()) {

                    try {
                        Node childNode = child.adaptTo(Node.class).getNode("jcr:content");
                        pageTitle = childNode.getProperty("jcr:title").getString();
                        } catch (ValueFormatException e) {
                            e.printStackTrace();
                        } catch (PathNotFoundException e) {
                            e.printStackTrace();
                        } catch (RepositoryException e) {
                            e.printStackTrace();
                        }
                        
                    ObjectNode navigationItem = JsonNodeFactory.instance.objectNode();;
                    
                    if (!child.getName().equals(jcrContent)) {
                        navigationItem.put("title", pageTitle);
                        navigationItem.put("name", child.getName());
                        navigationItem.put("path", child.getPath());
                        navigationArray.add(navigationItem);
                    }
                }
            }
            return navigationArray;
        } catch (Exception e) {
            log.error("{} Navigation JSON object building has failed.", LOG_PREFIX);
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Deactivate
    protected void deactivate() {
        if (resourceResolver != null && resourceResolver.isLive()) {
            resourceResolver.close();
        }
    }
}