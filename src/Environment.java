import java.util.HashMap;
import java.util.Map;

public class Environment {

    Map<String, Integer> map;

    public Environment(){
        map = new HashMap<>();
    }

    public int getAddress(String var){
        return map.get(var);
    }

    public void define(String var, int i){
        map.put(var, i);
    }
}
