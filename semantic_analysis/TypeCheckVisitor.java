import syntaxtree.*;
import visitor.*;
import java.util.*;
import symboltable.*;


public class TypeCheckVisitor extends GJDepthFirst <String, MyStringList> {
  SymbolTable table;
  ClassInfo currentclass;
  FunctionInfo currentfunction;

  public TypeCheckVisitor(SymbolTable stable) {
    table = stable;
    currentfunction = null;
    currentclass = null;
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
  public String visit(MainClass n,MyStringList argu) throws Exception {
      String cname = n.f1.f0.toString();
      currentclass = table.getClass(cname);
      String fname = n.f6.toString();
      currentfunction = currentclass.getFunction(fname);

      n.f0.accept(this,argu);
      n.f1.accept(this,argu);
      n.f2.accept(this,argu);
      n.f3.accept(this,argu);
      n.f4.accept(this,argu);
      n.f5.accept(this,argu);
      n.f6.accept(this,argu);
      n.f7.accept(this,argu);
      n.f8.accept(this,argu);
      n.f9.accept(this,argu);
      n.f10.accept(this,argu);
      n.f11.accept(this,argu);
      n.f12.accept(this,argu);
      n.f13.accept(this,argu);
      n.f14.accept(this,argu);
      n.f15.accept(this,argu);
      n.f16.accept(this,argu);
      n.f17.accept(this,argu);
      currentclass=null;
      currentfunction=null;
      return null;
  }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public String visit(TypeDeclaration n, MyStringList argu) throws Exception {
      n.f0.accept(this, argu);
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
  public String visit(ClassDeclaration n,MyStringList argu) throws Exception {
    String cname = n.f1.f0.toString();
    currentclass = table.getClass(cname);  
    n.f0.accept(this,argu);
    n.f1.accept(this,argu);
    n.f2.accept(this,argu);
    n.f3.accept(this,argu);
    n.f4.accept(this,argu);
    n.f5.accept(this,argu);
    currentclass=null;
    currentfunction=null;
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
  public String visit(ClassExtendsDeclaration n,MyStringList argu) throws Exception {
    String cname = n.f1.f0.toString();
    currentclass = table.getClass(cname);
    n.f0.accept(this,argu);
    n.f1.accept(this,argu);
    n.f2.accept(this,argu);
    n.f3.accept(this,argu);
    n.f4.accept(this,argu);
    n.f5.accept(this,argu);
    n.f6.accept(this,argu);
    n.f7.accept(this,argu);
    currentclass=null;
    currentfunction=null;
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
  public String visit(MethodDeclaration n,MyStringList argu) throws Exception {
    String functionname = n.f2.f0.toString();
    currentfunction = currentclass.getFunction(functionname);    
    n.f2.accept(this,argu);
    n.f3.accept(this,argu);
    n.f4.accept(this,argu);
    n.f5.accept(this,argu);
    n.f6.accept(this,argu);
    n.f7.accept(this,argu);
    n.f8.accept(this,argu);
    n.f9.accept(this,argu);
    String returntype = n.f10.accept(this,argu);
    if (table.typeCheck(currentfunction.getType(),returntype) == false)
        throw new Exception("Wrong return type");
    n.f11.accept(this,argu);
    n.f12.accept(this,argu);
    currentfunction = null;
    return null;
  }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   @Override 
   public String visit(AssignmentStatement n, MyStringList argu) throws Exception {
      String name = n.f0.f0.toString();
      VariableInfo var;
      if ((var = currentfunction.getVariable(name)) ==null) {
          if ((var = currentfunction.getParameter(name)) == null) {
             if ((var = currentclass.getVariable(name)) == null) {
              String tempname = currentclass.getParentName();
              if (tempname == null)
                throw new Exception("Variable is not declared" + " " + name);
              ClassInfo temp = table.getClass(tempname);
              if ((var = table.getRecursiveVariableCheck(temp,name)) == null)
                throw new Exception("Variable is not declared" + " " + name);
            }
          }
      }
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type = n.f2.accept(this, argu);
      if (table.typeCheck(var.getType(),type) == false)
        throw new Exception("Mismatching types");

      n.f3.accept(this, argu);
      return null;

   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   @Override 
   public String visit(ArrayAssignmentStatement n, MyStringList argu) throws Exception {
      String name = n.f0.f0.toString();
      VariableInfo var;
      if ((var = currentfunction.getVariable(name)) ==null) {
          if ((var = currentfunction.getParameter(name)) == null) {
            if ((var = currentclass.getVariable(name)) == null) {
              String tempname = currentclass.getParentName();
              if (tempname == null)
                throw new Exception("Variable is not declared" + " " + name);
              ClassInfo temp = table.getClass(tempname);
              if ((var = table.getRecursiveVariableCheck(temp,name)) == null)
                throw new Exception("Variable is not declared" + " " + name);
            }
          }
      }
      
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp1 = n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      String exp2 = n.f5.accept(this, argu);
      if (var.getType().equals("array") == false || exp1.equals("int")  == false || exp2.equals("int") == false)
        throw new Exception("Arrays can only have int types");
      n.f6.accept(this, argu);
      return null;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   @Override 
   public String visit(IfStatement n, MyStringList argu) throws Exception {
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp = n.f2.accept(this, argu);
      if (exp.equals("boolean") == false)
        throw new Exception("if expressions can only be boolean");
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      return null;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   @Override 
   public String visit(WhileStatement n, MyStringList argu) throws Exception {
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp = n.f2.accept(this, argu);
      if (exp.equals("boolean") == false)
        throw new Exception("while expressions can only be boolean");
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return null;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   @Override 
   public String visit(PrintStatement n, MyStringList argu) throws Exception {
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp = n.f2.accept(this, argu);
      if (exp == null)
        System.out.println("null here");
      if (exp.equals("int") == false)
        throw new Exception("Only int allowed in print");
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return null;
   }

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
   @Override 
   public String visit(AndExpression n, MyStringList argu) throws Exception {
      
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if (type1 == null || type2 == null)
        System.out.println("Problem");
      if (type1.equals("boolean") == false || type2.equals("boolean") == false)
        throw new Exception("and expression must be between two booleans");
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(CompareExpression n, MyStringList argu) throws Exception {
    
      String exp1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp2 = n.f2.accept(this, argu);
      if (exp1.equals("int") == false || exp2.equals("int") == false)
        throw new Exception("Only ints in logical operations");
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(PlusExpression n, MyStringList argu) throws Exception {
      String exp1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp2 = n.f2.accept(this, argu);
      if (exp1.equals("int") == false || exp2.equals("int") == false)
        throw new Exception("Only ints in plus expressions");
      return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(MinusExpression n, MyStringList argu) throws Exception {
      String exp1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp2 = n.f2.accept(this, argu);
      if (exp1.equals("int") == false || exp2.equals("int") == false)
        throw new Exception("Only ints in minus expressions");
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(TimesExpression n, MyStringList argu) throws Exception {
      String exp1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp2 = n.f2.accept(this, argu);
      if (exp1.equals("int") == false || exp2.equals("int") == false)
        throw new Exception("Only ints in times expressions");
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   @Override 
   public String visit(ArrayLookup n, MyStringList argu) throws Exception {
      String exp1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String exp2 = n.f2.accept(this, argu);
      if (exp1.equals("array") == false || exp2.equals("int") == false)
        throw new Exception("Arrays must be ints");
      n.f3.accept(this, argu);
      return "int";     
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   @Override 
   public String visit(ArrayLength n, MyStringList argu) throws Exception {
      
      String exp1 = n.f0.accept(this, argu);
      if (exp1.equals("array") == false)
        throw new Exception("Length can only used in arrays");
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   @Override 
   public String visit(MessageSend n, MyStringList argu) throws Exception {
      String classname = n.f0.accept(this, argu);
      ClassInfo cl;
      if ((cl = (table.getClass(classname))) == null)
        throw new Exception("Class is not declared");
      String functionname = n.f2.accept(this, argu);

      FunctionInfo function;
      if ((function = cl.getFunction(functionname)) == null) {
        if (cl.getParentName() != null) {
          ClassInfo parentClass = table.getClass(cl.getParentName());
          FunctionInfo parentFunction;
          if ((function = table.getRecursiveFunctionCheck(cl,functionname)) == null) {
            throw new Exception("Function is not declared");
          }
        }
        else
          throw new Exception("Function is not declared");
      }

      MyStringList checklist = new MyStringList();
      n.f3.accept(this, argu);
      n.f4.accept(this, checklist);
      if (checklist.types.size() != function.getParameters().size())
        throw new Exception("Wrong parameters number");
      for (int i = 0;i < checklist.types.size();++i) {
        if (table.typeCheck(function.getParameters().get(i).getName(),checklist.types.get(i)))
          throw new Exception ("Parameters type dont match their declaration");
      }
      n.f5.accept(this, argu);
      return function.getType();
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public String visit(ExpressionList n, MyStringList argu) throws Exception {
      
      String type = n.f0.accept(this, argu);
      if (argu.addType(type) == false)
        throw new Exception("Cannot have the same name parameter >1 times in a function");
      n.f1.accept(this, argu);
      return null;
   }

   /**
    * f0 -> ( ExpressionTerm() )*
    */
   public String visit(ExpressionTail n, MyStringList argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionTerm n, MyStringList argu) throws Exception {
       
      n.f0.accept(this, argu);
      String type = n.f1.accept(this, argu);
      if (argu.addType(type) == false)
        throw new Exception("Cannot have the same name parameter >1 times in a function");
      return null;
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
   public String visit(PrimaryExpression n, MyStringList argu) throws Exception {
      String name = n.f0.accept(this,argu);
      if (n.f0.choice.getClass().equals(Identifier.class)) {
        VariableInfo var; 
        if ((var = currentfunction.getVariable(name)) ==null) {
          if ((var = currentfunction.getParameter(name)) == null) {
            if ((var = currentclass.getVariable(name)) == null) {
              String tempname = currentclass.getParentName();
              if (tempname == null)
                throw new Exception("Variable is not declared" + " " + name);
              ClassInfo temp = table.getClass(tempname);
              if ((var = table.getRecursiveVariableCheck(temp,name)) == null)
                throw new Exception("Variable is not declared" + " " + name);
            }
          }
        }

        return var.getType();
      }
      return name;
   }



  /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
   @Override
   public String visit(Type n, MyStringList argu) throws Exception {
    String type =  n.f0.accept(this, argu);
    if (type.equals("int") == false && type.equals("boolean") == false && type.equals("array") == false && table.getClass(type) == null)
      throw new Exception("Not accepted type");
    return type;

   }





    /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
  @Override
   public String visit(ArrayType n,MyStringList argu) throws Exception {
      
      n.f0.accept(this,null);
      n.f1.accept(this,null);
      n.f2.accept(this,null);
      return "array";
   }

   /**
    * f0 -> "boolean"
    */
   @Override
   public String visit(BooleanType n,MyStringList argu) throws Exception {
      return "boolean";
   }

   /**
    * f0 -> "int"
    */
   @Override
   public String visit(IntegerType n,MyStringList argu) throws Exception {
      return "int";
   }
   @Override
    public String visit(IntegerLiteral n, MyStringList argu) throws Exception {
      return "int";
   }

   /**
    * f0 -> "true"
    */
   @Override
   public String visit(TrueLiteral n, MyStringList argu) throws Exception {
      return "boolean";
   }

   /**
    * f0 -> "false"
    */
   @Override
   public String visit(FalseLiteral n, MyStringList argu) throws Exception {
      return "boolean";
   }

   /**
    * f0 -> <IDENTIFIER>
    */

  @Override
   public String visit(Identifier n,MyStringList argu) throws Exception {
      n.f0.accept(this,null);
      return n.f0.toString();

   }

   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n, MyStringList argu) throws Exception {
      n.f0.accept(this, argu);
      return currentclass.getName();

   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(ArrayAllocationExpression n, MyStringList argu) throws Exception {

      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      String exp = n.f3.accept(this, argu);
      if (exp.equals("int") == false)
        throw new Exception("Array can only be int");
      n.f4.accept(this, argu);
      return "array";
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n, MyStringList argu) throws Exception {
      String name = n.f1.f0.toString();
      ClassInfo cl;  
      if ((cl = table.getClass(name)) == null)
        throw new Exception("Class is not declared");
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      return name;
   }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   public String visit(NotExpression n, MyStringList argu) throws Exception {
      
      n.f0.accept(this, argu);
      String name = n.f1.accept(this, argu);
      if (name.equals("boolean") == false)
        throw new Exception("Not expression must be boolean");
      return name;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n, MyStringList argu) throws Exception {
      n.f0.accept(this, argu);
      String type = n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return type;
   }



}