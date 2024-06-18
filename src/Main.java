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

        // int[5] x;
        Stmt varDecl = new Stmt.VariableDeclaration("int[]", "x", 5);

        Expr.ArrayAccessExpr accessZero = new Expr.ArrayAccessExpr(new Expr.VariableExpr("x"), new Expr.Literal(0));
        Expr.ArrayAccessExpr accessFour = new Expr.ArrayAccessExpr(new Expr.VariableExpr("x"), new Expr.Literal(4));

        Stmt varInit = new Stmt.ExpressionStatement(new Expr.AssignExpr(accessZero, new Expr.Literal(1)));
        Stmt varInit2 = new Stmt.ExpressionStatement(new Expr.AssignExpr(accessFour, new Expr.Literal(0)));

        // print x
        // x++;
        // y = y + x;
        Stmt body = new Stmt.BlockStatement(
                List.of(new Stmt.ExpressionStatement(new Expr.AssignExpr(accessFour, new Expr.BinOp(accessFour, accessZero, BinaryOperator.PLUS))),
                        new Stmt.PrintStatement(accessFour),
                        new Stmt.ExpressionStatement(new Expr.AssignExpr(accessZero, new Expr.BinOp(accessZero, new Expr.Literal(1), BinaryOperator.PLUS)))
                        )
        );

        // while x <= 5
        Stmt whileLoop = new Stmt.WhileStatement(new Expr.BinOp(accessZero, new Expr.Literal(5), BinaryOperator.LESS_EQUAL),
                body);

        CodeGenerator codeGenerator = new CodeGenerator();

        Code code = codeGenerator.generateCode(List.of(varDecl, varInit, varInit2, whileLoop));

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

        vm.execute(code);

        //System.out.println(vm);
    }
}