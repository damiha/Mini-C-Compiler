import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator implements Expr.Visitor<Code>, Stmt.Visitor<Code>{

    Environment environment;

    // points to first free cell (stack allocation)
    int n;

    static Map<String, Integer> dataTypeToSize = new HashMap<>();

    static {
        dataTypeToSize.put("int", 1);
        dataTypeToSize.put("int*", 1);
    }

    public CodeGenerator(){
        environment = new Environment();
        n = 0;
    }

    // receives an abstract syntax tree
    public Code generateCode(Program program){

        Code code = new Code();

        // TODO: change size of global variables later when structs are added
        int k = 0;
        for(Stmt.VariableDeclaration varDecl : program.globalDeclarations){
            code.addCode(code(varDecl));
            k++;
        }

        // call the main function

        // we first have to move stack pointer not to modify the global variables ( + 1 to reserve for return value)
        code.addInstruction(new Instr.Alloc(k + 1));

        code.addInstruction(new Instr.Mark());

        code.addInstruction(new Instr.LoadC("main"));

        code.addInstruction(new Instr.Call());

        // destroy all global variables after the main call (first stack cell has return value)
        code.addInstruction(new Instr.Slide(k));

        code.addInstruction(new Instr.Halt());

        for(Stmt.FunctionDeclaration funDecl : program.functionDeclarations){
            code.addCode(code(funDecl));
        }

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
    public Code visitArrayAccessExpr(Expr.ArrayAccessExpr arrayAccessExpr, GenerationMode mode) {

        // base + index * block size
        Code code = codeL(arrayAccessExpr.arrayExpr);

        String arrayVarName = arrayAccessExpr.arrayExpr.varName;

        int blockSize = dataTypeToSize.get(getBaseType(environment.getType(arrayVarName)));
        code.addInstruction(new Instr.LoadC(blockSize));

        code.addCode(codeR(arrayAccessExpr.indexExpr));

        code.addInstruction(new Instr.Mul());
        code.addInstruction(new Instr.Add());

        if(mode == GenerationMode.R){
            code.addInstruction(new Instr.Load());
        }

        return code;
    }

    @Override
    public Code visitAddressExpr(Expr.AddressExpr expr, GenerationMode mode) {

        checkNoLValue(mode, "address expression (&) has no l -value");

        // gives the l code (the address) of the expression
        return codeL(expr.expr);
    }

    @Override
    public Code visitDeRefExpr(Expr.DeRefExpr expr, GenerationMode mode) {

        // r value is easy, just load the l value
        // what about l value of a *p ?

        // normally the l-value of the pointer is its address (where the pointer is stored)
        // with * we dereference it and say, give me the l-value of what the pointer is pointing to
        // the pointer value is the
        Code code = codeR(expr.expr);

        if(mode == GenerationMode.R){
            code.addInstruction(new Instr.Load());
        }

        return code;
    }

    @Override
    public Code visitCallExpr(Expr.CallExpr expr, GenerationMode mode) {

        checkNoLValue(mode, "Function evaluation has no l-value");

        Code code = new Code();

        // load the parameters on the stack (left most parameter should be on top)
        int m = 0;
        for(Expr parameterExpr : expr.parameterExpressions.reversed()){
            code.addCode(codeR(parameterExpr));

            // TODO: adjust (we can pass structs and they get copied by value)
            m++;
        }

        // the return value sits on top of all parameters
        // save one spot on the stack for the return value (is exposed after we call slide)
        // functions with void are assumed to return 0
        code.addInstruction(new Instr.LoadC(0));

        // this saves the EP and the old frame pointer?
        code.addInstruction(new Instr.Mark());

        code.addInstruction(new Instr.LoadC(expr.functionName));

        code.addInstruction(new Instr.Call());

        // after return, the return value sits on top of the stack
        // we need to delete the parameter values to get back to initial configuration
        code.addInstruction(new Instr.Slide(m));

        return code;
    }

    @Override
    public Code visitAssignExpr(Expr.AssignExpr assignExpr, GenerationMode mode) {

        checkNoLValue(mode, "assignment expression has no l-value");

        Code value = codeR(assignExpr.value);

        // if the target has no l-value, the assignment code generation fails
        value.addCode(codeL(assignExpr.target));
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

    private String getBaseType(String typeName){
        if(typeName.endsWith("[]")){
            return typeName.substring(0, typeName.length() - 2);
        }
        return typeName;
    }

    @Override
    public Code visitVariableDeclaration(Stmt.VariableDeclaration variableDeclaration) {

        String typeName = variableDeclaration.type;

        int k = variableDeclaration.nElements * dataTypeToSize.get(getBaseType(typeName));

        // n only gets used when new variables are declared

        // variable is saved (starting from address n)
        environment.define(typeName, variableDeclaration.variableName, n);
        n += k;

        Code code = new Code();
        code.addInstruction(new Instr.Alloc(k));

        return code;
    }

    @Override
    public Code visitFunctionDeclaration(Stmt.FunctionDeclaration functionDeclaration) {

        // TODO: process the parameters that are declared

        Code code = new Code();
        code.registerFunction(functionDeclaration.functionName);
        code.addCode(code(functionDeclaration.body));

        return code;
    }

    @Override
    public Code visitReturnStatement(Stmt.ReturnStatement returnStatement) {

        Code code = returnStatement.expr != null ? codeR(returnStatement.expr) : new Code();

        // TODO: copy topmost stack value to destination of return

        code.addInstruction(new Instr.Return());

        return code;
    }
}
