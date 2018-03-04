package symboltable;
import java.util.*;


public class SymbolTable {
	LinkedHashMap <String,ClassInfo> classes;

	public SymbolTable() {
		classes = new LinkedHashMap<String,ClassInfo>();
	}
	
	public boolean classExists(String name) {
		/*System.out.println("Class name: " + name);*/
		return classes.containsKey(name);
	}

	public boolean addClass(String name,String parentName) {
		if (classExists(name))
			return false;
		ClassInfo newclass = new ClassInfo(name,parentName);
		classes.put(name, newclass);
		return true;
	}

	public ClassInfo getClass(String name) {
		return classes.get(name);
	}

	public int getNumberOfClasses() {
		return classes.size();
	}

	public LinkedHashMap getClasses() {
		return classes;
	}
	public void printSymbolTable() {
		for (String key : classes.keySet()) {
			classes.get(key).printClass();
		}
	}

	//check if the given class "current" has a variable with name "name".If not recursively check all it's parents until you reach a class with no parent.
	public VariableInfo getRecursiveVariableCheck(ClassInfo current,String name) {
		VariableInfo variable;
		if ((variable = current.getVariable(name)) != null)
			return variable;
		String pname = current.getParentName();
		if (pname != null)
			current = getClass(pname);
		else 
			return null;
		variable = getRecursiveVariableCheck(current,name);
		return variable;
	}

	//check if the given class "current" has a function with name "fname".If not recursively check all it's parents until you reach a class with no parent.
	public FunctionInfo getRecursiveFunctionCheck(ClassInfo current,String fname) {
		FunctionInfo function;
		if ((function = current.getFunction(fname)) != null)
			return function;

		String pname = current.getParentName();
		if (pname != null)
			current = getClass(pname);
		else 
			return null;
		
		function = getRecursiveFunctionCheck(current,fname);
		return function;
	}

	//only needed for printing llvm code
    /*in the Vtable of a class we also have the functions of its parent classes.They must also be in the same place they are in the parent's Vtable otherwise
    otherwise inheritance wouldn't work properly.However if a function of a parent class gets overrided in a child class we want the version of the child class
    in the Vtable.This function ensures that we wll print the version we need*/
	public void recursivefcheck(LinkedHashMap<String, recHelp> map,ClassInfo current) {
		String pname ;

		if ((pname = current.getParentName()) != null) {
			ClassInfo newclass = getClass(pname);
			recursivefcheck(map,newclass);
		}
			
		
		LinkedHashMap<String,FunctionInfo> functions = current.getFunctions();
		//system.out.println("in class " + current.getName());
		for (String key : functions.keySet()) {
			FunctionInfo function = current.getFunction(key);
			
			//if we haven't encoutered the function already put her in the hasmap 
			if (map.get(key) == null) {
				recHelp help = new recHelp();
				help.name = current.getName() + "." + key;
				help.func = function;
				map.put(key,help);
			}
			//we have already encountered the function but its being overriden.Change the function type.
			else {
				recHelp help = map.get(key);
				help.name = current.getName() + "." + key;
				help.func = function;

			}
		}
		return;
	}

	
	//only needed for printing llvm code
	public String printVTable() {
	    String llfile="";
	    for (String key : classes.keySet()) {
	      	String parentFunctions;
	      	//system.out.println(key);
	      	ClassInfo cl = getClass(key);
	      	llfile += "@." + cl.getName() + "_vtable = global [" + cl.functionoffset + " x i8*] [";
			int count = 0;
			//system.out.println(" eimai h " + cl.getName());
			LinkedHashMap<String, recHelp> map = new LinkedHashMap<String, recHelp>();
			recursivefcheck(map,cl);

			for (String key2 : map.keySet()) {
				if (count != 0)
					llfile += ", ";
				//main is a special case in minijava
				if (key2.equals("main")) {
					llfile = llfile.replace('1','0');
					break;
				}
				recHelp help;
				help = map.get(key2);
				FunctionInfo function = help.func;
				String type = typeTransform(function.getType());
				llfile += "i8* bitcast (" + type + " (i8*";
				for (VariableInfo var : function.getParameters()) {
					String type2 = typeTransform(var.getType());
					llfile += "," + type2;
				}
				llfile += ")* @" +  help.name + " to i8*)";
				++count;
			}
			llfile += "]";
	      	llfile += "\n";
	    }
	    
	    return llfile;
  	}

    //transform minijava types to llvm code types.
	public String typeTransform(String type) {
		String lltype = "";
		if (type.equals("int"))
			lltype += "i32";
		else if ((type.equals("boolean")))
		    lltype += "i1";
		else if ((type.equals("array")))
			lltype += "i32*";
		else
		    lltype += "i8*";
		return lltype;
	}

	/*check if two objects are allowed to interact.They must be of the same type or of the same family of types(inheritance,the reason we need recursive check,
		and we don't just check if type1 == type2)*/
	public boolean typeCheck(String type1, String type2) {  
		if (type1.equals(type2)) {
			return true;
		}
		ClassInfo cl;
		if ((cl = getClass(type2)) == null)
			return false;
		if (cl.parentName == null) return false;
		return typeCheck(type1, cl.parentName);
	}
}