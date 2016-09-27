/* ****************************************
 * MyTemplateExample.java
 * Eclipse project: SpinExamplesLib133AsSrc
 * based on: TemplateExample.java
 * 2015-07-01 TOIMII!!!!!
 * 
 * MODEL: model.read("data/models/bicycle2.ttl"); 
 * Model created in TBC project: C:\Special_Programs\TBC_projects\2015std\MiscModels 
 * TEMPLATES from bicycle2.ttl:
 * 		BicycleNameTemplate2 löytyy, koska se on suoraan subClassOf spin:Template
 * MUTTA BicycleNameTemplate1 EI löydy, koska se on subClassOf spin:SelectTemplate
 * 
 * TEMPLATE created in this program: MyTemplate
 * 
 */
package siima.spin;

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
import org.topbraid.spin.util.SystemTriples;
import org.topbraid.spin.vocabulary.ARG;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MyTemplateExample {

	

		private static final String NS = "http://example.org/model#";
		private static final String BICNS = "http://siima.net/ont/bicycle#";
		
		private static final String PREFIX = "ex";
		private static final String BICPREFIX = "bic";
		
		// Query of the template - argument will be arg:predicate
		private static final String QUERY =
			"SELECT *\n" +
			"WHERE {\n" +
			"    ?bicycle ?predicate ?object .\n" +
			"}";

		//"    ?bicycle ?predicate <http://siima.net/ont/bicycle#Vehicle> .\n"

		
		public static void main(String[] args) {
			
			System.out.println("*************** MyTemplateExample.java CONSOLE PRINT ****************\n");
			// Initialize system functions and templates
			SPINModuleRegistry.get().init();

			// Create main model
			Model model = JenaUtil.createDefaultModel();
			JenaUtil.initNamespaces(model.getGraph());
			//VPA: addin
			model.read("data/models/bicycle2.ttl");
			
			//model.add(SystemTriples.getVocabularyModel()); // Add some queryable triples
			model.setNsPrefix(PREFIX, NS);
			model.setNsPrefix(ARG.PREFIX, ARG.NS);
			//VPA:
			model.setNsPrefix(BICPREFIX, BICNS);
			SPINModuleRegistry.get().registerAll(model,null);
			
			
			// Create template
			Template template = createTemplate(model);

			// Now call the template
			com.hp.hpl.jena.query.Query arq = ARQFactory.get().createQuery((Select)template.getBody());
			QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, model);
			QuerySolutionMap arqBindings = new QuerySolutionMap();
			//-----VPA:addin---------
			Property bicname = model.createProperty(BICNS + "name");
			
			arqBindings.add("predicate", bicname); //original:RDFS.label);RDF.type); 
			qexec.setInitialBinding(arqBindings); // Pre-assign the arguments
			ResultSet rs = qexec.execSelect();
			RDFNode object = rs.next().get("object"); //"bicycle"); //
			System.out.println("============ MyTemplate Query Result ==============");
			System.out.println(">>" + bicname.getLocalName() + "'s value is " + object);
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
						
						arqBindings_vpa.add("wcount", typedObject); //RDFNode
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
