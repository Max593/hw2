package gapp.ulg.games;

import gapp.ulg.game.board.*;
import gapp.ulg.game.util.BoardOct;
import gapp.ulg.game.util.Utils;
import gapp.ulg.play.RandPlayer;

import static gapp.ulg.game.board.Board.Dir.*;
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
    private final Player player1;
    private final Player player2;
    private Board<PieceModel<Species>> board;
    private long time;
    private int size;
    private int cT;
    private List<GameRuler> gS; //Lista di tutti gli stati di gioco in ordine di esecuzione

    /** Crea un GameRuler per fare una partita a Othello, equivalente a
     * {@link Othello#Othello(long, int, String, String) Othello(0,8,p1,p2)}.
     * @param p1  il nome del primo giocatore
     * @param p2  il nome del secondo giocatore
     * @throws NullPointerException se p1 o p2 è null */
    public Othello(String p1, String p2) { this(0, 8, p1, p2); }

    /** Crea un GameRuler per fare una partita a Othello.
     * @param time  tempo in millisecondi per fare una mossa, se <= 0 significa nessun
     *              limite
     * @param size  dimensione della board, sono accettati solamente i valori 6,8,10,12
     * @param p1  il nome del primo giocatore
     * @param p2  il nome del secondo giocatore
     * @throws NullPointerException se {@code p1} o {@code p2} è null
     * @throws IllegalArgumentException se size non è uno dei valori 6,8,10 o 12 */
    public Othello(long time, int size, String p1, String p2) {
        if(p1 == null || p2 == null) { throw new NullPointerException("Il player1 o il player2 è null"); }
        if(!Arrays.asList(6, 8, 10, 12).contains(size)) { throw new IllegalArgumentException("Size non è 6, 8, 10 o 12"); }

        this.player1 = new RandPlayer<>(p1);
        this.player2 = new RandPlayer<>(p2);
        this.time = time;
        this.size = size;
        this.board = new BoardOct(size, size);
        board.put(new PieceModel(Species.DISC, "bianco"), new Pos(size/2, size/2)); //B
        board.put(new PieceModel(Species.DISC, "nero"), new Pos((size/2)+1, size/2)); //N
        board.put(new PieceModel(Species.DISC, "nero"), new Pos(size/2, (size/2)+1)); //N
        board.put(new PieceModel(Species.DISC, "bianco"), new Pos((size/2)+1, (size/2)+1)); //B
        this.cT = 1; //Inizia il player1 (nero)
        this.gS = new ArrayList<>(); //SPERIMENTALE
        for(Player i : Arrays.asList(player1, player2)) { i.setGame(copy()); } //Distribuisce una copia del gameruler ai giocatori
    }

    /** Il nome rispetta il formato:
     * <pre>
     *     Othello<i>Size</i>
     * </pre>
     * dove <code><i>Size</i></code> è la dimensione della board, ad es. "Othello8x8". */
    @Override
    public String name() { return "Othello"+size+"x"+size; }

    @Override
    public <T> T getParam(String name, Class<T> c) {
        if(name == null || c == null) { throw new NullPointerException("name o c sono null");}
        if(!Arrays.asList("Board", "Time").contains(name)) {
            throw new IllegalArgumentException("Nessun parametro corrispondente trovato");
        }
        if(name == "Time") {
            if(c != String.class) { throw new ClassCastException("Tipo del valore incompatibile"); }
            if(time > 60) {
                String ris = String.valueOf(time/60)+"m"+String.valueOf(time%60)+"s";
                return (T) ris;
            }
            if(time < 60) { return (T) (String.valueOf(time/60)+"s"); }
        }
        if(name == "Board") { return (T) (String.valueOf(size)+"x"+String.valueOf(size)); }
        return null; //TENTATIVO
    }

    @Override
    public List<String> players() { return Arrays.asList(player1.name(), player2.name()); }

    /** Assegna il colore "nero" al primo giocatore e "bianco" al secondo. */
    @Override
    public String color(String name) { //Ancora mai usato in maniera utile
        if(name == null) { throw new NullPointerException("Il nome del player non può essere null"); }
        if(!players().contains(name)) { throw new IllegalArgumentException("Il player non è presente nel gioco"); }
        if(players().indexOf(name) == 0) { return "nero"; }
        return "bianco";
    }

    @Override
    public Board<PieceModel<Species>> getBoard() { return Utils.UnmodifiableBoard(board); }

    /** Se il giocatore di turno non ha nessuna mossa valida il turno è
     * automaticamente passato all'altro giocatore. Ma se anche l'altro giuocatore
     * non ha mosse valide, la partita termina. */
    @Override
    public int turn() {
        if(cT == 1 && validMoves().size() == 0) { cT = 2; }
        if(cT == 2 && validMoves().size() == 0) { cT = 1; }
        if(player1.getMove() == null && player2.getMove() == null) { cT = 0; }
        return cT;
    }

    /** Se la mossa non è valida termina il gioco dando la vittoria all'altro
     * giocatore. */
    @Override
    public boolean move(Move<PieceModel<Species>> m) { //Ancora non ho determinato come passare la vittoria all'altro giocatore
        if(m == null) { throw new NullPointerException("La mossa non può essere null"); }
        if(result() > -1) { throw new IllegalStateException("Il gioco è già terminato"); }
        if(m.getKind() == Move.Kind.ACTION) {
            String c = "bianco";
            if(cT == 1) { c = "nero"; }
            for(Object i : m.getActions()) { //Le varie azioni
                if(((Action) i).getKind() == Action.Kind.ADD) {
                    board.put(new PieceModel<Species>(Species.DISC,c), (Pos) ((Action) i).getPos().get(0)); }
                if(((Action) i).getKind() == Action.Kind.SWAP) { //Potrebbe essere semplificato
                    for(Object p : ((Action) i).getPos()) {
                        board.put(new PieceModel<Species>(Species.DISC,c), (Pos) ((Action) i).getPos().get(0)); } } }
            if(cT == 1) { cT = 2; } //Se sta giocando il player1 passa al 2
            cT = 1;
            gS.add(copy()); //Copia lo status di gioco in caso di unMove
            return true;} //Ritorna il gioco al player1
        if(!isValid(m)) { cT = 0; } //Non ho ancora specificato come vince l'altro giocatore
        return false;
    }

    @Override
    public boolean unMove() {
        if(gS.size() > 0) {
            for(Player i : Arrays.asList(player1, player2)) { i.setGame(gS.get(gS.size()-2)); } //Dovrebbe essere -2 per prendere la penultima azione (da testare)
        }
        return false;
    }

    @Override
    public boolean isPlaying(int i) {
        if(!Arrays.asList(1, 2).contains(size)) { throw new IllegalArgumentException("L'indice non corrisponde a nessun giocatore"); }
        return result() <= -1; //Se il gioco è terminato è sempre false, alternativamente il giocatore è sicuramente in gioco
    }

    @Override
    public int result() {
        if(player1.getMove() == null && player2.getMove() == null) { //Se la partita è effettivamente finita
            if(score(1) == score(2)) { return 0; } //Parità
            if(score(1) > score(2)) { return 1; }
            if(score(2) > score(1)) { return 2;} }
        return -1; } //Il gioco NON è terminato

    /** Ogni mossa, eccetto l'abbandono, è rappresentata da una {@link Action} di tipo
     * {@link Action.Kind#ADD} seguita da una {@link Action} di tipo
     * {@link Action.Kind#SWAP}. */
    @Override
    public Set<Move<PieceModel<Species>>> validMoves() {//Le mosse inizieranno solo con ADD e seguiranno solo con SWAP
        Set<Move<PieceModel<Species>>> mosse = new HashSet<>(); //Insieme delle mosse (risultato), inizializzato come insieme vuoto
        String cA = "nero";
        String cP = "bianco";
        if(cT == 1) { cA = "bianco"; cP = "nero"; } //Colore delle pedine del player in base al turno di gioco [cA = Avversario, cP = Player ]

        for(Pos p : board.positions()){ //Tutte le posizioni della board
            if(board.get(p) == new PieceModel<Species>(Species.DISC, cA)) { //Solo pedine del colore opposto
                for(Board.Dir d : new Board.Dir[]{UP, UP_L, LEFT, DOWN_L}) { //Per la metà delle direzioni
                    List<Pos> posizioni = new ArrayList<>(); //Posizioni che userò nello swap
                    PieceModel<Species> p1 = new PieceModel<Species>(Species.DISC, cA); Pos pos1 = null;
                    PieceModel<Species> p2 = new PieceModel<Species>(Species.DISC, cA); Pos pos2 = null;
                    Pos tp = p; //Posizione temporanea per i vari adjacent
                    Board.Dir d2 = null; //Direzione opposta
                    if(d == UP) { d2 = DOWN;} if(d == UP_L) {d2 = DOWN_R;} if(d == LEFT) {d2 = RIGHT;} if(d == DOWN_L) { d2 = UP_R;}
                    while(p1 == new PieceModel<Species>(Species.DISC, cA)){
                        if(board.get(board.adjacent(tp, d)) == null || board.get(board.adjacent(tp, d)) != new PieceModel<Species>(Species.DISC, cA)) {
                            p1 = board.get(board.adjacent(tp, d));
                            pos1 = board.adjacent(tp, d);
                            break; }
                        tp = board.adjacent(tp, d);
                        posizioni.add(board.adjacent(tp, d));
                    }
                    tp = p; //Resetto la posizione temporanea
                    while(p2 == new PieceModel<Species>(Species.DISC, cA)){
                        if(board.get(board.adjacent(tp, d2)) == null || board.get(board.adjacent(tp, d2)) != new PieceModel<Species>(Species.DISC, cA)) {
                            p2 = board.get(board.adjacent(tp, d2));
                            pos2 = board.adjacent(tp, d2);
                            break; }
                        tp = board.adjacent(tp, d2);
                        posizioni.add(board.adjacent(tp, d2));
                    }
                    if(p1 == null && p2 != new PieceModel<Species>(Species.DISC, cA) && p2 != null) { //Se p1 = null e p2 = pezzo del player corrente
                        Action<PieceModel<Species>> a1 = new Action<>(pos1, new PieceModel<>(Species.DISC, cP));
                        Action<PieceModel<Species>> a2 = new Action<>(new PieceModel<>(Species.DISC, cP), (Pos[]) posizioni.toArray());
                        mosse.add(new Move<>(a1,a2));
                    }
                    if(p2 == null && p1 != new PieceModel<Species>(Species.DISC, cA) && p1 != null) { //Se p2 = null e p1 = pezzo del player corrente
                        Action<PieceModel<Species>> a1 = new Action<>(pos2, new PieceModel<>(Species.DISC, cP));
                        Action<PieceModel<Species>> a2 = new Action<>(new PieceModel<>(Species.DISC, cP), (Pos[]) posizioni.toArray());
                        mosse.add(new Move<>(a1,a2));
                    }
                }
            }
        }
        return mosse;
    }

    @Override
    public double score(int i) {
        if(!Arrays.asList(1, 2).contains(size)) { throw new IllegalArgumentException("L'indice di turnazione non fa riferimento a nessun giocatore"); }
        int counter = 0;
        if(i == 1) {
            for(Pos p : board.positions()) { if(board.get(p) == new PieceModel<Species>(Species.DISC, "nero")) {counter++;} }
        }
        if(i == 2) {
            for(Pos p : board.positions()) { if(board.get(p) == new PieceModel<Species>(Species.DISC, "bianco")) {counter++;} }
        }
        return counter;
    }

    @Override
    public GameRuler<PieceModel<Species>> copy() { //ERRORE DI STACK OVERFLOW, DA SISTEMARE!!
        /*return new Othello(time, size, player1, player2, board, cT, gS);*/
        return null; //TEMPORANEO
    }

    private Othello(long time, int size, Player p1, Player p2, Board<PieceModel<Species>> b, int cT, List<GameRuler> gS) { //Dovrebbe essere una copia speculare di Othello (speriamo)
        this.player1 = p1;
        this.player2 = p2;
        this.time = time;
        this.size = size;
        this.board = b;
        this.cT = cT;
        this.gS = gS;
    }

    @Override
    public Mechanics<PieceModel<Species>> mechanics() { return null; } //Ancora da fare
}
