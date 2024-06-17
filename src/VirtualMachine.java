import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.Map;

public class VirtualMachine {

    int stackSize = 1000;
    int instructionSize = 200;

    Object[] stack;
    Instr[] codeStore;
    Map<Integer, Integer> jumpTable;

    Instr instructionRegister;

    int stackPointer;
    int programCounter;

    boolean isRunning;

    boolean debugPrintActivated = false;

    public VirtualMachine(){
        stack = new Object[stackSize];
        codeStore = new Instr[instructionSize];
    }

    private void init(Code code){

        jumpTable = code.jumpTable;
        Instr[] instructionsArray = code.instructions.toArray(new Instr[0]);

        System.arraycopy(instructionsArray, 0, codeStore, 0, instructionsArray.length);
        stackPointer = 0;
        programCounter = 0;
        isRunning = true;
    }

    public void execute(Code code){

        init(code);

        while(isRunning){
            instructionRegister = codeStore[programCounter++];
            execute(instructionRegister);

            if(debugPrintActivated) {
                System.out.println(this);
            }
        }
    }

    private void execute(Instr instruction){
        if(instruction instanceof Instr.Halt){
            isRunning = false;
        }
        if(instruction instanceof Instr.JumpZ){
            if((Integer)stack[stackPointer] == 0){
                programCounter = jumpTable.get(((Instr.JumpZ) instruction).jumpLabel);
                stackPointer--;
            }
        }
        if(instruction instanceof Instr.Alloc){
            stackPointer += ((Instr.Alloc) instruction).k;
        }
        // unconditional jump
        if(instruction instanceof Instr.Jump){
            programCounter = jumpTable.get(((Instr.Jump) instruction).jumpLabel);
            stackPointer--;
        }
        if(instruction instanceof Instr.Print){
            System.out.println(stack[stackPointer]);
            stackPointer--;
        }
        if(instruction instanceof Instr.Pop){
            if(stackPointer > 0){
                stackPointer--;
            }
            else {
                throw new RuntimeException("Cannot pop from empty stack.");
            }
        }
        else if(instruction instanceof Instr.LoadC){
            stackPointer++;
            stack[stackPointer] = ((Instr.LoadC)instruction).q;
        }
        else if(instruction instanceof  Instr.Load){

            // consumes the address to load from
            Integer addressToLoadFrom = (Integer)stack[stackPointer];
            stack[stackPointer] = stack[addressToLoadFrom];
        }
        else if(instruction instanceof Instr.Add){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) + ((Integer)o2);
                }

                throw new RuntimeException("Add doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.LessOrEqual){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) <= ((Integer)o2) ? 1 : 0;
                }

                throw new RuntimeException("LessOrEqual doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Equal){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer) o1).equals((Integer) o2) ? 1 : 0;
                }

                throw new RuntimeException("Equal doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Store){
            // first is address
            // below is value
            stack[(Integer) stack[stackPointer]] = stack[stackPointer - 1];
            // address is consumed
            stackPointer--;
        }
    }

    private void executeBinaryOperation(BiFunction<Object, Object, Object> binaryOperation){

        if(stackPointer == 0){
            throw new RuntimeException("binary operation needs two operands.");
        }

        // top most value gets consumed (still there but above the stack pointer so doesn't count)
        stackPointer--;
        stack[stackPointer] = binaryOperation.apply(stack[stackPointer], stack[stackPointer + 1]);
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder(String.format("PC: %d, SP: %d\n", programCounter, stackPointer));
        int nLastToDisplay = 5;

        for(int i = (nLastToDisplay - 1); i >= 0; i--){

            if(stackPointer - i < 0)
                continue;

            res.append("| ").append(stack[stackPointer - i]);
        }

        return res.append("\n").toString();
    }
}
