import java.util.List;

public class Main {
    public static void main(String[] args) {

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

        CodeGenerator codeGenerator = new CodeGenerator();
        codeGenerator.environment.define("x", 100);

        Instr[] code = codeGenerator.generateCode(List.of(stmt1, stmt2));

        for(Instr instr : code){
            System.out.println(instr);
        }


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

        System.out.println(vm);
    }
}