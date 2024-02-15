package components;

import java.util.HashSet;

public class Alphabet extends HashSet<Character> {
    public static final Character EPSILON = '\uFFFF';

    public static final Alphabet ALL_LETTERS = Alphabet.withSymbols("qwertyuiopasdfghjklzxcvbnm");
    public static final Alphabet ALL_NUMBERS = Alphabet.withSymbols("1234567890-+.");
    public static Alphabet withSymbols(char firstSymbol, char ... otherSymbols) {
        Alphabet output = new Alphabet();
        output.add(firstSymbol);
        for (Character c : otherSymbols) output.add(c);

        return output;
    }

    public static Alphabet withSymbols(String symbols) {
        if (symbols.isEmpty()) {
            throw new AutomatonCrashException("Invalid Alphabet: must contain at least 1 symbol", symbols);
        }
        Alphabet output = new Alphabet();
        for (Character c : symbols.toCharArray()) {
            output.add(c);
        }
        return output;
    }
}
