package com.sample.core.osgiconfigs;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Collections Listener Configuration", description = "Configuration listing the page resources that should be updated when DAM collections change.")
public @interface CollectionsListenerConfig {
	@AttributeDefinition(name = "Pages to Update", description = "The pages that should be invoked via Sling when DAM collections are updated or changed.", type = AttributeType.STRING )
	String[] pagesToUpdate();
}