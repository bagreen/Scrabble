import java.util.*;
import java.util.stream.Collectors;

public class SmartAI implements ScrabbleAI {

    private GateKeeper gateKeeper;

    @Override
    public void setGateKeeper(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    @Override
    public ScrabbleMove chooseMove() {
        // start of the game, want to play all of our tiles
        if (gateKeeper.getSquare(Location.CENTER) == Board.DOUBLE_WORD_SCORE) {
            return move(true);
        }

        // what to do the rest of the time
        else {
            return move(false);
        }
    }

    private String findAnagrams(String[] dictionary) {
        ArrayList<Character> hand = gateKeeper.getHand();

        String bestWord = "";

        for (String word : dictionary) {
            // we found a word!
            if (bestWord.length() != 0) {
                break;
            }

            else if (word.length() <= hand.size()) {
                bestWord = word;
                ArrayList<Character> handCopy = new ArrayList<>(hand);

                for (int i = 0; i < word.length(); i++) {
                    int index = handCopy.indexOf(word.charAt(i));

                    // letter is not in word
                    if (index == -1) {
                        bestWord = "";
                        break;
                    }

                    else {
                        handCopy.remove(index);
                    }
                }
            }
        }
        return bestWord;
    }

    private ScrabbleMove move(boolean first) {
        // Sets up our dictionary
        In in = new In("src/enable1.txt");
        ArrayList<String> input = new ArrayList<>(Arrays.asList(in.readAllLines()));
        String output = input.stream().distinct().sorted((x, y) -> Integer.compare(y.length(), x.length())).collect(Collectors.joining(","));
        String[] dictionary = output.split(",");


        // gets the best anagram of our letters
        String word = findAnagrams(dictionary);

        // if we're going first
        if (first) {
            return new PlayWord(word, Location.CENTER, Location.HORIZONTAL);
        }

        // need to figure out how to do this...
        else {
            return bestMove;
        }
    }
}