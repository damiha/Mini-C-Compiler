import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Constructor;

public class Code {

    List<Instr> instructions;
    Map<Integer, Integer> jumpTable;

    Map<String, Integer> functionNamesToCodeStart;

    static List<Class<? extends Instr>> classes = new ArrayList<>();

    static {
        classes.add(Instr.Halt.class);
        classes.add(Instr.Enter.class);
        classes.add(Instr.Mark.class);
        classes.add(Instr.Call.class);
        classes.add(Instr.Return.class);
        classes.add(Instr.Slide.class);
        classes.add(Instr.LoadRC.class);
        classes.add(Instr.LoadC.class);
        classes.add(Instr.Add.class);
        classes.add(Instr.LessOrEqual.class);
        classes.add(Instr.Equal.class);
        classes.add(Instr.Mul.class);
        classes.add(Instr.Store.class);
        classes.add(Instr.Pop.class);
        classes.add(Instr.Load.class);
        classes.add(Instr.JumpZ.class);
        classes.add(Instr.Jump.class);
        classes.add(Instr.Print.class);
        classes.add(Instr.Alloc.class);
    }

    public Code(){
        instructions = new ArrayList<>();
        jumpTable = new HashMap<>();
        functionNamesToCodeStart = new HashMap<>();
    }

    public Code(List<Instr> instructions){
        this();
        this.instructions.addAll(instructions);
    }

    public Code(String fileName){
        this();

        List<String> lines = getLines(fileName);

        linesToInstructions(lines);
    }

    private List<String> getLines(String fileName){
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private void linesToInstructions(List<String> lines){
        for(String line : lines){
            lineToInstruction(line);
        }
    }

    private void lineToInstruction(String line){
        String[] splitBySpace = line.split(" ");

        // first one that is not jump label information must be instruction
        int indexOfInstruction = 0;
        for(int i = 0; i < splitBySpace.length; i++){
            if(!splitBySpace[i].contains(":")){
                indexOfInstruction = i;
                break;
            }
        }

        for(int i = 0; i < indexOfInstruction; i++){
            jumpLabelFromString(splitBySpace[i]);
        }

        String instructionName = splitBySpace[indexOfInstruction];

        String argument = splitBySpace.length > indexOfInstruction + 1 ? splitBySpace[indexOfInstruction + 1] : null;
        for(int i = indexOfInstruction + 2; i < splitBySpace.length; i++){
            argument += (" " + splitBySpace[i]);
        }

        Instr instruction = createInstanceIfMatch(instructionName, Code.classes, argument);

        if(instruction == null){
            throw new RuntimeException(String.format("Could not parse %s\n", line));
        }

        instructions.add(instruction);
    }

    public Instr createInstanceIfMatch(String targetString, List<Class<? extends Instr>> classes, String argument) {
        for (Class<? extends Instr> clazz : classes) {
            try {
                Instr tempInstance;

                if (argument != null) {
                    if (isStringArgument(argument)) {
                        tempInstance = createInstanceWithStringArgument(clazz, argument);
                    } else {
                        tempInstance = createInstanceWithIntOrObjectArgument(clazz, argument);
                    }
                } else {
                    tempInstance = createInstanceWithNoArgument(clazz);
                }

                if (tempInstance.toString().equals(targetString + (argument == null ? "" : " " + argument))) {
                    return tempInstance;
                }
            } catch (Exception e) {
                // Optionally log the exception
            }
        }
        return null;
    }

    private boolean isStringArgument(String argument) {
        return argument.startsWith("'") && argument.endsWith("'");
    }

    private Instr createInstanceWithStringArgument(Class<? extends Instr> clazz, String argument) throws Exception {
        String arg = argument.substring(1, argument.length() - 1); // Remove quotes
        return clazz.getDeclaredConstructor(Object.class).newInstance(arg);
    }

    private Instr createInstanceWithIntOrObjectArgument(Class<? extends Instr> clazz, String argument) throws Exception {

        // this shouldn't fail
        int intArg = Integer.parseInt(argument);

        try {
            return clazz.getDeclaredConstructor(int.class).newInstance(intArg);
        } catch (Exception e) {
            // If parsing fails, try creating an instance with an Object constructor
            return clazz.getDeclaredConstructor(Object.class).newInstance(intArg);
        }
    }

    private Instr createInstanceWithNoArgument(Class<? extends Instr> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    private void jumpLabelFromString(String jumpInformation){

        // cut away the :
        jumpInformation = jumpInformation.substring(0, jumpInformation.length() - 1);

        if(isFunctionName(jumpInformation)){

            // the instruction is added afterward and then the jump label points to the index (because size increases by 1)
            functionNamesToCodeStart.put(jumpInformation, instructions.size());
        }
        else{
            jumpTable.put(Integer.parseInt(jumpInformation), instructions.size());
        }
    }

    private boolean isFunctionName(String jumpInformation){
        char c = jumpInformation.charAt(0);
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
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

    public void saveToFile(String fileName){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(toString());
            System.out.println("File written successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
