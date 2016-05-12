package gapp.ulg.game.util;

import gapp.ulg.game.board.Board;
import gapp.ulg.game.board.Pos;

import java.util.*;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Gli oggetti BoardOct implementano l'interfaccia {@link Board} per rappresentare
 * board generali con sistema di coordinate {@link System#OCTAGONAL}
 * modificabili.
 * @param <P>  tipo del modello dei pezzi */
public class BoardOct<P> implements Board<P> {

    public final int width;
    public final int height;
    public final System system;
    public final Map<Pos, P> positions;

    /** Crea una BoardOct con le dimensioni date (può quindi essere rettangolare).
     * Le posizioni della board sono tutte quelle comprese nel rettangolo dato e le
     * adiacenze sono tutte e otto, eccetto per le posizioni di bordo.
     * @param width  larghezza board
     * @param height  altezza board
     * @throws IllegalArgumentException se width <= 0 o height <= 0 */
    public BoardOct(int width, int height) {
        if(width <= 0 || height <= 0) { throw new IllegalArgumentException("La larghezza o l'altezza non possono essere <= 0"); }

        this.system = System.OCTAGONAL;
        this.width = width;
        this.height = height;

        Map<Pos, P> temp = new HashMap<>();
        for(int i = 0; i < width; i++) { for(int h = 0; h < height; h++) { temp.put(new Pos(i, h), null); } } //Crea la posizione sulla scacchiera e non pone nessun pezzo
        this.positions = temp; }

    /** Crea una BoardOct con le dimensioni date (può quindi essere rettangolare)
     * escludendo le posizioni in exc. Le adiacenze sono tutte e otto, eccetto per
     * le posizioni di bordo o adiacenti a posizioni escluse. Questo costruttore
     * permette di creare board per giochi come ad es.
     * <a href="https://en.wikipedia.org/wiki/Camelot_(board_game)">Camelot</a>
     * @param width  larghezza board
     * @param height  altezza board
     * @param exc  posizioni escluse dalla board
     * @throws NullPointerException se exc è null
     * @throws IllegalArgumentException se width <= 0 o height <= 0 */
    public BoardOct(int width, int height, Collection<? extends Pos> exc) {
        if(exc == null) { throw new NullPointerException("L'insieme delle posizioni escluse non può essere null"); }
        if(width <= 0 || height <= 0) { throw new IllegalArgumentException("La larghezza o l'altezza non possono essere <= 0"); }

        this.system = System.OCTAGONAL;
        this.width = width;
        this.height = height;

        Map<Pos, P> temp = new HashMap<>();
        for(int i = 0; i < width; i++) {
            for(int h = 0; h < height; h++) {
                if(!exc.contains(new Pos(i, h))) { temp.put(new Pos(i, h), null); } } }
        this.positions = temp;
    }

    @Override
    public System system() { return system; }

    @Override
    public int width() { return width; }

    @Override
    public int height() { return height; }

    @Override
    public Pos adjacent(Pos p, Dir d) {
        if(p == null || d == null) { throw new NullPointerException("La posizione o la direzione non possono essere null"); }

        if(positions.containsKey(p)) {
            if(d == Dir.UP) {
                Pos tp = new Pos(p.getB(), p.getT()+1);
                if(positions.containsKey(tp)) { return tp; } }
            if(d == Dir.DOWN && p.getT()-1 >= 0) {
                Pos tp = new Pos(p.getB(), p.getT()-1);
                if(positions.containsKey(tp)) { return tp; } }
            if(d == Dir.LEFT && p.getB()-1 >= 0) {
                Pos tp = new Pos(p.getB()-1, p.getT());
                if(positions.containsKey(tp)) { return tp; } }
            if(d == Dir.RIGHT) {
                Pos tp = new Pos(p.getB()+1, p.getT());
                if(positions.containsKey(tp)) { return tp; } }
            if(d == Dir.UP_L && p.getB()-1 >= 0) {
                Pos tp = new Pos(p.getB()-1, p.getT()+1);
                if(positions.containsKey(tp)) { return tp; } }
            if(d == Dir.UP_R) {
                Pos tp = new Pos(p.getB()+1, p.getT()+1);
                if(positions.containsKey(tp)) { return tp; } }
            if(d == Dir.DOWN_L && p.getB()-1 >= 0 && p.getT()-1 >= 0) {
                Pos tp = new Pos(p.getB()-1, p.getT()-1);
                if(positions.containsKey(tp)) { return tp; } }
            if(d == Dir.DOWN_R && p.getT()-1 >= 0) {
                Pos tp = new Pos(p.getB()+1, p.getT()-1);
                if(positions.containsKey(tp)) { return tp; } }
        }
        return null; }

    @Override
    public List<Pos> positions() {
        List<Pos> temp = new ArrayList<>(positions.keySet());
        return Collections.unmodifiableList(temp); }

    @Override
    public P get(Pos p) {
        if(p == null) { throw new NullPointerException("La posizione non può essere null"); }
        if(positions.containsKey(p)) { return positions.get(p); } return null;
    }

    @Override
    public boolean isModifiable() { return true; }

    @Override
    public P put(P pm, Pos p) {
        if(!isModifiable()) { throw new UnsupportedOperationException("Questa Board è immodificabile"); }
        if(pm == null || p == null) { throw new NullPointerException("La posizione o il pezzo sono null"); }
        if(!positions.containsKey(p)) { throw new IllegalArgumentException("La posizione non è parte della board");}
        positions.put(p, pm);
        return pm;
    }

    @Override
    public P remove(Pos p) {
        if(!isModifiable()) { throw new UnsupportedOperationException("Questa Board è immodificabile"); }
        if(p == null) { throw new NullPointerException("La posizione non può essere null"); }
        if(!positions.containsKey(p)) { throw new IllegalArgumentException("La posizione non è parte della board"); }
        P old = positions.get(p);
        positions.put(p, null);
        return old;
    }
}
