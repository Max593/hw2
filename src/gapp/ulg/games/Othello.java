package gapp.ulg.games;

import gapp.ulg.game.board.*;
import gapp.ulg.game.util.BoardOct;
import gapp.ulg.game.util.Utils;
import gapp.ulg.play.RandPlayer;

import static gapp.ulg.game.board.PieceModel.Species;

import java.util.*;


/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Un oggetto Othello rappresenta un GameRuler per fare una partita a Othello. Il
 * gioco Othello si gioca su una board di tipo {@link Board.System#OCTAGONAL} 8x8.
 * Si gioca con pezzi o pedine di specie {@link Species#DISC} di due
 * colori "nero" e "bianco". Prima di inziare a giocare si posizionano due pedine
 * bianche e due nere nelle quattro posizioni centrali della board in modo da creare
 * una configurazione a X. Quindi questa è la disposzione iniziale (. rappresenta
 * una posizione vuota, B una pedina bianca e N una nera):
 * <pre>
 *     . . . . . . . .
 *     . . . . . . . .
 *     . . . . . . . .
 *     . . . B N . . .
 *     . . . N B . . .
 *     . . . . . . . .
 *     . . . . . . . .
 *     . . . . . . . .
 * </pre>
 * Si muove alternativamente (inizia il nero) appoggiando una nuova pedina in una
 * posizione vuota in modo da imprigionare, tra la pedina che si sta giocando e
 * quelle del proprio colore già presenti sulla board, una o più pedine avversarie.
 * A questo punto le pedine imprigionate devono essere rovesciate (da bianche a nere
 * o viceversa, azione di tipo {@link Action.Kind#SWAP}) e diventano
 * di proprietà di chi ha eseguito la mossa. È possibile incastrare le pedine in
 * orizzontale, in verticale e in diagonale e, a ogni mossa, si possono girare
 * pedine in una o più direzioni. Sono ammesse solo le mosse con le quali si gira
 * almeno una pedina, se non è possibile farlo si salta il turno. Non è possibile
 * passare il turno se esiste almeno una mossa valida. Quando nessuno dei giocatori
 * ha la possibilità di muovere o quando la board è piena, si contano le pedine e si
 * assegna la vittoria a chi ne ha il maggior numero. Per ulteriori informazioni si
 * può consultare
 * <a href="https://it.wikipedia.org/wiki/Othello_(gioco)">Othello</a> */
public class Othello implements GameRuler<PieceModel<Species>> {

    private long time;
    private int size;
    private Player player1;
    private Player player2;
    private Board<PieceModel<Species>> board;
    private int cT; //Turno corrente
    private List<GameRuler<PieceModel<Species>>> gS;

    /** Crea un GameRuler per fare una partita a Othello, equivalente a
     * {@link Othello#Othello(long, int, String, String) Othello(0,8,p1,p2)}.
     * @param p1  il nome del primo giocatore
     * @param p2  il nome del secondo giocatore
     * @throws NullPointerException se p1 o p2 è null */
    public Othello(String p1, String p2) {
        this(0, 8, p1, p2);
    }

    /** Crea un GameRuler per fare una partita a Othello.
     * @param time  tempo in millisecondi per fare una mossa, se <= 0 significa nessun
     *              limite
     * @param size  dimensione della board, sono accettati solamente i valori 6,8,10,12
     * @param p1  il nome del primo giocatore
     * @param p2  il nome del secondo giocatore
     * @throws NullPointerException se {@code p1} o {@code p2} è null
     * @throws IllegalArgumentException se size non è uno dei valori 6,8,10 o 12 */
    public Othello(long time, int size, String p1, String p2) {
        if(p1 == null || p2 == null) { throw new NullPointerException("Il nome di uno dei due giocatori non può essere null"); }
        if(!Arrays.asList(6, 8, 10, 12).contains(size)) { throw new IllegalArgumentException("La dimensione della board non rientra nei valori accettati"); }
        this.time = time; this.size = size;
        this.player1 = new RandPlayer<>(p1); this.player2 = new RandPlayer<>(p2);
        this.board = new BoardOct<>(size, size);
        this.board.put(new PieceModel<>(Species.DISC, "bianco"), new Pos(size/2, size/2)); //B
        this.board.put(new PieceModel<>(Species.DISC, "nero"), new Pos((size/2)+1, size/2)); //N
        this.board.put(new PieceModel<>(Species.DISC, "nero"), new Pos(size/2, (size/2)+1)); //N
        this.board.put(new PieceModel<>(Species.DISC, "bianco"), new Pos((size/2)+1, (size/2)+1)); //B
        this.cT = 1; //Inizia sempre il player1 (colore nero)
        this.gS = new ArrayList<>();
        this.player1.setGame(this); this.player2.setGame(this); //Assegno una copia del gioco ai player
    }

    /** Il nome rispetta il formato:
     * <pre>
     *     Othello<i>Size</i>
     * </pre>
     * dove <code><i>Size</i></code> è la dimensione della board, ad es. "Othello8x8". */
    @Override
    public String name() { return "Othello"+String.valueOf(size)+"x"+String.valueOf(size); }

    @Override
    public <T> T getParam(String name, Class<T> c) {
        return null; //TEMPORANEO
    }

    @Override
    public List<String> players() { return Collections.unmodifiableList(Arrays.asList(player1.name(), player2.name())); }

    /** Assegna il colore "nero" al primo giocatore e "bianco" al secondo. */
    @Override
    public String color(String name) {
        if(name == null) { throw new NullPointerException("name non può essere null"); }
        if(!players().contains(name)) { throw new IllegalArgumentException("Inserire il nome di un player presente in partita"); }
        if(name == player1.name()) { return "nero"; }
        return "bianco"; //Unica altra possibilità
    }

    @Override
    public Board<PieceModel<Species>> getBoard() { return Utils.UnmodifiableBoard(board); }

    /** Se il giocatore di turno non ha nessuna mossa valida il turno è
     * automaticamente passato all'altro giocatore. Ma se anche l'altro giuocatore
     * non ha mosse valide, la partita termina. */
    @Override
    public int turn() {
        if(cT == 1 && player1.getMove() == null) { cT = 2; }
        if(cT == 2 && player2.getMove() == null) { cT = 1; }
        if(player1.getMove() == null && player2.getMove() == null) { cT = 0; } //Il gioco finisce automaticamente
        return cT;
    }

    /** Se la mossa non è valida termina il gioco dando la vittoria all'altro
     * giocatore. */
    @Override
    public boolean move(Move<PieceModel<Species>> m) {
        if(m == null) { throw new NullPointerException("La mossa non può essere null"); }
        if(cT == 0) { throw new IllegalStateException("La partita è già terminata"); }
        if(isValid(m)) {
            for(Action i : m.getActions()) {
                if(i.getKind() == Action.Kind.ADD) { board.put((PieceModel) i.getPiece(), (Pos) i.getPiece()); }
                if(i.getKind() == Action.Kind.SWAP) { for(Object p : i.getPos()) { board.put((PieceModel) i.getPiece(), (Pos) p); } }
            }
            return true;
        }
        cT = 0; //Termina il game se la mossa non è valida.
        return false; //Non so come far vincere l'altro player
    }

    @Override
    public boolean unMove() {
        return false; //TEMPORANEO
    }

    @Override
    public boolean isPlaying(int i) {
        if(!Arrays.asList(1, 2).contains(i)) { throw new IllegalArgumentException("Il giocatore selezionato non è presente in questa partita"); }
        if(cT == 0) { return false; }
        return true; //Se il gioco non è terminato è sempre vero
    }

    @Override
    public int result() {
        if(cT != 0) { return -1; }
        if(score(1) == score(2)) { return 0; }
        if(score(1) > score(2)) { return 1; }
        return 2; //Unico caso rimasto
    }

    /** Ogni mossa, eccetto l'abbandono, è rappresentata da una {@link Action} di tipo
     * {@link Action.Kind#ADD} seguita da una {@link Action} di tipo
     * {@link Action.Kind#SWAP}. */
    @Override
    public Set<Move<PieceModel<Species>>> validMoves() { //Spero sto macello funzioni...
        if(cT == 0) { throw new IllegalStateException("Il gioco è già terminato"); }
        Set<Move<PieceModel<Species>>> moveL = new HashSet<>(); //Insieme risultato anche se vuoto verrà ritornato

        PieceModel<Species> pA = new PieceModel<>(Species.DISC, "bianco"); //Avversario
        PieceModel<Species> pP = new PieceModel<>(Species.DISC, "nero"); //Player
        if(cT == 2) { pA = pP; pP = new PieceModel<>(Species.DISC, "bianco"); } //In caso stia giocando il player2
        List<Board.Dir> directions = Arrays.asList(Board.Dir.UP, Board.Dir.UP_L, Board.Dir.LEFT,
                Board.Dir.DOWN_L, Board.Dir.DOWN, Board.Dir.DOWN_R, Board.Dir.RIGHT, Board.Dir.UP_R);

        for(Pos p : board.positions()) {
            if(board.get(p) == null) { //Se la posizione è vuota
                List<Pos> swapPos = new ArrayList<>(); //Posizioni da aggiungere a quelle da swappare
                for(Board.Dir d : directions) {
                    if(Objects.equals(board.get(board.adjacent(p, d)),pA)) { //Se è presente una pedina avversaria in una qualunque direzione adiacente alla posizione vuota
                        PieceModel<Species> probe = pA;
                        for(Board.Dir d1 : directions) {
                            Pos tempP = board.adjacent(p, d1);
                            List<Pos> swapPosTemp = new ArrayList<>();
                            while(probe == pA) {
                                if(Objects.equals(tempP,pA)) { probe = board.get(tempP); swapPosTemp.add(board.adjacent(p,d1)); }
                                if(Objects.equals(tempP,pP)) { break; } //Se incontra una sua pedina per chiudere il ciclo
                                if(Objects.equals(tempP,null)) { swapPosTemp.clear(); break; } //Se è presente una posizione vuota alla fine del probe NON aggiunge le posizioni salvate
                            }
                            swapPos.addAll(swapPosTemp); //Aggiunge tutte le nuove posizioni alla lista delle posizioni della mossa.
                        }
                    }
                }
                if(swapPos.size() != 0) {
                    Action aA = new Action(p, pP);
                    Pos[] swapPosA = swapPos.toArray(new Pos[swapPos.size()]);
                    Action aS = new Action(pP, (Pos[]) swapPosA);
                    moveL.add(new Move(Arrays.asList(aA, aS))); //Aggiungo la mossa appena creata
                }
            }
        }
        return moveL;
    }

    @Override
    public double score(int i) {
        if(!Arrays.asList(1, 2). contains(i)) { throw new IllegalArgumentException("Selezionare un giocatore presente nella partita"); }

        int score1 = board.get(new PieceModel<>(Species.DISC, "nero")).size();
        int score2 = board.get(new PieceModel<>(Species.DISC, "bianco")).size();

        if(score1 == score2) { return 0; } //Parità
        if(score1 > score2) { return 1; } //Vittoria del player1
        return 2; //Unica alta possibilità, vittoria player2
    }

    @Override
    public GameRuler<PieceModel<Species>> copy() {
        return null; //TEMPORANEO
    }

    @Override
    public Mechanics<PieceModel<Species>> mechanics() { return null; } //TEMPORANEO
}
