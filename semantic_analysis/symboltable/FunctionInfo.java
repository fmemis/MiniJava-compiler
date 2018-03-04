package symboltable;
import syntaxtree.*;
import java.util.*;

public class FunctionInfo {
	String name;
	String type;
	LinkedHashMap<String,VariableInfo> variables;
	ArrayList<VariableInfo> parameters;
	public int regcount;
	int offset;
	

	public FunctionInfo(String name,String type) {
		this.name = name;
		this.type = type;
		variables = new LinkedHashMap<String,VariableInfo>();

		parameters = new ArrayList<VariableInfo>();
		regcount = 0;
		offset = 0;
		
	}

	public String addRegister() {
		String reg = "%_" + regcount;
		++regcount;
		return reg;
	}




	public boolean variableExists(String name) {
		return (parameterExists(name) || variables.containsKey(name));
	}

	public boolean addVariable(String name,String type) {
		if (variableExists(name))
			return false;
		VariableInfo variable = new VariableInfo(name,type);
		variables.put(name,variable);
		return true;
	}

	public boolean parameterExists(String name){
		for (VariableInfo var : parameters) {
			if (var.getName().equals(name))
				return true;
		}
		return false;
	}

	public boolean addParameter(String name,String type) {
		if (parameterExists(name))
			return false;
		VariableInfo variable = new VariableInfo(name,type);
		parameters.add(variable);
		return true;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public VariableInfo getVariable(String name) {
		return variables.get(name);
	}

	public VariableInfo getParameter(String name) {
		for (VariableInfo var : parameters) {
			if (var.getName().equals(name))
				return var;
		}
		return null;
	}

	public int getOffset() {
    	return offset;
  	}

	public void setOffset(int offset) {
	    this.offset = offset;
	}


	public ArrayList<VariableInfo> getParameters() {
		return parameters;
	}

	public void printEverything() {
		System.out.println("Function name is " + this.name + " with type " + this.type + "and offset " + this.offset);
		for (VariableInfo var : parameters);
			//System.out.println("parameter is " + var.getName() + " with type " + var.getType());
		for (String key : variables.keySet()); 
			//System.out.println("Variable is " + key + " with type " + variables.get(key).getType());
		
	}

	public String printVTable(String classname) {
		String llfile = "";
		String type = "";
		if (getType().equals("int"))
			type += "i32";
		else if ((getType().equals("boolean")))
			type += "i1";
		else if ((getType().equals("array")))
			type += "i32*";
		else
			type += "i8*";

		llfile += "i8* bitcast (" + type + " (i8*";
		for (VariableInfo var : parameters) {
			String type2 = "";
			if (var.getType().equals("int"))
				type2 += "i32";
			else if ((var.getType().equals("boolean")))
				type2 += "i1";
			else if ((var.getType().equals("array")))
				type2 += "i32*";
			else
				type2 += "i8*";
			llfile += "," + type2;
		}
		llfile += ")* @" + classname + "." + name + " to i8*)";
		return llfile;
	}	

}