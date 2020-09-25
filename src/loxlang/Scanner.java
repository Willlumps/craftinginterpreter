package loxlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static loxlang.TokenType.*;

public class Scanner {
    // Raw source code stored as a string
    private final String source;
    // List used to store generated tokens
    private final List<Token> tokens = new ArrayList<>();
    // Start and current fields are offsets in the string
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Constructor
    Scanner (String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        // Loop until we have reached the end of the source code
        while (!isAtEnd()) {
            // Beginning of the next lexeme
            start = current;
            // Scan for next token
            scanToken();
        }
        // Add one final "end of file" token
        // Not needed but it makes the parser a little cleaner
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
        }
    }

    // Returns true if we have reached the end of the source code
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Consumes the next character in the source file and returns it
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Grabs the text of the current lexeme and creates a token for it.
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
