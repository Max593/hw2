package gapp.ulg.games;

import gapp.ulg.game.board.*;

import java.util.*;

import static gapp.ulg.game.board.PieceModel.Species;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Un oggetto {@code MNKgame} rappresenta un GameRuler per fare una partita a un
 * (m,n,k)-game, generalizzazioni del ben conosciuto Tris o Tic Tac Toe.
 * <br>
 * Un gioco (m,n,k)-game si gioca su una board di tipo {@link Board.System#OCTAGONAL}
 * di larghezza (width) m e altezza (height) n. Si gioca con pezzi o pedine di specie
 * {@link Species#DISC} di due colori "nero" e "bianco". All'inizio la board è vuota.
 * Poi a turno ogni giocatore pone una sua pedina in una posizione vuota. Vince il
 * primo giocatore che riesce a disporre almeno k delle sue pedine in una linea di
 * posizioni consecutive orizzontale, verticale o diagonale. Chiaramente non è
 * possibile passare il turno e una partita può finire con una patta.
 * <br>
 * Per ulteriori informazioni si può consultare
 * <a href="https://en.wikipedia.org/wiki/M,n,k-game">(m,n,k)-game</a> */
public class MNKgame implements GameRuler<PieceModel<Species>> {
    /** Crea un {@code MNKgame} con le impostazioni date.
     * @param time  tempo in millisecondi per fare una mossa, se <= 0 significa nessun
     *              limite
     * @param m  larghezza (width) della board
     * @param n  altezza (height) della board
     * @param k  lunghezza della linea
     * @param p1  il nome del primo giocatore
     * @param p2  il nome del secondo giocatore
     * @throws NullPointerException se {@code p1} o {@code p2} è null
     * @throws IllegalArgumentException se i valori di {@code m,n,k} non soddisfano
     * le condizioni 1 <= {@code k} <= max{{@code M,N}} <= 20 e 1 <= min{{@code M,N}} */
    public MNKgame(long time, int m, int n, int k, String p1, String p2) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    /** Il nome rispetta il formato:
     * <pre>
     *     <i>M,N,K</i>-game
     * </pre>
     * dove <code><i>M,N,K</i></code> sono i valori dei parametri M,N,K, ad es.
     * "4,5,4-game". */
    @Override
    public String name() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }

    @Override
    public <T> T getParam(String name, Class<T> c) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public List<String> players() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }

    /** @return il colore "nero" per il primo giocatore e "bianco" per il secondo */
    @Override
    public String color(String name) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public Board<PieceModel<Species>> getBoard() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }

    @Override
    public int turn() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }

    /** Se la mossa non è valida termina il gioco dando la vittoria all'altro
     * giocatore.
     * Se dopo la mossa la situazione è tale che nessuno dei due giocatori può
     * vincere, si tratta quindi di una situazione che può portare solamente a una
     * patta, termina immediatamente il gioco con una patta. Per determinare se si
     * trova in una tale situazione controlla che nessun dei due giocatori può
     * produrre una linea di K pedine con le mosse rimanenti (in qualsiasi modo siano
     * disposte le pedine rimanenti di entrambi i giocatori). */
    @Override
    public boolean move(Move<PieceModel<Species>> m) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public boolean unMove() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public boolean isPlaying(int i) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public int result() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }

    /** Ogni mossa (diversa dall'abbandono) è rappresentata da una sola {@link Action}
     * di tipo {@link Action.Kind#ADD}. */
    @Override
    public Set<Move<PieceModel<Species>>> validMoves() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public GameRuler<PieceModel<Species>> copy() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public Mechanics<PieceModel<Species>> mechanics() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }
}
