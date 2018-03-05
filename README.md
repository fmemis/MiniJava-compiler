# MiniJava-compiler
A two part project. 1st part)Semantic analysis 2nd part)Generating intermediate code(MiniJava -> LLVM)

## Project Description
In both parts of the project we already have a Mini Java grammar with which we can build a syntax tree.
We then can traverse this tree with visitors.

### Part1
We implement two visitors.One for building a symbol table of the minijava program we read(SymbolTableBuilderVisitor.java), and another to perform the semantic check(TypeCheckVisitor.java).We also need a main class of course,and classes for the symbol table.

#### Details
SymbolTableBuilderVisitor.java: Builds symbol table. Also performs some type checking(mostly related to declarations,doesn't have all the SymbolTable to check for everything)

TypeCheckVisitor.java:Performs the semantic check.

symboltable:a folder containing all the classes that make up the symbol table.

MyStringList.java: a small class which will be given as an argument to the functions of the TypeCheckVisitor

### Part2
We implement two visitors.One for building a symbol table of the minijava program we read(SymbolTableBuilderVisitor.java) - same as before, and another to generate the intermediate code(LLVM) (AssemblyVisitor.java).

#### Details

SymbolTableBuilderVisitor.java:Same as before

AssemblyVisitor.java:Generates the intermediate code in order to print it and create a .ll file(which we can compile and run).

## Compile and execute instructions

To compile just run make(same for both parts).

To execute: java Main input.java

In part2 to run the generated intermediate(LLVM)code:

clang-4.0 -o out1 code.ll

./out1

In the input folder of part2 there are two different minijava programs as input examples.A very small one(Factorial.java) and a big one(TreeVisitor.java).


