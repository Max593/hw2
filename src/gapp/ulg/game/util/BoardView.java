package gapp.ulg.game.util;

import gapp.ulg.game.board.Board;
import gapp.ulg.game.board.Pos;

import java.util.List;

/**
 * Created by maxmo on 06/04/2016.
 */
public class BoardView<P> implements Board<P> {
    private Board<P> boardOrig;

    public BoardView(Board<P> b) { this.boardOrig = b; } //Costruttore

    @Override
    public boolean isModifiable(){ return false; }

    @Override
    public P put(P pm, Pos p) { throw new UnsupportedOperationException("La view non è modificabile"); }

    @Override
    public void put(P pm, Pos p, Dir d, int n) { throw new UnsupportedOperationException("La view non è modificabile"); }

    @Override
    public P remove(Pos p) { throw new UnsupportedOperationException("La view non è modificabile"); }

    @Override
    public final System system() { return boardOrig.system(); }

    @Override
    public final int width() { return boardOrig.width(); }

    @Override
    public int height() { return boardOrig.height(); }

    @Override
    public final Pos adjacent(Pos p, Dir d) { return boardOrig.adjacent(p, d); }

    @Override
    public final List<Pos> positions() { return boardOrig.positions(); }

    @Override
    public final P get(Pos p) { return boardOrig.get(p); }
}
