import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.Map;

public class VirtualMachine {

    int stackSize = 1000;
    int instructionSize = 200;

    Object[] stack;
    Instr[] codeStore;
    Map<Integer, Integer> jumpTable;
    Map<String, Integer> functionTable;

    Instr instructionRegister;

    int stackPointer;

    // TODO: use the extreme pointer to detect stack overflows!
    int extremePointer;
    int framePointer;
    int programCounter;

    boolean isRunning;

    boolean debugPrintActivated = false;

    public VirtualMachine(){
        stack = new Object[stackSize];
        codeStore = new Instr[instructionSize];
    }

    private void init(Code code){

        jumpTable = code.jumpTable;
        functionTable = code.functionNamesToCodeStart;

        Instr[] instructionsArray = code.instructions.toArray(new Instr[0]);

        System.arraycopy(instructionsArray, 0, codeStore, 0, instructionsArray.length);
        stackPointer = -1;
        framePointer = -1;
        extremePointer = -1;
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

            // return value from main
            System.out.printf("VM: exited with code %s\n", stack[0]);
        }
        else if(instruction instanceof Instr.JumpZ){
            if((Integer)stack[stackPointer] == 0){
                programCounter = jumpTable.get(((Instr.JumpZ) instruction).jumpLabel);
                stackPointer--;
            }
        }
        else if(instruction instanceof Instr.Mark){
            // stack pointer currently points to cell reserved for the return value
            stack[stackPointer + 1] = extremePointer;

            // old frame pointer to return stack frame
            stack[stackPointer + 2] = framePointer;

            stackPointer += 2;
        }
        else if(instruction instanceof Instr.Call){
            // the name of the function lies on the topmost stack value
            int oldPC = programCounter;

            String functionName = (String)stack[stackPointer];

            programCounter = functionTable.get(functionName);

            // the name of the function gets consumed and the old program counter gets stored there
            stack[stackPointer] = oldPC;
            framePointer = stackPointer;
        }
        else if(instruction instanceof Instr.Return){

            // point to return  value
            stackPointer = framePointer - 3;

            programCounter = (Integer)stack[framePointer];
            extremePointer = (Integer)(stack[framePointer - 2]);
            framePointer = (Integer)stack[framePointer - 1];
        }
        else if(instruction instanceof Instr.Slide){

            int m = ((Instr.Slide)instruction).m;

            if(stackPointer < m){
                throw new RuntimeException("Slide m and m > size of stack.");
            }

            stack[stackPointer - m] = stack[stackPointer];
            stackPointer = stackPointer - m;
        }
        else if(instruction instanceof Instr.LoadRC){
            int relativeAddress = ((Instr.LoadRC)instruction).j;

            int absoluteAddress = relativeAddress + framePointer;
            stackPointer++;
            stack[stackPointer] = absoluteAddress;
        }
        else if(instruction instanceof Instr.Alloc){
            stackPointer += ((Instr.Alloc) instruction).k;
        }
        // unconditional jump
        else if(instruction instanceof Instr.Jump){
            programCounter = jumpTable.get(((Instr.Jump) instruction).jumpLabel);
            stackPointer--;
        }
        else if(instruction instanceof Instr.Print){
            System.out.printf("VM: %s\n", stack[stackPointer]);
            stackPointer--;
        }
        else if(instruction instanceof Instr.Pop){
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
        else if(instruction instanceof Instr.FlipSign){
            stack[stackPointer] = -((Integer)stack[stackPointer]);
        }
        else if(instruction instanceof Instr.Neg){
            int stackValue = ((Integer)stack[stackPointer]);
            stack[stackPointer] = stackValue == 0 ? 1 : 0;
        }
        else if(instruction instanceof Instr.Add){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) + ((Integer)o2);
                }

                throw new RuntimeException("Add doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Div){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) / ((Integer)o2);
                }

                throw new RuntimeException("Div doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Greater){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) > ((Integer)o2) ? 1 : 0;
                }

                throw new RuntimeException("Greater doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.GreaterOrEqual){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) >= ((Integer)o2) ? 1 : 0;
                }

                throw new RuntimeException("GreaterOrEqual doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.And){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){

                    int notNormalizedValue = ((Integer)o1) * ((Integer)o2);
                    return notNormalizedValue == 0 ? 0 : 1;
                }

                throw new RuntimeException("Add doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Or){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){

                    int o1Val = ((Integer)o1);
                    int o2Val = ((Integer)o2);
                    return ((o1Val != 0) || (o2Val != 0)) ? 1 : 0;
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
        else if(instruction instanceof Instr.Mul){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) * ((Integer)o2);
                }

                throw new RuntimeException("Mul doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Sub){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) - ((Integer)o2);
                }

                throw new RuntimeException("Sub doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Mod){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) % ((Integer)o2);
                }

                throw new RuntimeException("Mod doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.Less){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return ((Integer)o1) < ((Integer)o2) ? 1 : 0;
                }

                throw new RuntimeException("Less doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }
        else if(instruction instanceof Instr.UnEqual){
            executeBinaryOperation((o1, o2) -> {
                if(o1 instanceof Integer && o2 instanceof Integer){
                    return !((Integer) o1).equals((Integer) o2) ? 1 : 0;
                }

                throw new RuntimeException("Less doesn't support combination (" + o1.getClass().getName() + ", " + o2.getClass().getName() + ")");
            });
        }

        else if(instruction instanceof Instr.Store){
            // first is address
            // below is value
            stack[(Integer) stack[stackPointer]] = stack[stackPointer - 1];
            // address is consumed
            stackPointer--;
        }
        else {
            throw new RuntimeException("Unknown Instruction " + instruction);
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
        StringBuilder res = new StringBuilder(String.format("PC: %d, SP: %d, INSTR: %s\n", programCounter, stackPointer, codeStore[programCounter]));
        res.append("STACK: ");
        int nLastToDisplay = 20;

        for(int i = (nLastToDisplay - 1); i >= 0; i--){

            if(stackPointer - i < 0)
                continue;

            res.append("| ").append(stack[stackPointer - i]);
        }

        // if we are not in the base frame, print as well
        if(framePointer >= 0) {
            res.append("\nFRAME: ");

            for (int i = framePointer + 1; i <= stackPointer; i++) {
                res.append("| ").append(stack[i]);
            }
        }

        return res.append("\n").toString();
    }
}
