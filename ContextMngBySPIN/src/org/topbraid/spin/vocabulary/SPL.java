/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.vocabulary;

import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;



/**
 * Vocabulary of the SPIN Standard Modules Library (SPL).
 * 
 * @author Holger Knublauch
 */
public class SPL {

	/* VPA 2019-05-15 Ontology Location has been changed
	 * CURL CHECK
	 *  C:\Users\vpa>curl "http://spinrdf.org/spl"
		<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
		<html><head>
		<title>301 Moved Permanently</title>
		</head><body>
		<h1>Moved Permanently</h1>
		<p>The document has moved <a href="https://spinrdf.org/spl">here</a>.</p>
		<hr>
		<address>Apache/2.4.18 (Ubuntu) Server at spinrdf.org Port 80</address>
		</body></html>
	 * 
	 * C:\Users\vpa>curl "https://spinrdf.org/spl" 
	 * curl: (1) Protocol https not supported or disabled in libcurl
	 */
	
	//VPA2019: change to https did not help
	public final static String BASE_URI = "http://spinrdf.org/spl";
	
	public final static String NS = BASE_URI + "#";
	
	public final static String PREFIX = "spl";
	

    public final static Resource Argument = ResourceFactory.createResource(NS + "Argument");
    
	public final static Resource Attribute = ResourceFactory.createResource(NS + "Attribute");
    
	public final static Resource InferDefaultValue = ResourceFactory.createResource(NS + "InferDefaultValue");
    
	public final static Resource ObjectCountPropertyConstraint = ResourceFactory.createResource(NS + "ObjectCountPropertyConstraint");
	
	public final static Resource RunTestCases = ResourceFactory.createResource(NS + "RunTestCases");
    
	public final static Resource SPINOverview = ResourceFactory.createResource(NS + "SPINOverview");
    
	public final static Resource TestCase = ResourceFactory.createResource(NS + "TestCase");

	
	public final static Resource objectCount = ResourceFactory.createResource(NS + "objectCount");
	
	public final static Resource subjectCount = ResourceFactory.createResource(NS + "subjectCount");

	
	public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");

	public static final Property dynamicEnumRange = ResourceFactory.createProperty(NS + "dynamicEnumRange");
	
	public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");
	
	public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");
	
	public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");
    
    public final static Property optional = ResourceFactory.createProperty(NS + "optional");
    
	public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
	
	public final static Property valueType = ResourceFactory.createProperty(NS + "valueType");
	
	static {
		// Force initialization
		SP.getURI();
	}
	
	
	private static Model model;
	

	/**
	 * Gets a Model with the content of the SPL namespace, from a file
	 * that is bundled with this API.
	 * @return the namespace Model
	 */
	public static synchronized Model getModel() {
		if(model == null) {
			model = ModelFactory.createDefaultModel();
			InputStream is = SPL.class.getResourceAsStream("/etc/spl.spin.ttl"); //VPA2019: po. /etc/spl.spin.ttl oli /etc/spl.spin.rdf 
			if(is == null) {
				model.read(SPL.BASE_URI);
			}
			else {
				model.read(is, "http://spinrdf.org/spl", "TTL"); //VPA2019: lang "TTL" added
			}
		}
		return model;
	}
}
