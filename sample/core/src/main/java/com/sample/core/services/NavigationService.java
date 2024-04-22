package com.sample.core.services;

import java.util.List;
import com.sample.core.pojos.NavigationItem;
import org.apache.sling.api.SlingHttpServletRequest;
import com.google.gson.JsonArray;

public interface NavigationService {
    List<NavigationItem> getNavigation(SlingHttpServletRequest request, String startPath);
    JsonArray getNavigationAsJson(SlingHttpServletRequest request, String startPath);
}