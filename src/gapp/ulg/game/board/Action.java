package gapp.ulg.game.board;

import java.util.*;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi nè i campi pubblici.</b>
 * <br>
 * Un oggetto Action rappresenta un'azione che può essere parte della mossa di
 * un giocatore nel suo turno di gioco. Gli oggetti Action sono immutabili.
 * <br>
 * Un Action è di uno dei seguenti quattro tipi:
 * <ul>
 *     <li>{@link Kind#ADD}: Aggiunge un pezzo di un modello dato, ad es. aggiungere
 *     un pezzo in giochi come Go e Othello.</li>
 *     <li>{@link Kind#REMOVE}: Rimuove uno o più pezzi, ad es. catturare uno o più
 *     pezzi in Go o mangiare l'alfiere nero con la regina bianca negli Scacchi.</li>
 *     <li>{@link Kind#MOVE}: Muove uno o più pezzi in una direzione, ad es. muovere
 *     la torre bianca negli Scacchi, una pedina nella Dama o muovere una fila di
 *     tre pezzi nel gioco Abalone.</li>
 *     <li>{@link Kind#JUMP}: Muove un pezzo, eventualmente saltando sopra altri
 *     pezzi, ad es. muovere un cavallo negli Scacchi o una pedina nella Dama
 *     saltando e mangiando una pedina avversaria</li>
 *     <li>{@link Kind#SWAP}: Sostituisce uno o più pezzi con pezzi di un modello
 *     dato, ad es. rovesciare pezzi da bianchi a neri in Othello o promuovere a
 *     dama una pedina bianca nella Dama.</li>
 * </ul>
 * In generale la mossa di un giocatore è costituita da una o più azioni, cioè,
 * oggetti Action. Ad es. in Go una mossa senza cattura è costituita solamente
 * da un Action di tipo {@link Kind#ADD} ma una mossa con cattura consiste di un
 * Action di tipo {@link Kind#ADD} seguita da una Action di tipo
 * {@link Kind#REMOVE}.
 * @param <P>  tipo del modello dei pezzi
 * @see Move */
public class Action<P> {
    public enum Kind {
        /** Aggiunge un pezzo di un modello dato */
        ADD,
        /** Rimuove uno o più pezzi */
        REMOVE,
        /** Muove uno o più pezzi in una direzione */
        MOVE,
        /** Muove un pezzo, eventualmente saltando sopra altri pezzi */
        JUMP,
        /** Sostituisce uno o più pezzi con pezzi di un modello dato */
        SWAP
    }

    /** Tipo dell'azione, non è mai null */
    public final Kind kind;
    /** Modello di pezzo, è diverso da null solamente per {@link Kind#ADD} e
     * {@link Kind#SWAP} */
    public final P piece;
    /** Lista immodificabile di posizioni, non è mai null ed ha sempre
     * lunghezza >= 1. Ha lunghezza esattamente 1 per {@link Kind#ADD}. */
    public final List<Pos> pos;
    /** Direzione di movimento, è diversa da null solamente per {@link Kind#MOVE} */
    public final Board.Dir dir;
    /** Numero passi nella direzione {@link Action#dir}, ha significato solamente
     * quando il tipo dell'azione è {@link Kind#MOVE} */
    public final int steps;

    /** Crea un'azione di tipo {@link Kind#ADD} che aggiunge nella posizione p un
     * pezzo di modello pm.
     * @param p  posizione del nuovo pezzo
     * @param pm  modello del pezzo da aggiungere
     * @throws NullPointerException se p o pm è null */
    public Action(Pos p, P pm) {
        if(p == null || pm == null) { throw new NullPointerException("La posizione o il modello da aggiungere è null."); }
        this.kind = Kind.ADD;
        this.piece = pm;
        this.pos = Collections.singletonList(p); //Lista immutabile di un singolo elemento, molto conveniente
        this.dir = null;
        this.steps = 0;
    }

    /** Crea un'azione di tipo {@link Kind#REMOVE} che rimuove i pezzi nelle
     * posizioni date.
     * @param pp  posizioni dei pezzi da rimuovere
     * @throws NullPointerException se una delle posizioni è null
     * @throws IllegalArgumentException se non è data almeno una posizione o sono
     * date posizioni duplicate */
    public Action(Pos...pp) {
        if(pp.length == 0) { throw new IllegalArgumentException("Non è stata inserita nemmeno una posizione"); }
        Set<Pos> test = new HashSet<>(Arrays.asList(pp)); //Insieme delle ripetizioni (nessuna ripetizione)
        if(test.size() < pp.length) { throw new IllegalArgumentException("Sono presenti posizioni duplicate"); }
        for(Pos i : pp){
            if(i == null) { throw new NullPointerException("Una delle posizioni è null"); }
        }
        this.kind = Kind.REMOVE;
        this.piece = null;
        this.pos = Arrays.asList(pp);
        this.dir = null;
        this.steps = 0;
    }

    /** Crea un'azione di tipo {@link Kind#MOVE} che muove tutti i pezzi nelle
     * posizioni pp nella direzione d per ns passi.
     * @param d  direzione di movimento
     * @param ns  numero passi
     * @param pp  posizioni di partenza dei pezzi da muovere
     * @throws NullPointerException se d è null o una delle posizioni è null
     * @throws IllegalArgumentException se ns < 1 o non è data almeno una posizione
     * o sono date posizioni duplicate */
    public Action(Board.Dir d, int ns, Pos...pp) {
        if(d == null) { throw new NullPointerException("La direzione è null"); }
        for(Pos i : pp) { if(i == null) { throw new NullPointerException("Una delle posizioni è null"); } }
        if(ns < 1 || pp.length == 0) { throw new IllegalArgumentException("Il numero di passi è < 1 o non è stata data almeno una posizione"); }
        Set<Pos> test = new HashSet<>(Arrays.asList(pp)); //Lista di tutte le posizioni in input
        if(test.size() < pp.length) { throw new IllegalArgumentException("Sono presenti posizioni duplicate"); }
        this.kind = Kind.MOVE;
        this.piece = null;
        this.pos = Arrays.asList(pp);
        this.dir = d;
        this.steps = ns;
    }

    /** Crea un'azione di tipo {@link Kind#JUMP} che muove il pezzo dalla posizione
     * p1 alla posizione p2.
     * @param p1  posizione di partenza
     * @param p2  posizione di arrivo
     * @throws NullPointerException se p1 o p2 è null
     * @throws IllegalArgumentException se p1 è uguale a p2 */
    public Action(Pos p1, Pos p2) {
        if(p1 == null || p2 == null) { throw new NullPointerException("Una delle posizioni è null"); }
        if(p1.equals(p2)) { throw new IllegalArgumentException("Le posizioni sono uguali"); }
        this.kind = Kind.JUMP;
        this.piece = null;
        this.pos = Arrays.asList(p1, p2);
        this.dir = null;
        this.steps = 0;
    }

    /** Crea un'azione di tipo {@link Kind#SWAP} che sostituisce tutti i pezzi
     * nelle posizioni pp con pezzi di modello pm.
     * @param pm  modello del nuovo pezzo
     * @param pp  posizioni dei pezzi da sostituire
     * @throws NullPointerException se pm è null o una delle posizioni è null
     * @throws IllegalArgumentException se non è data almeno una posizione o sono
     * date posizioni duplicate */
    public Action(P pm, Pos...pp) {
        if(pm == null) { throw new NullPointerException("Il modello è null"); }
        for(Pos i : pp) { if(i == null) { throw new NullPointerException("Una delle posizioni è null"); } }
        if(pp.length == 0) { throw new IllegalArgumentException("Non sono state date posizioni in input"); }
        Set<Pos> test = new HashSet<>(Arrays.asList(pp));
        if(test.size() < pp.length) { throw new IllegalArgumentException("Sono presenti posizioni duplicate"); }
        this.kind = Kind.SWAP;
        this.piece = pm;
        this.pos = Arrays.asList(pp);
        this.dir = null;
        this.steps = 0;
    }

    public Kind getKind(){ return kind; }
    public P getPiece() { return piece; }
    public List<Pos> getPos() { return pos; }
    public Board.Dir getDir() { return dir; }
    public int getSteps() { return steps; }

    /** Ritorna true se e solo se x è un oggetto di tipo {@link Action} ed ha gli
     * stessi valori dei campi {@link Action#kind}, {@link Action#piece},
     * {@link Action#pos}, {@link Action#dir} e {@link Action#steps}.
     * <br>
     * <b>ATTENZIONE: due liste {@link Action#pos} sono considerate uguali se
     * contengono le stesse posizioni indipendentemente dall'ordine.</b>
     * @param x  un oggetto (o null)
     * @return true se x è uguale a questa azione */
    @Override
    public boolean equals(Object x) {
        if(x instanceof Action && Objects.equals(((Action) x).getKind(),kind) &&
                Objects.equals(((Action) x).getPiece(),piece) && ((Action) x).getPos().containsAll(pos) &&
                Objects.equals(((Action) x).getDir(),dir) && Objects.equals(((Action) x).getSteps(),steps)) { return true; } return false; }

    /** Ridefinito coerentemente alla ridefinizione di {@link Action#equals(Object)}.
     * @return hash code di questa azione */
    @Override
    public int hashCode() {
        Set<Pos> sPos = new HashSet<>();
        sPos.addAll(pos);
        return Objects.hash(kind, piece, sPos, dir, steps);
    }
}
