import syntaxtree.*;
import visitor.*;
import java.util.*;
import symboltable.*;

public class SymbolTableBuilderVisitor extends GJNoArguDepthFirst <String> {
  SymbolTable table;
  /*we need to keep track of the class and function we are in, in order to store the variables and functions
  in the right place of the symbol table*/
  ClassInfo currentclass;
  FunctionInfo currentfunction;
  /*variable and function offsets are needed to produce LLVM code*/
  int functionoffset;
  int variableoffset;
  //offset flag is used to determine if we are in an extended class or just a regular one
  boolean offsetflag;

  
  public SymbolTableBuilderVisitor() {
    currentclass = null;
    currentfunction = null;
    table = new SymbolTable();
    functionoffset = 0;
    variableoffset = 0;
    offsetflag = false;
    
  }

  public SymbolTable getSymbolTable() {
    return table;
  }

  /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
  @Override
  public String visit(MainClass n) throws Exception {
      String cname = n.f1.f0.toString();
      if (table.addClass(cname,null) == false)
        throw new Exception("There is already a class with the same name");
      String fname = n.f6.toString();
      String type = n.f5.toString();
      ClassInfo cl = table.getClass(cname);
      if (cl.addFunction(fname,type) == false)
        throw new Exception("There is already a function with the same name");
      currentclass = cl;
      currentfunction = cl.getFunction(fname);
      String argname = n.f11.f0.toString();
      
      currentfunction.addParameter(argname,"String[]");

      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      n.f11.accept(this);
      n.f12.accept(this);
      n.f13.accept(this);
      n.f14.accept(this);
      n.f15.accept(this);
      n.f16.accept(this);
      n.f17.accept(this);
      currentclass=null;
      currentfunction=null;
      return null;
  }

/**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */

  @Override
  public String visit(ClassDeclaration n) throws Exception {
    if(currentclass != null || currentfunction != null)
      throw new Exception("Cannot declare class inside of a function or class");
    String cname = n.f1.f0.toString();
    if (table.addClass(cname,null) == false)
      throw new Exception("There is already a class with the same name");
    currentclass = table.getClass(cname);  
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    currentclass=null;
    currentfunction=null;
    variableoffset = 0;
    functionoffset = 0;
    return null;
  }

  /**
  * f0 -> "class"
  * f1 -> Identifier()
  * f2 -> "extends"
  * f3 -> Identifier()
  * f4 -> "{"
  * f5 -> ( VarDeclaration() )*
  * f6 -> ( MethodDeclaration() )*
  * f7 -> "}"
  */


  @Override
  public String visit(ClassExtendsDeclaration n) throws Exception {
    offsetflag = true;
    if(currentclass != null || currentfunction != null)
      throw new Exception("Cannot extend class inside of a function or class");
    String cname = n.f1.f0.toString();
    /*//System.out.println("the name is " + cname);*/
    String extendname = n.f3.f0.toString();
    
    if (table.classExists(extendname) == false)
      throw new Exception("Cannot extend a non existent class");

    if (table.addClass(cname,extendname) == false)
      throw new Exception("There is already a class with the same name");
    currentclass = table.getClass(cname);
    currentclass.variableoffset = table.getClass(currentclass.getParentName()).variableoffset;
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
    n.f7.accept(this);
    currentclass=null;
    currentfunction=null;
    variableoffset = 0;
    functionoffset = 0;
    offsetflag = false;
    return null;
  }

/**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */

@Override
  public String visit(VarDeclaration n) throws Exception {
    if(currentclass == null)
      throw new Exception("Cannot declare variables outside of a class");
    
    
    
    
    String type = n.f0.accept(this);
    String variable = n.f1.f0.toString();
    if(currentfunction == null) {
      if(currentclass.addVariable(variable, type) == false)
        throw new Exception("Cannot add variable with the same name");
    }
    else {
      if(currentfunction.addVariable(variable, type) == false)
        throw new Exception("Cannot add variable with the same name");
    }
    if (currentfunction == null) {
      
      if (offsetflag == true && currentclass.variableoffset == 0) {
        //currentclass = table.getClass(currentclass.getParentName());

        variableoffset = table.getClass(currentclass.getParentName()).variableoffset;
        //currentclass.variableoffset += variableoffset;
        //System.out.println(currentclass.getName() + "starts from variable offset " + currentclass.variableoffset);
      }
      int offset = variableoffset;
      if (type.equals("int")) {
        variableoffset += 4;
        currentclass.variableoffset += 4;
      }
      else if (type.equals("boolean")) {
        variableoffset += 1;
        currentclass.variableoffset += 1;
       }
      else if (type.equals("array")) {
        variableoffset += 8;
        currentclass.variableoffset += 8;
       }
      else {
        variableoffset += 8;
        currentclass.variableoffset += 8;
       }
      if (currentclass.variableExists(variable)) {
        VariableInfo vartemp = currentclass.getVariable(variable);
        vartemp.setOffset(offset);
      }
      //else 
        //System.out.println("problem"); 
      
     // System.out.println(currentclass.getName() + "." + variable + " : " + offset);
    }
    n.f1.accept(this);
    n.f2.accept(this);
    
    return null;
  }

  /**
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */

  @Override 
  public String visit(MethodDeclaration n) throws Exception {
    if(currentclass == null)
      throw new Exception("Cannot declare function outside of class");
    if (currentfunction != null)
      throw new Exception("Cannot declare function in the inside of another function");  
    n.f0.accept(this);
   
    String type = n.f1.accept(this);
    
    String functionname = n.f2.f0.toString();



    if(currentclass.addFunction(functionname,type) == false)
      throw new Exception("Cannot add function with the same name");
    currentfunction = currentclass.getFunction(functionname);
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    boolean flag =false;
    if (currentclass.getParentName() != null) {
      ClassInfo parentClass = table.getClass(currentclass.getParentName());
      FunctionInfo parentFunction;
      //check if function already exists in a parent class
      if ((parentFunction = table.getRecursiveFunctionCheck(parentClass,functionname)) != null) {
        flag = true;
        currentfunction.setOffset(parentFunction.getOffset());

        //current class function offset starts for the function offset of its parent class because it is extended
        if (currentclass.functionoffset == 0)
          currentclass.functionoffset = parentClass.functionoffset;
        if (parentFunction.getType().equals(currentfunction.getType()) == false)
          throw new Exception("Cannot overload a class");
        ArrayList<VariableInfo> parentParameters = parentFunction.getParameters();
        ArrayList<VariableInfo> currentParameters = currentfunction.getParameters();
        if (currentParameters.size() != parentParameters.size())
          throw new Exception("Cannot overload a class");
        for (int i = 0;i < currentParameters.size();++i) {
          if (parentParameters.get(i).getType().equals(currentParameters.get(i).getType()) == false) 
            throw new Exception("Cannot overload a class");
           /*if (parentParameters.get(i).getName().equals(currentParameters.get(i).getName()) == false)
            throw new Exception("Cannot overload a class");*/
        }
      }  
    }

    if (flag == false) {
      if (offsetflag == true && currentclass.functionoffset == 0) {
        //currentclass = table.getClass(currentclass.getParentName());
        functionoffset = table.getClass(currentclass.getParentName()).functionoffset;
        currentclass.functionoffset += functionoffset;
        currentfunction.setOffset(functionoffset);
      }
      int offset = functionoffset;
      functionoffset += 1;
      currentclass.functionoffset += 1;
      currentfunction.setOffset(offset);
      
      //System.out.println(currentclass.getName() + "." + functionname + " : " + offset);

    }
    n.f6.accept(this);
    n.f7.accept(this);
    n.f8.accept(this);
    n.f9.accept(this);
    n.f10.accept(this);
    n.f11.accept(this);
    n.f12.accept(this);
    currentfunction = null;
    return null;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   */
  @Override
  public String visit(FormalParameter n) throws Exception {
    if(currentclass == null || currentfunction == null)
      throw new Exception("Cannot have a parameter outside of a function");
    
    String type = n.f0.accept(this);
    String parametername = n.f1.f0.toString();
    
    
    if(currentfunction.addParameter(parametername,type) == false)
      throw new Exception("Cannot add 2 or more parameters with the same name");
    n.f1.accept(this);
    return null;
  }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
  @Override
   public String visit(ArrayType n) throws Exception {
      
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return "array";
   }

   /**
    * f0 -> "boolean"
    */
   @Override
   public String visit(BooleanType n) throws Exception {
      return "boolean";
   }

   /**
    * f0 -> "int"
    */
   @Override
   public String visit(IntegerType n) throws Exception {
      return "int";
   }

   /**
    * f0 -> <IDENTIFIER>
    */

  @Override
   public String visit(Identifier n) throws Exception {
      n.f0.accept(this);
      return n.f0.toString();

   }

}