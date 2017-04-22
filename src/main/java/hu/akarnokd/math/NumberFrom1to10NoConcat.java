package hu.akarnokd.math;

import java.util.*;

import ix.Ix;
import net.objecthunter.exp4j.ExpressionBuilder;

public class NumberFrom1to10NoConcat {

    static boolean validParent(StringBuilder s) {
        int p = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                p++;
            }
            if (s.charAt(i) == ')') {
                p--;
            }
            if (p < 0) {
                return false;
            }
        }
        for (int i = 0; i < s.length() - 2; i++) {
            if (s.charAt(i) == '(' 
                    && Character.isDigit(s.charAt(i + 1))
                    && s.charAt(i + 2) == ')') {
                return false;
            }
        }

        while (p-- > 0) {
            s.append(')');
        }
        return true;
    }
    
    static long ipow(int base, int exp) {
        long result = 1;
        for (int i = 0; i < exp; i++) {
            result *= base;
        }
        return result;
    }
    
    static final class MultiIndex {
        
        final int[] indices;
        
        final int[] limits;

        long counter;
        
        MultiIndex(int numIndices) {
            this.indices = new int[numIndices];
            this.limits = new int[numIndices];
        }

        boolean next() {
            counter++;
            for (int i = 0; i < indices.length; i++) {
                int a = indices[i] + 1;
                if (a == limits[i]) {
                    indices[i] = 0;
                    if (i == indices.length - 1) {
                        return false;
                    }
                } else {
                    indices[i] = a;
                    return true;
                }
            }
            return false;
        }
    }

    static final int report = 10_000_000;
    
    static String findExpression(Map<Integer, String> map, boolean noDivide, int maxParen, boolean noConcat) throws Exception {
        List<String> first = new ArrayList<>(Arrays.asList(
                ""
                , "-", "(", "-(", "(-"
                , "((", "-((", "(((", "-((("
                , "((-", "((-", "(((-", "(((-"
                ));

        List<String> second = new ArrayList<>(
                Arrays.asList(
                        ""
                        , "+", "-", "*"
                        , "+("
                        , "-("
                        , "*("
                        , "/"
                        , "^"
                        , "^("
                        ,    "+(("
                        ,    "-(("
                        ,    "*(("
                        , "/("
                        ,    "/(("
                        ,    "^(("
                ));

        List<String> between = new ArrayList<>(
                Arrays.asList(
                        ""
                        , "+", "-", "*"
                        ,")+", "+(", ")+("
                        ,")-", "-(", ")-("
                        ,")*", "*(", ")*("
                        , "/", "^"
                        ,")^", "^(", ")^("
                        ,    "))+", "+((", "))+(", ")+((", "))+(("
                        ,    "))-", "-((", "))-(", ")-((", "))-(("
                        ,    "))*", "*((", "))*(", ")*((", "))*(("
                        , ")/", "/(", ")/(",
                            "))/", "/((", "))/(", ")/((", "))/(("
                        ,    "))^", "^((", "))^(", ")^((", "))^(("
                ));

        List<String> beforeLast = new ArrayList<>(
                Arrays.asList(
                        ""
                        , "+", "-", "*"
                        ,")+"
                        ,")-"
                        ,")*"
                        ,"))+"
                        ,"))-"
                        ,"))*"
                        , "^"
                        ,")^"
                        ,"))^"
                        , "/"
                        , ")/"
                        , "))/"
                ));

        if (noDivide) {
            first.removeIf(v -> v.contains("/"));
            second.removeIf(v -> v.contains("/"));
            between.removeIf(v -> v.contains("/"));
            beforeLast.removeIf(v -> v.contains("/"));
        }
        
        String maxParenStart = Ix.repeatValue("(").take(maxParen + 1).join("").first();
        String maxParenEnd = Ix.repeatValue(")").take(maxParen + 1).join("").first();

        first.removeIf(v -> v.contains(maxParenStart) || v.contains(maxParenEnd));
        second.removeIf(v -> v.contains(maxParenStart) || v.contains(maxParenEnd));
        between.removeIf(v -> v.contains(maxParenStart) || v.contains(maxParenEnd));
        beforeLast.removeIf(v -> v.contains(maxParenStart) || v.contains(maxParenEnd));

        if (noConcat) {
            second.removeIf(String::isEmpty);
            between.removeIf(String::isEmpty);
            beforeLast.removeIf(String::isEmpty);
        }
        
        long all = first.size() * ipow(between.size(), 7) * second.size() * beforeLast.size();
        System.out.printf("%,d%n", all);

        MultiIndex mi = new MultiIndex(10);
        mi.limits[0] = first.size();
        mi.limits[1] = second.size();
        for (int i = 2; i < 9; i++) {
            mi.limits[i] = between.size();
        }
        mi.limits[9] = beforeLast.size();

        StringBuilder b = new StringBuilder();
        long invalid = 0;
        long valid = 0;
        int[] indices = mi.indices;
        do {
            b.setLength(0);
            b.append(first.get(indices[0]));
            b.append('1');
            b.append(second.get(indices[1]));
            b.append('2');
            for (int i = 2; i < 8; i++) {
                b.append(between.get(indices[i]));
                b.append((char)('0' + (i + 1)));
            }
            
            b.append(beforeLast.get(indices[9]));
            b.append('9');
            
            if (validParent(b)) {
                String expr = b.toString();
                double result;
                
                try {
                    result = new ExpressionBuilder(expr)
                    .build().evaluate();
                    
                    valid++;
                } catch (ArithmeticException ignored) {
                    invalid++;
                    result = Double.NaN;
                } catch (Exception ex) {
                    System.err.println(expr);
                    throw ex;
                }
                if (result > 0 && result < 11112 && result == Math.floor(result)) {
                    String str = map.get((int)result);
                    if (str == null/* || str.length() > expr.length()*/) {
                        map.put((int)result, expr);
                    }
                }
            } else {
                invalid++;
            }
            if (mi.counter % report == 0) {
                System.out.printf("Total: %,d (%.6f%%), Valid: %,d (%.6f%%), Invalid: %,d, Found: %,d%n", 
                        mi.counter + 1,
                        ((mi.counter + 1) * 100d) / all,
                        valid, (valid * 100d / (mi.counter + 1)),
                        invalid,
                        map.size()
                        );
            }
        } while (mi.next());
        return null;
    }

    public static void main(String[] args) throws Exception {
        Map<Integer, String> map = new HashMap<>();

        findExpression(map, true, 1, true);
        
        System.out.print("Numbers found: " + map.size());
        for (int i = 1; i < 11112; i++) {
            if (!map.containsKey(i)) {
                System.out.printf("  %d%n", i);
            }
        }
    }
}
