public abstract class Expr {

    abstract <T> T accept(Visitor<T> visitor, GenerationMode mode);

    static class Literal extends Expr{
        Object value;

        public Literal(Object value){
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitLiteral(this, mode);
        }
    }
    static class BinOp extends Expr{
        Expr left, right;
        BinaryOperator operator;

        public BinOp(Expr left, Expr right, BinaryOperator operator){
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitBinary(this, mode);
        }
    }

    static class AssignExpr extends Expr{
        VariableExpr target;
        Expr value;

        public AssignExpr(VariableExpr target, Expr value){
            this.target = target;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitAssignExpr(this, mode);
        }
    }

    static class VariableExpr extends Expr{
        String varName;

        public VariableExpr(String varName){
            this.varName = varName;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitVariableExpr(this, mode);
        }
    }
    interface Visitor<T>{
        T visitLiteral(Literal literal, GenerationMode mode);
        T visitBinary(BinOp binOp, GenerationMode mode);
        T visitAssignExpr(AssignExpr assignExpr, GenerationMode mode);
        T visitVariableExpr(VariableExpr variableExpr, GenerationMode mode);
    }
}
