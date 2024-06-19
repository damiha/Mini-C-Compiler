import java.util.List;

public class Main {
    public static void main(String[] args) {

        Stmt.FunctionDeclaration factorialDef = new Stmt.FunctionDeclaration("int", "factorial",
                List.of(
                        new Stmt.VariableDeclaration("int", "n")
                ), new Stmt.BlockStatement(
                        List.of(
                                new Stmt.IfStatement(new Expr.BinOp(new Expr.VariableExpr("n"), new Expr.Literal(1), BinaryOperator.LESS_EQUAL),
                                        new Stmt.ReturnStatement(new Expr.Literal(1)), new Stmt.BlockStatement(
                                                List.of(
                                new Stmt.VariableDeclaration("int", "z"),
                                new Stmt.ExpressionStatement(new Expr.AssignExpr(new Expr.VariableExpr("z"),
                                        new Expr.BinOp(new Expr.VariableExpr("n"), new Expr.CallExpr("factorial", List.of(new Expr.BinOp(new Expr.VariableExpr("n"), new Expr.Literal(-1), BinaryOperator.PLUS))), BinaryOperator.MUL))),
                                new Stmt.ReturnStatement(new Expr.VariableExpr("z"))
                                ))))
                        )
        );


        Stmt.FunctionDeclaration mainDef = new Stmt.FunctionDeclaration("int", "main",
                List.of(), new Stmt.BlockStatement(
                List.of(
                        new Stmt.PrintStatement(new Expr.CallExpr("factorial", List.of(new Expr.Literal(5)))),
                        new Stmt.PrintStatement(new Expr.Literal("done with main"))
        )));

        Program program = new Program(List.of(), List.of(mainDef, factorialDef));

        CodeGenerator codeGenerator = new CodeGenerator();

        Code code = codeGenerator.generateCode(program);

        System.out.println(code);

        VirtualMachine vm = new VirtualMachine();

        vm.execute(code);

        //System.out.println(vm);
    }
}