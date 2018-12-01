import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SmartAI implements ScrabbleAI {

    private static final boolean[] ALL_TILES = {true, true, true, true, true, true, true};
    private static String[] dictionary = new String[172823];
    private GateKeeper gateKeeper;

    @Override
    public void setGateKeeper(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    private PlayWord findWord(String[] dictionary, boolean firstTurn) {
        ArrayList<Character> hand = gateKeeper.getHand();
        ArrayList<Location> placedTiles = findPlacedTiles();
        PlayWord bestMove = null;
        int bestScore = 0;
        String chosenWord = "";

        if (firstTurn) {
            placedTiles.add(Location.CENTER);
        }

        for (Location location : placedTiles) {
            char tile = gateKeeper.getSquare(location);

            for (String word : dictionary) {
                int tileIndex = word.indexOf(tile);
                String bestWord;

                if ((word.length() <= hand.size() + 1 && tileIndex != -1) || (firstTurn && word.length() <= hand.size())) {
                    ArrayList<Character> handCopy = new ArrayList<>(hand);
                    handCopy.add(tile);
                    char[] wordChar = word.toCharArray();
                    boolean foundWord = true;
                    boolean foundTile = false;

                    int charIndex = 0;
                    for (char i : wordChar) {
                        int index = handCopy.indexOf(i);

                        if (index == -1) {
                            int blankIndex = handCopy.indexOf(' ');

                            if (blankIndex != -1) {
                                handCopy.remove(blankIndex);
                                wordChar[charIndex] = Character.toUpperCase(i);
                            } else {
                                foundWord = false;
                                break;
                            }
                        } else if (!foundTile) {
                            if (i == tile) {
                                wordChar[charIndex] = ' ';
                                foundTile = true;
                            }
                        }
                        handCopy.remove(index);
                        charIndex++;
                    }

                    if ((foundWord && foundTile) || (foundWord && firstTurn)) {
                        bestWord = new String(wordChar);
                        int displace = bestWord.indexOf(' ');

                        if (firstTurn) {
                            displace = 0;
                        }

                        try {
                            gateKeeper.verifyLegality(bestWord, new Location(location.getRow() - displace, location.getColumn()), Location.HORIZONTAL);
                            int wordScore = gateKeeper.score(bestWord, new Location(location.getRow() - displace, location.getColumn()), Location.HORIZONTAL);

                            if (wordScore > bestScore) {
                                bestMove = new PlayWord(bestWord, new Location(location.getRow() - displace, location.getColumn()), Location.HORIZONTAL);
                                bestScore = wordScore;
                                chosenWord = bestWord.replace(' ', tile);
                                //System.out.println("Chosen word: [" + chosenWord + "]");
                            }
                        } catch (IllegalMoveException e) {
                            // skip!
                        }

                        try {
                            gateKeeper.verifyLegality(bestWord, new Location(location.getRow(), location.getColumn() - displace), Location.VERTICAL);
                            int wordScore = gateKeeper.score(bestWord, new Location(location.getRow(), location.getColumn() - displace), Location.VERTICAL);

                            if (wordScore > bestScore) {
                                bestMove = new PlayWord(bestWord, new Location(location.getRow(), location.getColumn() - displace), Location.VERTICAL);
                                bestScore = wordScore;
                                chosenWord = bestWord.replace(' ', tile);
                                //System.out.println("Chosen word: [" + chosenWord + "]");
                            }
                        } catch (IllegalMoveException e) {
                            // skip!
                        }
                    }
                }
            }
        }

//        if (chosenWord.length() > 0) System.out.print("Played: [" + chosenWord + "]");
//        else System.out.print("Traded in");
//        System.out.print(", hand: ");
//        for (char letter : hand) System.out.print(letter + ",");
//        System.out.println();
//        System.out.println();

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

        boolean firstTurn = false;

        if (gateKeeper.getSquare(Location.CENTER) == Board.DOUBLE_WORD_SCORE) {
            firstTurn = true;
        }

        PlayWord bestMove = findWord(dictionary, firstTurn);

        if (bestMove != null) {
            return bestMove;
        } else {
            return new ExchangeTiles(ALL_TILES);
        }
    }
}
