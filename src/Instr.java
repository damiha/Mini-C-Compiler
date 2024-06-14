public abstract class Instr {
    static class Halt extends Instr{
        @Override
        public String toString() {
            return "Halt";
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
    static class Mul extends Instr{
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
}
