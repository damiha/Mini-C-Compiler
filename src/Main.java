import java.util.List;

public class Main {
    public static void main(String[] args) {

        Stmt.FunctionDeclaration addDef = new Stmt.FunctionDeclaration("int", "add",
                List.of(
                        new Stmt.VariableDeclaration("int", "x"),
                        new Stmt.VariableDeclaration("int", "y")
                ), new Stmt.BlockStatement(
                        List.of(
                                new Stmt.PrintStatement(new Expr.BinOp(new Expr.Literal(5), new Expr.Literal(42), BinaryOperator.PLUS)),
                                new Stmt.ReturnStatement(new Expr.Literal(55))
                                )
                        )
        );

        Stmt.FunctionDeclaration mainDef = new Stmt.FunctionDeclaration("int", "main",
                List.of(), new Stmt.BlockStatement(
                List.of(new Stmt.PrintStatement(new Expr.CallExpr("add", List.of(new Expr.Literal(5), new Expr.Literal(10))))
        )));

        Program program = new Program(List.of(), List.of(mainDef, addDef));

        CodeGenerator codeGenerator = new CodeGenerator();

        Code code = codeGenerator.generateCode(program);

        //System.out.println(code);

        VirtualMachine vm = new VirtualMachine();

        vm.execute(code);

        //System.out.println(vm);
    }
}