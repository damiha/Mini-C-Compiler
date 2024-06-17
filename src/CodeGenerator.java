import java.util.ArrayList;
import java.util.List;

public class CodeGenerator implements Expr.Visitor<List<Instr>>, Stmt.Visitor<List<Instr>>{

    Environment environment;

    // points to first free cell (stack allocation)
    int n;

    public CodeGenerator(){
        environment = new Environment();
        n = 0;
    }

    // receives an abstract syntax tree
    public Instr[] generateCode(List<Stmt> statements){

        List<Instr> instructions = new ArrayList<>();

        for(Stmt statement : statements){
            instructions.addAll(code(statement));
        }

        instructions.add(new Instr.Halt());

        return instructions.toArray(new Instr[0]);
    }

    public List<Instr> code(Stmt statement){
        return statement.accept(this);
    }

    public List<Instr> codeR(Expr expr){
        return expr.accept(this, GenerationMode.R);
    }

    public List<Instr> codeL(Expr expr){
        return expr.accept(this, GenerationMode.L);
    }

    @Override
    public List<Instr> visitLiteral(Expr.Literal literal, GenerationMode mode) {

        checkNoLValue(mode, "Literal has no l-value.");

        return new ArrayList<Instr>(List.of(new Instr.LoadC(literal.value)));
    }

    private void checkNoLValue(GenerationMode mode, String message){
        if(mode == GenerationMode.L){
            throw new RuntimeException(message);
        }
    }

    @Override
    public List<Instr> visitBinary(Expr.BinOp binOp, GenerationMode mode) {

        checkNoLValue(mode, "Binary operation has no l-value");

        List<Instr> left = codeR(binOp.left);
        List<Instr> right = codeR(binOp.right);

        left.addAll(right);

        switch(binOp.operator){
            case BinaryOperator.PLUS:
                left.add(new Instr.Add());
                break;
            default:
                throw new RuntimeException("Unknown operator");
        }

        return left;
    }

    @Override
    public List<Instr> visitVariableExpr(Expr.VariableExpr variableExpr, GenerationMode mode) {

        if(mode == GenerationMode.L){
            return new ArrayList<>(List.of(new Instr.LoadC(environment.getAddress(variableExpr.varName))));
        }
        else{
            return new ArrayList<>(List.of(new Instr.LoadC(environment.getAddress(variableExpr.varName)), new Instr.Load()));
        }
    }

    @Override
    public List<Instr> visitAssignExpr(Expr.AssignExpr assignExpr, GenerationMode mode) {

        checkNoLValue(mode, "assignment expression has no l-value");

        List<Instr> value = codeR(assignExpr.value);
        List<Instr> target = codeL(assignExpr.target);

        value.addAll(target);
        value.add(new Instr.Store());

        return value;
    }

    @Override
    public List<Instr> visitExpressionStatement(Stmt.ExpressionStatement e) {

        List<Instr> exprCode = codeR(e.expr);
        exprCode.add(new Instr.Pop());

        return exprCode;
    }

    @Override
    public List<Instr> visitIfStatement(Stmt.IfStatement ifStatement) {

        // TODO

        return List.of();
    }

    @Override
    public List<Instr> visitBlockStatement(Stmt.BlockStatement blockStatement) {
        List<Instr> instructions = new ArrayList<>();

        for(Stmt instruction : blockStatement.statements){
            instructions.addAll(code(instruction));
        }

        return instructions;
    }
}