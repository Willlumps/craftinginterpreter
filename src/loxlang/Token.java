package loxlang;

class Token {

    final TokenType type; // Type of token
    final String lexeme;  // Pieced together lexeme
    final Object literal; // An object!!..?
    final int line;       // The line it is on

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
