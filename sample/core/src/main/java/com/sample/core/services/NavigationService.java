package com.sample.core.services;

import java.util.List;
import com.sample.core.pojos.NavigationItem;
import org.apache.sling.api.SlingHttpServletRequest;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface NavigationService {
    List<NavigationItem> getNavigation(SlingHttpServletRequest request, String startPath);
    ArrayNode getNavigationAsJson(SlingHttpServletRequest request, String startPath);
}