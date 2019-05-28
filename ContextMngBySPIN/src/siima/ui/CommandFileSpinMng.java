/* siima.ui.CommandFileSpinMng.java
 * 2016-02-22 v1.0
 * 2016-02-10 TOIMII
 * 
 * 
 */
package siima.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.topbraid.spin.system.SPINLabels;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import siima.spin.ModelSpinManager;
import siima.utils.UIPrompt;

public class CommandFileSpinMng {

	private JSONParser parser;
	private JSONObject jsonrootobj;

	// from InteractiveSpinMang.java
	private ModelSpinManager mng;
	private List<String> urls = new ArrayList<String>();
	private List<String> altlocs = new ArrayList<String>();
	private boolean spinRegistryUpdated = false;

	public UIPrompt ui;
	public BufferedReader _input;

	public String mainModelLocalName; // "bicycle";
	public List<String> prefixlines;
	public boolean prefixlinesfilled = false;
	public boolean kb_loaded = false;

	/* =======================================
	 * 
	 * CONSTRUCTOR
	 * 
	 * =======================================
	 */
	
	public CommandFileSpinMng() {
		this.parser = new JSONParser();
		// from InteractiveSpinMang.java
		this.ui = new UIPrompt();
		this._input = new BufferedReader(new InputStreamReader(System.in));

		this.mng = new ModelSpinManager();
		this.urls = new ArrayList<String>();
		this.altlocs = new ArrayList<String>();
		this.prefixlines = new ArrayList<String>();
	}

	/* =======================================
	 * 
	 * METHODS
	 * 
	 * =======================================
	 */
	
	public void checkConstraintsCommands(JSONObject comobj){
		//TODO: testing
		System.out.println("----+ Command: checkConstraintsCommands");
		JSONObject subobj = (JSONObject) comobj.get("constraints");
		//if(subobj!=null){
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		Boolean addrdf = (Boolean) subobj.get("addrdf");
		System.out.println("----+----+checkConstraintsCommands name:" + name + " and type:" + type + " and addrdf:" + addrdf);
		//}
		if(!spinRegistryUpdated){ mng.createInferredModelAndRegister(); 
		this.spinRegistryUpdated =true;
		} 
		
		if("all".equalsIgnoreCase(type)){
			if((addrdf!=null)&&(addrdf)) mng.checkSPINConstraints(mng.getInferredTriples()); // Save results to model
			else mng.checkSPINConstraints(null); 
		} else if("resource".equalsIgnoreCase(type)){
			String uri = (String) subobj.get("uri");
			System.out.println("TEST:" + uri);
			if(uri!=null){
			Resource resource = mng.getMainOntModel().getResource(uri);//"http://siima.net/ont/bicycle#Bicycle_4"						
			System.out.println("Resource " + SPINLabels.get().getLabel(resource));
			if((addrdf!=null)&&(addrdf)) mng.checkSPINConstraintForResource(resource, mng.getInferredTriples());	// Save results to model			
			else mng.checkSPINConstraintForResource(resource, null);
			}
		}	
	}
	
	
	
	public void runSpinConstructorsCommand(JSONObject comobj){
		System.out.println("----+ Command: runSpinConstructorsCommand");
		JSONObject subobj = (JSONObject) comobj.get("constructors");
		if(subobj!=null){
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		System.out.println("----+----+runSpinConstructorsCommand name:" + name + " and type:" + type);
		}
		runSpinConstructors();
		
	}
	
	public void templateCallCommand(JSONObject comobj) {
		System.out.println("----+ Command: templateCallCommand");
		Map<String, RDFNode> argumentNodeMap = new HashMap<String, RDFNode>();
		String queryvariable = null;

		JSONObject jsontemplate = (JSONObject) comobj.get("template");
		String name = (String) jsontemplate.get("name");
		String type = (String) jsontemplate.get("type");
		System.out.println("----+----+Template name:" + name + " and type:" + type);
		JSONArray argums = (JSONArray) jsontemplate.get("args");
		if(argums!=null){
		for (int i = 0; i < argums.size(); i++) {
			JSONObject argu = (JSONObject) argums.get(i);
			String arguname = (String) argu.get("name");
			System.out.println(i + ":----+----+----+Argument name:" + arguname);
			String argutype = (String) argu.get("type");
			System.out.println(i + ":----+----+----+Argument type:" + argutype);
			String arguvalue = (String) argu.get("value");
			System.out.println(i + ":----+----+----+Argument value:" + arguvalue);
			argumentNodeMap = mng.addArgumentNodeToMap(argumentNodeMap, arguname, argutype, arguvalue, arguvalue, null);
		} }

		List<String> queryVars=null; 
		if ("select".equalsIgnoreCase(type)){ 
			JSONArray jsonqueryvars = (JSONArray) jsontemplate.get("queryVars");
			queryVars = (List)jsonqueryvars;
		}
		mng.callTemplateByName(name, argumentNodeMap, type, queryVars); //queryvariable);

	}
	
	public void createNewTemplateCommand(JSONObject comobj) {
		//2016-02-18
		System.out.println("----+ Command: createNewTemplateCommand");
		Map<String, RDFNode> argumentNodeMap = new HashMap<String, RDFNode>();
		Map<String, String> argumentCommentMap = new HashMap<String, String>();
		String queryvariable = null;

		JSONObject subobj = (JSONObject) comobj.get("template");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		
		String namespace = (String) subobj.get("ns");
		String templateURI=namespace+name;
		
		StringBuffer queryStr = new StringBuffer();
		String preprefixes = (String) subobj.get("preprefixes");				
		if ("yes".equalsIgnoreCase(preprefixes)) {
			prefillQueryPrefixList();
			for (int i = 0; i < this.prefixlines.size(); i++)
				queryStr.append(this.prefixlines.get(i));
		}
		
		/*
		 *  Build the Query String for the template
		 *  
		 */
		switch (type) {
		case "select": {	
			// Example:"SELECT * WHERE { ?s rdf:type ?o . } LIMIT 10 "
			queryStr.append(" SELECT " + subobj.get("select") + " WHERE { " + subobj.get("where") + " }");
			String limit = (String) subobj.get("limit");
			if (limit != null)
				queryStr.append(" LIMIT " + limit);
			System.out.println("----QueryString: " + queryStr.toString() );
					
		}
			break;
		case "update": {
			//Delete . 
			if(subobj.get("delete")!=null)
				queryStr.append(" DELETE { " + subobj.get("delete") + " } ");
			if(subobj.get("insert")!=null)
				queryStr.append(" INSERT { " +  subobj.get("insert") + " } ");
			if(subobj.get("where")!=null)
				queryStr.append(" WHERE { " + subobj.get("where") + " }");
			System.out.println("----QueryString: " + queryStr.toString());
		}
			break;
		case "construct": {
			// Example: CONSTRUCT { ?wouri a workcoreplus:WorkOrder . ?wouri
			// workcoreplus:work_order_id 666 . }
			// WHERE { BIND
			// (IRI(fn:concat("http://ssp4t5.net/mimosa/workcoreplus#WorkOrder_",
			// str(666))) AS ?wouri) .}
			String addToModel = (String) subobj.get("resultTriples");
			queryStr.append(" CONSTRUCT {" + subobj.get("construct") + " } WHERE { " + subobj.get("where") + " }");
			System.out.println("----QueryString: " + queryStr.toString());

		}
			break;
		case "describe": {
			queryStr.append(" DESCRIBE " + subobj.get("describe") + " WHERE { " + subobj.get("where") + " }");
			System.out.println("----QueryString: " + queryStr.toString());
		}
		 break;

		default: {
			System.out.println("----+???? Sparql query for template name: " + name + " has UNKNOWN type: " + type + " ?????");
		}
		}
		
		/*
		 *  Build the ArgumentNodeMap for the template
		 */				
		System.out.println("----+----+Template name:" + name + " and type:" + type);
		JSONArray argums = (JSONArray) subobj.get("args");
		if(argums!=null){
		for (int i = 0; i < argums.size(); i++) {
			JSONObject argu = (JSONObject) argums.get(i);
			String arguname = (String) argu.get("name");
			System.out.println(i + ":----+----+----+Argument name:" + arguname);
			String argutype = (String) argu.get("type");
			System.out.println(i + ":----+----+----+Argument type:" + argutype);
			String argucomment = (String) argu.get("argComment");
			System.out.println(i + ":----+----+----+Argument comment:" + argucomment);
			
			argumentNodeMap=mng.addSPLArgumentDeclarationToMap(argumentNodeMap, arguname, argutype);
			argumentCommentMap.put(arguname, argucomment);
		} }

		/*
		 *  Creating the new template
		 */		
		mng.createTemplate(mng.getMainOntModel(), queryStr.toString(), templateURI, argumentNodeMap, argumentCommentMap);
		System.out.println("===== New Template Created ======");
		
	}

	

	public void runInferencesCommand(JSONObject comobj) {
		System.out.println("----+ Command: runInferencesCommand");
		JSONObject subobj = (JSONObject) comobj.get("inference");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		System.out.println("----+----+ name:" + name);
		switch (name) {
		case "pre_inference": {

			if ("iterative".equalsIgnoreCase(type))
				runInferences(false);
			else if ("singlepass".equalsIgnoreCase(type))
				runInferences(true);
			else
				System.out.println("----+????+ type:" + type + " Unknown????");

		}
			break;
		case "post_inference":
			break;
		default: {
			System.out.println("----+????+ name:" + name + " Unknown?????");
		}
		}

	}

	public void execAttachedQueryCommand(JSONObject comobj) {
		//TODO: Only attached Select query exec implemented.
		//Example in class: "http://siima.net/ont/bicycle#Bicycle"
		System.out.println("----+ Command: execAttachedQueryCommand");
		JSONObject subobj = (JSONObject) comobj.get("query");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		String clsuri = (String) subobj.get("classuri");
		Boolean reasoner=(Boolean)subobj.get("reasoner");
		System.out.println("----+----+ name:" + name + "\n----+----+ type:" + type);
		System.out.println("----+----+ class:" + clsuri + "\n----+----+ reasoner:" + reasoner);
		
		if("select".equalsIgnoreCase(type)){
		OntModel querymodel;
		if((reasoner!=null)&&(reasoner)) querymodel = mng.getOntModelWithReasoner();
		else querymodel = mng.getMainOntModel();
		
		JSONArray jsonqueryvars = (JSONArray) subobj.get("queryVars");
		List queryVars = (List)jsonqueryvars;			
		Resource cls = querymodel.getResource(clsuri); //mng.getMainOntModel()
				
		mng.execAttachedQuery(cls, querymodel, queryVars); //mng.getMainOntModel()
		} else System.out.println("????? Only SELECT type Attached query implemented. Type was: " + name ); 
		
	}
	
	public void sparqlQueryCommand(JSONObject comobj) {
		/*
		 * (2016-10-03) 
		 * Imported sub model can be specified as the target model of a query.
		 * This is important for UPDATE and DELETE queries, which cannot delete triples
		 * from imported sub models, if the target is the main model (Bug?)
		 * The URI of the target imported model is given in json object "target_import_uri".
		 * 
		 * Boolean "reasoner" object should be true, if the model with reasoner 
		 * should be used as the target model.
		 */
		//
		//VPA: reasoner option added.
		System.out.println("----+ Command: sparqlQueryCommand");
		JSONObject subobj = (JSONObject) comobj.get("query");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		Boolean reasoner=(Boolean)subobj.get("reasoner");
		String target_import_uri =  (String) subobj.get("target_import_uri"); // e.g. "http://siima.net/ont/accessories"
		System.out.println("----+----+ name:" + name + "\n"
				+ "----+----+ reasoner:" + reasoner + "\n"
						+ "----+----+ IF query target is imported model, its has URI:" + target_import_uri);
		OntModel querymodel;
		
		if((target_import_uri!=null)&&(!"".equalsIgnoreCase(target_import_uri))) querymodel = mng.getMainOntModel().getImportedModel(target_import_uri); 
		else if((reasoner!=null)&&(reasoner)) querymodel = mng.getOntModelWithReasoner();
		else querymodel = mng.getMainOntModel();
		
		
		StringBuffer queryStr = new StringBuffer();
		String preprefixes = (String) subobj.get("preprefixes");
		if ("yes".equalsIgnoreCase(preprefixes)) {
			prefillQueryPrefixList();
			for (int i = 0; i < this.prefixlines.size(); i++)
				queryStr.append(this.prefixlines.get(i));
		}

		switch (type) {
		case "select": {	
			// Example:"SELECT * WHERE { ?s rdf:type ?o . } LIMIT 10 "
			queryStr.append(" SELECT " + subobj.get("select") + " WHERE { " + subobj.get("where") + " }");
			String limit = (String) subobj.get("limit");
			if (limit != null)
				queryStr.append(" LIMIT " + limit);
			// System.out.println("----QueryString: " + queryStr.toString() );
			JSONArray jsonqueryvars = (JSONArray) subobj.get("queryVars");
			List queryVars = (List)jsonqueryvars;			
			mng.sparqlSelectQuery(querymodel, queryStr, queryVars); //mng.getMainOntModel()
			
		}
			break;
		case "update": {
			//Delete TOIMII ainakin bicycle mallissa. 
			if(subobj.get("delete")!=null)
				queryStr.append(" DELETE { " + subobj.get("delete") + " } ");
			if(subobj.get("insert")!=null)
				queryStr.append(" INSERT { " +  subobj.get("insert") + " } ");
			if(subobj.get("where")!=null)
				queryStr.append(" WHERE { " + subobj.get("where") + " }");
			System.out.println("----QueryString: " + queryStr.toString());
			mng.sparqlUpdateQuery(querymodel, queryStr); //mng.getMainOntModel()
		}
			break;
		case "construct": {
			// Example: CONSTRUCT { ?wouri a workcoreplus:WorkOrder . ?wouri
			// workcoreplus:work_order_id 666 . }
			// WHERE { BIND
			// (IRI(fn:concat("http://ssp4t5.net/mimosa/workcoreplus#WorkOrder_",
			// str(666))) AS ?wouri) .}
			String addToModel = (String) subobj.get("resultTriples");
			queryStr.append(" CONSTRUCT {" + subobj.get("construct") + " } WHERE { " + subobj.get("where") + " }");
			System.out.println("----QueryString: " + queryStr.toString());
			if ("add".equalsIgnoreCase(addToModel))
				mng.sparqlConstructQuery(querymodel, mng.getInferredTriples(), queryStr); //mng.getMainOntModel()
			else
				mng.sparqlConstructQuery(querymodel, null, queryStr); //mng.getMainOntModel()
		}
			break;
		case "describe": {
			queryStr.append(" DESCRIBE " + subobj.get("describe") + " WHERE { " + subobj.get("where") + " }");
			System.out.println("----QueryString: " + queryStr.toString());
			mng.sparqlDescribeQuery(querymodel, null, queryStr); // mng.getMainOntModel()
		}
		 break;

		default: {
			System.out.println("----+???? Sparql query by name: " + name + " has UNKNOWN type: " + type + " ?????");
		}
		}

	}

	public void listTemplatesCommand(JSONObject comobj){
		System.out.println("----+ Command: listTemplatesCommand");
		JSONObject subobj = (JSONObject) comobj.get("listing");
		if(subobj!=null){
			String name = (String) subobj.get("name");
			String type = (String) subobj.get("type");
			System.out.println("----+----+listTemplatesCommand name:" + name + " and type:" + type);
			// Listing all templates
			mng.getTemplates();
			// Detailed info 
			JSONArray infolist = (JSONArray) subobj.get("info");
			for(Object nameuriobj : infolist){
				JSONObject jsonnameuri = (JSONObject)nameuriobj;
				String localName=(String)jsonnameuri.get("localname");
				String uri=(String)jsonnameuri.get("uri");
				mng.printTemplateInfo(localName, uri); //String uri)
				
			}		
		}	
	}
	
	public void listFunctionsCommand(JSONObject comobj){
		System.out.println("----+ Command: listFunctionsCommand");
		JSONObject subobj = (JSONObject) comobj.get("listing");
		if(subobj!=null){
			String name = (String) subobj.get("name");
			String type = (String) subobj.get("type");
			System.out.println("----+----+listFunctionsCommand name:" + name + " and type:" + type);
			// Listing all templates
			mng.getFunctions();
			
		}	
	}
	
	public void listNsPrefixesCommand(JSONObject comobj){
		System.out.println("----+ Command: listNsPrefixesCommand");
		JSONObject subobj = (JSONObject) comobj.get("listing");
		if(subobj!=null){
			String name = (String) subobj.get("name");
			String type = (String) subobj.get("type");
			System.out.println("----+----+listNsPrefixesCommand name:" + name + " and type:" + type);
			if("modelprefixes".equalsIgnoreCase(type)){
				System.out.println("\n----+----+ Listing Model Prefixes with Namespaces +----+----+");
			 mng.getNsPrefixeMap(mng.getMainOntModel()); //returns Map<String,String> prefixMap			 
			} else if("sparqlprefixes".equalsIgnoreCase(type)){
				if(!this.prefixlinesfilled) prefillQueryPrefixList();
				System.out.println("\n----+----+ Listing Predefined Sparql query Prefixes with Namespaces +----+----+");
				for(int i=0; i<this.prefixlines.size();i++){
					System.out.println("("+(i+1)+") " + this.prefixlines.get(i));
				}
			}
			
		}	
	}
	
	
	
	public void userPromptCommand(JSONObject comobj){
		System.out.println("----+ Command: userPromptCommand");
		JSONObject subobj = (JSONObject) comobj.get("userprompt");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		String prompttext = (String) subobj.get("prompt");
		String defaults = (String) subobj.get("default");		
		String prompt = "Q" + prompttext + ">>>?";
		String reply=ui.getInputOrDefault(prompt, _input, defaults);
		System.out.println("----+ User Reply: " + reply + "\n-------------------");
	}
	
	public void writeModelCommand(JSONObject comobj) {
		System.out.println("----+ Command: writeModelCommand");
		JSONObject subobj = (JSONObject) comobj.get("write");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type"); // console/file
		String filepath = (String) subobj.get("filePath"); // data/inferences/inferred.ttl
		String modelcat = (String) subobj.get("modelcat"); // inferred/main/mainall/merged
		String format = (String) subobj.get("format"); // https://jena.apache.org/documentation/io/rdf-output.html
		if(format==null) format="TURTLE"; //Default format

		System.out.println("----+----+ KB name:" + name);

		try {

			if (("file".equalsIgnoreCase(type)) && (filepath != null)) {
				FileWriter out = new FileWriter(filepath); // "C:/Temp/test.ttl");
				System.out.println("\n---- Printing Inferred Triples or models to File:" + filepath + " ----\n");
				if ("inferred".equalsIgnoreCase(modelcat))
					mng.getInferredTriples().write(out, format);
				else if ("main".equalsIgnoreCase(modelcat))
					mng.getMainOntModel().write(out, format);
				else if ("mainall".equalsIgnoreCase(modelcat))
					mng.getMainOntModel().writeAll(out, format);
				else if ("merged".equalsIgnoreCase(modelcat)){
					mng.mergeMainModelAndInferredTriples().write(out, format);
				}
					
			} else if ("console".equalsIgnoreCase(type)) {
				System.out.println("\n---- Printing Inferred Triples or models to Console ----\n"
						+ "------------------------------------------------");
				if ("inferred".equalsIgnoreCase(modelcat))
					mng.getInferredTriples().write(System.out, format);
				else if ("main".equalsIgnoreCase(modelcat))
					mng.getMainOntModel().write(System.out, format);
				else if ("mainall".equalsIgnoreCase(modelcat))
					mng.getMainOntModel().writeAll(System.out, format);
				else if ("merged".equalsIgnoreCase(modelcat)){
					mng.mergeMainModelAndInferredTriples().write(System.out, format);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createInferenceModelCommand(JSONObject comobj){
		System.out.println("----+ Command: createInferenceModelCommand");
		JSONObject subobj = (JSONObject) comobj.get("create");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		System.out.println("----+----+ name:" + name);
		mng.createInferredModelAndRegister();
		this.spinRegistryUpdated = true;
		
	}
	/*
	public void createInferredModelAndRegister() {
		System.out.println("\n---- OPERATION: createInferredModelAndRegister ----\n");
		mng.createInferredModelAndRegister();
		this.spinRegistryUpdated = true;
		System.out.println("\n---- --------- ----\n");
	}
	*/
	
	public void loadKnowledgeBaseCommand(JSONObject comobj) {
		System.out.println("----+ Command: loadKnowledgeBaseCommand");
		StringBuffer sbuff= new StringBuffer();
		JSONObject subobj = (JSONObject) comobj.get("knowledgeBase");
		String name = (String) subobj.get("name");
		String type = (String) subobj.get("type");
		System.out.println("----+----+ KB name:" + name);
		
		if("predefined".equalsIgnoreCase(type)){
		
		switch (name) {
		case "context_mimosa": {
			startPredefinedKB_ssp4t5net();
		}
			break;
		case "bicycle": {
			startPredefinedKB_Bicycle();
		}
			break;
		default: {
			System.out.println("----+????+ name:" + name + " Unknown?????");
		}
		}
		} else if("file&uri".equalsIgnoreCase(type)){
			
			JSONObject mainOntology = (JSONObject) subobj.get("ontology");
			JSONArray  imports = (JSONArray)  subobj.get("imports");
			
			String ontname = (String) mainOntology.get("name");
			String ontfolder = (String) mainOntology.get("folder");
			String ontfile = (String) mainOntology.get("file");
			String onturi = (String) mainOntology.get("uri");
			altlocs.add(ontfolder + "/" + ontfile);
			urls.add(onturi);
			
			sbuff.append("-ontname:"+ ontname + " -ontfolder:" + ontfolder + " -ontfile: " + ontfile + " -onturi: " + onturi+ "\n");
			
			if(imports!=null){
			for(Object importobj : imports){				
				JSONObject jsonimport = (JSONObject) importobj;				
				String impname = (String) jsonimport.get("name");
				String impfolder = (String) jsonimport.get("folder");
				String impfile = (String) jsonimport.get("file");
				String impuri = (String) jsonimport.get("uri");				
				if(impfolder==null) impfolder=ontfolder;
				altlocs.add(impfolder + "/" + impfile);
				urls.add(impuri);
				sbuff.append("-impname:"+ impname + " -impfolder:" + impfolder + " -impfile: " + impfile + " -impuri: " + impuri+ "\n");				
				
			}
			}
			System.out.println("\n----------- Knowledge Base Info  ----------\n" + sbuff.toString());
			/* ----------- Build Models ----------*/
			OntModel baseont = mng.loadModelWithImports(urls, altlocs);
			// Set mainOntModel
			mng.setMainOntModel(baseont);
			// Set ntModelWithReasoner
			mng.setOntModelWithReasoner(mng.createReasonerModel(mng
							.getMainOntModel()));
			 
			System.out.println("\n----------- Knowledge Base Ready  ----------");
			
		}
	}
	
	/* **************************
	 * 
	 * CMSCommand selection
	 * 
	 * *************************/
	

	public void runCMSCommand(JSONObject comobj) {
		String ctype = (String) comobj.get("commandType");
		switch (ctype) {
		case "loadKnowledgeBase": {
			if (!kb_loaded)
				loadKnowledgeBaseCommand(comobj);
			kb_loaded = true;
		}
			break;
		case "createInferenceModel": {
			if (kb_loaded)
				createInferenceModelCommand(comobj);
		}
			break;
		case "runInferences": {
		if (kb_loaded)
			runInferencesCommand(comobj);
		}		
			break;
		case "createNewTemplate": {
			if (kb_loaded)
				createNewTemplateCommand(comobj);

		}
			break;
		case "templateCall": {
			if (kb_loaded)
				templateCallCommand(comobj);

		}
			break;
		case "sparqlQuery": {
			if (kb_loaded)
				sparqlQueryCommand(comobj);

		}
		break;
		case "execAttachedQuery": {
		if (kb_loaded)
			execAttachedQueryCommand(comobj);

		} 
		break;
		case "runSpinConstructors": {
		if (kb_loaded)
			runSpinConstructorsCommand(comobj);

		} 
		break; 
		case "checkConstraints": {
			if (kb_loaded)
				checkConstraintsCommands(comobj);

		}
			break;
		case "writeModel": {
			if (kb_loaded)
				writeModelCommand(comobj);

		}
			break;
		case "listTemplates": {
			if (kb_loaded)
				listTemplatesCommand(comobj);

		} 
		break;
		case "listFunctions": {
		if (kb_loaded)
			listFunctionsCommand(comobj);

		}
		break;
		case "listNsPrefixes": {
		if (kb_loaded)
			listNsPrefixesCommand(comobj);
		} 
		break;
		case "userinput": {
		if (kb_loaded)
			userPromptCommand(comobj);

		} 
			break;
		default: {
			System.out.println("???????????Command:" + ctype + " Unknown????????");
		}
		}
	}

	
	
	/*
	 * =======================================
	 * 
	 * from InteractiveSpinMng.java
	 * 
	 * =======================================
	 */

	public void runSpinConstructors(){
		/* RUNS ONLY SPIN:CONSTRUCTORS (not spin rules) */
			
		System.out.println("\n---- OPERATION: Run Constructors ----\n");	
				
		if(!spinRegistryUpdated){ mng.createInferredModelAndRegister(); 
			this.spinRegistryUpdated =true;
		}
		mng.runConstructors(mng.getMainOntModel(), mng.getInferredTriples());
		System.out.println("\n---- --------- ----\n");
	}
	
	
	
	public void runInferences(boolean singlePass) {
		System.out.println("\n---- OPERATION: Run Inferences ----\n");
		mng.createInferredModelAndRegister();
		mng.runAllSpinInferences(singlePass);// singlePass=false: run
												// iteratively
		this.spinRegistryUpdated = true;
		System.out.println("\n---- --------- ----\n");
	}

	public void prefillQueryPrefixList() {

		if (!this.prefixlinesfilled) {
			this.prefixlines = new ArrayList<String>();
			// FOR general and spin ontologies
			this.prefixlines.add("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
			this.prefixlines.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
			this.prefixlines.add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
			this.prefixlines.add("PREFIX sp: <http://spinrdf.org/sp#> ");
			this.prefixlines.add("PREFIX spif: <http://spinrdf.org/spif#> ");
			this.prefixlines.add("PREFIX spin: <http://spinrdf.org/spin#> ");
			this.prefixlines.add("PREFIX spl: <http://spinrdf.org/spl#> ");
			this.prefixlines.add("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ");
			// FOR ssp4t5.net ontologies
			this.prefixlines.add("PREFIX context_mimosa: <http://ssp4t5.net/test/context_mimosa#> ");
			this.prefixlines.add("PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#> ");
			this.prefixlines.add("PREFIX arg: <http://spinrdf.org/arg#> ");
			this.prefixlines.add("PREFIX combined: <http://ssp4t5.net/mimosa/combined#> ");
			this.prefixlines.add("PREFIX diagplus: <http://ssp4t5.net/mimosa/diagplus#> ");
			this.prefixlines.add("PREFIX fn: <http://www.w3.org/2005/xpath-functions#> ");
			this.prefixlines.add("PREFIX mctxcore: <http://ssp4t5.net/context/mctxcore#> ");
			this.prefixlines.add("PREFIX mctxsimple: <http://ssp4t5.net/context/mctxsimple#> ");
			// also
			this.prefixlines.add("PREFIX workcoreplus: <http://ssp4t5.net/mimosa/workcoreplus#> ");
			this.prefixlines.add("PREFIX cmcoreplus: <http://ssp4t5.net/mimosa/cmcoreplus#> ");
			this.prefixlines.add("PREFIX regasset: <http://ssp4t5.net/mimosa/regasset#> ");
			this.prefixlines.add("PREFIX regcore: <http://ssp4t5.net/mimosa/regcore#> ");

			// FOR bicycle ontologies
			this.prefixlines.add("PREFIX accessories: <http://siima.net/ont/accessories#> ");
			this.prefixlines.add("PREFIX bicycle: <http://siima.net/ont/bicycle#> ");
			
			// FOR "external isa-95 clone model
			this.prefixlines.add("PREFIX extproseg: <http://external.net/isa95/prosegmodel#> "); //external.net
			//this.prefixlines.add("PREFIX proxyproseg: <http://ssp4t5.net/proxy/isa95/prosegmodel#> ");
			this.prefixlines.add("PREFIX proxy: <http://ssp4t5.net/external/proxy#> "); 

			this.prefixlinesfilled = true;
		}
	}
	public void startPredefinedKB_Bicycle(){
		/* --- Main ontology --- */
		String ont_folder = "data/models/importing_models"; 
		
		//VPA May2019 change:
		String main_ont_file= "bicycle_may2019.ttl"; //bicycle.ttl
		String main_ont_url="http://siima.net/ont/bicycle"; 
		
		altlocs.add(ont_folder + "/" + main_ont_file);
		urls.add(main_ont_url);
		
		/* --- Imported ontologies --- */		
		String imp_ont_file="accessories.ttl"; 
		String imp_ont_url= "http://siima.net/ont/accessories"; 
		
		altlocs.add(ont_folder + "/" + imp_ont_file);
		urls.add(imp_ont_url);
		
		/* VPA May 2019 FOLLOWING ADDITIONS NEEDED because spin ontology web addresses have been changed 
		 * Location is now https:// (earlier http://) but the URI is still with http://
		 * TESTING with data/json/csmCommands_bicycle_store_test3_update.json
		 * 
		 * NOW web locations start with https (however uris are not changed):
		 * https://spinrdf.org/spl
		 * https://spinrdf.org/spin
		 * https://spinrdf.org/sp
		 * 
		 * ADDED also 28May2019
		 * http://topbraid.org/spin/owlrl-all
		 * http://topbraid.org/spin/rdfsplus
		 * http://topbraid.org/spin/spinowl
		*/
		
		altlocs.add("etc/spl.spin.ttl");
		urls.add("http://spinrdf.org/spl");
		
		altlocs.add("etc/spin.ttl");
		urls.add("http://spinrdf.org/spin");
		
		altlocs.add("etc/sp.ttl");
		urls.add("http://spinrdf.org/sp");
		
		
		altlocs.add("siima/topbraid/ontologies/owlrl-all.ttl");
		urls.add("http://topbraid.org/spin/owlrl-all");
		
		altlocs.add("siima/topbraid/ontologies/rdfsplus.ttl");
		urls.add("http://topbraid.org/spin/rdfsplus");
		
		altlocs.add("siima/topbraid/ontologies/spinowl.ttl");
		urls.add("http://topbraid.org/spin/spinowl");
		
		
		 
		
		/* TESTING adding following to solve: PROBLEM: data/json/csmCommands_bicycle_store_test3_update.json 
		 * command 9: name:SELECT Query SUPERCLASS Vehicles
		 * solution: set "reasoner": true in JSON query-object
		 * 
		 * 
		altlocs.add("etc/rdf-schema.rdf");
		urls.add("http://www.w3.org/2000/01/rdf-schema#");
		*/
		
		/* ----------- Build Models ----------*/
		//VPA: 2016-02-15 Testing a new Model Build  methods
		//now in dump.txt: OntModel baseont = mng.test();
		//now in dump.txt: OntModel baseont = mng.loadModelWithSubModels(urls, altlocs); //EI TOIMI
		OntModel baseont = mng.loadModelWithImports(urls, altlocs);
		// Set mainOntModel
		mng.setMainOntModel(baseont);
		// Set ntModelWithReasoner
		
		mng.setOntModelWithReasoner(mng.createReasonerModel(mng.getMainOntModel()));
		
		this.mainModelLocalName="bicycle"; 
		System.out.println("\n----------- Knowledge Base Ready  ----------");
		
	}
	
	
	private void startPredefinedKB_ssp4t5net() {
		/* --- Main ontology --- */
		String ont_folder = "data/models/ssp4t5net";

		String main_ont_file = "context_mimosa.ttl";
		String main_ont_url = "http://ssp4t5.net/test/context_mimosa";

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

		/* ----------- Build Models ---------- */
		OntModel baseont = mng.loadModelWithImports(urls, altlocs);
		// Set mainOntModel
		mng.setMainOntModel(baseont);
		// Set ntModelWithReasoner
		mng.setOntModelWithReasoner(mng.createReasonerModel(mng.getMainOntModel()));

		this.mainModelLocalName = "context_mimosa";
		System.out.println("\n----------- Knowledge Base Ready  ----------");

	}

	/*
	 * =======================================
	 * 
	 *  MAIN
	 * 
	 * =======================================
	 */
	
	public static void main(String[] args) {

		CommandFileSpinMng csm = new CommandFileSpinMng();
		InteractiveSpinMng ism = new InteractiveSpinMng();
		BufferedReader _input = new BufferedReader(new InputStreamReader(
				System.in));
		boolean kb_loaded = false;
		StringBuffer strbuff = new StringBuffer();
		StringBuffer stepnotebuffer = new StringBuffer();
		
		try {
			System.out.println("================ CommandFileSpinMng running... ===============\n");
			System.out.println("\n------------ Command Workflow file?  -----------");
			String prompt = ">>>> Give Workflow filepath ?>";
			String defaults = "data/json/csmCommands.json"; 
			String ont_folder=ism.ui.getInputOrDefault(prompt, _input, defaults);
			
			
			JSONObject jsonrootobj = (JSONObject) csm.parser.parse(new FileReader(ont_folder));
			JSONObject header = (JSONObject) jsonrootobj.get("CSMHeader");
			
			strbuff.append("--------------------------------------------------\n"
					+ "+'CSMHeader':\n+----+'filename': '" + header.get("filename") + "'\n"
					+ "+----+'created': '" + header.get("created") + "'\n"
					+ "+----+'updated': '" + header.get("updated") + "'\n"
					+ "+----+'history': '" + header.get("history") + "'\n"
					+ "--------------------------------------------------\n");
			System.out.println("\n" + strbuff.toString());
			
			JSONObject workflow = (JSONObject) header.get("workflow");
			String wftype = (String) workflow.get("type");
			System.out.println("--Reading commands from csm command file (json) ... \n\n");
			JSONArray commands = (JSONArray) jsonrootobj.get("CSMCommands");

			if ("indexed".equalsIgnoreCase(wftype)) {
				JSONArray indexes = (JSONArray) workflow.get("indexes");
				for (Object indobj : indexes) {
					Number ind = (Number) indobj;
					JSONObject comobj = (JSONObject) commands.get(ind.intValue());
					String stepnote = (String) comobj.get("stepnote");
					stepnotebuffer.append("->| " + stepnote + "(" + ind.intValue() + ") |-");
					System.out.println("\n\n========== Next Command Index:(" + ind + "): " + stepnote + "========== ");					
					csm.runCMSCommand(comobj);
				}
			} else if ("all".equalsIgnoreCase(wftype)) {
				/* running all Commands */

				for (int ci = 0; ci < commands.size(); ci++) {

					JSONObject comobj = (JSONObject) commands.get(ci);
					String ctype = (String) comobj.get("commandType");
					System.out.println("----+ Command type:" + ctype);
					csm.runCMSCommand(comobj);
				}
			} else {
				System.out.println("???????????Workflow type:" + wftype + " Unknown????????");
			} // end if
			
			System.out.println("\n\n========== WORKFLOW STEPS LOG ==========\n");
			System.out.println(" (start)-" + stepnotebuffer.toString() + "->(end)");					
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
