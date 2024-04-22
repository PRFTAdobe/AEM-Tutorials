package com.sample.core.models;

import com.sample.core.pojos.NavigationItem;
import com.sample.core.services.NavigationService;

import javax.annotation.PostConstruct;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import javax.inject.Inject;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class})
public class Navigation {

    @Inject
    private NavigationService navigationService;

    @ValueMapValue
    private String startPath;

    @Self
    private SlingHttpServletRequest request;

    private List<NavigationItem> navLinks ;

    @PostConstruct
    protected void init() {
        navLinks = navigationService.getNavigation(request, startPath);     
    }

    public List<NavigationItem> getNavLinks() {
        return navLinks;
    }

}
