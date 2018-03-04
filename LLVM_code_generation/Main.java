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
        //build the symbol table
        SymbolTableBuilderVisitor sv = new  SymbolTableBuilderVisitor();
        root.accept(sv);
        SymbolTable symbolTable = sv.getSymbolTable();
        //symbolTable.printSymbolTable();
        
        

        String llcode = "";
        //first print the Vtable ath the beginning of the llvm code.
        llcode = symbolTable.printVTable();
        //some help functions
        llcode += "\n\ndeclare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n\n" +
        "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n"
        + "define void @print_int(i32 %i) {" +
        "\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)"
        + "\n\tret void\n}\n\ndefine void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*"
        + "\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)" +
        "\n\tret void\n}";
        
        //finally the visitor which will print the majority of the necessary llvm code.
        AssemblyVisitor av = new AssemblyVisitor(symbolTable,null);
        root.accept(av,null);
        llcode += av.getLLCode();

        //print the code at the console and at a file named code.ll
        System.out.println(llcode);
        PrintWriter writer = new PrintWriter("code.ll", "UTF-8");
        writer.println(llcode);
        
        writer.close();

        
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