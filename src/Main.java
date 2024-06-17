import java.util.List;

public class Main {
    public static void main(String[] args) {

        /*
        Stmt stmt1 = new Stmt.ExpressionStatement(
                new Expr.AssignExpr(
                new Expr.VariableExpr("x"),
                new Expr.BinOp(
                        new Expr.Literal(5),
                        new Expr.Literal(10),
                        BinaryOperator.PLUS)
        ));

        Stmt stmt2 = new Stmt.ExpressionStatement(
                new Expr.AssignExpr(
                        new Expr.VariableExpr("x"),
                        new Expr.BinOp(
                             new Expr.VariableExpr("x"),
                             new Expr.Literal(50),
                             BinaryOperator.PLUS
                        )
                )
        );
        */

        // x = 0
        Stmt varInit = new Stmt.ExpressionStatement(new Expr.AssignExpr(new Expr.VariableExpr("x"), new Expr.Literal(0)));


        // print x
        // x++;
        Stmt body = new Stmt.BlockStatement(
                List.of(new Stmt.PrintStatement(new Expr.VariableExpr("x")),
                        new Stmt.ExpressionStatement(new Expr.AssignExpr(new Expr.VariableExpr("x"), new Expr.BinOp(new Expr.VariableExpr("x"), new Expr.Literal(1), BinaryOperator.PLUS)))
                        )
        );

        // while x < 5
        Stmt whileLoop = new Stmt.WhileStatement(new Expr.BinOp(new Expr.VariableExpr("x"), new Expr.Literal(5), BinaryOperator.LESS_EQUAL),
                body);

        CodeGenerator codeGenerator = new CodeGenerator();

        // location = 100
        codeGenerator.environment.define("x", 100);

        Code code = codeGenerator.generateCode(List.of(varInit, whileLoop));

        System.out.println(code);

        VirtualMachine vm = new VirtualMachine();

        // Example 1
        /*
        Instr[] code = {
                new Instr.LoadC(7),
                new Instr.LoadC(1),
                new Instr.Add(),
                new Instr.Halt()
        };
        */

        // vm.execute(code);

        //System.out.println(vm);
    }
}