public abstract class Stmt {

    abstract <T> T accept(Stmt.Visitor<T> visitor);
    static class ExpressionStatement extends Stmt{

        Expr expr;

        public ExpressionStatement(Expr expr){
            this.expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    public interface Visitor<T>{
        T visitExpressionStatement(ExpressionStatement e);
    }
}
