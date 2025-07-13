import java.util.*;

// Main JSON reader class
class JSONReader {
    private String json;
    private int pos;
    
    public JSONReader(String json) {
        this.json = json.trim();
        this.pos = 0;
    }
    
    public Object parse() {
        skipWhitespace();
        return parseValue();
    }
    
    private Object parseValue() {
        skipWhitespace();
        
        if (pos >= json.length()) {
            throw new RuntimeException("Unexpected end of JSON");
        }
        
        char c = json.charAt(pos);
        
        switch (c) {
            case '{': return parseObject();
            case '[': return parseArray();
            case '"': return parseString();
            case 't': return parseTrue();
            case 'f': return parseFalse();
            case 'n': return parseNull();
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return parseNumber();
                }
                throw new RuntimeException("Unexpected character: " + c);
        }
    }
    
    private Map<String, Object> parseObject() {
        Map<String, Object> obj = new HashMap<>();
        consume('{');
        skipWhitespace();
        
        // Handle empty object
        if (pos < json.length() && json.charAt(pos) == '}') {
            consume('}');
            return obj;
        }
        
        while (true) {
            skipWhitespace();
            
            // Parse key
            if (json.charAt(pos) != '"') {
                throw new RuntimeException("Expected string key");
            }
            String key = parseString();
            
            skipWhitespace();
            consume(':');
            skipWhitespace();
            
            // Parse value
            Object value = parseValue();
            obj.put(key, value);
            
            skipWhitespace();
            if (pos >= json.length()) {
                throw new RuntimeException("Unexpected end of JSON");
            }
            
            char c = json.charAt(pos);
            if (c == '}') {
                consume('}');
                break;
            } else if (c == ',') {
                consume(',');
            } else {
                throw new RuntimeException("Expected ',' or '}'");
            }
        }
        
        return obj;
    }
    
    private List<Object> parseArray() {
        List<Object> arr = new ArrayList<>();
        consume('[');
        skipWhitespace();
        
        // Handle empty array
        if (pos < json.length() && json.charAt(pos) == ']') {
            consume(']');
            return arr;
        }
        
        while (true) {
            skipWhitespace();
            Object value = parseValue();
            arr.add(value);
            
            skipWhitespace();
            if (pos >= json.length()) {
                throw new RuntimeException("Unexpected end of JSON");
            }
            
            char c = json.charAt(pos);
            if (c == ']') {
                consume(']');
                break;
            } else if (c == ',') {
                consume(',');
            } else {
                throw new RuntimeException("Expected ',' or ']'");
            }
        }
        
        return arr;
    }
    
    private String parseString() {
        consume('"');
        StringBuilder sb = new StringBuilder();
        
        while (pos < json.length() && json.charAt(pos) != '"') {
            char c = json.charAt(pos);
            
            if (c == '\\') {
                pos++;
                if (pos >= json.length()) {
                    throw new RuntimeException("Unexpected end of JSON in string");
                }
                
                char escaped = json.charAt(pos);
                switch (escaped) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        // Unicode escape sequence
                        if (pos + 4 >= json.length()) {
                            throw new RuntimeException("Invalid unicode escape");
                        }
                        String hex = json.substring(pos + 1, pos + 5);
                        try {
                            int codePoint = Integer.parseInt(hex, 16);
                            sb.append((char) codePoint);
                            pos += 4;
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid unicode escape");
                        }
                        break;
                    default:
                        throw new RuntimeException("Invalid escape sequence: \\" + escaped);
                }
            } else {
                sb.append(c);
            }
            pos++;
        }
        
        if (pos >= json.length()) {
            throw new RuntimeException("Unterminated string");
        }
        
        consume('"');
        return sb.toString();
    }
    
    private Number parseNumber() {
        int start = pos;
        
        // Handle negative sign
        if (json.charAt(pos) == '-') {
            pos++;
        }
        
        // Parse integer part
        if (pos >= json.length() || !Character.isDigit(json.charAt(pos))) {
            throw new RuntimeException("Invalid number");
        }
        
        if (json.charAt(pos) == '0') {
            pos++;
        } else {
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                pos++;
            }
        }
        
        boolean isFloat = false;
        
        // Parse decimal part
        if (pos < json.length() && json.charAt(pos) == '.') {
            isFloat = true;
            pos++;
            if (pos >= json.length() || !Character.isDigit(json.charAt(pos))) {
                throw new RuntimeException("Invalid number");
            }
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                pos++;
            }
        }
        
        // Parse exponent part
        if (pos < json.length() && (json.charAt(pos) == 'e' || json.charAt(pos) == 'E')) {
            isFloat = true;
            pos++;
            if (pos < json.length() && (json.charAt(pos) == '+' || json.charAt(pos) == '-')) {
                pos++;
            }
            if (pos >= json.length() || !Character.isDigit(json.charAt(pos))) {
                throw new RuntimeException("Invalid number");
            }
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                pos++;
            }
        }
        
        String numberStr = json.substring(start, pos);
        try {
            if (isFloat) {
                return Double.parseDouble(numberStr);
            } else {
                long longVal = Long.parseLong(numberStr);
                if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number: " + numberStr);
        }
    }
    
    private Boolean parseTrue() {
        if (json.substring(pos, pos + 4).equals("true")) {
            pos += 4;
            return true;
        }
        throw new RuntimeException("Invalid literal");
    }
    
    private Boolean parseFalse() {
        if (json.substring(pos, pos + 5).equals("false")) {
            pos += 5;
            return false;
        }
        throw new RuntimeException("Invalid literal");
    }
    
    private Object parseNull() {
        if (json.substring(pos, pos + 4).equals("null")) {
            pos += 4;
            return null;
        }
        throw new RuntimeException("Invalid literal");
    }
    
    private void consume(char expected) {
        if (pos >= json.length() || json.charAt(pos) != expected) {
            throw new RuntimeException("Expected '" + expected + "'");
        }
        pos++;
    }
    
    private void skipWhitespace() {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }
}

// Utility class for pretty printing
class JSONPrinter {
    public static void printObject(Object obj, int indent) {
        String indentStr = "  ".repeat(indent);
        
        if (obj == null) {
            System.out.print("null");
        } else if (obj instanceof String) {
            System.out.print("\"" + obj + "\"");
        } else if (obj instanceof Number || obj instanceof Boolean) {
            System.out.print(obj);
        } else if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            System.out.println("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) System.out.println(",");
                first = false;
                System.out.print(indentStr + "  \"" + entry.getKey() + "\": ");
                printObject(entry.getValue(), indent + 1);
            }
            System.out.println();
            System.out.print(indentStr + "}");
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            System.out.println("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) System.out.println(",");
                System.out.print(indentStr + "  ");
                printObject(list.get(i), indent + 1);
            }
            System.out.println();
            System.out.print(indentStr + "]");
        }
    }
}

// Example usage and test class
public class JSONTest {
    public static void main(String[] args) {
        // Test cases
        String[] testCases = {
            "{\"name\":\"John\",\"age\":30,\"active\":true}",
            "[1,2,3,\"hello\",true,null]",
            "{\"users\":[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]}",
            "{\"pi\":3.14159,\"e\":2.71828,\"negative\":-42}",
            "{\"escaped\":\"Hello\\nWorld\\t!\",\"unicode\":\"\\u0048\\u0065\\u006C\\u006C\\u006F\"}",
            "{}",
            "[]",
            "null",
            "true",
            "false",
            "42",
            "\"simple string\""
        };
        
        for (String testCase : testCases) {
            System.out.println("Testing: " + testCase);
            try {
                JSONReader reader = new JSONReader(testCase);
                Object result = reader.parse();
                System.out.print("Result: ");
                JSONPrinter.printObject(result, 0);
                System.out.println("\n");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }
        
        // Example of accessing parsed data
        System.out.println("=== Accessing parsed data ===");
        String jsonStr = "{\"name\":\"Alice\",\"age\":25,\"hobbies\":[\"reading\",\"coding\"],\"address\":{\"city\":\"New York\",\"zip\":10001}}";
        JSONReader reader = new JSONReader(jsonStr);
        Object parsed = reader.parse();
        
        if (parsed instanceof Map) {
            Map<String, Object> person = (Map<String, Object>) parsed;
            System.out.println("Name: " + person.get("name"));
            System.out.println("Age: " + person.get("age"));
            
            List<Object> hobbies = (List<Object>) person.get("hobbies");
            System.out.println("Hobbies: " + hobbies);
            
            Map<String, Object> address = (Map<String, Object>) person.get("address");
            System.out.println("City: " + address.get("city"));
            System.out.println("ZIP: " + address.get("zip"));
        }
    }
}