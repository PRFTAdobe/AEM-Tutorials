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
            		if (propertyName.contains("_")) {
            			node.getProperty(propertyName).remove();
            		}
        	}
	}
	
	public void setCollectionProperties() {		
		try
		{		
			Node node = null;
			Node nameNode = null;
			Node resNode = null;
			String pathSuffix = getPathSuffix();
	    		ModifiableValueMap currentResource = resource.adaptTo(ModifiableValueMap.class);
			Node currentNode = resource.adaptTo(Node.class);
			
		   	 if(resourcePath != null) {		    	
				//Clear out the existing id properties, which will be re-added if they are listed in sling:resources.
		    		removeCollectionProperties(currentNode);
		    		Resource res = resourceResolver.getResource(resourcePath + "/sling:members");
				node = res.adaptTo(Node.class);
		    		Property slingRes = node.getProperty("sling:resources");
					if (slingRes != null) {
						Value[] paths = slingRes.getValues();
							for (int i=0; i<paths.length; i++) {
					    	  		Resource nameRes = resourceResolver.getResource(paths[i].toString());
					    	       		Resource contentRes = resourceResolver.getResource(paths[i].toString() + pathSuffix);
					    	       		nameNode = nameRes.adaptTo(Node.class); 
				    	    	   		resNode = contentRes.adaptTo(Node.class); 
				    	    	   		String name = nameNode.getName(); 

				    	    	   		//Remove the extension and period from the node name, to avoid property namespacing issues.
				    	    	  		name = name.substring(0, name.length() - 4);
				    	    	  		if (name.substring(name.length() - 1).equals(".")) {
				    	    		   		name = name.substring(0, name.length() - 1);
				    	    	   		}
				    	    	   				    	    	   
					    	       		if (contentType.equals("text-fragments")) {
                                       					//Obtain the current fragment's property which contains the text content.
					    	    	  		String txtVal = resNode.getProperty("text-content").getValue().toString();							    	 
					    	    	  		currentResource.put("_"+name + " ",txtVal);					    	    	   
					    	       		} else if (contentType.equals("svg-icons" )) {
					    	    		   	String mimeType = resNode.getProperty(JcrConstants.JCR_MIMETYPE).getValue().toString();					    	    	   
						    	    	   	//Get the SVG markup if the mimetype indicates the current node is an SVG image.
                                           				if (mimeType.equals("image/svg+xml")) {								    	    	   
								    	    	   Property iconBin = resNode.getProperty(JcrConstants.JCR_DATA);
								    	           Binary binary =  iconBin.getBinary();
								    	           InputStream binStream = binary.getStream();	
								    	           String iconVal = getDAMJcrData(binStream);							    	        
								    	           currentResource.put("_"+name + " ",iconVal);
						    	    		}						    	    	
							       } else {
                                       					//If the current node isn't a content fragment or SVG, return it's resource path.
					    	    	   		currentResource.put("_"+name + " ",paths[i].toString());
					    	       		}
					    	       
					    	    	}
							
					}
				    
			 }
			
		    resourceResolver.commit();  
			
		} catch(Exception e) {
			log.error("{} Include Collections Model cannot obtain collection data.", LOG_PREFIX);
			log.error(e.getMessage(), e);
		}
	}
}
