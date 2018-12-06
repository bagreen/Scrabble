import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SmartAI implements ScrabbleAI {
    private static final boolean[] ALL_TILES = {true, true, true, true, true, true, true};
    private static String[] dictionary = new String[172823];
    private static int bestScore = 0;
    private GateKeeper gateKeeper;

    @Override
    public void setGateKeeper(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    private PlayWord getMove(Location location, String orientationWord, Location orientation, PlayWord bestMove) {
        boolean first = false;

        if (gateKeeper.getSquare(Location.CENTER) == Board.DOUBLE_WORD_SCORE) {
            first = true;
        }

        for (String word : dictionary) {
            if ((word.length() > orientationWord.length() + gateKeeper.getHand().size()) || (word.length() <= orientationWord.length())) {
                continue;
            }

            ArrayList<Character> letters = gateKeeper.getHand();
            char[] wordChar = word.toCharArray();

            if (!first) {
                int wordIndex = word.indexOf(orientationWord);

                if (wordIndex == -1) {
                    continue;
                }

                // does this need to be in this if statement?
                for (int i = 0; i < orientationWord.length(); i++) {
                    wordChar[i + wordIndex] = ' ';
                }
            }

            boolean validWord = true;

            int charIndex = 0;
            for (char i : wordChar) {
                if (i == ' ') {
                    continue;
                }

                int index = letters.indexOf(i);

                if (index == -1) {
                    int blankIndex = letters.indexOf(' ');

                    if (blankIndex != -1) {
                        letters.remove(blankIndex);
                        wordChar[charIndex] = Character.toUpperCase(i);
                    }
                    else {
                        validWord = false;
                        break;
                    }
                }
                letters.remove(index);
                charIndex++;
            }

            if (validWord) {
                String bestWord = new String(wordChar);

                // unclear if this is necessary, more testing needed
//                if (first) {
//                    displace = 0;
//                }

                int displacex = bestWord.indexOf(' ');
                int displacey = bestWord.indexOf(' ');

                if (orientation == Location.HORIZONTAL) {
                    displacey = 0;
                }
                else if (orientation == Location.VERTICAL) {
                    displacex = 0;
                }

                Location playLocation = new Location(location.getRow() - displacex, location.getColumn() - displacey);

                try {
                    gateKeeper.verifyLegality(bestWord, playLocation, orientation);
                    int wordScore = gateKeeper.score(bestWord, playLocation, orientation);

                    if (wordScore > bestScore) {
                        bestMove = new PlayWord(bestWord, playLocation, orientation);
                        bestScore = wordScore;
                    }
                }
                catch (IllegalMoveException e) {
                    // skip!
                }
            }
        }

        return bestMove;
    }

    private boolean isOccupied(Location location) {
        return Character.isAlphabetic(gateKeeper.getSquare(location));
    }

    private ArrayList<Location> findPlacedTiles() {
        ArrayList<Location> placedTiles = new ArrayList<>();

        for (int row = 0; row < Board.WIDTH; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                Location location = new Location(row, col);
                if (isOccupied(location)) {
                    placedTiles.add(location);
                }
            }
        }

        return placedTiles;
    }

    @Override
    public ScrabbleMove chooseMove() {
        // has dictionary been made yet?
        // if not, let's set it up
        if (dictionary[0] == null) {
            In in = new In("enable1.txt");
            ArrayList<String> input = new ArrayList<>(Arrays.asList(in.readAllLines()));
            String output = input.stream().distinct().sorted((x, y) -> Integer.compare(y.length(), x.length())).collect(Collectors.joining(","));
            dictionary = output.split(",");
        }

        ArrayList<Location> placedTiles = findPlacedTiles();
        PlayWord bestMove = null;


        if (gateKeeper.getSquare(Location.CENTER) == Board.DOUBLE_WORD_SCORE) {
            bestMove = getMove(Location.CENTER, "", Location.HORIZONTAL, bestMove);
            bestMove = getMove(Location.CENTER, "", Location.VERTICAL, bestMove);
        }
        else {
            for (Location location : placedTiles) {
                String horizontalWord = "";
                String verticalWord = "";

                Location neighborCheck = location;
                while (neighborCheck.isOnBoard() && isOccupied(neighborCheck)) {
                    horizontalWord += gateKeeper.getSquare(neighborCheck);
                    neighborCheck = neighborCheck.neighbor(Location.HORIZONTAL);
                }

                neighborCheck = location;
                while (neighborCheck.isOnBoard() && isOccupied(neighborCheck)) {
                    verticalWord += gateKeeper.getSquare(neighborCheck);
                    neighborCheck = neighborCheck.neighbor(Location.VERTICAL);
                }

                bestMove = getMove(location, horizontalWord, Location.HORIZONTAL, bestMove);
                bestMove = getMove(location, verticalWord, Location.VERTICAL, bestMove);
            }
        }

        bestScore = 0;

        if (bestMove != null) {
            return bestMove;
        }

        return new ExchangeTiles(ALL_TILES);
    }
}
