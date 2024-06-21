import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {

    List<Token> tokens;

    int current = 0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private boolean isAtEnd(){
        return current >= tokens.size() || peek().type == TokenType.EOF;
    }

    private boolean check(TokenType type){
        return !isAtEnd() && peek().type == type;
    }

    // check = checkAtK with k = 0
    // realizes a lookahead
    private boolean checkAtK(TokenType type, int k){
        return (current + k < tokens.size()) && ((tokens.get(current + k)).type == type);
    }

    private boolean checkSequence( int start, TokenType... types){
        for(int i = 0; i < types.length; i++){
            if(!checkAtK(types[i], start + i)){
                return false;
            }
        }
        return true;
    }

    private boolean checkTypes(TokenType... types){
        for(TokenType t : types){
            if(check(t)){
                return true;
            }
        }
        return false;
    }

    private void advance(){
        current++;
    }

    private boolean match(TokenType type){

        if(check(type)){
            advance();
            return true;
        }

        return false;
    }

    private Token consume(TokenType type, String message){
        if(match(type)){
            return previous();
        }
        error(message);
        return null;
    }

    public Program parse(){

        List<Stmt> topLevelStatements = program();

        List<Stmt.VariableDeclaration> variableDeclarations = new ArrayList<>();
        List<Stmt.FunctionDeclaration> functionDeclarations = new ArrayList<>();

        for(Stmt stmt : topLevelStatements){
            if(stmt instanceof Stmt.VariableDeclaration){
                variableDeclarations.add((Stmt.VariableDeclaration) stmt);
            }
            else if(stmt instanceof Stmt.FunctionDeclaration){
                functionDeclarations.add((Stmt.FunctionDeclaration)stmt);
            }
            else{
                error("Top level statements can only be variable or function declarations");
            }
        }

        return new Program(variableDeclarations, functionDeclarations);
    }

    private List<Stmt> program(){

        List<Stmt> declarations = new ArrayList<>();

        while(!isAtEnd()){
            declarations.add(declaration());
        }

        return declarations;
    }

    // we separate declaration and statement because we don't want to allow variable declarations in if blocks for example
    private Stmt declaration(){

        if(isVariableDeclaration()){
            return variableDeclaration();
        }
        else if(isFunctionDeclaration()){
            return functionDeclaration();
        }
        return statement();
    }

    private boolean isVariableDeclaration(){

        // variables need to begin with their data type
        // TODO: support more data types
        if(!check(TokenType.INT)){
            return false;
        }

        if(checkAtK(TokenType.STAR, 1)){
            // pointer declaration
            // int* name (and then a left paren would signal a function that returns a pointer)
            return checkAtK(TokenType.IDENTIFIER, 2) && !checkAtK(TokenType.LEFT_PAREN, 3);
        }
        else{
            // regular variable or array (int arr[ is sufficient for parsing)
            return checkAtK(TokenType.IDENTIFIER, 1) && !checkAtK(TokenType.LEFT_PAREN, 2);
        }
    }

    private Stmt variableDeclaration(){
        return variableDeclaration(false);
    }

    private Stmt variableDeclaration(boolean functionDeclaration){

        String type = consume(TokenType.INT, "Variable declaration must start with a type").lexeme;

        if(match(TokenType.STAR)){
            type += "*";
        }

        String varName = consume(TokenType.IDENTIFIER, "Type must be followed by variable name.").lexeme;

        if(match(TokenType.LEFT_BRACKET)){

            if(functionDeclaration){
                error("Arrays need to be passed to functions as pointers");
            }

            type += "[]";

            // past the first bracket
            int nElements = (Integer) consume(TokenType.NUMBER, "Array size must be specified as a number.").value;

            consume(TokenType.RIGHT_BRACKET, "Closing bracket missing");
            consume(TokenType.SEMICOLON, "; expected");

            return new Stmt.VariableDeclaration(type, varName, nElements);
        }


        Expr initializer = null;
        // either a regular variable or a pointer
        if(match(TokenType.EQUAL)){

            if(functionDeclaration){
                error("C does not support default parameters.");
            }
            initializer = expression();
        }

        if(!functionDeclaration) {
            consume(TokenType.SEMICOLON, "Variable declaration needs to end with ;");
        }

        return new Stmt.VariableDeclaration(type, varName, initializer);
    }

    private void error(String message){
        throw new RuntimeException(String.format("[%d] %s", peek().line, message));
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){

        Expr expr = logic_or();

        if(match(TokenType.EQUAL)){
            Expr value = assignment();

            return new Expr.AssignExpr(expr, value);
        }
        return expr;
    }

    private Expr logic_or(){

        Expr expr = logic_and();

        while(match(TokenType.DOUBLE_PIPE)){
            Expr rightHandSide = logic_and();
            expr = new Expr.BinOp(expr, rightHandSide, BinaryOperator.OR);
        }

        return expr;
    }

    private Expr logic_and(){
        Expr expr = equality();

        while(match(TokenType.DOUBLE_AMPERSAND)){
            Expr rightHandSide = equality();
            expr = new Expr.BinOp(expr, rightHandSide, BinaryOperator.AND);
        }

        return expr;
    }

    private Expr equality(){
        Expr expr = comparison();

        while(checkTypes(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)){

            BinaryOperator operator = null;
            if(match(TokenType.EQUAL_EQUAL)) {
                operator = BinaryOperator.EQUAL;
            }
            // second match is important to advance the current pointer?
            else if(match(TokenType.BANG_EQUAL)){
                operator = BinaryOperator.UNEQUAL;
            }
            Expr rightHandSide = comparison();
            expr = new Expr.BinOp(expr, rightHandSide, operator);
        }

        return expr;
    }

    private Expr comparison(){
        Expr expr = term();

        while(checkTypes(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)){

            BinaryOperator operator = null;
            if(match(TokenType.LESS)) {
                operator = BinaryOperator.LESS;
            }
            else if(match(TokenType.LESS_EQUAL)){
                operator = BinaryOperator.LESS_EQUAL;
            }
            else if(match(TokenType.GREATER_EQUAL)){
                operator = BinaryOperator.GREATER_EQUAL;
            }
            else if(match(TokenType.GREATER)){
                operator = BinaryOperator.GREATER;
            }
            Expr rightHandSide = term();
            expr = new Expr.BinOp(expr, rightHandSide, operator);
        }

        return expr;
    }

    private Expr term(){
        Expr expr = factor();

        while(checkTypes(TokenType.PLUS, TokenType.MINUS)){

            BinaryOperator operator = null;
            if(match(TokenType.PLUS)) {
                operator = BinaryOperator.PLUS;
            }
            // second match is important to advance the current pointer?
            else if(match(TokenType.MINUS)){
                operator = BinaryOperator.MINUS;
            }
            Expr rightHandSide = factor();
            expr = new Expr.BinOp(expr, rightHandSide, operator);
        }

        return expr;
    }

    private Expr factor(){

        Expr expr = unary();

        while(checkTypes(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)){

            BinaryOperator operator = null;
            if(match(TokenType.STAR)) {
                operator = BinaryOperator.MUL;
            }
            else if(match(TokenType.SLASH)){
                operator = BinaryOperator.DIV;
            }
            else if(match(TokenType.PERCENT)){
                operator = BinaryOperator.MOD;
            }
            Expr rightHandSide = unary();
            expr = new Expr.BinOp(expr, rightHandSide, operator);
        }

        return expr;
    }

    private Expr unary(){

        if(match(TokenType.BANG)){
            return new Expr.NegatedExpr(unary());
        }
        else if(match(TokenType.MINUS)){
            return new Expr.UnaryMinusExpr(unary());
        }
        else if(match(TokenType.AMPERSAND)){
            return new Expr.AddressExpr(unary());
        }
        else if(match(TokenType.STAR)){
            return new Expr.DeRefExpr(unary());
        }
        return call();
    }

    private Expr call(){

        Expr expr = primary();

        if(match(TokenType.LEFT_PAREN)){
            // real call
            List<Expr> arguments = arguments();
            consume(TokenType.RIGHT_PAREN, "Missing ).");

            String functionName = ((Expr.VariableExpr)expr).varName;

            return new Expr.CallExpr(functionName, arguments);
        }
        else if(match(TokenType.LEFT_BRACKET)){
            // array indexing
            Expr accessExpr = expression();

            consume(TokenType.RIGHT_BRACKET, "Missing ]");

            return new Expr.ArrayAccessExpr((Expr.VariableExpr)expr, accessExpr);
        }
        return expr;
    }

    private Expr primary(){

        if(match(TokenType.STRING) || match(TokenType.NUMBER)){
            return new Expr.Literal(previous().value);
        }
        else if(match(TokenType.IDENTIFIER)){
            return new Expr.VariableExpr(previous().lexeme);
        }
        else {
            // must be a grouping
            consume(TokenType.LEFT_PAREN,"Grouping expr must start with (");
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Opening ( needs to be closed.");
            return expr;
        }
    }

    private List<Expr> arguments(){

        List<Expr> arguments = new ArrayList<>();

        // no arguments in call
        if(match(TokenType.RIGHT_PAREN)){
            return arguments;
        }

        // at least one argument
        do {
            arguments.add(expression());
        } while (match(TokenType.COMMA));

        return arguments;
    }

    private boolean isFunctionDeclaration(){

        // add more datatypes
        if(!check(TokenType.INT)){
            return false;
        }

        if(checkAtK(TokenType.STAR, 1)){
            return checkSequence(2, TokenType.IDENTIFIER, TokenType.LEFT_PAREN);
        }

        return checkSequence(1, TokenType.IDENTIFIER, TokenType.LEFT_PAREN);
    }

    private Stmt functionDeclaration(){

        String returnType = consume(TokenType.INT, "function needs return type").lexeme;

        if(match(TokenType.STAR)){
            returnType += "*";
        }

        String functionName = consume(TokenType.IDENTIFIER, "function needs name").lexeme;

        consume(TokenType.LEFT_PAREN, "( expected after function name");

        // get variable declarations
        List<Stmt.VariableDeclaration> variableDeclarationsInFunctionDeclaration = variableDeclarationsInFunctionDeclaration();

        consume(TokenType.RIGHT_PAREN, "Closing ) expected.");

        consume(TokenType.LEFT_BRACE, "Function body needs to start with {");
        Stmt.BlockStatement body = blockStatement();

        return new Stmt.FunctionDeclaration(returnType, functionName, variableDeclarationsInFunctionDeclaration, body);
    }

    private Stmt statement(){

        if(match(TokenType.PRINT)){
            return printStatement();
        }
        else if(match(TokenType.IF)){
            return ifStatement();
        }
        else if(match(TokenType.WHILE)){
            return whileStatement();
        }
        else if(match(TokenType.RETURN)){
            return returnStatement();
        }
        else if(match(TokenType.LEFT_BRACE)){
            return blockStatement();
        }
        return expressionStatement();
    }

    private Stmt printStatement(){
        consume(TokenType.LEFT_PAREN, "( needed after print");
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "Closing ) needed");
        consume(TokenType.SEMICOLON, "missing ;");
        return new Stmt.PrintStatement(expr);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Statement must end with ;");
        return new Stmt.ExpressionStatement(expr);
    }

    private Stmt ifStatement(){

        consume(TokenType.LEFT_PAREN, "( is needed before condition");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, ") needed after condition");

        // if it's a block ({}), then the braces will be automatically consumed
        Stmt ifBranch = statement();

        Stmt elseBranch = null;
        if(match(TokenType.ELSE)){
            elseBranch = statement();
        }

        return new Stmt.IfStatement(condition, ifBranch, elseBranch);
    }

    private Stmt whileStatement(){

        consume(TokenType.LEFT_PAREN, "( needs to come after while");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, ") needs to come after condition");

        Stmt body = statement();

        return new Stmt.WhileStatement(condition, body);
    }

    private Stmt returnStatement(){
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Return statement needs to end with ;");
        return new Stmt.ReturnStatement(expr);
    }

    // assumes that the opening { has already been removed
    private Stmt.BlockStatement blockStatement(){

        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE)){
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Block statement must end with }");

        return new Stmt.BlockStatement(statements);
    }

    private List<Stmt.VariableDeclaration> variableDeclarationsInFunctionDeclaration(){
        List<Stmt.VariableDeclaration> vars = new ArrayList<>();

        if(isVariableDeclaration()){
            vars.add((Stmt.VariableDeclaration)variableDeclaration(true));
        }

        while(match(TokenType.COMMA)){
            vars.add((Stmt.VariableDeclaration)variableDeclaration(true));
        }

        return vars;
    }
}
