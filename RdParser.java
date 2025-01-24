import java.util.*;

public class RdParser {
    static Scanner scanner = new Scanner(System.in);
    static String input;
    static int pos;
    static Map<String, Integer> variables = new HashMap<>();
    static boolean syntaxError;

    public static void main(String[] args) {
        while (true) {
            System.out.print(">> ");
            input = scanner.nextLine().trim();
            if (input.equals("terminate")) {
                break;
            }
            pos = 0;
            syntaxError = false;
            try {
                program();
                if (!syntaxError && pos == input.length()) {
                    // If the entire input has been processed without errors
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                if (!syntaxError) {
                    System.out.println("Syntax Error!!");
                }
            }
        }
    }

    static void program() {
        variables.clear(); // Clear variables for each new program input
        while (pos < input.length()) {
            skipWhitespace();
            if (peek("int")) {
                declaration();
            } else {
                statement();
            }
            skipWhitespace();
            if (syntaxError) break;
        }
    }

    static void declaration() {
        match("int");
        skipWhitespace();
        String var = parseVar();
        if (syntaxError) return;
        skipWhitespace();
        if (match(";")) {
            variables.put(var, 0);
        } else {
            syntaxError = true;
        }
    }

    static void statement() {
        if (peek("print")) {
            match("print");
            skipWhitespace();
            if (isRelop()) {
                boolean result = parseBexpr();
                if (syntaxError) return;
                skipWhitespace();
                if (match(";")) {
                    System.out.println(result ? "TRUE" : "FALSE");
                } else {
                    syntaxError = true;
                }
            } else {
                int result = parseAexpr();
                if (syntaxError) return;
                skipWhitespace();
                if (match(";")) {
                    System.out.println(result);
                } else {
                    syntaxError = true;
                }
            }
        } else if (peek("do")) {
            match("do");
            skipWhitespace();
            if (match("{")) {
                while (!peek("}")) {
                    statement();
                    skipWhitespace();
                    if (syntaxError) return;
                }
                match("}");
                skipWhitespace();
                if (match("while")) {
                    skipWhitespace();
                    if (match("(")) {
                        boolean result = parseBexpr();
                        if (syntaxError) return;
                        skipWhitespace();
                        if (match(")")) {
                            if (!result) {
                                syntaxError = true;
                            }
                        } else {
                            syntaxError = true;
                        }
                    } else {
                        syntaxError = true;
                    }
                } else {
                    syntaxError = true;
                }
            } else {
                syntaxError = true;
            }
        } else {
            String var = parseVar();
            if (syntaxError) return;
            skipWhitespace();
            if (match("=")) {
                skipWhitespace();
                int value = parseAexpr();
                if (syntaxError) return;
                skipWhitespace();
                if (match(";")) {
                    if (variables.containsKey(var)) {
                        variables.put(var, value);
                    } else {
                        syntaxError = true;
                    }
                } else {
                    syntaxError = true;
                }
            } else {
                syntaxError = true;
            }
        }
    }

    static boolean parseBexpr() {
        String relop = parseRelop();
        if (syntaxError) return false;
        skipWhitespace();
        int left = parseAexpr();
        if (syntaxError) return false;
        skipWhitespace();
        int right = parseAexpr();
        if (syntaxError) return false;
        switch (relop) {
            case "==":
                return left == right;
            case "!=":
                return left != right;
            case "<":
                return left < right;
            case ">":
                return left > right;
            case "<=":
                return left <= right;
            case ">=":
                return left >= right;
            default:
                syntaxError = true;
                return false;
        }
    }

    static String parseRelop() {
        if (peek("==")) {
            match("==");
            return "==";
        } else if (peek("!=")) {
            match("!=");
            return "!=";
        } else if (peek("<=")) {
            match("<=");
            return "<=";
        } else if (peek(">=")) {
            match(">=");
            return ">=";
        } else if (peek("<")) {
            match("<");
            return "<";
        } else if (peek(">")) {
            match(">");
            return ">";
        }
        syntaxError = true;
        return null;
    }

    static int parseAexpr() {
        int value = parseTerm();
        skipWhitespace();
        while (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-' || input.charAt(pos) == '*' || input.charAt(pos) == '/')) {
            char operator = input.charAt(pos++);
            skipWhitespace();
            int nextTerm = parseTerm();
            if (syntaxError) return 0;
            skipWhitespace();
            switch (operator) {
                case '+':
                    value += nextTerm;
                    break;
                case '-':
                    value -= nextTerm;
                    break;
                case '*':
                    value *= nextTerm;
                    break;
                case '/':
                    value /= nextTerm;
                    break;
            }
        }
        return value;
    }

    static int parseTerm() {
        skipWhitespace();
        if (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            return parseNumber();
        } else if (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            String var = parseVar();
            if (syntaxError) return 0;
            if (variables.containsKey(var)) {
                return variables.get(var);
            } else {
                syntaxError = true;
                return 0;
            }
        } else if (pos < input.length() && input.charAt(pos) == '(') {
            pos++;
            int value = parseAexpr();
            if (syntaxError) return 0;
            skipWhitespace();
            if (match(")")) {
                return value;
            } else {
                syntaxError = true;
                return 0;
            }
        } else {
            syntaxError = true;
            return 0;
        }
    }

    static int parseNumber() {
        skipWhitespace();
        int start = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }
        if (pos - start > 10) {
            syntaxError = true;
            return 0;
        }
        return Integer.parseInt(input.substring(start, pos));
    }

    static String parseVar() {
        skipWhitespace();
        int start = pos;
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            pos++;
        }
        if (pos - start > 10) {
            syntaxError = true;
            return "";
        }
        return input.substring(start, pos);
    }

    static void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    static boolean match(String s) {
        if (input.startsWith(s, pos)) {
            pos += s.length();
            return true;
        }
        return false;
    }

    static boolean peek(String s) {
        return input.startsWith(s, pos);
    }

    static boolean isRelop() {
        return peek("==") || peek("!=") || peek("<=") || peek(">=") || peek("<") || peek(">");
    }
}
