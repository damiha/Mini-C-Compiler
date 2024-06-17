import java.util.List;

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

    static class BlockStatement extends Stmt {
        List<Stmt> statements;

        public BlockStatement(List<Stmt> statements){
            this.statements = statements;
        }


        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    static class IfStatement extends Stmt {
        Expr condition;

        // if there is no else branch, else branch = null
        Stmt ifBranch;
        Stmt elseBranch;

        public IfStatement(Expr condition, Stmt ifBranch, Stmt elseBranch){
            this.condition = condition;
            this.ifBranch = ifBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    public interface Visitor<T>{
        T visitExpressionStatement(ExpressionStatement e);
        T visitIfStatement(IfStatement ifStatement);
        T visitBlockStatement(BlockStatement blockStatement);
    }
}
