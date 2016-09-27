
/*
 * from ExcelToOwl 2013
 * 
 */
package siima.utils;

import java.io.BufferedReader;
import java.io.IOException;

public class UIPrompt {
	
	
	public Integer getInputNumber(String prompt, BufferedReader _input, String defaults){
		/* press 'enter' to select default value
		 * press 'space' to skip and return 'null'
		 * write text + enter to return text answer 
		 */
		
		String num=getInputOrDefault( prompt+" (integer assumed)",  _input,  defaults);
		//System.out.println("TEST getInputNumber: (" + num + ")");
		return Integer.valueOf(num);
		
		
	}
	
	
	public String getInputOrDefault(String prompt, BufferedReader _input, String defaults){
		/* press 'enter' to select default value
		 * press 'space' or write 'null' to skip and return 'null'
		 * write text + enter to return text answer 
		 */
		
		String term="?";
		try {
			System.out.println(prompt + " (Default:" + defaults + ")");
			term = _input.readLine();
			if ("".equalsIgnoreCase(term)) {
				term = defaults;
				System.out.println("A:Default selected." + "\n---");
			} else if((" ".equalsIgnoreCase(term))||("null".equalsIgnoreCase(term))) {
				term = null;
				System.out.println("A:Nothing selected." + "\n---");
			} else {	
				System.out.println("A:" + term + "\n---");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return term;
		
	}
	
	public String getUserInput(String prompt, BufferedReader _input)
			throws IOException {
		// NOte: removes spaces infront or at the end (trim)
		System.out.println(prompt); // + " (or ENTER for none)");

		String line = _input.readLine();

		if (line.trim().equals(""))
			line = null;

		return line;
	}
	

}
