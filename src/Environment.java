import java.util.HashMap;
import java.util.Map;

public class Environment {

    Map<String, Pair<Visibility, Integer>> nameToAddress;
    Map<String, String> nameToType;

    public Environment(){
        nameToAddress = new HashMap<>();
        nameToType = new HashMap<>();
    }

    // creates a deep copy
    public Environment(Environment env){

        this();

        for(String varName : env.nameToAddress.keySet()){
            nameToAddress.put(varName, env.nameToAddress.get(varName));
            nameToType.put(varName, env.nameToType.get(varName));
        }
    }

    public Pair<Visibility, Integer> getVisibilityAndAddress(String varName){

        if(nameToAddress.containsKey(varName)){
            return nameToAddress.get(varName);
        }

        throw new RuntimeException(String.format("Variable %s not found.", varName));
    }

    public String getType(String varName){
        return nameToType.get(varName);
    }

    public void define(String type, String varName, Visibility visibility, int i){
        nameToAddress.put(varName, new Pair<>(visibility, i));
        nameToType.put(varName, type);
    }
}
