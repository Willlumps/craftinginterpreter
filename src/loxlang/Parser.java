package loxlang;

import java.util.List;
import static loxlang.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equality();
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
}
