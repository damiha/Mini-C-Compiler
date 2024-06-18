import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Code {

    List<Instr> instructions;
    Map<Integer, Integer> jumpTable;

    Map<String, Integer> functionNamesToCodeStart;

    public Code(){
        instructions = new ArrayList<>();
        jumpTable = new HashMap<>();
        functionNamesToCodeStart = new HashMap<>();
    }

    public Code(List<Instr> instructions){
        this();
        this.instructions.addAll(instructions);
    }

    public int addJumpLabelAtEnd(){

        int indexOfEnd = instructions.size();
        int jumpLabel = jumpTable.size();

        jumpTable.put(jumpLabel, indexOfEnd);

        return jumpLabel;
    }

    public void registerFunction(String functionName){
        int indexOfEnd = instructions.size();

        functionNamesToCodeStart.put(functionName, indexOfEnd);
    }

    public void addCode(Code codeAfter){

        // merge jump tables

        int offset = instructions.size();
        int jumpLabelOffset = jumpTable.size();

        for(Integer jumpLabel : codeAfter.jumpTable.keySet()){

            int newJumpLabel = jumpLabelOffset + jumpLabel;
            int newDestination = offset + codeAfter.jumpTable.get(jumpLabel);

            // replace old jump label in code that is appended
            // only replace jump labels which match with old ones
            for(Instr instr : codeAfter.instructions){

                if(instr instanceof Instr.JumpZ && ((Instr.JumpZ) instr).jumpLabel == jumpLabel){
                    ((Instr.JumpZ) instr).jumpLabel = newJumpLabel;
                }
                else if (instr instanceof Instr.Jump && ((Instr.Jump) instr).jumpLabel == jumpLabel){
                    ((Instr.Jump) instr).jumpLabel = newJumpLabel;
                }
            }

            jumpTable.put(newJumpLabel, newDestination);
        }

        // merge function jump tables
        for(String functionName : codeAfter.functionNamesToCodeStart.keySet()){
            if(functionNamesToCodeStart.containsKey(functionName)){
                throw new RuntimeException(String.format("Function '%s' is declared multiple times.", functionName));
            }

            int oldFunctionStart = codeAfter.functionNamesToCodeStart.get(functionName);
            functionNamesToCodeStart.put(functionName, offset + oldFunctionStart);
        }

        instructions.addAll(codeAfter.instructions);
    }

    public void addInstruction(Instr newInstruction){
        instructions.add(newInstruction);
    }

    public String toString(){

        List<String> instructionRepresentations = new ArrayList<>();

        for(Instr instr : instructions){
            instructionRepresentations.add(instr.toString());
        }

        // add the jump labels
        for(Integer i : jumpTable.keySet()){
            String stringRep = instructionRepresentations.get(jumpTable.get(i));
            String newRep = String.format("%d: %s", i, stringRep);
            instructionRepresentations.set(jumpTable.get(i), newRep);
        }

        // add the function labels
        for(String functionName : functionNamesToCodeStart.keySet()){

            int index = functionNamesToCodeStart.get(functionName);

            String stringRep = instructionRepresentations.get(index);
            String newRep = String.format("%s: %s", functionName, stringRep);
            instructionRepresentations.set(index, newRep);
        }

        return String.join("\n", instructionRepresentations);
    }
}
