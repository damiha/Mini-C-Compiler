import java.util.HashMap;
import java.util.Map;

public class Environment {

    Map<String, Integer> nameToAddress;
    Map<String, String> nameToType;

    public Environment(){
        nameToAddress = new HashMap<>();
        nameToType = new HashMap<>();
    }

    public int getAddress(String varName){
        return nameToAddress.get(varName);
    }

    public String getType(String varName){
        return nameToType.get(varName);
    }

    public void define(String type, String varName, int i){
        nameToAddress.put(varName, i);
        nameToType.put(varName, type);
    }
}
