/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.io;

import java.util.Arrays;
import java.util.Scanner;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;
import static poker5cardgame.Log.*;

/**
 * Simple Keyboard source to play the game on the same computer.
 */
public class KeyboardSource implements Source {

    Scanner scan;

    public KeyboardSource() {
        scan = new Scanner(System.in);
    }

    public Move getNextMove() {
        Move move = new Move();

        String command = scan.nextLine();
        String[] args = command.split(" ");
        try {
            move.action = Action.valueOf(args[0]);
        } catch (Exception e) {
            FANCY_CLIENT("INVALID ACTION.\n", Format.BOLD, Format.RED);
            FANCY_CLIENT("Oh! It looks like you entered an ", Format.RED);
            FANCY_CLIENT("invalid action", Format.UNDERLINE, Format.RED);
            FANCY_CLIENT(". Please try again.\n", Format.RED);
            //System.err.println("INVALID ACTION");
            move.action = Action.NOOP;
        }

        try {
            switch (move.action) {
                case START:
                    move.id = Integer.valueOf(args[1]);
                    break;
                    
                case BET:
                    move.chips = Integer.valueOf(args[1]);
                    break;

                case RAISE:
                    move.chips = Integer.valueOf(args[1]);
                    break;

                case DRAW:
                    // Read a list of cards from console
                    int numCards = Integer.valueOf(args[1]);
                    move.cards = new Card[numCards];
                    for (int i = 0; i < numCards; i++)
                        move.cards[i] = Card.fromCode(args[2 + i]);
                    break;                                      
            }
        } catch (Exception e) {
            //System.err.println("SYNTAX ERROR");
            FANCY_CLIENT("SYNTAX ERROR.\n", Format.BOLD, Format.RED);
            FANCY_CLIENT("Oh! It looks like you did a ", Format.RED);
            FANCY_CLIENT("syntax error", Format.UNDERLINE, Format.RED);
            FANCY_CLIENT(". Please try again.\n", Format.RED);
            move.action = Action.NOOP;
        }
        return move;
    }

    public boolean sendMove(Move move) {
        // PRINTS SERVER REPLY TO Console
        KB_DEBUG("SERVER: " + move.action);

        // Optional Arguments
        switch (move.action) {
            case ANTE_STAKES:
                KB_DEBUG("ANTE: " + move.chips);
                KB_DEBUG("STAKES: " + move.cStakes + " " + move.sStakes);
                break;
            case DEALER_HAND:
                KB_DEBUG("DEALER:" + move.dealer);
                KB_DEBUG("HAND:" + Arrays.toString(move.cards));
                break;
            case BET:
                KB_DEBUG("" + move.chips);
                break;
            case RAISE:
                KB_DEBUG("" + move.chips);
                break;
            case DRAW_SERVER:
                KB_DEBUG("DRAWN " + Arrays.toString(move.cards));
                break;
            case SHOW:
                KB_DEBUG("SHOW" + Arrays.toString(move.cards));
                break;
            case NOOP:
                break;
        }

        return true;
    }

}
