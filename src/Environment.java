import java.util.HashMap;
import java.util.Map;

public class Environment {

    Map<String, Integer> nameToAddress;
    Map<String, String> nameToType;
    Environment parent;

    public Environment(){
        nameToAddress = new HashMap<>();
        nameToType = new HashMap<>();
    }

    public Environment(Environment parent){
        this();
        this.parent = parent;
    }

    public int getAddress(String varName){

        if(nameToAddress.containsKey(varName)){
            return nameToAddress.get(varName);
        }
        // TODO
        throw new RuntimeException(String.format("Variable %s not found.", varName));
    }

    public String getType(String varName){
        return nameToType.get(varName);
    }

    public void define(String type, String varName, int i){
        nameToAddress.put(varName, i);
        nameToType.put(varName, type);
    }
}
