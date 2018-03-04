import syntaxtree.*;
import visitor.*;
import java.util.*;
import symboltable.*;


public class AssemblyVisitor extends GJDepthFirst <String, MyStringList> {
  SymbolTable table;
  ClassInfo currentclass;
  FunctionInfo currentfunction;
  String llfile;
  /*int iflabelcount;
  int arrayInitcount;
  int looplabelcount;*/

  public AssemblyVisitor(SymbolTable stable,String begin) {
    table = stable;
    currentfunction = null;
    currentclass = null;
    llfile = "";
    /*iflabelcount = 0;
    arrayInitcount = 0;
    looplabelcount = 0;*/
  }

  public String newIfLabel() {
    String label = "if" + currentfunction.regcount;
    /*++iflabelcount;*/
    ++currentfunction.regcount;
    return label;
  }

  public String newArrayInitLabel() {
    String label = "arr_alloc" + currentfunction.regcount;
    /*++arrayInitcount;*/
    ++currentfunction.regcount;
    return label;
  }

  public String newLoopLabel() {
    String label = "loop" + currentfunction.regcount;
    /*++looplabelcount;*/
    ++currentfunction.regcount;
    return label;
  }

  public String newOobLabel() {
    String label = "oob" + currentfunction.regcount;
    ++currentfunction.regcount;
    return label;
  }

  public String newAndLabel() {
    String label = "andclause" + currentfunction.regcount;
    ++currentfunction.regcount;
    return label;
  }


  public String getLLCode() {
    return llfile;
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
      llfile += "\n\ndefine i32 @main() {";

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
      llfile += "\n\tret i32 0\n}";
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
  public String visit(ClassDeclaration n,MyStringList argu) throws Exception {
    String cname = n.f1.f0.toString();
    currentclass = table.getClass(cname);  
    
    n.f4.accept(this,argu);
  
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
  public String visit(ClassExtendsDeclaration n, MyStringList argu) throws Exception {
    String cname = n.f1.f0.toString();
    currentclass = table.getClass(cname); 
    n.f3.accept(this, null);    
    n.f6.accept(this, null);
    return null;
  } 

  /**
  * f0 -> Type()
  * f1 -> Identifier()
  * f2 -> ";"
  */
  public String visit(VarDeclaration n, MyStringList argu) throws Exception {
    String type = n.f0.accept(this, null);
    String name = n.f1.f0.toString();
    VariableInfo var = null;
    //must never enter this if.Always the else.
    if ((var = currentfunction.getVariable(name)) ==null) {
          ////System.out.println("dont really know");
          if ((var = currentclass.getVariable(name)) == null) {
            String tempname = currentclass.getParentName();
            ClassInfo temp = table.getClass(tempname);
            if ((var = temp.getVariable(name)) == null)
              throw new Exception("Variable is not declared" + " " + name);
          }
      }
    else {
      llfile += "\n\t%" + name + " = alloca " + table.typeTransform(var.getType());
    }
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
  public String visit(MethodDeclaration n, MyStringList argu) throws Exception {
    String fname = n.f2.f0.toString();
    if ((currentfunction = currentclass.getFunction(fname)) == null) {
      if (currentclass.getParentName() != null) {
        ClassInfo parentClass = table.getClass(currentclass.getParentName());
        FunctionInfo parentFunction;
        if ((currentfunction = table.getRecursiveFunctionCheck(parentClass,fname)) == null) {
          throw new Exception("problem");
        }
      }
      else
        throw new Exception("problem");
    }
      
    String type = currentfunction.getType();
    String ftype = table.typeTransform(type);

    llfile += "\n\ndefine " + ftype + " @" + currentclass.getName() + "." + currentfunction.getName() + "(";
    ArrayList<VariableInfo> parameters = currentfunction.getParameters();
    llfile += "i8* %this";
    String vtype = "";
    for (VariableInfo var : parameters) {
      type = var.getType();
      vtype = table.typeTransform(type);
      llfile += ", " + vtype + " %." + var.getName();  
    }
    llfile += ") {";

    for (VariableInfo var : parameters) {
      type = var.getType();
      vtype = table.typeTransform(type);
      llfile += "\n\t "  + "%" + var.getName() + " = alloca " + vtype
      + "\n\tstore " + vtype + " %." + var.getName() + ", " + vtype + "* %" + var.getName();  
    }
    
    /*n.f4.accept(this, null);*/
    n.f7.accept(this, null);
    llfile += "\n";
    n.f8.accept(this, null);
    
    String register = n.f10.accept(this, null);
    
    
    llfile += "\n\tret " + ftype + " " + register + "\n}";
    return null;
  }

  /**
  * f0 -> Block()
  *   | AssignmentStatement()
  *   | ArrayAssignmentStatement()
  *   | IfStatement()
  *   | WhileStatement()
  *   | PrintStatement()
  */
  public String visit(Statement n, MyStringList argu) throws Exception {
    if (argu != null)
      n.f0.accept(this, argu);
    else {
      MyStringList argument = new MyStringList();
      n.f0.accept(this, argument);

    }
    
    return null;
  }

  /**
  * f0 -> "{"
  * f1 -> ( Statement() )*
  * f2 -> "}"
  */
  public String visit(Block n, MyStringList argu) throws Exception {
    n.f1.accept(this, argu);
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
    String expres = n.f2.accept(this,argu);
    VariableInfo var;
    if ((var = currentfunction.getVariable(name)) ==null) {
        if ((var = currentfunction.getParameter(name)) == null) {
          
          if ((var = currentclass.getVariable(name)) == null) {
            String tempname = currentclass.getParentName();
            ClassInfo temp = table.getClass(tempname);
            if ((var = table.getRecursiveVariableCheck(temp,name)) == null)
              throw new Exception("Variable is not declared" + " " + name);
            else {
              String type = table.typeTransform(var.getType());
              String reg = currentfunction.addRegister();
              int offset = var.getOffset() + 8;
              llfile += "\n\t" + reg + " = getelementptr i8, i8* %this, i32 " + offset;
              String reg2 = currentfunction.addRegister();
              llfile += "\n\t" + reg2 + " = bitcast i8* " + reg + " to " + type + "*" +
              "\n\tstore " + type + " " + expres + ", " + type + "* " + reg2;

            }
            
          }

          else {
            String type = table.typeTransform(var.getType());
            String reg = currentfunction.addRegister();
            int offset = var.getOffset() + 8;
            llfile += "\n\t" + reg + " = getelementptr i8, i8* %this, i32 " + offset;
            String reg2 = currentfunction.addRegister();
            llfile += "\n\t" + reg2 + " = bitcast i8* " + reg + " to " + type + "*" +
            "\n\tstore " + type + " " + expres + ", " + type + "* " + reg2;

          }
        }

        else {
          String type = table.typeTransform(var.getType());
          llfile += "\n\tstore " + type + " " + expres + ", " + type + "* " + "%" + var.getName();
        }
    }
    else {
      String type = table.typeTransform(var.getType());
      if (expres == null)
        throw new Exception("problem");
      llfile += "\n\tstore " + type + " " + expres + ", " + type + "* " + "%" + var.getName();
    }
    
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
      String tempreg = "xmm";
      String reg1 = currentfunction.addRegister();
      String reg2 = currentfunction.addRegister();
      String reg3 = currentfunction.addRegister();
      String reg4 = currentfunction.addRegister();
      ++currentfunction.regcount; 
      String ooblabel1 = newOobLabel();
      String ooblabel2 = newOobLabel();
      String ooblabel3 = newOobLabel();
      String name = n.f0.f0.toString();
      
      String tempString = "";
      VariableInfo var;
      if ((var = currentfunction.getVariable(name)) ==null) {
          if ((var = currentfunction.getParameter(name)) == null) {
            if ((var = currentclass.getVariable(name)) == null) {
              String tempname = currentclass.getParentName();
              ClassInfo temp = table.getClass(tempname);
              if ((var = table.getRecursiveVariableCheck(temp,name)) == null)
                throw new Exception("Variable is not declared" + " " + name);
              else {
                String regclass = currentfunction.addRegister();
                int offset = var.getOffset() + 8;
                llfile += "\n\t" + regclass + " = getelementptr i8, i8* %this, i32 " + offset;
                String reg2class = currentfunction.addRegister();
                llfile += "\n\t" + reg2class + " = bitcast i8* " + regclass + " to i32**";
                String reg3class = currentfunction.addRegister();
                llfile += "\n\t" + reg3class + " = load i32*, i32** " + reg2class;
                tempString += "\n\t" + reg1 + " = load i32, i32* " + reg3class; 
                tempreg = reg3class;

              }
            }
            else {

              String regclass = currentfunction.addRegister();
              int offset = var.getOffset() + 8;
              llfile += "\n\t" + regclass + " = getelementptr i8, i8* %this, i32 " + offset;
              String reg2class = currentfunction.addRegister();
              llfile += "\n\t" + reg2class + " = bitcast i8* " + regclass + " to i32**";
              String reg3class = currentfunction.addRegister();
              llfile += "\n\t" + reg3class + " = load i32*, i32** " + reg2class;
              tempString += "\n\t" + reg1 + " = load i32, i32* " + reg3class; 
              tempreg = reg3class;

            }
          }
          else {
            String reg3function = currentfunction.addRegister();
            llfile += "\n\t" + reg3function + " = load i32*, i32** %" + var.getName();
            tempString += "\n\t" + reg1 + " = load i32, i32* " + reg3function;
            tempreg =reg3function;
          }
        }
        else {
          String reg3function = currentfunction.addRegister();
          llfile += "\n\t" + reg3function + " = load i32*, i32** %" + var.getName();
          tempString += "\n\t" + reg1 + " = load i32, i32* " + reg3function;
          tempreg =reg3function;
        }

        String regres = n.f2.accept(this,argu);
        llfile += tempString;
        llfile += "\n\t" + reg2 + " = icmp ult i32 " + regres + ", " + reg1;
        llfile += "\n\tbr i1 " +  reg2 + ", label %" + ooblabel1 + ", label %" + ooblabel2 + "\n";
       
        llfile += "\n\n" + ooblabel1 + ":";

        llfile += "\n\t" + reg3 + " = add i32 " + regres + ", 1";
        llfile += "\n\t" + reg4 + " = getelementptr i32, i32* " +  tempreg + ", i32 " + reg3;
        String regres2 = n.f5.accept(this,argu);
        if (regres2 == null)
          throw new Exception("some problemm");
        llfile += "\n\tstore i32 " +  regres2 + ", i32* " + reg4;
        llfile += "\n\tbr label %" + ooblabel3;

        llfile += "\n\n" + ooblabel2 + ":" + "\n\tcall void @throw_oob()\n\tbr label %" + ooblabel3;

        llfile += "\n\n" + ooblabel3 + ":\n"; 


      
        
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
      String label0 = newIfLabel();
      String label1= newIfLabel();
      String label2 = newIfLabel();
      String expres = n.f2.accept(this, argu);
      
      llfile += "\n\tbr i1 " + expres + ", label %" +  label0 + ", label %" + label1;
      llfile += "\n\n" + label0 + ":\n";   
      n.f4.accept(this, argu);
      llfile +=  "\n\n\t" + "br label %" + label2;
      llfile += "\n" + label1 + ":\n";
      n.f6.accept(this, argu);
      llfile +=  "\n\n\t" + "br label %" + label2;
      llfile += "\n\n" + label2 + ":\n";
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
      String label1 = newLoopLabel();
      String label2 = newLoopLabel();
      String label3 = newLoopLabel();

      llfile += "\n\t br label %" + label1 + "\n\n" + label1 + ":\n"; 

      String expres = n.f2.accept(this, argu);
      llfile += "\n\t br i1 " + expres + ", label %" + label2 + ", label %" + label3;

      llfile += "\n\n" + label2 + ":";
      
      
      String expres2 = n.f4.accept(this, argu);
      llfile += "\n\tbr label %" + label1;
      llfile += "\n\n" + label3 + ":";
      return null;
   }

   /**
    * f0 -> "//System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   @Override 
   public String visit(PrintStatement n, MyStringList argu) throws Exception {
      
      String reg = n.f2.accept(this, argu);
      
      llfile += "\n\tcall void (i32) @print_int(i32 " + reg + ")";
      return null;
   }

   /**
  * f0 -> AndExpression()
  *   | CompareExpression()
  *   | PlusExpression()
  *   | MinusExpression()
  *   | TimesExpression()
  *   | ArrayLookup()
  *   | ArrayLength()
  *   | MessageSend()
  *   | Clause()
  */
  @Override
  public String visit(Expression n, MyStringList argu) throws Exception {
    String reg;
    if (argu != null)
      reg = n.f0.accept(this, argu);
    else {
      MyStringList argument = new MyStringList();
      reg = n.f0.accept(this, argument);

    }
  
    return reg;
  }

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */


   @Override 
   public String visit(AndExpression n, MyStringList argu) throws Exception {
      
      String regres1 = n.f0.accept(this, argu);
      String reg = currentfunction.addRegister();
      String andlabel1 = newAndLabel();
      String andlabel2 = newAndLabel();
      String andlabel3 = newAndLabel();
      String andlabel4 = newAndLabel();
      llfile += "\n\tbr label %" + andlabel1;
      llfile += "\n\n" + andlabel1 + ":";
      llfile += "\n\t br i1 " + regres1 + ", label %" + andlabel2 + ", label %" + andlabel4;


      llfile += "\n\n" + andlabel2 + ":";      
      String regres2 = n.f2.accept(this, argu);
      llfile += "\n\tbr label %" + andlabel3;

      llfile += "\n\n" + andlabel3 + ":";
      llfile += "\n\tbr label %" + andlabel4;

      llfile += "\n\n" + andlabel4 + ":";
      llfile += "\n\t" + reg + " = phi i1 [ 0, %" + andlabel1 + " ], [ " + regres2 + ", %" + andlabel3 + " ]";

      return reg;
      
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(CompareExpression n, MyStringList argu) throws Exception {
      String slt = "icmp slt i32 ";
      String exp1res = n.f0.accept(this, argu);
      String reg1 = "";
      String reg2 = "";
      String regres = "";
      if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        slt += exp1res + ", ";
        argu.isJustAnum = false;
      } 
      else {
        reg1 = currentfunction.addRegister();
        llfile += "\n\t" + reg1 + " = load i32, i32* " + exp1res;
        slt += reg1 + ", ";
      }
      String exp2res = n.f2.accept(this, argu);
      if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        slt += exp2res;
        argu.isJustAnum = false;
      }
      else {
        reg2 = currentfunction.addRegister();
        llfile += "\n\t" + reg2 + " = load i32, i32* " + exp2res;
        slt += reg2;
      }
      regres = currentfunction.addRegister();
      llfile +=  "\n\t" + regres + " = " + slt;

      
      return regres;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(PlusExpression n, MyStringList argu) throws Exception {
      String add = "add i32 ";
      String exp1res = n.f0.accept(this, argu);
      String reg1 = "";
      String reg2 = "";
      String regres = "";
      if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        add += exp1res + ", ";
        argu.isJustAnum = false;
      } 
      else {
        reg1 = currentfunction.addRegister();
        llfile += "\n\t" + reg1 + " = load i32, i32* " + exp1res;
        add += reg1 + ", ";
      }
      String exp2res = n.f2.accept(this, argu);
      if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        add += exp2res;
        argu.isJustAnum = false;
      }
      else {
        reg2 = currentfunction.addRegister();
        llfile += "\n\t" + reg2 + " = load i32, i32* " + exp2res;
        add += reg2;
      }
      regres = currentfunction.addRegister();
      llfile +=  "\n\t" + regres + " = " + add;

      
      return regres;
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(MinusExpression n, MyStringList argu) throws Exception {
      String sub = "sub i32 ";
      String exp1res = n.f0.accept(this, argu);
      String reg1 = "";
      String reg2 = "";
      String regres = "";
      if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        sub += exp1res + ", ";
        argu.isJustAnum = false;
      } 
      else {
        reg1 = currentfunction.addRegister();
        llfile += "\n\t" + reg1 + " = load i32, i32* " + exp1res;
        sub += reg1 + ", ";
      }
      String exp2res = n.f2.accept(this, argu);
      if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        sub += exp2res;
        argu.isJustAnum = false;
      }
      else {
        reg2 = currentfunction.addRegister();
        llfile += "\n\t" + reg2 + " = load i32, i32* " + exp2res;
        sub += reg2;
      }
      regres = currentfunction.addRegister();
      llfile +=  "\n\t" + regres + " = " + sub;

      
      return regres;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   @Override 
   public String visit(TimesExpression n, MyStringList argu) throws Exception {
      String mul = "mul i32 ";
      String exp1res = n.f0.accept(this, argu);
      String reg1 = "";
      String reg2 = "";
      String regres = "";
      if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        mul += exp1res + ", ";
        argu.isJustAnum = false;
      } 
      else {
        reg1 = currentfunction.addRegister();
        llfile += "\n\t" + reg1 + " = load i32, i32* " + exp1res;
        mul += reg1 + ", ";
      }

      String exp2res = n.f2.accept(this, argu);
        if (1 == 1) {
      /*if (argu.isJustAnum) {*/
        mul += exp2res;
        argu.isJustAnum = false;
      }
      else {
        reg2 = currentfunction.addRegister();
        llfile += "\n\t" + reg2 + " = load i32, i32* " + exp2res;
        mul += reg2;
      }
      regres = currentfunction.addRegister();
      llfile +=  "\n\t" + regres + " = " + mul;

       
      return regres;
   }

   /**
  * f0 -> PrimaryExpression()
  * f1 -> "["
  * f2 -> PrimaryExpression()
  * f3 -> "]"
  */
  public String visit(ArrayLookup n, MyStringList argu) throws Exception {
    String reg1 =  currentfunction.addRegister();
    String reg2 = currentfunction.addRegister();
    String reg3 =  currentfunction.addRegister();
    String reg4 =  currentfunction.addRegister();
    String reg5 =  currentfunction.addRegister();
    String reg6 =  currentfunction.addRegister();
    
    String ooblabel1 = newOobLabel();
    String ooblabel2 = newOobLabel();
    String ooblabel3 = newOobLabel();

    String regres1 = n.f0.accept(this,argu);
    

    String regres2 = n.f2.accept(this,argu);
    llfile += "\n\t" + reg1 + " = load i32, i32 *" + regres1;
    llfile += "\n\t" + reg2 + " = icmp ult i32 " + regres2 + ", " + reg1;

    llfile += "\n\tbr i1 " + reg2 + ", label %" + ooblabel1 + ", label %" + ooblabel2;
    
    llfile += "\n" + ooblabel1 + ":\n\t" + reg3 + " = add i32 " + regres2 + ", 1";
    
    llfile += "\n\t" + reg4 + " = getelementptr i32, i32* " + regres1 + ", i32 " + reg3;
    llfile += "\n\t" + reg5 +  " = load i32, i32* " + reg4;


    llfile += "\n\tbr label %" + ooblabel3;

    llfile += "\n\n" + ooblabel2 + ":" + "\n\tcall void @throw_oob()\n\tbr label %" + ooblabel3;
    


    llfile += "\n\n" + ooblabel3 + ":\n"; 



    return reg5;



  }

   /**
  * f0 -> PrimaryExpression()
  * f1 -> "."
  * f2 -> Identifier()
  * f3 -> "("
  * f4 -> ( ExpressionList() )?
  * f5 -> ")"
  */
  public String visit(MessageSend n, MyStringList argu) throws Exception {
   
    MyStringList argument = new MyStringList();
    String regres = n.f0.accept(this,argument);
    ClassInfo tempclass = table.getClass(argument.type);
    if (tempclass == null) {}
    String reg1 = currentfunction.addRegister();
    String reg2 = currentfunction.addRegister();
    String reg3 = currentfunction.addRegister();
    String reg4 = currentfunction.addRegister();
    String reg5 = currentfunction.addRegister();

    
    String fname = n.f2.f0.toString();
    

    argument.type = null;
    FunctionInfo tempfunction;
    if ((tempfunction = tempclass.getFunction(fname)) == null) {
      if (tempclass.getParentName() != null) {
        ClassInfo parentClass = table.getClass(tempclass.getParentName());
        FunctionInfo parentFunction;
        if ((tempfunction = table.getRecursiveFunctionCheck(parentClass,fname)) == null) {
          throw new Exception("problem");
        }
      }
      else
        throw new Exception("problem");
    }
    int offset = tempfunction.getOffset();
    llfile += "\n\t; " + tempclass.getName() + "." + fname + " : " + offset;
    llfile += "\n\t" + reg1 + " = bitcast i8* " + regres + " to i8***";
    llfile += "\n\t" + reg2 + " = load i8**, i8*** " + reg1;
    llfile += "\n\t" + reg3 + " = getelementptr i8*, i8** " + reg2 + ", i32 " + offset;
    llfile += "\n\t" + reg4 + " = load i8*, i8** " + reg3;
    String ftype = tempfunction.getType();
    argu.type = ftype;
    String type = table.typeTransform(ftype);
    ftype = "";
    ftype += type;
    llfile += "\n\t" + reg5 + " = bitcast i8* " + reg4 + " to " + type + " (i8*";
    String reg6 = currentfunction.addRegister();
    ArrayList<VariableInfo> parameters = tempfunction.getParameters();
    type = "";
    String vtype = "";
    ArrayList<String> llparameterTypeList = new ArrayList<String>();
    for (VariableInfo var : parameters) {
      vtype = var.getType();
      type = table.typeTransform(vtype);
      llparameterTypeList.add(type);

      llfile += ", " + type;  
    }
    llfile += ")*";
    MyStringList checklist = new MyStringList();
    n.f4.accept(this, checklist);
    
    llfile += "\n\t" + reg6 + " = call " + ftype + " " + reg5 + "(i8* " + regres;
    if (checklist.types.size() != tempfunction.getParameters().size())
        throw new Exception("Wrong parameters number");
    
    for (int i = 0;i < checklist.types.size();++i) {
     String currentreg = checklist.types.get(i);
     String currenttype = llparameterTypeList.get(i);
     llfile += ", " + currenttype + " " + currentreg;
    }
    llfile += ")";  
    
    return reg6;

  }

  /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public String visit(ExpressionList n, MyStringList argu) throws Exception {
      
      String reg = n.f0.accept(this, argu);
      argu.addType(reg);
        
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
       
      String reg = n.f1.accept(this, argu);
      argu.addType(reg);
        
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
      String resreg = "";
      if (n.f0.choice.getClass().equals(Identifier.class)) {
        VariableInfo var; 
        if ((var = currentfunction.getVariable(name)) ==null) {
          if ((var = currentfunction.getParameter(name)) == null) {
            if ((var = currentclass.getVariable(name)) == null) {
              String tempname = currentclass.getParentName();
              ClassInfo temp = table.getClass(tempname);
              if ((var = table.getRecursiveVariableCheck(temp,name)) == null)
                throw new Exception("Variable is not declared" + " " + name);
              else {
                String reg1 = currentfunction.addRegister();
                int offset = var.getOffset() + 8;
                llfile +="\n\t" + reg1 +  " = getelementptr i8, i8* %this, i32 " + offset;
                String reg2 = currentfunction.addRegister();
                String type = table.typeTransform(var.getType());
                llfile +="\n\t" + reg2 + " = bitcast i8* " + reg1 + " to " + type + "*";
                resreg = currentfunction.addRegister();
                llfile +="\n\t" + resreg + " = load " + type + ", " + type + "* " + reg2;
                if (argu != null)
                  argu.type = var.getType();

              }
            }
            else {
              String reg1 = currentfunction.addRegister();
              int offset = var.getOffset() + 8;
              llfile +="\n\t" + reg1 +  " = getelementptr i8, i8* %this, i32 " + offset;
              String reg2 = currentfunction.addRegister();
              if (reg1 == reg2)
                throw new Exception("whaaaaaaaaaaat");

              String type = table.typeTransform(var.getType());
              llfile +="\n\t" + reg2 + " = bitcast i8* " + reg1 + " to " + type + "*";
              resreg = currentfunction.addRegister();
              llfile +="\n\t" + resreg + " = load " + type + ", " + type + "* " + reg2;
              if (argu != null)
                argu.type = var.getType();


            }
          }
          else {
            resreg = currentfunction.addRegister();
            String type = table.typeTransform(var.getType());
            llfile +="\n\t" + resreg + " = load " + type + ", " + type + "* %" + name;
            if (argu != null)
                argu.type = var.getType();

          }
        }
        else {
          resreg = currentfunction.addRegister();
          String type = table.typeTransform(var.getType());
          llfile +="\n\t" + resreg + " = load " + type + ", " + type + "* %" + name;
          if (argu != null)
                argu.type = var.getType();   
        }

        return resreg;
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

   /**
    * f0 -> <INTEGER_LITERAL>
    */

   @Override
    public String visit(IntegerLiteral n, MyStringList argu) throws Exception {
      argu.isJustAnum = true;
      return n.f0.toString();
   }

   /**
    * f0 -> "true"
    */
   @Override
   public String visit(TrueLiteral n, MyStringList argu) throws Exception {
    argu.isJustAnum = true;
      return "1";
   }

   /**
    * f0 -> "false"
    */
   @Override
   public String visit(FalseLiteral n, MyStringList argu) throws Exception {
    argu.isJustAnum = true;
      return "0";
   }

   /**
    * f0 -> <IDENTIFIER>
    */

  @Override
   public String visit(Identifier n,MyStringList argu) throws Exception {
      return n.f0.toString();

   }

   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n, MyStringList argu) throws Exception {
    argu.type = currentclass.getName();
      
      return "%this";

   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(ArrayAllocationExpression n, MyStringList argu) throws Exception {
    String reg1 = currentfunction.addRegister();
    String reg2 = currentfunction.addRegister();
    String reg3 = currentfunction.addRegister();
    String reg4 = currentfunction.addRegister();
    String arraylabel1 = newArrayInitLabel();
    String arraylabel2 = newArrayInitLabel();
    String reg = n.f3.accept(this, argu);
    llfile += "\n\t" + reg4 + " = icmp slt i32 " + reg + ", 0";
    llfile += "\n\t" + "br i1 " + reg4 + ", label %" + arraylabel1 + ", label %" + arraylabel2;
    llfile +="\n" + arraylabel1 + ":\n\t call void @throw_oob()\n\tbr label %" + arraylabel2;
    llfile += "\n" + arraylabel2 + ":";
    llfile += "\n\t" + reg1 + " = add i32 " + reg + ", 1";
    llfile += "\n\t" + reg2 + " = call i8* @calloc(i32 4, i32 " + reg1 + ")";
    llfile += "\n\t" + reg3 + " = bitcast i8* " + reg2 + " to i32*";
    llfile += "\n\t" + "store i32 " + reg + ", i32* " + reg3; 
      
      
      return reg3;
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

      String reg1 = currentfunction.addRegister();
      String reg2 = currentfunction.addRegister();
      String reg3 = currentfunction.addRegister();
      
      int offset = cl.variableoffset + 8;
      llfile += "\n\t" + reg1 + " = call i8* @calloc(i32 1, i32 " + offset + ")";
      llfile += "\n\t" + reg2 + " = bitcast i8* " + reg1 + " to i8***";
      llfile += "\n\t" + reg3 + " = getelementptr [" + cl.getNumberOfFunctions() + 
      " x i8*], [" + cl.getNumberOfFunctions() + " x i8*]* @." + name + "_vtable, i32 0, i32 0";
      llfile += "\n\t" +  "store i8** " + reg3 + ", i8*** " + reg2;
      argu.type = name; 
      return reg1;

   }

   public String visit(Clause n, MyStringList argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   public String visit(NotExpression n, MyStringList argu) throws Exception {
      n.f0.accept(this, argu);
      String reg = currentfunction.addRegister();
      String regres = n.f1.accept(this, argu);
      llfile += "\n\t" + reg +  " = xor i1 1, " + regres;
      return reg;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n, MyStringList argu) throws Exception {
      return n.f1.accept(this, argu);
      
   }





}