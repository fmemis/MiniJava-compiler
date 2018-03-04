import java.io.*;
import java.util.*;
import visitor.*;
import syntaxtree.*;
import symboltable.*;



public class Main {
  public static void main(String [] args) throws FileNotFoundException, ParseException, Exception,IOException {
      FileInputStream in = null;
      try {
        in = new FileInputStream(args[0]);
        MiniJavaParser parser = new MiniJavaParser(in);
        Goal root = parser.Goal();

        SymbolTableBuilderVisitor sv = new  SymbolTableBuilderVisitor();
        //build the symbol table
        root.accept(sv);
        SymbolTable symbolTable = sv.getSymbolTable();
        TypeCheckVisitor typecheck =new TypeCheckVisitor(symbolTable);
        //semantic check
        root.accept(typecheck,null);
        System.out.println("Program is ok");
        
      }
     catch(ParseException ex){
          System.out.println(ex.getMessage());
      }
      catch(FileNotFoundException ex){
          System.out.println(ex.getMessage());
      }
      catch(Exception ex){
          System.out.println(ex.getMessage());
      }
      finally{
        try{
          if(in != null) in.close();
        }
        catch(IOException ex){
            System.err.println(ex.getMessage());
        }
      }
   }
}