package symboltable;
import syntaxtree.*;
import java.util.*;

public class ClassInfo {
	String name;
	String parentName;
	LinkedHashMap<String,FunctionInfo> functions;
	LinkedHashMap<String,VariableInfo> variables;
	public int variableoffset;
	public int functionoffset;

	public ClassInfo(String name,String parentName) {
		this.name = name;
		this.parentName = parentName;
		functions = new LinkedHashMap<String,FunctionInfo>();
		variables = new LinkedHashMap<String,VariableInfo>();
		variableoffset = 0;
		functionoffset = 0;

	}

	public boolean functionExists(String name) {
		return functions.containsKey(name);
	}

	public boolean addFunction(String name,String type) {
		if (functionExists(name))
			return false;
		FunctionInfo function = new FunctionInfo(name,type);
		functions.put(name,function);
		return true;
		
	}

	public boolean variableExists(String name) {
		return variables.containsKey(name);
	}

	public boolean addVariable(String name,String type) {
		if (variableExists(name))
			return false;
		VariableInfo variable = new VariableInfo(name,type);
		variables.put(name,variable);
		return true;
		
	}

	public String getName() { 
		return name; 
	}

	public String getParentName() {
		return parentName;
	}



	public LinkedHashMap<String,FunctionInfo> getFunctions() {
		return functions;
	}


	public FunctionInfo getFunction(String name) {
		return functions.get(name);
	}

	public VariableInfo getVariable(String name) {
		return variables.get(name);
	}

	public int getNumberOfFunctions() {
		return functions.size();
	}

	public void printClass() {
		System.out.println("Classname and parentname are " + this.name + " " + this.parentName);
		for (String key : variables.keySet()) {
			System.out.println("Variable is " + key + " with type " + variables.get(key).getType() + " and offset " + variables.get(key).getOffset());
		}
		for (String key : functions.keySet()) {
			functions.get(key).printEverything();
		}
	}

	/*public String printVTable() {
		String llfile="";
		llfile += "@." + name + "_vtable = global [" + getNumberOfFunctions() + " x i8*] [";
		int count = 0;
		for (String key : functions.keySet()) {
			if (count != 0)
				llfile += ", ";
			System.out.println(key);
			if (key.equals("main")) {
				llfile = llfile.replace('1','0');
				break;
			}
			FunctionInfo function = getFunction(key);
			llfile += function.printVTable(name);
			++count;
		}
		llfile += "]";
		return llfile;
	}*/

}