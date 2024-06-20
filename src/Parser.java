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
        throw new RuntimeException(message);
    }

    public Program parse(){

        List<Stmt> topLevelStatements = program();

        // TODO: transform so that variable declarations are first, then function definitions (CHECK THAT ONLY THOSE TWO ARE ON TOP LEVEL)

        return null;
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

        String type = consume(TokenType.INT, "Variable declaration must start with a type").lexeme;
        boolean isPointer = false;

        if(checkAtK(TokenType.STAR, 1)){
            type += "*";
            advance();
            isPointer = true;
        }

        String varName = consume(TokenType.IDENTIFIER, "Type must be followed by variable name.").lexeme;

        if(check(TokenType.LEFT_BRACKET)){
            if(isPointer){
                throw new RuntimeException("Arrays with pointers as elements are currently not supported.");
            }
            type += "[]";

            advance();
            // past the first bracket
            int nElements = (Integer) consume(TokenType.NUMBER, "Array size must be specified as a number.").value;

            consume(TokenType.RIGHT_BRACKET, "Closing bracket missing");

            return new Stmt.VariableDeclaration(type, varName, nElements);
        }

        // either a regular variable or a pointer
        if(match(TokenType.EQUAL)){
            Expr initializer = expression();
            return new Stmt.VariableDeclaration(type, varName, initializer);
        }

        return new Stmt.VariableDeclaration(type, varName);
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){

        Expr expr = logic_or();

        if(match(TokenType.EQUAL)){
            Expr value = expression();

            return new Expr.AssignExpr(expr, value);
        }
        return expr;
    }

    private Expr logic_or(){

        Expr expr = logic_and();

        while(match(TokenType.DOUBLE_PIPE)){
            Expr rightHandSide = expression();
            expr = new Expr.BinOp(expr, rightHandSide, BinaryOperator.OR);
        }

        return expr;
    }

    private Expr logic_and(){
        Expr expr = equality();

        while(match(TokenType.DOUBLE_AMPERSAND)){
            Expr rightHandSide = expression();
            expr = new Expr.BinOp(expr, rightHandSide, BinaryOperator.AND);
        }

        return expr;
    }

    private Expr equality(){
        Expr expr = comparison();

        while(checkTypes(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)){

            BinaryOperator operator = null;
            if(match(TokenType.EQUAL)) {
                operator = BinaryOperator.EQUAL;
            }
            // second match is important to advance the current pointer?
            else if(match(TokenType.BANG_EQUAL)){
                operator = BinaryOperator.UNEQUAL;
            }
            Expr rightHandSide = expression();
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
            Expr rightHandSide = expression();
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
            Expr rightHandSide = expression();
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
            Expr rightHandSide = expression();
            expr = new Expr.BinOp(expr, rightHandSide, operator);
        }

        return expr;
    }

    private Expr unary(){

        if(match(TokenType.BANG)){
            return new Expr.NegatedExpr(unary());
        }
        else if(match(TokenType.MINUS)){
            // TODO: add unary minus to instruction set (should consume the single stack value)
            // can't be emulated by binary minus (we would need 0 - N but that would mean N is on top and zero is below)
            return null;
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
        }
        else if(match(TokenType.LEFT_BRACKET)){
            // array indexing
        }
        return expr;
    }

    private Expr primary(){
        return null;
    }

    private List<Expr> arguments(){
        return null;
    }

    private boolean isFunctionDeclaration(){
        return false;
    }

    private Stmt functionDeclaration(){
        return null;
    }

    private Stmt statement(){
        return null;
    }
}
