import java.util.List;

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
        final Expr target;
        final Expr value;

        public AssignExpr(Expr target, Expr value){
            this.target = target;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitAssignExpr(this, mode);
        }
    }

    // the ! operator
    static class NegatedExpr extends Expr{
        final Expr expr;

        public NegatedExpr(Expr expr){
            this.expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitNegatedExpr(this, mode);
        }
    }

    // C's & operator
    static class AddressExpr extends Expr{
        final Expr expr;

        public AddressExpr(Expr expr){
            this.expr = expr;
        }


        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitAddressExpr(this, mode);
        }
    }

    // C's * operator
    static class DeRefExpr extends Expr{
        final Expr expr;

        public DeRefExpr(Expr expr){
            this.expr = expr;
        }


        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitDeRefExpr(this, mode);
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

    static class CallExpr extends Expr{

        String functionName;
        List<Expr> parameterExpressions;

        public CallExpr(String functionName, List<Expr> parameterExpressions){
            this.functionName = functionName;
            this.parameterExpressions = parameterExpressions;
        }


        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitCallExpr(this, mode);
        }
    }

    static class ArrayAccessExpr extends Expr{

        VariableExpr arrayExpr;
        Expr indexExpr;

        public ArrayAccessExpr(VariableExpr arrayExpr, Expr indexExpr){
            this.arrayExpr = arrayExpr;
            this.indexExpr = indexExpr;
        }


        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitArrayAccessExpr(this, mode);
        }
    }
    interface Visitor<T>{
        T visitLiteral(Literal literal, GenerationMode mode);
        T visitBinary(BinOp binOp, GenerationMode mode);
        T visitAssignExpr(AssignExpr assignExpr, GenerationMode mode);
        T visitVariableExpr(VariableExpr variableExpr, GenerationMode mode);
        T visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr, GenerationMode mode);
        T visitAddressExpr(AddressExpr expr, GenerationMode mode);
        T visitDeRefExpr(DeRefExpr expr, GenerationMode mode);
        T visitCallExpr(CallExpr expr, GenerationMode mode);
        T visitNegatedExpr(NegatedExpr expr, GenerationMode mode);
    }
}
