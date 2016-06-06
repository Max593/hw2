package gapp.ulg.games;

import gapp.ulg.game.board.*;
import gapp.ulg.game.util.BoardOct;
import gapp.ulg.game.util.Utils;
import gapp.ulg.play.RandPlayer;

import static gapp.ulg.game.board.PieceModel.Species;

import java.util.*;
import java.util.concurrent.*;


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
    private Player<PieceModel<Species>> player1;
    private Player<PieceModel<Species>> player2;
    private Board<PieceModel<Species>> board;
    private int cT; //Turno corrente
    private int forced; //In caso di resa o mossa invalida forza la vittoria di un player senza considerare il punteggio
    private List<GameRuler<PieceModel<Species>>> gS; //Stati di gioco salvati in ordine di esecuzione

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
        this.time = time;
        this.size = size;
        this.player1 = new RandPlayer<>(p1); this.player2 = new RandPlayer<>(p2); //Creo i players
        this.board = new BoardOct<>(size, size); //Inizializzo la board sullo stato iniziare (BNNB)
        this.board.put(new PieceModel<>(Species.DISC, "bianco"), new Pos((size/2)-1, size/2)); //B 3,4
        this.board.put(new PieceModel<>(Species.DISC, "nero"), new Pos(size/2, size/2)); //N 4,4
        this.board.put(new PieceModel<>(Species.DISC, "nero"), new Pos((size/2)-1, (size/2)-1)); //N 3,3
        this.board.put(new PieceModel<>(Species.DISC, "bianco"), new Pos((size/2), (size/2)-1)); //B 4,3
        this.cT = 1; //Inizia sempre il player1 (colore nero)
        this.forced = -1;
        this.gS = new ArrayList<>(); //Primissimo status di gioco salvato nell'apposita lista
        gS.add(copy());
        this.player1.setGame(this); this.player2.setGame(this); //Assegno una copia del gioco ai players
    }

    /** Il nome rispetta il formato:
     * <pre>
     *     Othello<i>Size</i>
     * </pre>
     * dove <code><i>Size</i></code> è la dimensione della board, ad es. "Othello8x8". */
    @Override
    public String name() { return "Othello"+String.valueOf(size)+"x"+String.valueOf(size); } //Nome del gioco con le dimensioni della board

    @Override
    public <T> T getParam(String name, Class<T> c) {
        return null;
    } //Temporaneo, ancora da scrivere

    @Override
    public List<String> players() { return Collections.unmodifiableList(Arrays.asList(player1.name(), player2.name())); }

    /** Assegna il colore "nero" al primo giocatore e "bianco" al secondo. */
    @Override
    public String color(String name) {
        if(name == null) { throw new NullPointerException("name non può essere null"); }
        if(!players().contains(name)) { throw new IllegalArgumentException("Inserire il nome di un player presente in partita"); }
        if(player1.name().equals(name)) { return "nero"; }
        return "bianco"; //Unica altra possibilità
    }

    @Override
    public Board<PieceModel<Species>> getBoard() { return Utils.UnmodifiableBoard(board); }

    /** Se il giocatore di turno non ha nessuna mossa valida il turno è
     * automaticamente passato all'altro giocatore. Ma se anche l'altro giuocatore
     * non ha mosse valide, la partita termina. */
    @Override
    public int turn() { return cT; }

    /** Se la mossa non è valida termina il gioco dando la vittoria all'altro
     * giocatore. */
    @Override
    public boolean move(Move<PieceModel<Species>> m) {
        if(m == null) { throw new NullPointerException("La mossa non può essere null"); }
        if(cT == 0) { throw new IllegalStateException("La partita è già terminata"); }
        if(isValid(m) && m.getKind() != Move.Kind.RESIGN) { //Si esegue la mossa
            for(Action i : m.getActions()) {
                if(i.getKind() == Action.Kind.ADD) { board.put((PieceModel) i.getPiece(), (Pos) i.getPos().get(0)); }
                if(i.getKind() == Action.Kind.SWAP) { for(Object p : i.getPos()) { board.put((PieceModel) i.getPiece(), (Pos) p); } }
            }
            if(cT == 2) { cT = 1; } //Passa il turno all'altro player
            else if(cT == 1) { cT = 2; }
            if(validMoves().isEmpty()) { //Non dovrebbe stare dentro il turn?? (funziona però)
                if(cT == 2) { cT = 1; } //Passa il turno all'altro player
                else if(cT == 1) { cT = 2; }
                if(validMoves().isEmpty()) { cT = 0; }
            }
            gS.add(copy());
            return true;
        }
        if(m.getKind() == Move.Kind.RESIGN
                && !validMoves().isEmpty()) { //Vince l'altro player se ci si arrende
            if(cT == 2) { forced = 1; }
            else if(cT == 1) { forced = 2; }
            cT = 0;
            gS.add(copy());
            return true;
        }
        if(cT == 2) { forced = 1; } //Vince l'altro player se si compie una mossa non valida
        else if(cT == 1) { forced = 2; }
        cT = 0; //Termina il game se la mossa non è valida.
        gS.add(copy());
        return false;
    }

    @Override
    public boolean unMove() { //Vorrei scrivere board = copy board passata, non ci riesco
        if(gS.size() == 1) { return false; } //Se abbiamo appena iniziato il gioco

        Board<PieceModel<Species>> past = gS.get(gS.size()-2).getBoard(); //Prende la penultima board
        for(Pos p : board.positions()) { //Per ogni posizione della board
            if(board.get(p) != null && past.get(p) == null) { board.remove(p); } //Elimina tutti gli ADD
            else if(board.get(p) != null && !board.get(p).equals(past.get(p))) {
                if(past.get(p) != null) { board.put((PieceModel) past.get(p), p); } //Rigira gli SWAP
            }
        }

        cT = gS.get(gS.size()-2).turn(); //Ritorna al turno di gioco passato (ritorna anche in gioco se necessario)
        gS.remove(gS.size()-1); //Elimina lo status su cui è stato fatto unMove
        player1.setGame(copy()); player2.setGame(copy()); //Reimposta i players allo stato attuale
        return true;
    }

    @Override
    public boolean isPlaying(int i) { //I giocatori non si eliminano, dunque NON sono in partita solo a gioco terminato
        if(!Arrays.asList(1, 2).contains(i)) { throw new IllegalArgumentException("Il giocatore selezionato non è presente in questa partita"); }
        return cT != 0;
    }

    @Override
    public int result() {
        if(cT != 0) { return -1; }
        else if(forced > -1) { return forced; }
        else if(score(1) > score(2)) { return 1; }
        else if(score(1) == score(2)) { return 0; }
        else return 2;
    }

    /** Ogni mossa, eccetto l'abbandono, è rappresentata da una {@link Action} di tipo
     * {@link Action.Kind#ADD} seguita da una {@link Action} di tipo
     * {@link Action.Kind#SWAP}. */
    @Override
    public Set<Move<PieceModel<Species>>> validMoves() { //Prima o poi dovrò trovare la forza di riscriverlo in multithreading...
        if(cT == 0) { throw new IllegalStateException("Il gioco è già terminato"); }
        Set<Move<PieceModel<Species>>> moveSet = new HashSet<>(); //Insieme risultato anche se vuoto verrà ritornato
        List<Board.Dir> directions = Arrays.asList(Board.Dir.UP, Board.Dir.UP_L, Board.Dir.LEFT,
                Board.Dir.DOWN_L, Board.Dir.DOWN, Board.Dir.DOWN_R, Board.Dir.RIGHT, Board.Dir.UP_R);
        PieceModel<Species> pA = new PieceModel<>(Species.DISC, "bianco"), pP = new PieceModel<>(Species.DISC, "nero");
        if(cT == 2) { pA = new PieceModel<>(Species.DISC, "nero"); pP = new PieceModel<>(Species.DISC, "bianco"); } //Se sta giocando il player 2;

        for(Pos p : board.positions()) { //Per ogni posizione della board
            if(board.get(p) == null) {
                Set<Pos> swap = new HashSet<>();
                for(Board.Dir d : directions) {
                    Set<Pos> tSwap = new HashSet<>();
                    try {
                        if(board.get(board.adjacent(p, d)).equals(pA)) {
                            Pos next = board.adjacent(p, d);
                            while(true) {
                                if(board.get(next) == null) { tSwap = new HashSet<>(); break; } //Se incontra una posizione vuota svuota la lista tSwap e interrompe il ciclo
                                if(board.get(next).equals(pP)) { break; } //Interrompe il ciclo avendo trovato una pedina propria
                                if(board.adjacent(next,d) == null) { tSwap = new HashSet<>(); break; } //Se va uscendo dalla board
                                if(board.get(next).equals(pA)) { tSwap.add(next); next = board.adjacent(next, d); } //Aggiunge la Pos alla tSwap e aggiorna next
                            }
                        }
                    } catch (NullPointerException e) { continue; }
                    if(tSwap.size() > 0) { swap.addAll(tSwap); }
                }
                if(swap.size() > 0) {
                    Pos[] swapArr = new Pos[swap.size()];
                    swapArr = swap.toArray(swapArr);
                    Action aA = new Action(p, pP);
                    Action aS = new Action(pP, swapArr);
                    moveSet.add(new Move(Arrays.asList(aA, aS)));
                }
            }
        }

        if(moveSet.size() > 0) { moveSet.add(new Move(Move.Kind.RESIGN)); }
        return Collections.unmodifiableSet(moveSet);
    }
/*
    @Override
    public Set<Move<PieceModel<Species>>> validMoves() { //Multithreading troppo lento per qualche motivo...
        if(cT == 0) { throw new IllegalStateException("Il gioco è già terminato"); }
        Set<Move<PieceModel<Species>>> moveSet = new HashSet<>(); //Insieme risultato anche se vuoto verrà ritornato
        List<Board.Dir> directions = Arrays.asList(Board.Dir.UP, Board.Dir.UP_L, Board.Dir.LEFT,
                Board.Dir.DOWN_L, Board.Dir.DOWN, Board.Dir.DOWN_R, Board.Dir.RIGHT, Board.Dir.UP_R);

        class Operation implements Callable {
            private Pos p;
            public Operation(Pos p) { this.p = p; }

            @Override
            public Set<Move<PieceModel<Species>>> call() throws Exception {
                PieceModel<Species> pA = new PieceModel<>(Species.DISC, "bianco"), pP = new PieceModel<>(Species.DISC, "nero");
                if(cT == 2) { pA = new PieceModel<>(Species.DISC, "nero"); pP = new PieceModel<>(Species.DISC, "bianco"); } //Se sta giocando il player 2;
                Set<Move<PieceModel<Species>>> tempSet = new HashSet<>();
                Set<Pos> swap = new HashSet<>();

                for(Board.Dir d : directions) {
                    Set<Pos> tSwap = new HashSet<>();
                    try {
                        if(board.get(board.adjacent(p, d)).equals(pA)) {
                            Pos next = board.adjacent(p, d);
                            while(true) {
                                if(board.get(next) == null) { tSwap = new HashSet<>(); break; } //Se incontra una posizione vuota svuota la lista tSwap e interrompe il ciclo
                                if(board.get(next).equals(pP)) { break; } //Interrompe il ciclo avendo trovato una pedina propria
                                if(board.adjacent(next,d) == null) { tSwap = new HashSet<>(); break; } //Se va uscendo dalla board
                                if(board.get(next).equals(pA)) { tSwap.add(next); next = board.adjacent(next, d); } //Aggiunge la Pos alla tSwap e aggiorna next
                            }
                        }
                    } catch (NullPointerException e) { continue; }
                    if(tSwap.size() > 0) { swap.addAll(tSwap); }
                }

                if(swap.size() > 0) {
                    Pos[] swapArr = new Pos[swap.size()];
                    swapArr = swap.toArray(swapArr);
                    Action aA = new Action(p, pP);
                    Action aS = new Action(pP, swapArr);
                    tempSet.add(new Move(Arrays.asList(aA, aS)));
                }

                return tempSet;
            }
        }

        ExecutorService service = Executors.newCachedThreadPool();
        Set<Future<Set<Move<PieceModel<Species>>>>> listFut = new HashSet<>();
        for(Pos p : board.positions()) { //Per ogni posizione della board
            if(board.get(p) == null) {
                Callable<Set<Move<PieceModel<Species>>>> callable = new Operation(p);
                Future<Set<Move<PieceModel<Species>>>> future = service.submit(callable);
                listFut.add(future);
            }
        }

        for(Future<Set<Move<PieceModel<Species>>>> future : listFut) {
            try {
                if(!future.get().isEmpty()) { moveSet.addAll(future.get()); }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        service.shutdown();
        if(moveSet.size() > 0) { moveSet.add(new Move(Move.Kind.RESIGN)); }
        return Collections.unmodifiableSet(moveSet);
    }
     */

    @Override
    public double score(int i) {
        if(!Arrays.asList(1, 2). contains(i)) { throw new IllegalArgumentException("Selezionare un giocatore presente nella partita"); }

        int score1 = board.get(new PieceModel<>(Species.DISC, "nero")).size();
        int score2 = board.get(new PieceModel<>(Species.DISC, "bianco")).size();

        if(i == 1) { return score1; }
        return score2;
    }

    @Override
    public GameRuler<PieceModel<Species>> copy() { return new Othello(time, size, player1, player2, Utils.bCopy(board, size, size), cT, forced, gS); }

    private Othello(long t, int s, Player p1, Player p2, Board b, int cT, int forced, List gS) {
        this.time = t;
        this.size = s;
        this.player1 = p1;
        this.player2 = p2;
        this.board = b;
        this.cT = cT;
        this.forced = forced;
        this.gS = gS;
    }

    @Override
    public Mechanics<PieceModel<Species>> mechanics() { //Funziona, non lo toccherò mai più

        List<PieceModel<Species>> pcs = Arrays.asList(new PieceModel<>(Species.DISC, "nero"), new PieceModel<>(Species.DISC, "bianco")); //Tutti i pezzi di gioco

        Map<Pos, PieceModel<Species>> posMap = new HashMap<>(); //Usato per la situazione starter
        posMap.put(new Pos((size/2)-1, size/2), new PieceModel<>(Species.DISC, "bianco")); //B 3,4
        posMap.put(new Pos(size/2, size/2), new PieceModel<>(Species.DISC, "nero")); //N 4,4
        posMap.put(new Pos((size/2)-1, (size/2)-1), new PieceModel<>(Species.DISC, "nero")); //N 3,3
        posMap.put(new Pos((size/2), (size/2)-1), new PieceModel<>(Species.DISC, "bianco")); //B 4,3

        Next<PieceModel<Species>> prossimaM = s -> {
            if(s == null) { throw new NullPointerException("La situazione di gioco non può essere null"); }

            ConcurrentMap<Move<PieceModel<Species>>, Situation<PieceModel<Species>>> nextMoves = new ConcurrentHashMap<>(); //Mappa soluzione

            class Operation implements Callable{
                private Move<PieceModel<Species>> mov;
                private GameRuler<PieceModel<Species>> othello;

                public Operation(Move m, GameRuler g) {
                    this.mov = m;
                    this.othello = g;
                }

                @Override
                public Map<Move<PieceModel<Species>>, Situation<PieceModel<Species>>> call() throws Exception {
                    Map<Move<PieceModel<Species>>, Situation<PieceModel<Species>>> res = new HashMap<>(); //Risultato
                    othello.move(mov); Map<Pos, PieceModel<Species>> mapSit = new HashMap<>();
                    othello.getBoard().positions().stream().filter(p -> othello.getBoard().get(p) != null).forEach(p -> {
                        mapSit.put(p, othello.getBoard().get(p));
                    });
                    res.put(mov, new Situation<>(mapSit, othello.turn()));
                    return res;
                }
            }

            ExecutorService executor = Executors.newCachedThreadPool();
            Set<Future<Map<Move<PieceModel<Species>>, Situation<PieceModel<Species>>>>> listFut = new HashSet<>();
            for(Move<PieceModel<Species>> m : validMoves()) {
                Callable<Map<Move<PieceModel<Species>>, Situation<PieceModel<Species>>>> callable = new Operation(m, copy());
                Future<Map<Move<PieceModel<Species>>, Situation<PieceModel<Species>>>> future = executor.submit(callable);
                listFut.add(future);
            }

            for(Future<Map<Move<PieceModel<Species>>, Situation<PieceModel<Species>>>> future : listFut) {
                try {
                    nextMoves.putAll(future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            executor.shutdown();
            return nextMoves;
        };

        return new Mechanics<>(time, Collections.unmodifiableList(pcs), board.positions(), 2, new Situation<>(posMap, 1), prossimaM);
    }
}