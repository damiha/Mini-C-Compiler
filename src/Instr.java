public abstract class Instr {
    static class Halt extends Instr{
        @Override
        public String toString() {
            return "Halt";
        }
    }

    // TODO: add VM interpretation (we don't have extreme pointer yet)
    static class Enter extends Instr{

        @Override
        public String toString(){
            return "Enter";
        }
    }

    static class Mark extends Instr{

        @Override
        public String toString(){
            return "Mark";
        }
    }

    static class Call extends Instr{

        @Override
        public String toString(){
            return "Call";
        }
    }

    static class Return extends Instr{

        @Override
        public String toString(){
            return "Return";
        }
    }

    static class Slide extends Instr{

        int m;

        public Slide(int m){
            this.m = m;
        }

        @Override
        public String toString(){
            return "Slide " + m;
        }
    }

    static class LoadRC extends Instr {

        int j;

        public LoadRC(int j){
            this.j = j;
        }
        public String toString(){
            return "LoadRC " + j;
        }
    }

    static class LoadC extends Instr{
        // object to be loaded into the stack
        Object q;

        public LoadC(Object q){
            this.q = q;
        }

        public String toString(){
            if(q instanceof Integer){
                return "LoadC " + (Integer)q;
            }
            else if(q instanceof Double){
                return String.format("LoadC %.3f", (Double)q);
            }
            else if(q instanceof String){
                return String.format("LoadC '%s'", (String)q);
            }
            return "LoadC";
        }
    }
    static class Add extends Instr{
        @Override
        public String toString() {
            return "Add";
        }
    }

    static class LessOrEqual extends Instr{
        @Override
        public String toString(){
            return "LessOrEqual";
        }
    }
    static class Equal extends Instr{
        @Override
        public String toString(){
            return "Equal";
        }
    }
    static class Mul extends Instr{

        @Override
        public String toString(){
            return "Mul";
        }
    }
    static class Store extends Instr{
        @Override
        public String toString() {
            return "Store";
        }
    }
    static class Pop extends Instr{

        @Override
        public String toString() {
            return "Pop";
        }
    }
    static class Load extends Instr{
        @Override
        public String toString() {
            return "Load";
        }
    }

    static class Neg extends Instr{
        @Override
        public String toString(){ return "Neg"; }
    }

    static class JumpZ extends Instr{

        int jumpLabel;

        public JumpZ(int jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        @Override
        public String toString(){
            return String.format("JumpZ %d", jumpLabel);
        }
    }

    // unconditional jump
    static class Jump extends Instr{
        int jumpLabel;

        public Jump(int jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        public String toString(){
            return String.format("Jump %d", jumpLabel);
        }
    }

    static class Print extends Instr{

        @Override
        public String toString(){
            return "Print";
        }
    }

    static class Alloc extends Instr{

        // memory size
        int k;

        public Alloc(int k){
            this.k = k;
        }

        @Override
        public String toString(){
            return String.format("Alloc %d", k);
        }
    }
}
