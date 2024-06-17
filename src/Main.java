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

        Stmt stmt1 = new Stmt.PrintStatement(new Expr.BinOp(new Expr.Literal(5), new Expr.Literal(7), BinaryOperator.PLUS));
        Stmt stmt2 = new Stmt.PrintStatement(new Expr.BinOp(new Expr.Literal(13), new Expr.Literal(7), BinaryOperator.PLUS));
        Stmt stmt3 = new Stmt.PrintStatement(new Expr.Literal("Hello World!"));

        Stmt ifStatement = new Stmt.IfStatement(new Expr.Literal(1), stmt1, stmt2);

        CodeGenerator codeGenerator = new CodeGenerator();
        codeGenerator.environment.define("x", 100);

        Code code = codeGenerator.generateCode(List.of(ifStatement, stmt3));

        //System.out.println(code);

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

        vm.execute(code);

        //System.out.println(vm);
    }
}