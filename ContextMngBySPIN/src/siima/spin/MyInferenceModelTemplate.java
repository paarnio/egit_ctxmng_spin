/* MyInferenceModelTemplate.java
 * TOIMIIKO? 2015-07-12
 * TOIMII kuten MyTemplateExample.java
 * Lähes sama kuin MyTemplateExample.java
 * MUTTA käyttäen inferenceModel:a ja hiukan erilaista Query:ä
 * 
 * Ks MYÖS: KennedysInferencingAndConstraintsExample.java
 * 
 */
package siima.spin;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.ARG;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MyInferenceModelTemplate {
	

	private static final String NS = "http://example.org/model#";
	private static final String BICNS = "http://siima.net/ont/bicycle#";
	
	private static final String PREFIX = "ex";
	private static final String BICPREFIX = "bic";
	
	// Query of the template - argument will be arg:predicate
	private static final String QUERY =
		"SELECT *\n" +
		"WHERE {\n" +
		"    ?bicycle ?predicate <http://siima.net/ont/bicycle#Vehicle> .\n" +
		"}";

	//oli: "    ?bicycle ?predicate ?object .\n" +
	
	public static void main(String[] args) {
		
		System.out.println("*************** MyTemplateExample.java CONSOLE PRINT ****************\n");
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();
/* */
		// Create main model
		Model main_model = JenaUtil.createDefaultModel();
		JenaUtil.initNamespaces(main_model.getGraph()); //???
		//VPA: addin
		//main_model.read("data/models/bicycle2.ttl");

		/* VPA NEW From:C:\javalab\eclwork\example_code\jena_code\jena_example  */
		String inputFileName="data/models/bicycle2.ttl";
		//OntModel ontModel = ModelFactory.createOntologyModel();
	   
	    /*
	     InputStream in = FileManager.get().open(inputFileName);
	    if (in == null) {
	        throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	    }
	    ontModel.read(in, "");
	*/

		main_model = FileManager.get().loadModel(inputFileName, "TURTLE");
		
		//model.add(SystemTriples.getVocabularyModel()); // Add some queryable triples
		main_model.setNsPrefix(PREFIX, NS);
		main_model.setNsPrefix(ARG.PREFIX, ARG.NS);
		//VPA:
		main_model.setNsPrefix(BICPREFIX, BICNS);
		SPINModuleRegistry.get().registerAll(main_model,null);
		
		
	    Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
	    //reasoner = reasoner.bindSchema(ontModel);
	    // Obtain standard OWL-DL spec and attach the Pellet reasoner
	    OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM;
	    ontModelSpec.setReasoner(reasoner);
	    // Create ontology model with reasoner support
	    OntModel model = ModelFactory.createOntologyModel(ontModelSpec, main_model); //ontModel);
	    /* :VPA NEW */
		
	
		
		// Create template
		Template template = createTemplate(model);

		// Now call the template
		com.hp.hpl.jena.query.Query arq = ARQFactory.get().createQuery((Select)template.getBody());
		QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, model);
		QuerySolutionMap arqBindings = new QuerySolutionMap();
		//-----VPA:addin---------
		//Property bicname = model.createProperty(BICNS + "name");
		
		arqBindings.add("predicate", RDF.type); // bicname); //original:RDFS.label);
		qexec.setInitialBinding(arqBindings); // Pre-assign the arguments
		ResultSet rs = qexec.execSelect();
		RDFNode subject = rs.next().get("bicycle"); 
		//"object");
		System.out.println("============ MyTemplate Query Result ==============");
		//System.out.println(">>" + bicname.getLocalName() + "'s value is " + object);
		System.out.println(">> one ?bicycle variable unifies to" + subject);
		System.out.println("==================================================\n");
		
		
		/************************************************
		 * -----------VPA additions:--------------------
		 * 
		 * 
		 ************************************************/
		SPINModuleRegistry.get().register(template);
		//SPINModuleRegistry.get().registerAll(model,null);
		//----------Templates
		Collection<Template> alltemplates=SPINModuleRegistry.get().getTemplates();
		Iterator<Template> iter=alltemplates.iterator();
		System.out.println("\n================ALL MODEL TEMPLATES ================\n");
		for(;iter.hasNext();){
			Template tpl=iter.next();
			System.out.println("Template is " + tpl.getLocalName() + "------- with URI:" + tpl.getURI() + "\n-------- with comment:" + tpl.getComment());
		}
		System.out.println("\n====================================================\n");
		//-----------Functions
		Collection<Function> allfuncs=SPINModuleRegistry.get().getFunctions();
		Iterator<Function> fiter=allfuncs.iterator();
		System.out.println("\n================ALL MODEL FUNCTIONS ================\n");
		for(;fiter.hasNext();){
			Function fun=fiter.next();
			System.out.println("Function is " + fun.getLocalName() + "------- with URI:" + fun.getURI() + "\n-------- with comment:" + fun.getComment());
		}
		
		System.out.println("\n===================================================\n");
		//One template
		//Template rpc=SPINModuleRegistry.get().getTemplate("http://spinrdf.org/spl#RangePropertyConstraint", model);
		Template bictmp=SPINModuleRegistry.get().getTemplate("http://siima.net/ont/bicycle#BicycleNameTemplate2", model);
		System.out.println("Bicycle Template is " + bictmp.getLocalName() + "\n");
		System.out.println("Bicycle Template body " + bictmp.getBody() + "\n");
		
		// Now call the BicycleNameTemplate2 template
					com.hp.hpl.jena.query.Query arq_vpa = ARQFactory.get().createQuery((Select)bictmp.getBody());
					QueryExecution qexec_vpa = ARQFactory.get().createQueryExecution(arq_vpa, model);
					QuerySolutionMap arqBindings_vpa = new QuerySolutionMap();
					
					RDFNode typedObject=ResourceFactory.createTypedLiteral(new Integer(1));
					
					arqBindings_vpa.add("wcount", typedObject); //RDFNode , wcount=wheel count
					qexec.setInitialBinding(arqBindings_vpa); // Pre-assign the arguments
					ResultSet rs_vpa = qexec_vpa.execSelect();
					RDFNode name = rs_vpa.next().get("name");
					System.out.println("============ BicycleNameTemplate2 Query Result =============");
					System.out.println(">>Bicycle name is " + name);
					System.out.println("============================================================\n");
		
	}


	private static Template createTemplate(Model model) {
		
		// Create a template
		com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(model, QUERY);
		Query spinQuery = new ARQ2SPIN(model).createQuery(arqQuery, null);
		Template template = model.createResource(BICNS + "MyTemplate", SPIN.Template).as(Template.class);
		template.addProperty(SPIN.body, spinQuery);
		
		// Define spl:Argument at the template
		Resource argument = model.createResource(SPL.Argument);
		argument.addProperty(SPL.predicate, model.getProperty(ARG.NS + "predicate"));
		argument.addProperty(SPL.valueType, RDF.Property);
		argument.addProperty(RDFS.comment, "The predicate to get the value of.");
		template.addProperty(SPIN.constraint, argument);
		
		return template;
	}



}
