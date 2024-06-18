import java.util.List;

public class Main {
    public static void main(String[] args) {

        Stmt stmt1 = new Stmt.VariableDeclaration("int", "x");
        Stmt stmt2 = new Stmt.ExpressionStatement(new Expr.AssignExpr(new Expr.VariableExpr("x"), new Expr.Literal(5)));
        Stmt stmt3 = new Stmt.PrintStatement(new Expr.VariableExpr("x"));

        Stmt stmt4 = new Stmt.VariableDeclaration("int*", "xp");
        Stmt stmt5 = new Stmt.ExpressionStatement(new Expr.AssignExpr(new Expr.VariableExpr("xp"), new Expr.AddressExpr(new Expr.VariableExpr("x"))));

        // print address that pointer points to
        Stmt stmt6 = new Stmt.PrintStatement(new Expr.VariableExpr("xp"));
        Stmt stmt7 = new Stmt.PrintStatement(new Expr.DeRefExpr(new Expr.VariableExpr("xp")));
        Stmt stmt8 = new Stmt.ExpressionStatement(new Expr.AssignExpr(new Expr.DeRefExpr(new Expr.VariableExpr("xp")), new Expr.Literal(10)));
        Stmt stmt9 = new Stmt.PrintStatement(new Expr.VariableExpr("x"));

        // can you modify an array
        Stmt stmt10 = new Stmt.VariableDeclaration("int[]", "y", 5);
        Stmt stmt11 = new Stmt.ExpressionStatement(
                new Expr.AssignExpr(new Expr.VariableExpr("xp"),
                        new Expr.AddressExpr(new Expr.ArrayAccessExpr(new Expr.VariableExpr("y"), new Expr.Literal(3)))));

        Stmt stmt12 = new Stmt.ExpressionStatement(new Expr.AssignExpr(new Expr.DeRefExpr(new Expr.VariableExpr("xp")), new Expr.Literal(22)));
        Stmt stmt13 = new Stmt.PrintStatement(new Expr.ArrayAccessExpr(new Expr.VariableExpr("y"), new Expr.Literal(3)));

        CodeGenerator codeGenerator = new CodeGenerator();

        Code code = codeGenerator.generateCode(List.of(stmt1, stmt2, stmt3, stmt4, stmt5, stmt6, stmt7, stmt8, stmt9, stmt10, stmt11, stmt12, stmt13));

        System.out.println(code);

        VirtualMachine vm = new VirtualMachine();

        vm.execute(code);

        //System.out.println(vm);
    }
}