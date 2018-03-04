package symboltable;
import syntaxtree.*;
import java.util.*;

public class VariableInfo {
  String name;
  String type;
  int offset;

  public VariableInfo(String name, String type)
  {
    this.name = name;
    this.type = type;
    offset = 0;
  }

  public String getName() {
  	return name;
  }

  public String getType() {
  	return type;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

}