package siima.ui;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import siima.spin.ModelSpinManager;
//import siima.utils.UIPrompt;
import siima.utils.*;






//VPA
import org.apache.log4j.Logger;
import org.topbraid.spin.system.SPINLabels;

public class InteractiveSpinMng {

	
	private ModelSpinManager mng;
	private List<String> urls = new ArrayList<String>();
	private List<String> altlocs = new ArrayList<String>();
	private boolean spinRegistryUpdated =false;
	
	public UIPrompt ui;
	public BufferedReader _input;
	
	public String mainModelLocalName; //"bicycle";
	public List<String> prefixlines;
	
	// Constructor
	public InteractiveSpinMng() {
		this.ui = new UIPrompt();
		this._input = new BufferedReader(new InputStreamReader(
				System.in));
		
		this.mng = new ModelSpinManager();
		this.urls = new ArrayList<String>();
		this.altlocs = new ArrayList<String>();
		this.prefixlines= new ArrayList<String>();
		// FOR general and spin ontologies
		this.prefixlines.add("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
		this.prefixlines.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		this.prefixlines.add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		this.prefixlines.add("PREFIX sp: <http://spinrdf.org/sp#> ");
		this.prefixlines.add("PREFIX spif: <http://spinrdf.org/spif#> ");
		this.prefixlines.add("PREFIX spin: <http://spinrdf.org/spin#> ");
		this.prefixlines.add("PREFIX spl: <http://spinrdf.org/spl#> ");
		this.prefixlines.add("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ");
		//FOR ssp4t5.net ontologies
		 this.prefixlines.add("PREFIX ctxtop: <http://ssp4t5.net/test/context_mimosa#> ");
		 this.prefixlines.add("PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#> ");
		 this.prefixlines.add("PREFIX arg: <http://spinrdf.org/arg#> ");
		 this.prefixlines.add("PREFIX combined: <http://ssp4t5.net/mimosa/combined#> ");
		 this.prefixlines.add("PREFIX diagplus: <http://ssp4t5.net/mimosa/diagplus#> ");
		 this.prefixlines.add("PREFIX fn: <http://www.w3.org/2005/xpath-functions#> ");
		 this.prefixlines.add("PREFIX mctxcore: <http://ssp4t5.net/context/mctxcore#> ");
		 this.prefixlines.add("PREFIX mctxsimple: <http://ssp4t5.net/context/mctxsimple#> ");
		 //also
		 this.prefixlines.add("PREFIX workcoreplus: <http://ssp4t5.net/mimosa/workcoreplus#> ");
		 this.prefixlines.add("PREFIX cmcoreplus: <http://ssp4t5.net/mimosa/cmcoreplus#> ");
		 this.prefixlines.add("PREFIX regasset: <http://ssp4t5.net/mimosa/regasset#> ");
		 this.prefixlines.add("PREFIX regcore: <http://ssp4t5.net/mimosa/regcore#> ");
		//FOR bicycle ontologies
		this.prefixlines.add("PREFIX access: <http://siima.net/ont/accessories#> ");
		this.prefixlines.add("PREFIX bicycle: <http://siima.net/ont/bicycle#> ");
		
	}
	
	public void listTemplates(){
		mng.getTemplates();
		
	}
	public void listFunctions(){
		mng.getFunctions();
		
	}
	
	public void printTemplateInfo(){
		//Template is BicycleNameTemplate2------- with URI:http://siima.net/ont/bicycle#BicycleNameTemplate2
		int i=1;
		String prompt = "----P" + i++ + ": Template name?>";
		String defaults = "BicycleNameTemplate2"; 
		String name=ui.getInputOrDefault(prompt, _input, defaults);
		
		Map<String,String> nameUriMap= mng.getTemplatesNameUriMap();
		
		String uri = nameUriMap.get(name);
		mng.printTemplateInfo(null,uri);
		
	}
	
	public void setPredefinedPrefixes(){
		/* TODO: Testing: prefixMap's not populated 
		 * (Huom: ei korvaa prefix-rivejä sparql kyselyissä. Ks. asetettu constructorissa)
		 * Setting Prefix NS declarations for the main model
		 * 
		 */
		String option = this.mainModelLocalName;
		Map<String,String> prefixMap1 = new HashMap<String,String>();
		prefixMap1.put("bicycle","<http://siima.net/ont/bicycle#>");
		if(option!=null){
		switch(option){
		case "bicycle": mng.setNsPrefixes(prefixMap1); break;
		case "context_mimosa" : mng.setNsPrefixes(prefixMap1); break; //TODO
		}
		}
		
	}
	
	
	public void setNsPrefixes(){
		boolean go=true;
		String submenu = "\n\n-----------SUB-MENU----\n"
				+ "---OPERATION OPTIONS:\n"
				+ "---1. Set predefined model prefixes\n"
				+ "---2. Add Sparql prefix lines\n"
				+ "---3. Skip\n"
				+ "--------SELECT 1-3 ";
		String prompt = submenu;
		String defaults = "3";
		while(go) {
		String option=ui.getInputNumber(prompt, _input, defaults).toString();
		
		switch(option) {
		case "1": { setPredefinedPrefixes(); } break;
		case "2": { addSparqlPrefixLines(); } break;
		case "3": go=false; break;
		}
		}
		
	}
	
	
	public void addSparqlPrefixLines(){
		//Huom: Malliin asetetut prefix määritykset (setPredefinedPrefixes())
		//eivät näköjään korvaa tätä prefixlines-listaa??
		System.out.println("\n---- OPERATION: Add Sparql Prefix Lines ----\n");
		int i=1;
		String prompt = "----P" + i++ + ": Add Sparql Prefix Line?>\n(Note: end with a space character)";
		String defaults = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "; 
		String prefix=ui.getInputOrDefault(prompt, _input, defaults);		
		System.out.println("TEST:" + prefix);
		this.prefixlines.add(prefix + " ");
		System.out.println("\n---- --------- ----\n");
		
	}
	
	
	public void runQueries(){
		System.out.println("\n---- OPERATION: Run Query ----\n");
		
		//----------
		String submenu = "\n\n-----------SUB-MENU----\n"
				+ "---OPERATION OPTIONS:\n"
				+ "---1. Run SELECT query\n"
				+ "---2. Run UPDATE query\n"
				+ "---3. Run CONSTRUCT query\n"
				+ "---4. Skip\n"
				+ "--------SELECT 1-4 ";
		boolean go=true;
		//int i=1;
		while(go) {
		
			StringBuffer queryStr = new StringBuffer();
			for(int i=0; i<this.prefixlines.size(); i++)
				queryStr.append(this.prefixlines.get(i));
			
			String prompt = submenu;
			String defaults = "4"; 
			String option=ui.getInputNumber(prompt, _input, defaults).toString();
			
			switch(option) {
			case "1": {

		System.out.println("----EXAMPLE: " + "SELECT ?acc WHERE { ?acc rdf:type <http://siima.net/ont/accessories#Bell> . } ");
		System.out.println("----EXAMPLE: " + "SELECT ?wheels WHERE { ?bic rdf:type <http://siima.net/ont/bicycle#Bicycle> . "
		+ "?bic <http://siima.net/ont/bicycle#wheelcount> ?wheels . } ");		
		System.out.println("----EXAMPLE: Constructor checker query\n\t" + "SELECT ?year WHERE { ?bic rdf:type <http://siima.net/ont/bicycle#Bicycle> . "
		+ "?bic <http://siima.net/ont/bicycle#modelYear> ?year . } ");
		System.out.println("----EXAMPLE: Constructor checker query\n\t" + "SELECT ?bell WHERE { ?bell rdf:type <http://siima.net/ont/accessories#Bell> . "
				+ "?bic <http://siima.net/ont/bicycle#hasBell> ?bell . } ");
		System.out.println("---------------------------------------------\n\n");
		
		int j=1;
		 prompt = "----P" + j++ + ": Write SPARQL SELECT query?>";
		 defaults = "SELECT * WHERE { ?s rdf:type ?o . } LIMIT 10 "; 
		String query=ui.getInputOrDefault(prompt, _input, defaults);		
		//System.out.println("TEST:" + query);
		
		queryStr.append(query);
		
		prompt = "----P" + j++ + ": Query variable with ?-mark (e.g. ?acc)>";
		defaults = "?s"; 
		String queryvariable=ui.getInputOrDefault(prompt, _input, defaults);		
		//System.out.println("TEST:" + variable);
		
		List<String> queryVars = new ArrayList<String>();
		queryVars.add(queryvariable);
		mng.sparqlSelectQuery(mng.getMainOntModel(), queryStr, queryVars);		
			}
			break;
	 
		case "2": {
		
		int j=1;
		 prompt = "----P" + j++ + ": Write SPARQL UPDATE query?>";
		 defaults = "INSERT DATA { <http://siima.net/ont/accessories#bell_100_inserted> rdf:type <http://siima.net/ont/accessories#Bell> .  } "; 
		 String query=ui.getInputOrDefault(prompt, _input, defaults);		
			
		queryStr.append(query);
		mng.sparqlUpdateQuery(mng.getMainOntModel(), queryStr);
		}
	 	break;
		case "3": {
			
			int j=1;
			 prompt = "----P" + j++ + ": Write SPARQL CONSTRUCT query?>";
			 defaults = "CONSTRUCT { ?bic bicycle:name 'alias pyora' .} WHERE { ?bic a bicycle:Bicycle . } "; 
			 String query=ui.getInputOrDefault(prompt, _input, defaults);						
			queryStr.append(query);
			
			 prompt = "----P" + j++ + ": Add result model to InferredTriples model (y/n)?>";
			 defaults = "n"; 
			 String add=ui.getInputOrDefault(prompt, _input, defaults);
			
			if("y".equalsIgnoreCase(add))
			mng.sparqlConstructQuery(mng.getMainOntModel(), mng.getInferredTriples(), queryStr);
			else mng.sparqlConstructQuery(mng.getMainOntModel(), null, queryStr);
			}
		 	break;	 	
		case "4": go=false; break;
			}
		}


		
		System.out.println("\n---- --------- ----\n");
	}
	
	public void runInferences(){
	//singlePass=false: run iteratively
		boolean singlePass=false;
		String submenu = "\n\n-----------SUB-MENU----\n"
				+ "---OPERATION OPTIONS:\n"
				+ "---1. Run inferences iteratively (default)\n"
				+ "---2. Run inference only a single pass\n"
				+ "---3. Skip\n"
				+ "--------SELECT 1-3 ";
		String prompt = submenu;
		String defaults = "1"; 
		String option=ui.getInputNumber(prompt, _input, defaults).toString();
		
		switch(option) {
		case "1": { singlePass=false; runInferences(singlePass); } break;
		case "2": { singlePass=true; runInferences(singlePass); } break;
		case "3": break;
		}
		
	}
	public void runInferences(boolean singlePass){
		System.out.println("\n---- OPERATION: Run Inferences ----\n");		
		mng.createInferredModelAndRegister();
		mng.runAllSpinInferences(singlePass);//singlePass=false: run iteratively
		this.spinRegistryUpdated =true;
		System.out.println("\n---- --------- ----\n");
	}
	
	public void runTemplates(){
		//TODO: update template
		System.out.println("\n---- OPERATION: Run Templates ----\n");	
		mng.getTemplates();
		Map<String,RDFNode> argumentNodeMap = new HashMap<String,RDFNode>();
		
		String argname;
		String argtype;
		String argvalue;
		String arguri;
		String templatename=null;
		String querytype="select";
		String queryvariable=null;
		
		String submenu = "\n\n-----------SUB-MENU----\n"
				+ "---OPERATION OPTIONS:\n"
				+ "---1. Give template name and type\n"
				+ "---2. Print template info\n"
				+ "---3. Add argument node to map\n"
				+ "---4. Run template\n"
				+ "---5. Skip\n"
				+ "--------SELECT 1-5 ";
		boolean go=true;
		int i=1;
		while(go) {
			
			String prompt = submenu;
			String defaults = "5"; 
			String option=ui.getInputNumber(prompt, _input, defaults).toString();
			
			switch(option) {
			case "1": {
				prompt = "---P" + i++ + ": Template localname?>\n";
			 defaults = "BicycleNameTemplate2"; 
			 templatename=ui.getInputOrDefault(prompt, _input, defaults);
			 
			 prompt = "---P" + i++ + ": Template query type?>\n";
			 defaults = "select"; 
			 querytype=ui.getInputOrDefault(prompt, _input, defaults);
			}
			 break;
			case "2": {mng.printTemplateInfo(templatename, null);}
			 break;
			case "3": {
				prompt = "---P" + i++ + ": Argument name?>\n";
				defaults = "wcount"; 
				 argname=ui.getInputOrDefault(prompt, _input, defaults);
				
				prompt = "---P" + i++ + ": Argument type?>\n";
				defaults = "integer"; 
				 argtype=ui.getInputOrDefault(prompt, _input, defaults);
				
				prompt = "---P" + i++ + ": Literal Argument value?>\n";
				defaults = "1"; 
				 argvalue=ui.getInputOrDefault(prompt, _input, defaults);
				
				prompt = "---P" + i++ + ": Object Argument uri?>\n";
				defaults = "http://siima.net/dummy/"; 
				 arguri=ui.getInputOrDefault(prompt, _input, defaults);
				 
				 argumentNodeMap=mng.addArgumentNodeToMap(argumentNodeMap, argname, argtype, argvalue, arguri, null);
				 
			} break;
		
			case "4": {
				if("select".equalsIgnoreCase(querytype)){
				prompt = "---P" + i++ + ": Select template query variable?>\n";
				defaults = "name"; 
				queryvariable=ui.getInputOrDefault(prompt, _input, defaults); 
				} else if("construct".equalsIgnoreCase(querytype)){
					System.out.println("---- Calling construct type template ----\n");
				}
				
				List<String> queryVars = new ArrayList<String>();
				queryVars.add(queryvariable);
				mng.callTemplateByName(templatename, argumentNodeMap, querytype, queryVars);}
				break;
			
			case "5": go=false; break;
			default: go=false; break;
			}
		}
		System.out.println("\n---- --------- ----\n");
		//'-------------
		
		//argumentNodeMap=mng.addArgumentNodeToMap(argumentNodeMap, "wcount", "integer", "1", "http://siima.net/dummy/", null);
		//mng.callTemplateByName("BicycleNameTemplate2", argumentNodeMap, "name");
		
		System.out.println("\n---- --------- ----\n");
	}
	
	public void checkConstraints(){
		//DONE: runInferences(false) korvattu mng.createInferredModelAndRegister()
		//TODO: Toimiiko varmasti??
		System.out.println("\n---- OPERATION: Check Constraints ----\n");
		String submenu = "\n\n -----------SUB-MENU----\n"
				+ "---OPERATION OPTIONS:\n"
				+ "---1. Check all constraints\n"
				+ "---2. check Constraints for one resource\n"
				+ "---3. Skip\n"
				+ "--------SELECT 1-3 ";
		boolean go=true;
		int i=1;
		while(go) {
			
			String prompt = submenu;
			String defaults = "3"; 
			String option=ui.getInputNumber(prompt, _input, defaults).toString();
			
			switch(option) {
			case "1": {
				if(!spinRegistryUpdated){ mng.createInferredModelAndRegister(); 
				this.spinRegistryUpdated =true;
				} //runInferences(false);//singlePass=false: run iteratively
				mng.checkSPINConstraints(null); // Save results to model?
				
			} break;
			case "2": {
				if(!spinRegistryUpdated){ mng.createInferredModelAndRegister(); //VPA: runInferences(false);//singlePass=false: run iteratively
				this.spinRegistryUpdated =true;
				} //runInferences(false);//singlePass=false: run iteratively
				prompt = "---P" + i++ + ": Resource URI?>\n"
						+ "(Example: http://siima.net/ont/bicycle#Bicycle_4)\n";
				defaults = "http://siima.net/ont/bicycle#Bicycle_4"; 
				String uri=ui.getInputOrDefault(prompt, _input, defaults);
				System.out.println("TEST:" + uri);
				if(uri!=null){
				Resource resource = mng.getMainOntModel().getResource(uri);//"http://siima.net/ont/bicycle#Bicycle_4"						
				System.out.println("Resource " + SPINLabels.get().getLabel(resource));
				mng.checkSPINConstraintForResource(resource, null);	// Save results to model?			
				}
			} break;
			
			case "3": go=false; break;
			}
		}
		System.out.println("\n---- --------- ----\n");
		
	}
	
	public void runConstructors(){
		/* RUNS ONLY SPIN:CONSTRUCTORS (not spin rules)
		 * DONE: Pitääkö ennen mng.runConstructors() ajaa runInferences() EI
		 * Pelkkä mng.createInferredModelAndRegister() RIITTÄÄ !!!
		 */
		
		
		System.out.println("\n---- OPERATION: Run Constructors ----\n");	
		/* 
		 * CONSTRUCTORS
		 * TOIMII!!
		 * Bicycle constructor creates a statement: this-modelYear->"M2015".
		 * Handlebar constructor creates an instance of the Bell class and a statement: this-hasBell->new bell instance.  
		 * Further, Bell constructor creates a statement: this-created->datetime.
		 */
		//VPA TEST
		if(!spinRegistryUpdated){ mng.createInferredModelAndRegister(); //VPA: runInferences(false);//singlePass=false: run iteratively
			this.spinRegistryUpdated =true;
		}
		mng.runConstructors(mng.getMainOntModel(), mng.getInferredTriples());
		System.out.println("\n---- --------- ----\n");
	}
	
	public void writeModelToFile(){
		//TBC path: C:/SpecialPrograms/TBComposer/tbcworkspace/2016a/SWgenerated/swbicycle.ttl
		System.out.println("\n---- OPERATION: Write the main model into a file ----\n");
		String filepath="C:/Temp/fullmodel.ttl";
		
		System.out.println("\n----------- File path  ----------");
		int i=0;
		String prompt = "Q" + i++ + ": File path?>";
		String defaults = "data/temp/temp.ttl"; 
		filepath=ui.getInputOrDefault(prompt, _input, defaults);
		
		
		try {
			if(filepath!=null){
			FileWriter filewriter = new FileWriter(filepath); //"C:/Temp/test.ttl");
			//mng.getMainOntModel().write(filewriter, "TURTLE");
			mng.mergeMainModelAndInferredTriples().write(filewriter, "TURTLE");
			} else {
				mng.mergeMainModelAndInferredTriples().write(System.out, "TURTLE");
			}
			//VPA: ei saa sulkea: mng.mergeMainModelAndInferredTriples().close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("\n---- --------- ----\n");
		
	}
	
	
	
	public void printInferredTriples(){
		System.out.println("\n---- OPERATION: Print Inferred Triples ----\n");
		
		String filepath="data/inferences/inferred.ttl";
		
		System.out.println("\n----------- Writeting Inference results to a File or Printing to the Console  ----------");
		System.out.println("(press 'enter' to select default value;\n"
				+ "press 'space' or write 'null' to skip and return 'null';\n"
				+ "write 'filepath' + enter to return the filepath to the result file)\n"
				+ ">"); 
		
		int i=0;
		String prompt = "Q" + i++ + ": File path?>";
		String defaults = "data/inferences/inferred.ttl"; 
		filepath=ui.getInputOrDefault(prompt, _input, defaults);
		/* press 'enter' to select default value
		 * press 'space' or write 'null' to skip and return 'null'
		 * write text + enter to return text answer 
		 */
		
		
		try {
			if(filepath!=null){
			FileWriter out = new FileWriter(filepath); //"C:/Temp/test.ttl");
			mng.getInferredTriples().write(out, "TURTLE");
			} else {
				System.out.println("\n---- Printing Inferred Triples to Console ----\n"
						+ "------------------------------------------------");
				mng.getInferredTriples().write(System.out, "TURTLE");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("\n---- --------- ----\n");
	}
	
	public void startPredefinedKB_Bicycle(){
		/* --- Main ontology --- */
		String ont_folder = "data/models/importing_models"; 
		
		String main_ont_file= "bicycle.ttl"; 
		String main_ont_url="http://siima.net/ont/bicycle"; 
		
		altlocs.add(ont_folder + "/" + main_ont_file);
		urls.add(main_ont_url);
		
		/* --- Imported ontologies --- */		
		String imp_ont_file="accessories.ttl"; 
		String imp_ont_url= "http://siima.net/ont/accessories"; 
		
		altlocs.add(ont_folder + "/" + imp_ont_file);
		urls.add(imp_ont_url);
		
		/* ----------- Build Models ----------*/
		OntModel baseont = mng.loadModelWithImports(urls, altlocs);
		// Set mainOntModel
		mng.setMainOntModel(baseont);
		// Set ntModelWithReasoner
		mng.setOntModelWithReasoner(mng.createReasonerModel(mng
						.getMainOntModel()));
		
		this.mainModelLocalName="bicycle"; 
		System.out.println("\n----------- Knowledge Base Ready  ----------");
		
	}
	
	public void startPredefinedKB_ssp4t5net(){
		
		/* --- Main ontology --- */
		String ont_folder = "data/models/ssp4t5net"; 
		
		String main_ont_file= "context_mimosa.ttl"; 
		String main_ont_url="http://ssp4t5.net/test/context_mimosa"; 
		
		altlocs.add(ont_folder + "/" + main_ont_file);
		urls.add(main_ont_url);
		
		/* --- Imported ontologies --- */		

		
		altlocs.add(ont_folder + "/" + "mctxsimple.ttl");
		urls.add("http://ssp4t5.net/context/mctxsimple");
		
		altlocs.add(ont_folder + "/" + "combined.ttl");
		urls.add("http://ssp4t5.net/mimosa/combined");
		
		altlocs.add(ont_folder + "/" + "mctxcore.ttl");
		urls.add("http://ssp4t5.net/context/mctxcore");
		
		altlocs.add(ont_folder + "/" + "cmcoreplus.ttl");
		urls.add("http://ssp4t5.net/mimosa/cmcoreplus");
		
		altlocs.add(ont_folder + "/" + "diagplus.ttl");
		urls.add("http://ssp4t5.net/mimosa/diagplus");
		
		altlocs.add(ont_folder + "/" + "regasset.ttl");
		urls.add("http://ssp4t5.net/mimosa/regasset");
		
		altlocs.add(ont_folder + "/" + "regcore.ttl");
		urls.add("http://ssp4t5.net/mimosa/regcore");
		
		altlocs.add(ont_folder + "/" + "workcoreplus.ttl");
		urls.add("http://ssp4t5.net/mimosa/workcoreplus");
		
		
		/* ----------- Build Models ----------*/
		OntModel baseont = mng.loadModelWithImports(urls, altlocs);
		// Set mainOntModel
		mng.setMainOntModel(baseont);
		// Set ntModelWithReasoner
		mng.setOntModelWithReasoner(mng.createReasonerModel(mng
						.getMainOntModel()));
		
		this.mainModelLocalName="context_mimosa"; 
		System.out.println("\n----------- Knowledge Base Ready  ----------");	
		
	}
	
	public void buildKnowledgeBaseUI(){
		
		System.out.println("\n----------- Building Ontology Models ----------");
		/* --- Main ontology --- */
		System.out.println("\n----------- Main Ontology  ----------");
		int i=0;
		String prompt = "Q" + i++ + ": Ontology folder?>";
		String defaults = "data/models/importing_models"; 
		String ont_folder=ui.getInputOrDefault(prompt, _input, defaults);
		
		prompt = "Q" + i++ + ": Main Ontology file?>";
		defaults = "bicycle.ttl"; 
		String main_ont_file=ui.getInputOrDefault(prompt, _input, defaults);

		prompt = "Q" + i++ + ": Main Ontology url?>";
		defaults = "http://siima.net/ont/bicycle"; 
		String main_ont_url=ui.getInputOrDefault(prompt, _input, defaults);
		
		altlocs.add(ont_folder + "/" + main_ont_file);
		urls.add(main_ont_url);
		
		/* --- Imported ontologies --- */
		System.out.println("\n----------- Imported Ontologies  ----------");
		prompt = "Q" + i++ + ": How many imported ontologies>";
		defaults = "1"; 
		Integer num=ui.getInputNumber(prompt, _input, defaults);
		
		//System.out.println("TEST:" + num.intValue());
		
		if((num!=null)&&(num>0)){
			
			for(int n = 0; n<num.intValue();n++){
		
				prompt = "Q" + i++ + ": Imported Ontology file?>";
				defaults = "accessories.ttl"; 
				String imp_ont_file=ui.getInputOrDefault(prompt, _input, defaults);

				prompt = "Q" + i++ + ": Imported Ontology url?>";
				defaults = "http://siima.net/ont/accessories"; 
				String imp_ont_url=ui.getInputOrDefault(prompt, _input, defaults);
				
				altlocs.add(ont_folder + "/" + imp_ont_file);
				urls.add(imp_ont_url);
			}
		
		}
		
		//System.out.println("TEST:" + altlocs.get(0));
		
		/* ----------- Build Models ----------*/
		OntModel baseont = mng.loadModelWithImports(urls, altlocs);
		// Set mainOntModel
		mng.setMainOntModel(baseont);
		// Set ntModelWithReasoner
		mng.setOntModelWithReasoner(mng.createReasonerModel(mng
						.getMainOntModel()));
		 
		System.out.println("\n----------- Knowledge Base Ready  ----------");
	}
	
	
	
	public static void main(String[] args) {
				
		System.out.println("================ InteractiveSpinMng ===============\n\n"); 
		InteractiveSpinMng ism = new InteractiveSpinMng();
		
		String prompt = "?";
		String defaults;
		boolean go=true;
		
		//---
		
		List<String> urls = new ArrayList<String>();
		List<String> altlocs = new ArrayList<String>();
		
		
		
		
		/*-------------- User interaction --------------*/
		System.out.println("----- ModelSpinManager READY to start user interaction -----\n\n"); 
		
		/* press 'enter' to select default value
		 * press 'space' + enter to skip and return 'null'
		 * write text + enter to return text answer 
		 */
		
		String startmenu = "\n\n--------START-MENU---------\n"
				+ "OPERATION OPTIONS:\n"
				+ "1. Build knowledge base interactively\n"
				+ "2. Start predefined KB: bicycle\n"
				+ "3. Start predefined KB: ssp4t5.net\n"
				+ "4. Start predefined KB: \n"
				+ "5. Exit\n"
				+ "-----SELECT 1-5 ";
		
		
		prompt = startmenu;
		defaults = "5"; 
		String option=ism.ui.getInputNumber(prompt, ism._input, defaults).toString();
		
		switch(option) {
		case "1": ism.buildKnowledgeBaseUI(); break;
		case "2": ism.startPredefinedKB_Bicycle(); break;
		case "3": ism.startPredefinedKB_ssp4t5net(); break;
		case "4": go=false; break;
		case "5": go=false; break;
		}
		
		

		
		String menu = "\n\n--------MAIN-MENU---------\n"
				+ "OPERATION OPTIONS:\n"
				+ "1. Add Sparql prefix lines\n"
				+ "2. Run queries\n"
				+ "3. Run inferences\n"
				+ "4. Run templates\n"
				+ "5. Check constraints\n"
				+ "6. Run constructors\n"
				+ "7. Print inferred triples\n"
				+ "8. Print template info\n"
				+ "9. List all templates\n"
				+ "10.List all functions\n"
				+ "11.Write the main model into a file\n"
				+ "12.Exit\n"
				+ "-----SELECT 1-12 ";
				
		
		
		while(go) {
			
			prompt = menu;
			defaults = "12"; 
			option=ism.ui.getInputNumber(prompt, ism._input, defaults).toString();
			
			switch(option) {
			case "1": ism.setNsPrefixes(); break;
			case "2": ism.runQueries(); break;
			case "3": ism.runInferences(); break; 			
			case "4": ism.runTemplates(); break;
			case "5": ism.checkConstraints(); break;
			case "6": ism.runConstructors(); break;
			case "7": ism.printInferredTriples(); break;
			case "8": ism.printTemplateInfo(); break;
			case "9": ism.listTemplates(); break;
			case "10": ism.listFunctions(); break;
			case "11": ism.writeModelToFile(); break;
			case "12": go=false; break;
			}
			
		}
		
		
	}

}
