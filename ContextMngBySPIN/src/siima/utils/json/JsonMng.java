/*
 * Links:
 * 0: http://www.tutorialspoint.com/json/json_java_example.htm
 * 
 * 1. http://stackoverflow.com/questions/10926353/how-to-read-json-file-into-java-with-simple-json-library
 * 2. https://code.google.com/archive/p/json-simple/wikis/DecodingExamples.wiki
 * 
 */

package siima.utils.json;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonMng {

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();

		try {

			/* Reading ISM commands */

			Object obj = parser.parse(new FileReader("data/json/csmCommands.json"));
			JSONObject jsonrootobj = (JSONObject) obj;
			// System.out.println(jsonrootobj.toJSONString());
			JSONArray commands = (JSONArray) jsonrootobj.get("CSMCommands");
			for (int ci = 0; ci < commands.size(); ci++) {
				JSONObject comobj = (JSONObject) commands.get(ci);
				String ctype = (String) comobj.get("commandType");
				System.out.println("----+Command type:" + ctype);
				switch (ctype) {
				case "templateCall": {
					System.out.println("============Command: templateCall==========");
					JSONObject template = (JSONObject) comobj.get("template");
					String tname = (String) template.get("name");
					System.out.println("----+----+Template name:" + tname);
					JSONArray argums = (JSONArray) template.get("args");
					// JSONObject argu = (JSONObject) argums.get(0);
					for (int i = 0; i < argums.size(); i++) {
						JSONObject argu = (JSONObject) argums.get(i);
						String arguname = (String) argu.get("name");
						System.out.println(i + ":----+----+----+Argument name:" + arguname);
						String argutype = (String) argu.get("type");
						System.out.println(i + ":----+----+----+Argument type:" + argutype);
						String arguvalue = (String) argu.get("value");
						System.out.println(i + ":----+----+----+Argument value:" + arguvalue);
					}
				}
					break;
				case "loadKnowledgeBase": {
					System.out.println("============Command: loadKnowledgeBase==========");
				}
					break;
				case "runInferences": {
					System.out.println("============Command: runInferences==========");
				}
					break;
				default: {
					System.out.println("============Command:" + ctype + " Unknown==========");
				}
				}
			}
			/*
			 * Object obj = parser.parse(new
			 * FileReader("data/json/file1.json"));
			 * 
			 * JSONObject jsonObject = (JSONObject) obj;
			 * 
			 * String name = (String) jsonObject.get("blogURL"); //"name");
			 * System.out.println(name);
			 * 
			 * String city = (String) jsonObject.get("twitter"); //"city");
			 * System.out.println(city);
			 * 
			 * JSONObject social = (JSONObject) jsonObject.get("social");
			 * System.out.println(social.toJSONString());
			 * System.out.println(social.toString()); String rss = (String)
			 * social.get("rss"); System.out.println(rss);
			 * 
			 * 
			 * // loop array JSONArray cars = (JSONArray)
			 * jsonObject.get("cars"); Iterator<String> iterator =
			 * cars.iterator(); while (iterator.hasNext()) {
			 * System.out.println(iterator.next()); }
			 * 
			 * JSONArray nums = (JSONArray) jsonObject.get("numbers"); Number
			 * oneobj= (Number)nums.get(1); System.out.println("One is:" +
			 * oneobj.intValue());
			 */

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
