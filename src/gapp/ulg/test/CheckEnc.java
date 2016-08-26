package gapp.ulg.test;

import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.PieceModel;
import gapp.ulg.game.board.Pos;
import gapp.ulg.game.util.Probe;
import gapp.ulg.games.MNKgame;
import gapp.ulg.games.Othello;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maxmo on 25/08/2016.
 */
public class CheckEnc {

    public static void main(String[] args) {
        GameRuler<PieceModel<PieceModel.Species>> g1 = new Othello(4,8,"Marco","Maria");

        Map<Pos, PieceModel<PieceModel.Species>> m1 = new HashMap<>();
        m1.put(new Pos(1,3), new PieceModel<>(PieceModel.Species.DISC, "bianco"));
        m1.put(new Pos(2,3), new PieceModel<>(PieceModel.Species.DISC, "nero"));

        GameRuler.Situation<PieceModel<PieceModel.Species>> s1 = new GameRuler.Situation<>(m1,2);

        Probe.EncS<PieceModel<PieceModel.Species>> save1 = new Probe.EncS<>(g1.mechanics(),s1);
        Probe.EncS<PieceModel<PieceModel.Species>> save2 = save1;
        save1.decode(g1.mechanics());
        System.out.println(save1.hashCode()+" "+save2.hashCode());
    }

}
