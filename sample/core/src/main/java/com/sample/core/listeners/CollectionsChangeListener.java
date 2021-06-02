package com.sample.core.listeners;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.models.annotations.Source;
import com.sample.core.configs.CollectionsListenerConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.WCMMode;

@Component(service = EventListener.class, immediate = true)
@Designate(ocd = CollectionsListenerConfig.class)
public class CollectionsChangeListener implements EventListener {
	private static final String LOG_PREFIX = "SAMPLE Project:";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private String[] pages;

	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	
	private ResourceResolver serviceUserResolver;
	
    @Reference
    private RequestResponseFactory requestResponseFactory;
    
    @Reference
    private SlingRequestProcessor requestProcessor;
    
	private Session session;
		
	@Activate
	protected  void activate(final CSHDAMCollectionsListenerConfig config) {
		log.debug("{} DAM Collections Listener Activate: Start.", LOG_PREFIX);
		try {
			 pages = config.pagesToUpdate();
			 log.debug("{} DAM Collections Listener Activate: Done.", LOG_PREFIX);
		} catch (Exception e) {
			log.error("{} DAM Collections Listener Activate: Failed.", LOG_PREFIX);
			log.error(e.getMessage(), e);
		}
		
		try {
			
			Map<String, Object> serviceParams = new HashMap<>();
		    serviceParams.put(ResourceResolverFactory.SUBSERVICE, "sample-service-user");
			serviceUserResolver = this.resourceResolverFactory.getServiceResourceResolver(serviceParams);
			session = serviceUserResolver.adaptTo(Session.class);
			this.log.info("{} Resource Resolver acquired for the Collections Change Listener.", LOG_PREFIX);

			session.getWorkspace().getObservationManager().addEventListener(this,
					Event.PROPERTY_ADDED | 
					Event.PROPERTY_REMOVED |
					Event.PROPERTY_CHANGED |
					Event.NODE_ADDED  | 
					Event.NODE_REMOVED, "/content/dam/collections", true,null, null, false);			
			
		} catch (Exception e) {			
			log.error(e.getMessage(), e);
		}
		
		
	}
	
	@Override
	public void onEvent(EventIterator events) {
		try {
				 for (int  i = 0; i < pages.length; i++) {
					 //Add the HTML extension to the resource if not already present
					 String last = "";
					 if (pages[i].length() >= 5) {
						 last = pages[i].substring(pages[i].length() - 5);
						 if (last.equals(".html")) {
							 //If the extension is already defined then do nothing.
						 } else {
							 pages[i] = pages[i] + ".html";
						 }
					 }
					 
					 HttpServletRequest rq = requestResponseFactory.createRequest("GET", pages[i]);
				     WCMMode.DISABLED.toRequest(rq);
				     ByteArrayOutputStream out = new ByteArrayOutputStream();
				     HttpServletResponse rs = requestResponseFactory.createResponse(out);
				     requestProcessor.processRequest(rq, rs, serviceUserResolver);
				     out.flush();
				     out.close();
				     rs.getWriter().flush();
				     rs.getWriter().close();
				 }
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
	}
	
	@Deactivate
	protected void deactivate() {		
		if(session != null) {
			session.logout();
		}
	}
	
}
