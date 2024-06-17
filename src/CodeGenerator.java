import java.util.ArrayList;
import java.util.List;

public class CodeGenerator implements Expr.Visitor<Code>, Stmt.Visitor<Code>{

    Environment environment;

    // points to first free cell (stack allocation)
    int n;

    public CodeGenerator(){
        environment = new Environment();
        n = 0;
    }

    // receives an abstract syntax tree
    public Code generateCode(List<Stmt> statements){

        Code code = new Code();

        for(Stmt statement : statements){
            code.addCode(code(statement));
        }

        code.addInstruction(new Instr.Halt());

        return code;
    }

    public Code code(Stmt statement){
        return statement.accept(this);
    }

    public Code codeR(Expr expr){
        return expr.accept(this, GenerationMode.R);
    }

    public Code codeL(Expr expr){
        return expr.accept(this, GenerationMode.L);
    }

    @Override
    public Code visitLiteral(Expr.Literal literal, GenerationMode mode) {

        checkNoLValue(mode, "Literal has no l-value.");

        return new Code(List.of(new Instr.LoadC(literal.value)));
    }

    private void checkNoLValue(GenerationMode mode, String message){
        if(mode == GenerationMode.L){
            throw new RuntimeException(message);
        }
    }

    @Override
    public Code visitBinary(Expr.BinOp binOp, GenerationMode mode) {

        checkNoLValue(mode, "Binary operation has no l-value");

        Code left = codeR(binOp.left);
        Code right = codeR(binOp.right);

        left.addCode(right);

        switch(binOp.operator){
            case BinaryOperator.PLUS:
                left.addInstruction(new Instr.Add());
                break;
            case BinaryOperator.LESS_EQUAL:
                left.addInstruction(new Instr.LessOrEqual());
                break;
            case BinaryOperator.EQUAL:
                left.addInstruction(new Instr.Equal());
                break;
            default:
                throw new RuntimeException("Unknown operator");
        }

        return left;
    }

    @Override
    public Code visitVariableExpr(Expr.VariableExpr variableExpr, GenerationMode mode) {

        if(mode == GenerationMode.L){
            return new Code(List.of(new Instr.LoadC(environment.getAddress(variableExpr.varName))));
        }
        else{
            return new Code(List.of(new Instr.LoadC(environment.getAddress(variableExpr.varName)), new Instr.Load()));
        }
    }

    @Override
    public Code visitAssignExpr(Expr.AssignExpr assignExpr, GenerationMode mode) {

        checkNoLValue(mode, "assignment expression has no l-value");

        Code value = codeR(assignExpr.value);
        Code target = codeL(assignExpr.target);

        value.addCode(target);
        value.addInstruction(new Instr.Store());

        return value;
    }

    @Override
    public Code visitExpressionStatement(Stmt.ExpressionStatement e) {

        Code exprCode = codeR(e.expr);
        exprCode.addInstruction(new Instr.Pop());

        return exprCode;
    }

    @Override
    public Code visitIfStatement(Stmt.IfStatement ifStatement) {

        if(ifStatement.elseBranch != null){
            if(ifStatement.ifBranch == null){
                throw new RuntimeException("Needs to have an if branch to have an else branch");
            }

            return translateIfElse(ifStatement);
        }

        return translateIfWithoutElse(ifStatement);
    }

    private Code translateIfWithoutElse(Stmt.IfStatement ifStatement){
        Code code = codeR(ifStatement.condition);

        Instr.JumpZ jumpOverIfBlock = new Instr.JumpZ(-1);
        code.addInstruction(jumpOverIfBlock);

        Code ifCode = code(ifStatement.ifBranch);
        code.addCode(ifCode);

        // now we can create the jump label and set it
        int jumpLabel = code.addJumpLabelAtEnd();
        jumpOverIfBlock.jumpLabel = jumpLabel;

        return code;
    }

    private Code translateIfElse(Stmt.IfStatement ifStatement){

        Code code = codeR(ifStatement.condition);

        Instr.JumpZ jumpToElse = new Instr.JumpZ(-1);
        code.addInstruction(jumpToElse);

        Code ifCode = code(ifStatement.ifBranch);
        code.addCode(ifCode);

        Instr.Jump jumpOverElse = new Instr.Jump(-1);
        code.addInstruction(jumpOverElse);

        jumpToElse.jumpLabel = code.addJumpLabelAtEnd();

        code.addCode(code(ifStatement.elseBranch));
        jumpOverElse.jumpLabel = code.addJumpLabelAtEnd();

        return code;
    }

    @Override
    public Code visitBlockStatement(Stmt.BlockStatement blockStatement) {
        Code code = new Code();

        for(Stmt instruction : blockStatement.statements){
            code.addCode(code(instruction));
        }

        return code;
    }

    @Override
    public Code visitPrintStatement(Stmt.PrintStatement printStatement) {

        Code code = codeR(printStatement.expr);
        code.addInstruction(new Instr.Print());

        return code;
    }

    @Override
    public Code visitWhileStatement(Stmt.WhileStatement whileStatement) {

        Code code = new Code();
        int jumpLabelBeforeCondition = code.addJumpLabelAtEnd();

        code.addCode(codeR(whileStatement.condition));
        Instr.JumpZ jumpOverBody = new Instr.JumpZ(-1);
        code.addInstruction(jumpOverBody);

        code.addCode(code(whileStatement.body));
        code.addInstruction(new Instr.Jump(jumpLabelBeforeCondition));
        jumpOverBody.jumpLabel = code.addJumpLabelAtEnd();

        return code;
    }
}
