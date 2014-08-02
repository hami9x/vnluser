package rfx.server.util;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;

public class EvalGroovyScript {
	
	static void runGroovyScript(String scriptpath){
		try {
			int li = scriptpath.lastIndexOf("/");
			String root = "", scriptname = scriptpath;
			if( li>= 0){
				root  = scriptpath.substring(0, li);
				scriptname = scriptpath.substring(li+1);
			}
			
			String[] roots = new String[] { root };
			GroovyScriptEngine gse = new GroovyScriptEngine(roots);
			Binding binding = new Binding();		
			gse.run(scriptname, binding);
		} catch (IOException e) {			
			System.out.println(e.toString());
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}		

	public static void main(String[] args) {	
		//args = new String[] {"script/InitLocationData.groovy"};
		//args = new String[] {"script/InitCountryLocationData.groovy"};
		if(args.length == 1){
			runGroovyScript(args[0]);
		} else {
			System.err.println("missing param groovy script name!");
		}
	}
}
