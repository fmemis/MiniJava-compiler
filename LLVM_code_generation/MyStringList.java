import java.util.*;

public class MyStringList {
	public ArrayList<String> types;
	public boolean alert;
	public boolean alert2;
	public boolean isJustAnum;
	public String type;

	public MyStringList() {
		types = new ArrayList<String>();
		isJustAnum = false;
	}

	public boolean addType(String name) {
		/*for (String var : types) {
			if (var.equals(name))
				System.out.println(name);
				return false;
		}*/
		types.add(name);
		return true;
	}
}