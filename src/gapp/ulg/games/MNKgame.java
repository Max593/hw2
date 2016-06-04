package gapp.ulg.games;

import gapp.ulg.game.board.*;
import gapp.ulg.game.util.BoardOct;
import gapp.ulg.game.util.Utils;
import gapp.ulg.play.RandPlayer;

import java.util.*;

import static gapp.ulg.game.board.PieceModel.Species;
import static gapp.ulg.game.util.Utils.opposite;

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

    private long time;
    private int m;
    private int n;
    private int k;
    private Board<PieceModel<Species>> board;
    private int cT;
    private int forced;
    private List<GameRuler<PieceModel<Species>>> gS;
    private Player<PieceModel<Species>> player1;
    private Player<PieceModel<Species>> player2;


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
        if(p1 == null || p2 == null) { throw new NullPointerException("Player1 o Player2 non può essere null"); }
        if(m > 20 || n > 20 || m < 1 || n < 1 || k < 1 || k > 20) { throw new IllegalArgumentException("Uno dei valori di gioco non è accettabile"); }
        this.time = time;
        this.m = m;
        this.n = n;
        this.k = k;
        this.board = new BoardOct<>(m,n);
        this.cT = 1;
        this.forced = -1;
        this.gS = new ArrayList<>();
        this.player1 = new RandPlayer<>(p1); this.player2 = new RandPlayer<>(p2);
        player1.setGame(this); player2.setGame(this);
    }

    /** Il nome rispetta il formato:
     * <pre>
     *     <i>M,N,K</i>-game
     * </pre>
     * dove <code><i>M,N,K</i></code> sono i valori dei parametri M,N,K, ad es.
     * "4,5,4-game". */
    @Override
    public String name() { return String.valueOf(m)+","+String.valueOf(n)+","+String.valueOf(k)+"-game"; }

    @Override
    public <T> T getParam(String name, Class<T> c) {
        return null;
    } //Temporaneo

    @Override
    public List<String> players() { return Collections.unmodifiableList(Arrays.asList(player1.name(), player2.name())); }

    /** @return il colore "nero" per il primo giocatore e "bianco" per il secondo */
    @Override
    public String color(String name) {
        if(name == null) { throw new NullPointerException("name non può essere null"); }
        if(!players().contains(name)) { throw new IllegalArgumentException("Inserire il nome di un player presente in partita"); }
        if(player1.name().equals(name)) { return "nero"; }
        return "bianco"; //Unica altra possibilità
    }

    @Override
    public Board<PieceModel<Species>> getBoard() { return Utils.UnmodifiableBoard(board); }

    @Override
    public int turn() { return cT; }

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
        if(m == null) { throw new NullPointerException("La mossa non può essere null"); }
        if(cT == 0) { throw new IllegalStateException("Il gioco è già terminato"); }

        List<Board.Dir> directions = Arrays.asList(Board.Dir.UP, Board.Dir.UP_L, Board.Dir.LEFT, //Tutte le direzioni della board
                Board.Dir.DOWN_L, Board.Dir.DOWN, Board.Dir.DOWN_R, Board.Dir.RIGHT, Board.Dir.UP_R);
        List<Board.Dir> halfDirections = Arrays.asList(Board.Dir.UP, Board.Dir.UP_L, Board.Dir.LEFT, Board.Dir.DOWN_L); //Metà delle direzioni per applicazioni con opposite

        if(isValid(m) && m.getKind() != Move.Kind.RESIGN) { //Se si esegue una mossa valida che non è la Resa
            board.put(m.getActions().get(0).getPiece(), m.getActions().get(0).getPos().get(0)); //Esecuzione della mossa

            for(Board.Dir d : halfDirections) { //Controllo di vittoria sulla pedina appena inserita
                Pos pBase = m.getActions().get(0).getPos().get(0); //Posizione da cui si itera (pedina appena inserita)
                int counter = 1; //Numero di pedine consecutive (1 contando la pedina appena inserita)

                try { //Test nella direzione
                    Pos adj = board.adjacent(pBase, d);
                    while(true) {
                        if(adj == null) { break; } //Se si esce dalla board
                        else if(board.get(adj).equals(m.getActions().get(0).getPiece())) { counter++; adj = board.adjacent(adj, d); } //Se è una pedina alleata
                        else if(board.get(adj) == null) { break; } //Se è una pedina vuota
                        else if(!board.get(adj).equals(m.getActions().get(0).getPiece())) { break; } //Se è una pedina avversaria
                    }
                } catch(NullPointerException e) { continue; }

                if(counter < k) { //Se il contatore non ha raggiunto k
                    try { //Test nella direzione opposta
                        Pos adj = board.adjacent(pBase, opposite(d));
                        while(true) {
                            if (adj == null) { break; }
                            else if(board.get(adj).equals(m.getActions().get(0).getPiece())) { counter++; board.adjacent(adj, opposite(d)); }
                            else if(board.get(adj) == null) { break; }
                            else if(!board.get(adj).equals(m.getActions().get(0).getPiece())) { break; }
                        }
                    } catch(NullPointerException e) { continue; }
                }

                if(counter == k) { //Se è una mossa vincente, il gioco termina e vince il player corrente
                    forced = cT;
                    cT = 0;
                    return true;
                }
            }

            //Sistema che determina se il gioco deve terminare in anticipo [NUMERO DI CASELLE VUOTE IN LINEA / MOSSE RIMANENTI (caselle vuote/2) = se il player può ancora vincere]
            for(Pos p : board.positions()) { //Per tutte le posizioni della board (non mi servono tutte le posizioni a dire il vero, riformulare)

            }

            if(cT == 2) { cT = 1; } //Passa il turno all'altro player se il gioco non è finito o se non è destinato a finire in patta
            else if(cT == 1) { cT = 2; }
            gS.add(copy());
            return true;
        }

        if(m.getKind() == Move.Kind.RESIGN) { //Vince l'altro player se ci si arrende
            if(cT == 2) { forced = 1; }
            else if(cT == 1) { forced = 2; }
            cT = 0;
            gS.add(copy());
            return true;
        }

        if(cT == 2) { forced = 1; } //Vince l'altro player se si compie una mossa NON valida
        else if(cT == 1) { forced = 2; }
        cT = 0; //Termina il game se la mossa non è valida.
        gS.add(copy());
        return false;
    }

    @Override
    public boolean unMove() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public boolean isPlaying(int i) {
        if(!Arrays.asList(1, 2).contains(i)) { throw new IllegalArgumentException("Il giocatore selezionato non è presente in questa partita"); }
        return cT != 0;
    }

    @Override
    public int result() {
        if(cT != 0) { return -1; }
        return forced; //Se il gioco è terminato, settare a mano il vincitore con forced
    }

    /** Ogni mossa (diversa dall'abbandono) è rappresentata da una sola {@link Action}
     * di tipo {@link Action.Kind#ADD}. */
    @Override
    public Set<Move<PieceModel<Species>>> validMoves() {
        if(cT == 0) { throw new IllegalStateException("Il gioco è già terminato"); }
        Set<Move<PieceModel<Species>>> moveSet = new HashSet<>(); //Insieme risultato anche se vuoto verrà ritornato
        PieceModel<Species> pP = new PieceModel<>(Species.DISC, "nero");
        if(cT == 2) { pP = new PieceModel<>(Species.DISC, "bianco"); } //Se sta giocando il player 2;

        for(Pos p : board.positions()) { //Per ogni posizione della board
            if(board.get(p) == null) { //Per ogni posizione vuota
                Action<PieceModel<Species>> add = new Action<>(p, pP);
                moveSet.add(new Move<>(Arrays.asList(add)));
            }
        }

        if(moveSet.size() > 0) { moveSet.add(new Move(Move.Kind.RESIGN)); }
        return Collections.unmodifiableSet(moveSet);
    }

    @Override
    public GameRuler<PieceModel<Species>> copy() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public Mechanics<PieceModel<Species>> mechanics() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }
}
