package ch.atdit.warzonemodtools;

import java.util.Arrays;
import java.util.List;

public class Test {

    private static void print(Object object) {
        System.out.println(object);
    }

    private static String unescape(String string) {
        while (string.contains("§")) {
            int position = string.indexOf("§");
            String toReplace = string.substring(position, position + 2);
            print("To Replace: " + toReplace);
            string = string.replace(toReplace, "");
        }

        return string;
    }

    private static String getWords(List<String> words, int start, int end) {
        return String.join(" ", words.subList(start, end + 1));
    }

    public static void main(String[] args) {
        print(0xFFFFFF);
        String sentence = "[SILENT] PSVEEE has been banned by AtditC for 1 month for Inappropriate Behaviour";
        String test     = "---0---- --1--- -2- -3-- --4--- -5 --6--- -7- 8 --9-- 10- -----11------ ---12----";

        List<String> words = Arrays.asList(sentence.split(" "));

        print(getWords(words, 0, 13));
    }
}
