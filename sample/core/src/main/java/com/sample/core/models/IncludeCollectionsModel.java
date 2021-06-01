package com.sample.core.models;

import com.day.cq.commons.jcr.JcrConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(adaptables = { Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class IncludeCollectionsModel {

	private static final String LOG_PREFIX = "SAMPLE Project:";
	private final Logger log = LoggerFactory.getLogger(getClass());
	@ValueMapValue
	private String resourcePath;
	
	@ValueMapValue
	private String contentType;
	
	@Self
	 private Resource resource;
	
	@Inject
	@Source("sling-object")
	private ResourceResolver resourceResolver;


	//Determine the type of path suffix to use when obtaining JCR node properties.
	private String getPathSuffix() {
		 String suffix = "";
		 if (contentType.equals("text-fragments")) {
			 suffix = "/" + JcrConstants.JCR_CONTENT + "/data/master";
		 }
		 if (contentType.equals("svg-icons")) {
			 suffix = "/" + JcrConstants.JCR_CONTENT + "/renditions/original/" + JcrConstants.JCR_CONTENT;
		 }
		 return suffix;
	}
	
	//Convert the jcr:data property value to a string.
	private String getJcrData(InputStream binStream) throws IOException {
		int lh;
		byte[] buff = new byte[8192];
		ByteArrayOutputStream bA = new ByteArrayOutputStream();
		while ((lh = binStream.read(buff)) != -1) {
		    bA.write(buff, 0, lh);
		}
		return bA.toString(StandardCharsets.UTF_8.name());
	}
	
	private void removeCollectionProperties(Node node) throws RepositoryException {
		PropertyIterator propertiesIterator = node.getProperties();
        	while (propertiesIterator.hasNext()) {
            		String propertyName = propertiesIterator.nextProperty().getName();
            		if (propertyName.contains("id")) {
            			node.getProperty(propertyName).remove();
            		}
        	}
	}
	
	public void setCollectionProperties() {		
		try
		{		
			Node node = null;
			Node resourceNode = null;
			Node currentNode = resource.adaptTo(Node.class);
			String pathSuffix = getPathSuffix();
			
		    if(resourcePath != null) {
			    
			    	//Remove the existing "id" properties, which will be re-added if they are listed in sling:resources.
		    		removeCollectionProperties(currentNode);
			    
				Resource res = resourceResolver.getResource(resourceParth + "/sling:members");
				node = res.adaptTo(Node.class);
				Property slingRes = node.getProperty("sling:resources");
				
					if (slingRes != null) {
					    	 Value[] paths = slingRes.getValues();					    	 
					    	    for (int i=0; i<paths.length; i++) {
					    	       Resource contentResource = resourceResolver.getResource(paths[i].toString() + pathSuffix);
				    	    	       resourceNode = contentResource.adaptTo(Node.class); 
					    	       if (contentType.equals("text-fragments")) {
						           //Obtain the current fragment's property which contains the text content.
					    	    	   String txtVal = resourceNode.getProperty("text-content").getValue().toString();					    	    	   
					    	    	   currentNode.setProperty("id "+i,"value " + txtVal);					    	    	   
					    	       } else if (contentType.equals("svg-icons" )) {
								   //Get the SVG markup if the mimetype indicates the current node is an SVG image.
					    	    		   String mimeType = resourceNode.getProperty(JcrConstants.JCR_MIMETYPE).getValue().toString();					    	    	   
						    	    	   if (mimeType.equals("image/svg+xml")) {								    	    	   
								    	    	   Property iconBin = resourceNode.getProperty(JcrConstants.JCR_DATA);
								    	           Binary binary =  iconBin.getBinary();
								    	           InputStream binStream = binary.getStream();	
								    	           String iconVal = getJcrData(binStream);							    	        
								    	    	   currentNode.setProperty("id "+i,"value " + iconVal);
						    	    	   }						    	    	
					    	       } else {
							   //If the current node isn't a content fragment or SVG, return it's resource path.
					    	    	   currentNode.setProperty("id "+i,"resource " + paths[i].toString());
					    	       }
					    	    }
					    	 currentNode.getSession().save();
					}
				    
			 }
		} catch(Exception e) {
			log.error("{} Include Collections Model cannot generate data.", LOG_PREFIX);
			log.error(e.getMessage(), e);
		}
	}

}
