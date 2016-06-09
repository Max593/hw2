package gapp.ulg.play;

import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.Move;
import gapp.ulg.game.board.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Un oggetto RandPlayer è un oggetto che può giocare un qualsiasi gioco regolato
 * da un {@link GameRuler} perché, ad ogni suo turno, sceglie in modo random una
 * mossa tra quelle valide esclusa {@link Move.Kind#RESIGN}.
 * @param <P>  tipo del modello dei pezzi */
public class RandPlayer<P> implements Player<P> {
    public String name;
    private GameRuler<P> gameRul;

    /** Crea un giocatore random, capace di giocare a un qualsiasi gioco, che ad
     * ogni suo turno fa una mossa scelta in modo random tra quelle valide.
     * @param name  il nome del giocatore random
     * @throws NullPointerException se name è null */
    public RandPlayer(String name) {
        if(name == null) { throw new NullPointerException("Il nome del player non può essere null"); }
        this.name = name;
        this.gameRul = null;
    }

    @Override
    public String name() { return name; }

    @Override
    public void setGame(GameRuler<P> g) {
        if(g == null) { throw new IllegalArgumentException("Il gioco non può essere null"); }
        gameRul = g;
    }

    @Override
    public void moved(int i, Move<P> m) { //Ancora non funzionante
        if(gameRul == null || gameRul.result() != -1) { throw new IllegalStateException("Nessun gioco impostato o ormai terminato"); }
        if(m == null) { throw new NullPointerException("La mossa non può essere null"); }
        if(!Arrays.asList(1, 2).contains(i) || !gameRul.isValid(m)) { throw new IllegalArgumentException("Indice di turnazione errato o la mossa non è consentita nella situazione di gioco attuale"); }
    }

    @Override
    public Move<P> getMove() {
        if (gameRul == null || gameRul.result() >= 0 ||
                gameRul.players().indexOf(name) + 1 != gameRul.turn()) {
            throw new IllegalStateException("Il gioco potrebbe non essere impostato, terminato o non è il turno del giocatore");
        }
        if (gameRul.validMoves().size() == 0) { return null; } //Se non sono presenti mosse possibili in questa situazione di gioco ritorna null

        List<Move<P>> temp = new ArrayList<>();
        temp.addAll(gameRul.validMoves()); temp.remove(new Move(Move.Kind.RESIGN)); //Evito la mossa resign

        return temp.get(new Random().nextInt(temp.size()));
    }
}
