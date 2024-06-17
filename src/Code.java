import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Code {

    List<Instr> instructions;
    Map<Integer, Integer> jumpTable;

    public Code(){
        instructions = new ArrayList<>();
        jumpTable = new HashMap<>();
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

        return String.join("\n", instructionRepresentations);
    }
}
