/* ModelSpinManager.java
 * 2015-07-24-26 TOIMII
 * Combining (some of) the functionalities of siima.spin.My.. classes 
 * and org.topbraid.spin.examples classes.
 * 
 * 
 * 'Note that the Model should contain the triples from 
 * the system namespaces spin.owl and sp.owl
 *  - otherwise the system may not be able to transform 
 *  the SPIN RDF into the correct SPARQL string. 
 *  In a typical use case, the Model would be an OntModel 
 *  that also imports the SPIN system namespaces.'
 */

package siima.spin;

//import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.SPINConstraints;
import org.topbraid.spin.inference.SPINConstructors;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.update.Modify;
import org.topbraid.spin.model.update.impl.ModifyImpl;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.AbstractGraphListener;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.QueryWrapper;
import org.topbraid.spin.vocabulary.ARG;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

//import com.hp.hpl.jena.sparql.modify.
import com.hp.hpl.jena.update.*;

import org.apache.log4j.Level;
//VPA
import org.apache.log4j.Logger;

public class ModelSpinManager {
	private static final Logger logger=Logger.getLogger(org.apache.log4j.Logger.class.getName()); // also "myApp.user"); 
	
	public OntModel mainOntModel;
	public Model inferredTriples;
	public boolean readyToRun = false;
	public OntModel ontModelWithReasoner;

	// Class Constructor
	public ModelSpinManager() {
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();
	}

	public Model runConstructors(Model queryModel, Model targetModel){
		/* 
		 * SPIN CONSTRUCTORS
		 * TOIMII!!
		 * Bicycle constructor creates a statement: this-modelYear->"M2015".
		 * Handlebar constructor creates an instance of the Bell class and a statement: this-hasBell->new bell instance.  
		 * Further, Bell constructor creates a statement: this-created->datetime.
		 */
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: runConstructors()");
		SPINConstructors.constructAll(queryModel, targetModel, null);
		return targetModel;
	}
	
	public Map<String,RDFNode> addSPLArgumentDeclarationToMap(Map<String,RDFNode> argumentNodeMap, String argName, String argType){
		/* This method fills argumentNodeMap (argument -> argtype) for new constraint SPL.Arguments to be used for new Template creation */
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: addSPLArgumentDeclarationToMap()");
		RDFNode node=null;
		
		
		// RDF.Property
		// RDFS.Resource
		// XSD.dateTime
		// RDF.predicate;
		// RDFS.Literal
		// TODO: case "plain": node=RDFS.Literal ???
		
		switch(argType){
		case "XSD.language": node=XSD.language; break; //TODO: parse from format text@en
				
		//XSD Typed literals xsd-type
		case "XSD.xstring": node=XSD.xstring; break;
		case "XSD.xint": node=XSD.xint; break;		
		case "XSD.integer": node=XSD.integer; break;		
		case "XSD.xlong": node=XSD.xlong; break;
		case "XSD.xshort": node=XSD.xshort; break;
		case "XSD.xbyte": node=XSD.xbyte; break;
		case "XSD.xfloat": node=XSD.xfloat; break;
		case "XSD.xdouble": node=XSD.xdouble; break;
		case "XSD.xboolean": node=XSD.xboolean; break;		
		case "XSD.decimal": node=XSD.decimal; break;
		
		case "XSD.dateTime": node=XSD.dateTime; break;
		case "XSD.duration": node=XSD.duration; break;
		
		//URI objects
		//RDF
		case "RDF.Property": node=RDF.Property; break;
		case "RDF.predicate": node=RDF.predicate; break;
		case "RDF.subject": node=RDF.subject; break;
		case "RDF.object": node=RDF.object; break;
		//RDFS
		case "RDFS.Resource": node=RDFS.Resource; break;
		case "RDFS.Literal": node=RDFS.Literal; break;  //?? Is this the plain type??
		
		default: System.out.println("??Unknown argument type??"); break; 
		}
	if(node!=null) argumentNodeMap.put(argName, node);
	return argumentNodeMap;	
	}
	
	
	public Map<String,RDFNode> addArgumentNodeToMap(Map<String,RDFNode> argumentNodeMap, String argName, String argType, String literalValue, String objectUri, Model model){
		/* This method fills argumentNodeMap (argument -> value) for a Template to be called */		
		//?? Kumpaa tulisi k‰ytt‰‰ RDFNoden luontiin model vai ResourceFactory??
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: addArgumentNodeToMap()");
		RDFNode node=null;
		switch(argType){
		case "lang": node=ResourceFactory.createLangLiteral(literalValue, "en"); break; //TODO: parse from format text@en
		case "plain": node=ResourceFactory.createPlainLiteral(literalValue); break;
		//Typed literals xsd-type names
		case "string": node=ResourceFactory.createTypedLiteral(new String(literalValue)); break;
		case "int": node=ResourceFactory.createTypedLiteral(new Integer(literalValue)); break;
		case "integer": node=ResourceFactory.createTypedLiteral(new BigInteger(literalValue)); break;
		case "long": node=ResourceFactory.createTypedLiteral(new Long(literalValue)); break;
		case "short": node=ResourceFactory.createTypedLiteral(new Short(literalValue)); break;
		case "byte": node=ResourceFactory.createTypedLiteral(new Byte(literalValue)); break;
		case "float": node=ResourceFactory.createTypedLiteral(new Float(literalValue)); break;
		case "double": node=ResourceFactory.createTypedLiteral(new Double(literalValue)); break;
		case "boolean": node=ResourceFactory.createTypedLiteral(new Boolean(literalValue)); break;
		case "decimal": node=ResourceFactory.createTypedLiteral(new BigDecimal(literalValue)); break;
		//URI objects
		case "property": node=ResourceFactory.createProperty(objectUri); break;
		case "resource": node=ResourceFactory.createResource(objectUri); break;
		default: System.out.println("??Unknown argument type??"); break; 
		}
	if(node!=null) argumentNodeMap.put(argName, node);
	return argumentNodeMap;	
	}
	
	
	public Template getTemplate(String templateName){
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: getTemplate()");
		Template template=null;
		Collection<Template> alltemplates = SPINModuleRegistry.get()
				.getTemplates();
		Iterator<Template> iter = alltemplates.iterator();
		
		for (; iter.hasNext();) {
			Template tpl = iter.next();
			if(tpl.getLocalName().equalsIgnoreCase(templateName)) template= tpl;			
		}		
		return template;
	}
	
	
	public void printTemplateInfo(String localName, String uri){
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: printTemplateInfo()");
		Template tpl=null;
		if((uri==null)&&(localName!=null)){
			
			tpl=getTemplate(localName);
		} else if(uri!=null){
			tpl = SPINModuleRegistry.get().getTemplate(uri, this.mainOntModel);
		}
		
		if(tpl!=null){
		System.out.println("Template is " + tpl.getLocalName()
		+ "------- with URI:" + tpl.getURI()
		+ "\n-------- with comment:" + tpl.getComment());
		
		System.out.println("Template body " + tpl.getBody());
		Map<String, Argument> argumMap = tpl.getArgumentsMap();
		Set<String> keys=argumMap.keySet();
		Iterator<String> keyiter=keys.iterator();
		while(keyiter.hasNext()){
			System.out.println("Template argument key: " + keyiter.next());
		}
		} else System.out.println("???Template NOT FOUND");
	}
	
	
	public Map<String,String> getTemplatesNameUriMap(){
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: getTemplatesNameUriMap()");
		Map<String,String> nameUriMap = new HashMap<String,String>();
		Collection<Template> alltemplates = SPINModuleRegistry.get()
				.getTemplates();
		Iterator<Template> iter = alltemplates.iterator();
		
		for (; iter.hasNext();) {
			Template tpl = iter.next();
			nameUriMap.put(tpl.getLocalName(), tpl.getURI());			
		}		
		return nameUriMap;
	}
	
	public Collection<Template> getTemplates() {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: getTemplates()");
		// ----------Templates
		Collection<Template> alltemplates = SPINModuleRegistry.get()
				.getTemplates();
		Iterator<Template> iter = alltemplates.iterator();
		System.out
				.println("\n================ALL MODEL TEMPLATES ================\n");
		for (; iter.hasNext();) {
			Template tpl = iter.next();
			System.out.println("Template is " + tpl.getLocalName()
					+ "------- with URI:" + tpl.getURI()
					+ "\n------- with comment:" + tpl.getComment());
		}
		return alltemplates;
	}

	public Collection<Function> getFunctions() {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: getFunctions()");
		// -----------Functions
		Collection<Function> allfuncs = SPINModuleRegistry.get().getFunctions();
		Iterator<Function> fiter = allfuncs.iterator();
		System.out
				.println("\n================ALL MODEL FUNCTIONS ================\n");
		for (; fiter.hasNext();) {
			Function fun = fiter.next();
			System.out.println("Function is " + fun.getLocalName()
					+ "------- with URI:" + fun.getURI()
					+ "\n-------- with comment:" + fun.getComment());
		}

		return allfuncs;
	}

	public OntModel createReasonerModel(Model model) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: createReasonerModel()");
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		// reasoner = reasoner.bindSchema(ontModel);
		// Obtain standard OWL-DL spec and attach the Pellet reasoner
		OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM;
		ontModelSpec.setReasoner(reasoner);
		// Create ontology model with reasoner support
		OntModel modelwithreasoner = ModelFactory.createOntologyModel(
				ontModelSpec, model); // ontModel);
		return modelwithreasoner;
	}


	public void callTemplateByName(String templateName, 
			Map<String, RDFNode> argumentNodeMap, String querytype, List<String> queryVars) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: callTemplateByName()");
		// Now call the template
		Model model=getOntModelWithReasoner();
		Template template=getTemplate(templateName);
		
		if("select".equalsIgnoreCase(querytype))
			callSelectTemplate(template, model, argumentNodeMap, queryVars);
		else if("construct".equalsIgnoreCase(querytype)){
			//Testing model:TOIMII
			//Model resultModel = ModelFactory.createDefaultModel();
			Model resultModel = getInferredTriples();
			callConstructTemplate(template, model, resultModel, argumentNodeMap);
			System.out.println("=================CONSTRUCT TEMPLATE RESULTS===============\n");
			resultModel.write(System.out);
		} else if("update".equalsIgnoreCase(querytype)){
			System.out.println("=================CALLING UPDATE TEMPLATE ===============\n");
			callUpdateTemplate(template, model, model, argumentNodeMap);
		}
		//--------------
		
		System.out
				.println("==================================================\n");
	}

	public void  callUpdateTemplate(Template template, Model queryModel, Model targetModel,
			Map<String, RDFNode> initialBindings) {
				//TOIMII: Delete part works only 'in the base model' not in sub models?? 
				//See: https://groups.google.com/forum/#!topic/topbraid-users/1aDefUKlPnw 
				//SPINConstructors.java row 226-
				logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: callUpdateTemplate()");
				if(template!=null){
				if (template.getBody() instanceof ModifyImpl) {
                    System.out.println("Running update template ["+ template.getLabelTemplate()+"]");
                    System.out.println("Running update template BODY: ["+ template.getBody()+"]");
                    
                    UpdateRequest updateRequest = ARQFactory.get().createUpdateRequest((Modify)template.getBody());
                   
                    QuerySolutionMap bindings = new QuerySolutionMap();
					if(initialBindings != null) {
						for(String varName : initialBindings.keySet()) {
							RDFNode value = initialBindings.get(varName);
							bindings.add(varName, value);
							 System.out.println("varName: " + varName + " value: " + value );
						}
					}
                    
					GraphStore graphstore=GraphStoreFactory.create(queryModel);
					UpdateProcessor proc= UpdateExecutionFactory.create(updateRequest, graphstore, bindings);
					proc.execute();                   
                
                }
				} else { System.out.println("???Template missing: " + template);}
				
		}
				
	
	
	public List<Resource>  callConstructTemplate(Template template, Model queryModel, Model targetModel,
	Map<String, RDFNode> initialBindings) {
		//TODO See SPINConstructors.java row 226-
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: callConstructTemplate()");
		// Create query execution
		com.hp.hpl.jena.query.Query arq=null;
		List<Resource> newResources = new ArrayList<Resource>();
		
		Query arqQuery = ARQFactory.get().createQuery(	(Construct) template.getBody());
		
		final List<Triple> triples = new LinkedList<Triple>();
		AbstractGraphListener listener = new AbstractGraphListener() {
			
			@Override
			public void notifyAddTriple(Graph g, Triple t) {
				triples.add(t);
			}
			
			@Override
			public void notifyDeleteTriple(Graph g, Triple t) {
			}
			
			@Override
			protected void notifyRemoveAll(Graph source, Triple pattern) {
			}
		};
		
		
		if(arqQuery.isConstructType()) {
			
			
			QuerySolutionMap bindings = new QuerySolutionMap();
			if(initialBindings != null) {
				for(String varName : initialBindings.keySet()) {
					RDFNode value = initialBindings.get(varName);
					bindings.add(varName, value);
				}
			}
			
			QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, queryModel);
			qexec.setInitialBinding(bindings);
			
			// SEE COMMENT IN SPINConstructor.java r250:
			// "Execute construct and remember the order in which triples were inserted
			// Note that this does not work yet since Jena appears to have random order"
			Model resultModel = ModelFactory.createDefaultModel();
			resultModel.getGraph().getEventManager().register(listener);
			qexec.execConstruct(resultModel);
			qexec.close();
			
			
			// Add all new triples and any new resources
			for(Triple triple : triples) {
				Statement rs = queryModel.asStatement(triple);
				if(!targetModel.contains(rs)) {
					targetModel.add(rs);
					if(RDF.type.equals(rs.getPredicate())) {
						Resource subject = rs.getSubject();
						if(!newResources.contains(subject)) {
							newResources.add(subject);
						}
					}
					
				}
			}
		}
		return newResources;
	}
		
	
	
	public void callSelectTemplate(Template template, Model model,
			Map<String, RDFNode> predicateNodeMap, List<String> queryVars) { //String queryVar) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: callSelectTemplate()");
		// Create query execution
		com.hp.hpl.jena.query.Query arq=null;
		QueryExecution qexec=null;
		
		
		arq = ARQFactory.get().createQuery(
				(Select) template.getBody());
		 
		
		qexec = ARQFactory.get()
				.createQueryExecution(arq, model);
		
		// Set bindings
		QuerySolutionMap arqBindings = new QuerySolutionMap();
		Set<String> keys = predicateNodeMap.keySet();
		Iterator<String> iter = keys.iterator();

		while (iter.hasNext()) {
			String predicateName = iter.next();
			System.out.println("predicateName: " + predicateName);
			// Eg. ("predicate",RDF.type)
			arqBindings.add(predicateName, predicateNodeMap.get(predicateName)); 

		}
		qexec.setInitialBinding(arqBindings); // Pre-assign the arguments
		
		/* Templates query type? */
		
		if(arq.isSelectType()){
		
		ResultSet rs = qexec.execSelect();
		int solcnt=0;
		System.out
				.println("============ MyTemplate Query Result ==============");
		while (rs.hasNext()) {
			++solcnt;
			QuerySolution solution=rs.next();
			for(String qvar: queryVars){
				if(qvar.startsWith("?")) qvar=qvar.substring(1);
				RDFNode varunif = solution.get(qvar); //rs.next().get(qvar); // "bicycle");
				if (varunif != null)
					System.out.println("(" + solcnt + ") Template Query result: " + qvar + " = "
						+ varunif);
				else
					System.out.println("NO template Query variable result");
			}
		}
		} else {
			//See SPINConstructors.java row 226-
			System.out.println("Method: callSelectTemplate() ??? WRONG QUERY TYPE: SELECT QUERY EXPECTED ??? ");
			
		}
		
		System.out
				.println("==================================================\n");
	}

	public Template createTemplate(Model model, String query,
			String templateURI, Map<String, RDFNode> argumNodeMap,
			Map<String, String> argumCommentMap) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: createTemplate()");
		/*
		 * See MyModelTemplate.java Ex: valueType: for instance XSD.integer,
		 * RDF.Property, RDFS.Resource from com.hp.hpl.jena.vocabulary.
		 */
		// --- Create a template ---
		// Note: jena_Query
		com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(
				model, query);
		// Note: spin_Query
		org.topbraid.spin.model.Query spinQuery = new ARQ2SPIN(model)
				.createQuery(arqQuery, null);
		Template template = model.createResource(templateURI, SPIN.Template)
				.as(Template.class);
		template.addProperty(SPIN.body, spinQuery);

		Set<String> keys = argumNodeMap.keySet();
		Iterator<String> iter = keys.iterator();

		while (iter.hasNext()) {
			String argumentName = iter.next();
			System.out.println("argumentName: " + argumentName);

			// Define spl:Argument at the template
			Resource argument = model.createResource(SPL.Argument);
			argument.addProperty(SPL.predicate,
					model.getProperty(ARG.NS + argumentName)); // "predicate"));
			argument.addProperty(SPL.valueType, argumNodeMap.get(argumentName)); // XSD.integer
																					// RDF.Property
																					// RDFS.Resource
			argument.addProperty(RDFS.comment,
					"" + argumCommentMap.get(argumentName)); // "The predicate to get the value of.");
			template.addProperty(SPIN.constraint, argument);
		}

		return template;
	}

	public Template createTemplateWithOneArgument(Model model,
			String query, String templateURI, String argumentName,
			RDFNode valueType, String argComment) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: createTemplateWithOneArgument()");
		
		/*
		 * See MyModelTemplate.java Ex: valueType: for instance XSD.integer,
		 * RDF.Property, RDFS.Resource from com.hp.hpl.jena.vocabulary.
		 */
		// Create a template
		// Note: jena_Query
		com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(
				model, query);
		// Note: spin_Query
		org.topbraid.spin.model.Query spinQuery = new ARQ2SPIN(model)
				.createQuery(arqQuery, null);
		Template template = model.createResource(templateURI, SPIN.Template)
				.as(Template.class);
		template.addProperty(SPIN.body, spinQuery);

		// Define spl:Argument at the template
		Resource argument = model.createResource(SPL.Argument);
		argument.addProperty(SPL.predicate,
				model.getProperty(ARG.NS + argumentName)); // "predicate"));
		argument.addProperty(SPL.valueType, valueType); // XSD.integer
														// RDF.Property
														// RDFS.Resource
		argument.addProperty(RDFS.comment, argComment); // "The predicate to get the value of.");
		template.addProperty(SPIN.constraint, argument);

		return template;
	}

	public List<ConstraintViolation> checkSPINConstraintForResource(Resource resource, Model result) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: checkSPINConstraintForResource()");
		// Run constraints on a single instance only
		List<ConstraintViolation> localCVS = SPINConstraints.check(resource,
				null);
		// Save the results to result-model		
		if(result!=null) SPINConstraints.addConstraintViolationsRDF(localCVS, result, false); //(cvs, result, createSource)
		
		// Print results to console
		System.out
				.println("---Constraint violations for "
						+ SPINLabels.get().getLabel(resource) + ": #"
						+ localCVS.size());
		if (localCVS.size() > 0)
			//System.out.println("- first Constraint violation for "
			//		+ SPINLabels.get().getLabel(resource) + ": "
			//		+ localCVS.get(0).getMessage() + "'");
		for (ConstraintViolation cv : localCVS) {
			System.out.println(" - at "
					+ SPINLabels.get().getLabel(cv.getRoot()) + " with msg: '"
					+ cv.getMessage() + "'");
			System.out.println("---Constraint source query:\n" + cv.getSource());
		}
		
		return localCVS;
	}

	public List<ConstraintViolation> checkSPINConstraints(Model result) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: checkSPINConstraints()");
		// Run all constraints
		List<ConstraintViolation> cvs = SPINConstraints.check(
				this.mainOntModel, null);
		// Save the results to result-model		
		if(result!=null) SPINConstraints.addConstraintViolationsRDF(cvs, result, false); //(cvs, result, createSource)
		
		// Print results to console
		System.out.println("---Constraint violations:");
		for (ConstraintViolation cv : cvs) {
			System.out.println(" - at "
					+ SPINLabels.get().getLabel(cv.getRoot()) + " with msg: '"
					+ cv.getMessage() + "'");
			System.out.println("---Constraint source query:\n" + cv.getSource());
		}
		return cvs;
	}

	public void runAllSpinInferences(boolean singlePass) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: runAllSpinInferences()");
		// Run all inferences (SPIN rules)
		if (readyToRun) {
			SPINInferences.run(this.mainOntModel, this.inferredTriples, null,
					null, singlePass, null);
			System.out.println("---Inferred triples: "
					+ this.inferredTriples.size());
		} else
			System.out
					.println("---WARN: Model NOT ready-to-run SPIN inferences");
	}

	public void registerAllSpin(Model model, String toregister) {
		/*
		 * toregister: options: ALL, TEMPLATES, FUNCTIONS
		 * 
		 * SPINModuleRegistry.get().registerAll(): Registers all functions and
		 * templates from a given Model. Note that the Model should contain the
		 * triples from the system namespaces spin.owl and sp.owl - otherwise
		 * the system may not be able to transform the SPIN RDF into the correct
		 * SPARQL string. In a typical use case, the Model would be an OntModel
		 * that also imports the SPIN system namespaces.
		 */
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: registerAllSpin()");
		
		if ((toregister == null) || ("all".equalsIgnoreCase(toregister)))
			SPINModuleRegistry.get().registerAll(this.mainOntModel, null);
		else if ("templates".equalsIgnoreCase(toregister))
			SPINModuleRegistry.get().registerTemplates(model);
		else if ("functions".equalsIgnoreCase(toregister))
			SPINModuleRegistry.get().registerFunctions(model, null);
	}

	public OntModel mergeMainModelAndInferredTriples(){
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: mergeMainModelAndInferredTriples()");
		OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM;
		
		OntModel mergedModel = ModelFactory.createOntologyModel(
				ontModelSpec, this.mainOntModel); // ontModel);
		if(this.inferredTriples!=null)
		mergedModel.add(this.inferredTriples);	
		//Model mergedModel = ModelFactory.createDefaultModel();
		//mergedModel.add(this.mainOntModel);
		//mergedModel.add(this.inferredTriples);
		return mergedModel;
	}
	
	public void createInferredModelAndRegister() {
		// Create and add Model for inferred triples
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: createInferredModelAndRegister()");
		
		if ((this.mainOntModel != null) && (this.inferredTriples == null)) {
			System.out
					.println("---Creating inferred sub-model and registering locally defined functions");
			this.inferredTriples = ModelFactory.createDefaultModel();
			this.mainOntModel.addSubModel(this.inferredTriples);
			// Register locally defined functions
			SPINModuleRegistry.get().registerAll(this.mainOntModel, null);
			this.readyToRun = true;
		} else if ((this.mainOntModel != null) && (this.inferredTriples != null)) {
			SPINModuleRegistry.get().registerAll(this.mainOntModel, null);
			System.out
			.println("---SPIN REGISTRING ONLY (this.inferredTriples already created)");
			
		} else if (this.mainOntModel == null)
			System.out.println("---ERROR: this.mainOntModel==null");
		
	}

	
	public void setNsPrefixes(Map<String,String> prefixMap){
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: setNsPrefixes()");
		if(this.mainOntModel!=null){
			Set<String> keys=prefixMap.keySet();
			Iterator keyiter = keys.iterator();
			int i=0;
			while(keyiter.hasNext()) {
				String key=(String)keyiter.next();
				this.mainOntModel.setNsPrefix(key, prefixMap.get(key));
				++i;
				System.out.println("===== ("+(i+1)+") PREFIX:" + key + " NS:" + prefixMap.get(key));
			}
			
		}
	}
	
	
	public Map<String,String> getNsPrefixeMap(Model model){
		//this.mainOntModel
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: getNsPrefixeMap()");
		Map<String,String> prefixMap=null;
		if(model!=null){
			prefixMap=model.getNsPrefixMap();
			Set<String> keys=prefixMap.keySet();
			Iterator keyiter = keys.iterator();
			int i=0;
			System.out.println("\n===== Listing Model Prefixes with Namespaces =====");
			while(keyiter.hasNext()) {
				String key=(String)keyiter.next();
				++i;
				System.out.println("-- ("+(i+1)+") PREFIX:" + key + " NS:" + prefixMap.get(key));
			}			
		}
		return prefixMap;
	}
//	public OntModel loadModelUsingFileModelMaker(List<String> altlocs) {
		/*  test this instead of loadModelWithImports
		 * BUT Not working / not used /inferences not working
		 * book p.277 
		 */
/*		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: loadModelUsingFileModelMaker()");
		ModelMaker modelMaker = ModelFactory.createFileModelMaker(altlocs.get(0));
		Model modeltmp = modelMaker.createDefaultModel();
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, modeltmp);
		
		Model tmpOntology;
		if (altlocs.size() > 1) {
			for (int i = 1; i < altlocs.size(); i++) { // Note: start from index 1
				tmpOntology = FileManager.get().loadModel(altlocs.get(i));
				ontModel.addSubModel(tmpOntology);
			}
		}
		
		return ontModel;
	}
	
	*/	
	
	public OntModel loadModelWithImports(List<String> urls, List<String> altlocs) {
		
		/*
		 * This method will load automatically also the ontology files to be imported,
		 * IF these models have been defined as imported ontologies in the base ontology
		 * file (e.g  owl:imports <http://siima.net/ont/accessories> ;) 
		 * AND IF the alternative location in a local file system has been defined in
		 * List<String> altlocs. 
		 * (note: ontologies accessible from the URI location do not need the altloc path 
		 * (eg. spin ontologies)
		 * 
		 * The two lists (List<String> urls, List<String> altlocs) must correspond to each other:
		 * 0) urls(0) is the URI of the base ontology -> altlocs(0) is the file path to the base ontology file.
		 * 1) urls(1) is the URI of the first imported ontology -> altlocs(1) is the file path to the first imported ontology file.
		 * 2) continue same way to other models to be imported..
		 * 
		 * NOTE: VPA TESTED:
		 * SPARQL DELETE/UPDATE queries do not delete statements in imported models or extra added submodels.
		 */
		
		/* from MySpinInference.java */
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: loadModelWithImports()");
		System.out.println("---loadModelWithImports()");
		System.out.println("loadModelWithImports: the count of subModel files to be imported = #"
				+ (urls.size()-1));
		/* loading uri's and file path locations to FileManager */
		for (int i = 0; i < urls.size(); i++) {
			FileManager.get().getLocationMapper()
					.addAltEntry(urls.get(i), altlocs.get(i));
		}

		// the first url is for the main base ontology (TURTLE format assumed)
		Model baseOntology = FileManager.get().loadModel(altlocs.get(0),urls.get(0), "TURTLE");
		OntModel ontModel = JenaUtil.createOntologyModel(
				OntModelSpec.OWL_DL_MEM, baseOntology);
		
		//System.out.println("loadModelWithImports: Check (2016-10-03)");
		
		int importedSubModelCnt = ontModel.countSubModels();
		System.out.println("loadModelWithImports: imported subModel count: " + importedSubModelCnt);
		
		/* 
		 * Loading SubModels separately (Do we need this) 
		 * (TODO: This could be used, If you want to load extra sub models 
		 * that are not automatically imported) 
		 *
		 *
		Model tmpSubModel;
		if (urls.size() > 1) {
			for (int i = 1; i < urls.size(); i++) { // Note: start from index 1, because 0 is the base model
				FileManager.get().getLocationMapper()
				.addAltEntry(urls.get(i), altlocs.get(i));
				tmpSubModel = FileManager.get().loadModel(altlocs.get(i), urls.get(i), "TURTLE");
				//OntModel tmpOntModel = JenaUtil.createOntologyModel(OntModelSpec.OWL_DL_MEM, tmpOntology); 
				ontModel.addSubModel(tmpSubModel, true); // rebind = true/false?
			}
		}
		
		int finalsubcnt = ontModel.countSubModels();
		System.out.println("loadModelWithImports: subModel count after adding extra submodels: " + finalsubcnt);
		Model whatis = ontModel.getImportedModel(urls.get(1)); // NULL koska importtaus-rivi on poistettu bicycle mallista
		if(whatis!=null)System.out.println("??loadModelWithImports: imported model is: " + urls.get(1));
		*/
		
		System.out.println("\n\n");
		return ontModel;
	}

	/* **********************
	 * EXECUTING SPARQL QUERIES
	 * 
	 */

	public void execAttachedQuery(Resource cls, OntModel model, List<String> queryVars){
		//TODO: Only Select query exec implemented
		//Run a query attached to a class (by TBC)
		//Resolves prefixes used in the attached query
		//See also: SPINParsingExample.java
		//Example in class: "http://siima.net/ont/bicycle#Bicycle"
		StringBuffer qstrbuff= new StringBuffer();
		
		Map<String,String> prefixMap= model.getNsPrefixMap();
		Set keys=prefixMap.keySet();
		Iterator keyiter = keys.iterator();
		while(keyiter.hasNext()) {
			String key=(String)keyiter.next();
			qstrbuff.append("PREFIX " + key +": <" + prefixMap.get(key) + "> "); 
			//System.out.println("=====PREFIXES: PREFIX:" + key + " NS:" + prefixMap.get(key));
		}
		
		// Pick the attached spin:query from the class
		Property spinqueryprop=model.getProperty("http://spinrdf.org/spin#query");
		if(cls.hasProperty(spinqueryprop)){
			System.out.println("---- Resource: " + cls.getLocalName() + "has spin:query property");	
			StmtIterator stmts=cls.listProperties(spinqueryprop);
			if(stmts.hasNext()){ // Find only one attached query 
				Resource res=stmts.next().getResource();
				org.topbraid.spin.model.Query spinQuery= SPINFactory.asQuery(res);
				//Select spinQuery = (Select) res;
				System.out.println("----SPIN QUERY:\n" + spinQuery.toString());
				qstrbuff.append(spinQuery.toString());
				System.out.println("===== ATTACHED QUERY ======\n" + qstrbuff.toString());
				sparqlSelectQuery(model, qstrbuff, queryVars);
			} else System.out.println("----???? Resource: " + cls.getLocalName() + " DOES NOT have any attached queries (spin:query property)");	
			System.out.println("------------------------------");
		}
		
	}
	
	
	public void sparqlDescribeQuery(OntModel ontModel, Model targetModel, StringBuffer queryStr) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: sparqlDescribeQuery()");
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		Model results = qexec.execDescribe();
		if(targetModel!=null) targetModel.add(results);
		System.out.println("---DESCRIBE QUERY:\n " + queryStr.toString());
		results.write(System.out, "TURTLE");
	}
	
	public void sparqlConstructQuery(OntModel ontModel, Model targetModel, StringBuffer queryStr) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: sparqlConstructQuery()");
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		Model results = qexec.execConstruct();
		if(targetModel!=null) targetModel.add(results);
		System.out.println("---CONSTRUCT QUERY:\n " + queryStr.toString());
		results.write(System.out, "TURTLE");
	}
	
	public void sparqlUpdateQuery(OntModel ontModel, StringBuffer updateQueryStr) {
		//See: http://stackoverflow.com/questions/23102507/how-to-update-a-model-with-jena-api-and-sparql-eg-update-value-of-a-node
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: sparqlUpdateQuery()");
		UpdateAction.parseExecute(updateQueryStr.toString(), ontModel) ;
		System.out.println("---UPDATE QUERY:\n " + updateQueryStr.toString());
	}
	
	public void sparqlSelectQuery(OntModel ontModel, StringBuffer queryStr,
			List<String> queryVars) { //String queryVar) {
		logger.log(Level.INFO, "Entering: " + getClass().getName() + " method: sparqlQuery()");
		System.out.println("---sparqlQuery()");
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		System.out
		.println("============ Sparql SELECT Query Results ==============");
		int cnt = 0;
		try {
			ResultSet response = qexec.execSelect();
			while (response.hasNext()) {
				cnt++;
				QuerySolution solution = response.nextSolution();
				
				for(String qvar: queryVars){
					//if(qvar.startsWith("?")) qvar=qvar.substring(1); // Do we need this??			
					RDFNode varunif = solution.get(qvar); // "?acc");
					if (varunif != null)
						System.out.println("(" + cnt + ") Select Query result: " + qvar + " = "
							+ varunif);
					else
						System.out.println("NO Select Query variable result");
				}
			}

		} finally {
			qexec.close();
		}
		System.out.println("sparqlQuery: result rows #" + cnt);

	}

	public static void main(String[] args) {
		System.out.println("LOGGER is " + logger.getName());
		System.out.println("APPENDER is " + logger.getAppender("FILE")); //getAllAppenders().nextElement().getClass().getName());
		logger.log(Level.INFO,"=====ModelSpinManager main() STARTED AT: " + Calendar.getInstance().getTime() + "======");
		ModelSpinManager mng = new ModelSpinManager();
		logger.log(Level.INFO, "Entering: " + mng.getClass().getName() + " method: main()");
		List<String> urls = new ArrayList<String>();
		List<String> altlocs = new ArrayList<String>();

		// main ontology
		urls.add("http://siima.net/ont/bicycle");
		altlocs.add("data/models/importing_models/bicycle.ttl");
		// imported ontology
		urls.add("http://siima.net/ont/accessories");
		altlocs.add("data/models/importing_models/accessories.ttl");

		OntModel baseont = mng.loadModelWithImports(urls, altlocs);
		//test:ei toimi: OntModel baseont=mng.loadModelUsingFileModelMaker(altlocs);
		
		// Set mainOntModel
		mng.setMainOntModel(baseont);
		// Set ntModelWithReasoner
		mng.setOntModelWithReasoner(mng.createReasonerModel(mng
				.getMainOntModel()));

		StringBuffer queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("SELECT ?acc WHERE { ?acc rdf:type <http://siima.net/ont/accessories#Bell> . } ");
		List<String> queryVars = new ArrayList<String>();
		queryVars.add("?acc");
		mng.sparqlSelectQuery(mng.getMainOntModel(), queryStr, queryVars); //"?acc");

		queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("SELECT ?wheels WHERE { ?bic rdf:type <http://siima.net/ont/bicycle#Bicycle> . "
				+ "?bic <http://siima.net/ont/bicycle#wheelcount> ?wheels . } ");
		
		queryVars = new ArrayList<String>();
		queryVars.add("?wheels");
		mng.sparqlSelectQuery(mng.getMainOntModel(), queryStr, queryVars); //"?wheels");

		mng.createInferredModelAndRegister();

		mng.runAllSpinInferences(false); //singlePass=false: run iteratively

		// Running the same query again after spin inferencing.
		queryVars = new ArrayList<String>();
		queryVars.add("?wheels");
		mng.sparqlSelectQuery(mng.getMainOntModel(), queryStr, queryVars); //"?wheels");

		mng.checkSPINConstraints(null);

		Resource resource = mng.getMainOntModel().getResource(
				"http://siima.net/ont/bicycle#Bicycle_4");
		// Resource person = cvs.get(0).getRoot();
		System.out.println("Resource " + SPINLabels.get().getLabel(resource));
		mng.checkSPINConstraintForResource(resource, null);

		/*
		 * New template NOTE: Bicycle-class is subClass of Vehicle-class
		 * TEMPLATE query for instances of type :Vehicle requires subClassOf
		 * reasoning: 
		 * => NO RESULT: Using basic ontModel 
		 * => RESULTS OK: Using ontModel with reasoner.
		 */
		String query = "SELECT *\n"
				+ "WHERE {\n"
				+ "    ?bicycle ?predicate <http://siima.net/ont/bicycle#Vehicle> .\n"
				+ "}"; // #Vehicle tai #Bicycle

		Map<String, RDFNode> argumNodeMap = new HashMap<String, RDFNode>();
		argumNodeMap.put("predicate", RDF.Property);
		Map<String, String> argumCommentMap = new HashMap<String, String>();
		argumCommentMap.put("predicate", "The predicate to get the value of.");

		String templateURI = "http://siima.net/ont/bicycle#MyTestTemplate";
		Template template = mng.createTemplate(mng.getMainOntModel(), query,
				templateURI, argumNodeMap, argumCommentMap);

		Map<String, RDFNode> predicateNodeMap = new HashMap<String, RDFNode>();
		predicateNodeMap.put("predicate", RDF.type);
		// mng.callTemplate(template, mng.getMainOntModel(), null); // No
		// SubClassOf reasoning
		queryVars = new ArrayList<String>();
		queryVars.add("bicycle");
		mng.callSelectTemplate(template, mng.getOntModelWithReasoner(),
				predicateNodeMap, queryVars); //"bicycle"); // SubClassOf reasoning possible

		SPINModuleRegistry.get().register(template);

		mng.getTemplates();
		
		mng.getFunctions();
		/* 
		 * SPIN CONSTRUCTORS
		 * TOIMII!!
		 * Bicycle constructor creates a statement: this-modelYear->"M2015".
		 * Handlebar constructor creates an instance of the Bell class and a statement: this-hasBell->new bell instance.  
		 * Further, Bell constructor creates a statement: this-created->datetime.
		 */
		//SPINConstructors.constructAll(mng.getMainOntModel(), mng.getInferredTriples(), null);
		mng.runConstructors(mng.getMainOntModel(), mng.getInferredTriples());
		
		queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("SELECT ?year WHERE { ?bic rdf:type <http://siima.net/ont/bicycle#Bicycle> . "
				+ "?bic <http://siima.net/ont/bicycle#modelYear> ?year . } ");
		
		queryVars = new ArrayList<String>();
		queryVars.add("?year");
		mng.sparqlSelectQuery(mng.getMainOntModel(), queryStr, queryVars); //"?year");
		
		System.out.println("-----Listing Inferred triples:---- ");
		//17:43:45 WARN  exec  :: URI <http://spinrdf.org/spif#buildURI> has no registered function factory
		mng.getInferredTriples().write(System.out);
		/*
		 * console output fragment example:
		 *  <rdf:Description rdf:about="http://siima.net/ont/accessories#Bell_2015-08-01T17:53:28.636+03:00">
    	<j.0:created rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">2015-08-01T17:53:28.698+03:00</j.0:created>
    	<rdf:type rdf:resource="http://siima.net/ont/accessories#Bell"/>
  		</rdf:Description>
		 */

		queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("SELECT ?bell WHERE { ?bell rdf:type <http://siima.net/ont/accessories#Bell> . "
				+ "?bic <http://siima.net/ont/bicycle#hasBell> ?bell . } ");
		
		queryVars = new ArrayList<String>();
		queryVars.add("?bell");
		mng.sparqlSelectQuery(mng.getMainOntModel(), queryStr, queryVars); // "?bell");
		
		
		
		/*
		 * Update
		 * See: https://anandavala.wordpress.com/2013/06/02/sparql-update-example-code-for-create-insert-delete-edit/
		 */
		queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("INSERT DATA { <http://siima.net/ont/accessories#bell_100_inserted> rdf:type <http://siima.net/ont/accessories#Bell> . "
				+ " } ");
		
		mng.sparqlUpdateQuery(mng.getMainOntModel(), queryStr);
		
		//mng.getMainOntModel().write(System.out);
		
		queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("SELECT ?bell WHERE { ?bell rdf:type <http://siima.net/ont/accessories#Bell> . "
				+ " } ");
		
		queryVars = new ArrayList<String>();
		queryVars.add("?bell");
		mng.sparqlSelectQuery(mng.getMainOntModel(), queryStr,queryVars); // "?bell");
		
		//TODO: closing model? does it write changes to file? NO IT DOES NOT (ModelMaker needed for that. see p. 277)
		
		mng.getMainOntModel().close();
		
		
		
	}

	public OntModel getOntModelWithReasoner() {
		return ontModelWithReasoner;
	}

	public void setOntModelWithReasoner(OntModel ontModelWithReasoner) {
		this.ontModelWithReasoner = ontModelWithReasoner;
	}

	public OntModel getMainOntModel() {
		return mainOntModel;
	}

	public void setMainOntModel(OntModel ontModel) {
		this.mainOntModel = ontModel;
	}

	public Model getInferredTriples() {
		return inferredTriples;
	}

	public void setInferredTriples(Model inferredTriples) {
		this.inferredTriples = inferredTriples;
	}

}
