package loxlang;

import java.util.ArrayList;
import java.util.List;
import static loxlang.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        // Look for a variable declaration
        // If not bubble up the ladder to look for a higher precedence statement.
        try {
            if (match(VAR)) {
                return varDeclaration();
            }
            return statement();
        }
        // If an error occurs do some recovery stuff and try to get to the next declaration/statement
        catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(PRINT)) {
            return printStatement();
        }
        if(match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Print(expr);
    }

    private Stmt varDeclaration() {
        // Consume the identifier token for the variabl
        Token name = consume(IDENTIFIER, "Expect variable name.");

        // If there is no '=' there is no initializer expression and leaves it as null

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume((RIGHT_BRACE), "expect '}' after block.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = equality();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr equality() {
        // Store the first call to comparison() in a local variable.
        Expr expr = comparison();

        // If we don't find a '!=' or '==' we don't have any equality operators.
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            //If we are inside the loop we have found an equality operator

            // Grab the most recent consumed token and store it
            Token operator = previous();
            // Call comparison() to parse the right-hand operand.
            Expr right = comparison();

            // Combine the operator and the two operands into a new Binary Expr
            // And then loop around.
            // Each iteration we store the resulting expression in the same variable
            // Creating a left-associative tree of binary operators.
            expr = new Expr.Binary(expr, operator, right);
        }

        // Returns the expression, if an equality operator is never found
        // It will returns the called comparison().
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // Checks if the current token has any of the given types.
    // If a match is found, consume the token and return true.
    // Otherwise leave it alone and return false.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    // Returns true if the current token is of the given type
    // Does not consume the token, only gazes at it creepily from a distance
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    // Similar to the advance() method in our Scanner
    // Consumes the current token and returns it.
    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    // Checks if we have run out of tokens to parse
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // Returns the current token we have yet to consume.
    private Token peek() {
        return tokens.get(current);
    }

    // Returns the most recently consumed token.
    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
