package com.barabashkastuff.pzks.calculator.analyzer;

import com.barabashkastuff.pzks.calculator.domain.Token;
import com.barabashkastuff.pzks.calculator.domain.TokenType;
import com.barabashkastuff.pzks.calculator.exception.LexicalException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * LexicalAnalyzer Class
 *
 * @author Andrew S. Slepakurov
 * @version 27/09/2014
 */
@Component
@Scope("prototype")
public class LexicalAnalyzer implements IProcessor {
    private int currLinePosition = 1;
    private int currAbsolutePosition = 0;
    //  Consume
    private String expression;
    //  Produce
    private List<Token> tokens = new ArrayList<Token>();

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void process() throws LexicalException {
        for (; ; ) {
            Token token = getNextToken();
            if (token.getTokenType() == TokenType.EOE) {
                break;
            }
            tokens.add(token);
        }
    }

    private int getLength() {
        return expression.length();
    }

    private Token getNextToken() throws LexicalException {
        Token token = lexicProcessor();
        currLinePosition += token.getValue().length();
        currAbsolutePosition += token.getValue().length();
        return token;
    }

    private Token lexicProcessor() throws LexicalException {
        StringBuilder buffer = new StringBuilder();
        for (; currAbsolutePosition < getLength(); currAbsolutePosition++) {
            char currChar = expression.charAt(currAbsolutePosition);
            switch (currChar) {
                case ' ':
                    currLinePosition++;
                    continue;
                case '+':
                    return new Token(currLinePosition, currChar + "", TokenType.ADD);
                case '-':
                    return new Token(currLinePosition, currChar + "", TokenType.SUB);
                case '*':
                    return new Token(currLinePosition, currChar + "", TokenType.MULT);
                case '/':
                    return new Token(currLinePosition, currChar + "", TokenType.DIV);
                case '(':
                    return new Token(currLinePosition, currChar + "", TokenType.LEFT_BRACKET);
                case ')':
                    return new Token(currLinePosition, currChar + "", TokenType.RIGHT_BRACKET);

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    boolean isFloat = false;
                    TokenType type = TokenType.INT;
                    for (int tmpPos = currAbsolutePosition; tmpPos < getLength(); tmpPos++) {
                        currChar = expression.charAt(tmpPos);
                        if (currChar == '.') {
                            if (!isFloat) {
                                isFloat = true;
                                type = TokenType.FLOAT;
                                buffer.append((buffer.length() == 0) ? '0' + currChar : currChar + "");
                            } else {
                                throw new LexicalException("Wrong float expression at " + currLinePosition);
                            }
                        } else if (currChar >= '0' && currChar <= '9') {
                            buffer.append(currChar);
                        } else {
                            break;
                        }
                    }
                    if (buffer.toString().endsWith(".")) {
                        throw new LexicalException("Wrong float expression at " + currLinePosition);
                    }
                    return new Token(currLinePosition, buffer.toString(), type);
                default:
                    for (int tmpPos = currAbsolutePosition; tmpPos < getLength(); tmpPos++) {
                        currChar = expression.charAt(tmpPos);
                        if ((currChar >= 'a' && currChar <= 'z') ||
                                (currChar >= 'A' && currChar <= 'Z') ||
                                (currChar >= '0' && currChar <= '9')) {
                            buffer.append(currChar);
                        } else if ("!@#$%^&,~".contains("" + currChar)) {
                            throw new LexicalException("Wrong symbol at " + currLinePosition);
                        } else {
                            break;
                        }
                    }
                    return new Token(currLinePosition, buffer.toString(), TokenType.ID);
            }
        }
        return new Token(currLinePosition, "", TokenType.EOE);
    }
}
