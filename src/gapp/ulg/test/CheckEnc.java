package gapp.ulg.test;

import com.sun.deploy.util.StringUtils;
import gapp.ulg.game.board.Board;
import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.PieceModel;
import gapp.ulg.game.board.Pos;
import gapp.ulg.game.util.BoardOct;
import gapp.ulg.game.util.Probe;
import gapp.ulg.games.MNKgame;
import gapp.ulg.games.Othello;

import java.math.BigInteger;
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
        m1.put(new Pos(4,3), new PieceModel<>(PieceModel.Species.DISC, "bianco"));
        m1.put(new Pos(2,6), new PieceModel<>(PieceModel.Species.DISC, "nero"));
        m1.put(new Pos(7,3), new PieceModel<>(PieceModel.Species.DISC, "bianco"));
        m1.put(new Pos(1,5), new PieceModel<>(PieceModel.Species.DISC, "nero"));
        m1.put(new Pos(2,7), new PieceModel<>(PieceModel.Species.DISC, "bianco"));
        m1.put(new Pos(5,1), new PieceModel<>(PieceModel.Species.DISC, "nero"));
        m1.put(new Pos(7,4), new PieceModel<>(PieceModel.Species.DISC, "bianco"));
        m1.put(new Pos(1,5), new PieceModel<>(PieceModel.Species.DISC, "nero"));
        m1.put(new Pos(0,0), new PieceModel<>(PieceModel.Species.DISC, "bianco"));

        GameRuler.Situation<PieceModel<PieceModel.Species>> s1 = new GameRuler.Situation<>(m1,2);

        Probe.EncS<PieceModel<PieceModel.Species>> save1 = new Probe.EncS<>(g1.mechanics(),s1);
        save1.decode(g1.mechanics());

/*
        BoardOct<PieceModel<PieceModel.Species>> b1 = new BoardOct<>(4,4);
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "nero"), new Pos(1,1));
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "bianco"), new Pos(3,1));
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "bianco"), new Pos(1,2));
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "bianco"), new Pos(2,2));
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "nero"), new Pos(3,2));
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "nero"), new Pos(1,3));
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "nero"), new Pos(2,3));
        b1.put(new PieceModel<>(PieceModel.Species.DISC, "bianco"), new Pos(3,3));

        String codeS = "";
        for(Pos p : b1.positions()) {
            if(b1.get(p) == null) { codeS += 0; }
            else if(b1.get(p).equals(new PieceModel<>(PieceModel.Species.DISC, "nero"))) { codeS += 1; }
            else if(b1.get(p).equals(new PieceModel<>(PieceModel.Species.DISC, "bianco"))) { codeS += 2; }
        }
        BigInteger big = new BigInteger(codeS);
        String temp = String.valueOf(big);
        System.out.println(codeS+" "+big+" "+temp);
        int add = b1.positions().size()-temp.length();
        temp = new String(new char[add]).replace("\0", "0")+temp; System.out.println(temp); //Da eseguire in ricorsivo

        BoardOct<PieceModel<PieceModel.Species>> b2 = new BoardOct<>(4,4);
        int counter = 0;
        for(Pos p : b2.positions()) {
            if(String.valueOf(temp.charAt(counter)).equals("0")) { counter++; }
            else if(String.valueOf(temp.charAt(counter)).equals("1")) { counter++; b2.put(new PieceModel<>(PieceModel.Species.DISC, "nero"), p); }
            else if(String.valueOf(temp.charAt(counter)).equals("2")) { counter++; b2.put(new PieceModel<>(PieceModel.Species.DISC, "bianco"), p); }
        }

        String codeS1 = "";
        for(Pos p : b2.positions()) {
            if(b2.get(p) == null) { codeS1 += 0; }
            else if(b2.get(p).equals(new PieceModel<>(PieceModel.Species.DISC, "nero"))) { codeS1 += 1; }
            else if(b2.get(p).equals(new PieceModel<>(PieceModel.Species.DISC, "bianco"))) { codeS1 += 2; }
        }
        System.out.println(codeS1);
*/

    }

}
