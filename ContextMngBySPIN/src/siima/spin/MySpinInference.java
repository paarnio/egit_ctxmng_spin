/* MySpinInference.java
 * 2015-07-21 TOIMII omalla mallilla: bicycle.ttl imports accessories.ttl
 * based on KennedysInferencingAndConstraintExample..java
 * 
 * Ontologiat bicycle.ttl ja accessories.ttl ja SPIN säännöt luotu TBC:llä:
 * C:\SpecialPrograms\TBComposer\tbcworkspace\2015\MiscModels
 */

package siima.spin;

import java.util.List;

import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.SPINConstraints;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class MySpinInference {
	public static void main(String[] args) {
		
		 long startTime = System.currentTimeMillis();
		 long stopTime;
		 long elapsedTime;
		 
		 System.out.println("==== SPIN registry initialization ====");
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();
		
		//TIMER
		 stopTime = System.currentTimeMillis();
	     elapsedTime = stopTime - startTime;
	     System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);

		// Load main file
		//Model baseModel = ModelFactory.createDefaultModel();
		//baseModel.read("http://topbraid.org/examples/kennedysSPIN");
		
		// Create OntModel with imports
		//OntModel ontModel = JenaUtil.createOntologyModel(OntModelSpec.OWL_MEM,baseModel);
		
		/*
		 * VPA ADDIN: data/models/importing_models/bicycle.ttl
		 * 
		 * from: http://stackoverflow.com/questions/15986051/
		 * how-to-import-owl-file-into-eclipse-using-jena-and-create-instances-of-it
		 * 
		 * Katso myös, ?josko importtaus onnistuis kuten OWLRLExample.java:ssa metod loadModelWithImports(String url)
		 */
		System.out.println("==== BUILDING MODELS ====");
		
		FileManager.get().getLocationMapper().addAltEntry( "http://siima.net/ont/accessories", "data/models/importing_models/accessories.ttl" );
		FileManager.get().getLocationMapper().addAltEntry( "http://siima.net/ont/bicycle", "data/models/importing_models/bicycle.ttl" );
		Model baseOntology = FileManager.get().loadModel("http://siima.net/ont/bicycle" );
		Model accOntology = FileManager.get().loadModel("http://siima.net/ont/accessories" );
		
		OntModel ontModel = JenaUtil.createOntologyModel(OntModelSpec.OWL_DL_MEM,baseOntology); //oli OntModelSpec.OWL_MEM
		//OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,baseOntology);
	     //   m.addSubModel( baseOntology );
		//ontModel.read("data/models/importing_models/bicycle.ttl");
		ontModel.addSubModel(accOntology);
		
		//TIMER
		 stopTime = System.currentTimeMillis();
	     elapsedTime = stopTime - startTime;
	     System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
		
		//VPA addin: extra printing
		System.out.println("=========Listing model triples:======== ");
		accOntology.write(System.out);
		
		
		//TIMER
		stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
	     
		System.out.println("=========SPARQL QUERY 1 (for imported model):======== ");
		// book: 284
		StringBuffer queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("SELECT ?acc WHERE { ?acc rdf:type <http://siima.net/ont/accessories#Bell> . } ");
		
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		try {
			ResultSet response =qexec.execSelect();
			while (response.hasNext()){
				QuerySolution soln = response.nextSolution();
				RDFNode acc = soln.get("?acc");
				if(acc !=null) System.out.println("Accessory instance: " + acc);
				else System.out.println("NO Accessory instances");
			}
			
		} finally {
			qexec.close();
		}

		//TIMER
		stopTime = System.currentTimeMillis();
	     elapsedTime = stopTime - startTime;
	     System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
		
		
		// Create and add Model for inferred triples
		Model newTriples = ModelFactory.createDefaultModel();
		ontModel.addSubModel(newTriples);
		System.out.println("------Register locally defined functions ");
		// Register locally defined functions
		SPINModuleRegistry.get().registerAll(ontModel, null);
		
		//TIMER
		stopTime = System.currentTimeMillis();
	     elapsedTime = stopTime - startTime;
	     System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);

		// Run all inferences (SPIN rules)
		SPINInferences.run(ontModel, newTriples, null, null, false, null);
		System.out.println("------Inferred triples: " + newTriples.size());
		
		//TIMER
		stopTime = System.currentTimeMillis();
	     elapsedTime = stopTime - startTime;
	     System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
		
		//VPA addin: extra printing
		System.out.println("-----Listing Inferred triples:---- ");
		newTriples.write(System.out);
		
		//TIMER
		stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
	    
	    System.out.println("=========SPARQL QUERY 2 (for inferred content):======== ");
		// book: 284
		queryStr = new StringBuffer();
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("SELECT ?wheels WHERE { ?bic rdf:type <http://siima.net/ont/bicycle#Bicycle> . "
				+ "?bic <http://siima.net/ont/bicycle#wheelcount> ?wheels . } ");
		
		query = QueryFactory.create(queryStr.toString());
		qexec = QueryExecutionFactory.create(query, ontModel);
		try {
			ResultSet response =qexec.execSelect();
			while (response.hasNext()){
				QuerySolution soln = response.nextSolution();
				RDFNode wheels = soln.get("?wheels");
				if(wheels !=null) System.out.println("Wheel count: " + wheels);
				else System.out.println("NO wheels");
			}
			
		} finally {
			qexec.close();
		}
		//TIMER
		stopTime = System.currentTimeMillis();
	     elapsedTime = stopTime - startTime;
	     System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
	    
		
		// Run all constraints
		List<ConstraintViolation> cvs = SPINConstraints.check(ontModel, null);
		System.out.println("-----Constraint violations:");
		for(ConstraintViolation cv : cvs) {
			System.out.println(" - at " + SPINLabels.get().getLabel(cv.getRoot()) + ": " + cv.getMessage());
		}

		//TIMER
		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
	    System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
		
		// Run constraints on a single instance only
		Resource person = cvs.get(0).getRoot();
		List<ConstraintViolation> localCVS = SPINConstraints.check(person, null);
		System.out.println("------Constraint violations for " + SPINLabels.get().getLabel(person) + ": " + localCVS.size());
		
		//TIMER
		 stopTime = System.currentTimeMillis();
	     elapsedTime = stopTime - startTime;
	      System.out.println("CUMULATIVE RUN TIME:" + elapsedTime);
		
	}
}
