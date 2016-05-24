package gapp.ulg.test;

import gapp.ulg.game.board.*;
import gapp.ulg.games.Othello;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by maxmo on 22/05/2016.
 */
public class CheckStampa {

    public static void main(String[] args) {
        Othello o1 = new Othello("Marco", "Alice");
        String[] mArr = movs.split("\n");
        String[] bArr = boards.split("\n");
        String[] vmArr = valMovs.split("\n");
        int k = 0;

        while(k < 10) {
            if(eqBoard(o1.getBoard(), bArr[k])){
                System.out.println("Turn di Othello:"+o1.turn()+" Turn del grader:"+mArr[k].split("\\s")[0]);
                valMprinter(o1.validMoves());
                System.out.println(vmArr[k]);
                if(checkValidMoves(o1.validMoves(),vmArr[k])) { System.out.println("Valid Moves della mossa "+(k+1)+" OK"); }
                System.out.println("Eseguo "+mArr[k].split("\\s")[1]);
                printer(bArr[k+1]);
                o1.move(stringToMove(null, mArr[k].split("\\s")[1]));
                printer(o1.getBoard());
                k++;
                if(eqBoard(o1.getBoard(), bArr[k])) { System.out.println("Mossa "+k+" eseguita"+
                "\n ---------------------------"); } else { System.out.println("Mossa "+k+" NON eseguita"); break; }
            }
        }

    }

    private static String posPrinter(Pos p) {
        return String.valueOf(p.getB())+","+String.valueOf(p.getT());
    }

    private static void valMprinter(Set<Move<PieceModel<PieceModel.Species>>> valM) {
        for(Move<PieceModel<PieceModel.Species>> m : valM) {
            if(m.getKind() == Move.Kind.RESIGN) { System.out.println("RESIGN"); continue; }
            String moveLine = "";
            List<Action<PieceModel<PieceModel.Species>>> aList = m.getActions();
            for(Action a : aList) {
                if(a.getKind() == Action.Kind.ADD) {
                    Pos p = (Pos) a.getPos().get(0);
                    moveLine += "Add:"+String.valueOf(p.getB())+","+String.valueOf(p.getT())+"; ";
                }
                if(a.getKind() == Action.Kind.SWAP) {
                    moveLine += "Swapp:";
                    for(Object p : a.getPos()) {
                        Pos p1 = (Pos) p;
                        moveLine += posPrinter(p1)+" ";
                    }
                }

            }
            System.out.println(moveLine);
        }
    }

    private static void printer(String b) {
        int i = 0;
        while(i != 64) {
            System.out.println(b.substring(0, 8));
            b = b.substring(8);
            i += 8;
        }
        System.out.println();
    }

    private static void printer(Board b) {
        for(int i = 0; i < 8; i++) {
            String stamp = "";
            for(int h = 0; h < 8; h++) {
                if(b.get(new Pos(i, h)) == null) { stamp += "."; }
                else if(b.get(new Pos(i, h)).equals(new PieceModel<>(PieceModel.Species.DISC, "nero"))) { stamp += "X"; }
                else if(b.get(new Pos(i, h)).equals(new PieceModel<>(PieceModel.Species.DISC, "bianco"))) { stamp += "O"; }
            }
            System.out.println(stamp);
        }
        System.out.println();
    }

    private static <P> boolean eqBoard(Board<P> board, String sb) {
        PieceModel<PieceModel.Species> pN = new PieceModel<>(PieceModel.Species.DISC, "nero"),
                pB = new PieceModel<>(PieceModel.Species.DISC, "bianco");
        int k = 0;
        for (int b = 0 ; b < board.width() ; b++)
            for (int t = 0 ; t < board.height() ; t++) {
                char c = sb.charAt(k++);
                P pm = board.get(new Pos(b, t));
                switch (c) {
                    case '.':
                        if (pm != null) return false;
                        break;
                    case 'X':
                        if (!pN.equals(pm)) return false;
                        break;
                    case 'O':
                        if (!pB.equals(pm)) return false;
                        break;
                }
            }
        return true;
    }

    private static boolean checkValidMoves(Set<Move<PieceModel<PieceModel.Species>>> vm, String svm) {
        String[] mm = svm.split("\\s+");
        if (vm == null || vm.size() != mm.length) return false;
        for (String sm : mm) {
            Move<PieceModel<PieceModel.Species>> m = stringToMove(null, sm);
            boolean found = false;
            for (Move<PieceModel<PieceModel.Species>> cm : vm) {
                found = Objects.equals(m, cm);
                if (found) break;
            }
            if (!found) return false;
        }
        return true;
    }

    private static Move<PieceModel<PieceModel.Species>> stringToMove(Set<Move<PieceModel<PieceModel.Species>>> vms, String s) {
        if (s.equals("RESIGN")) return new Move<>(Move.Kind.RESIGN);
        String c = ""+s.charAt(0);
        s = s.replaceFirst(c, "").replace(c, ",");
        List<Pos> cc = new ArrayList<>();
        Integer b = null;
        for (String x : s.split(",")) {
            if (b == null) b = Integer.parseInt(x);
            else {
                cc.add(new Pos(b, Integer.parseInt(x)));
                b = null;
            }
        }
        PieceModel<PieceModel.Species> pm = new PieceModel<>(PieceModel.Species.DISC, (c.equals("X") ? "nero" : "bianco"));
        Action<PieceModel<PieceModel.Species>> add = new Action<>(cc.get(0), pm);
        cc.remove(0);
        Move<PieceModel<PieceModel.Species>> m;
        if (cc.size() > 0) {
            Action<PieceModel<PieceModel.Species>> swap = new Action<>(pm, cc.toArray(new Pos[cc.size()]));
            m = new Move<>(add, swap);
        } else m = new Move<>(add);
        if (vms == null) return m;
        for (Move<PieceModel<PieceModel.Species>> vm : vms)
            if (Objects.equals(vm, m)) {
                m = vm;
                break;
            }
        return m;
    }

    private static String boards =
            "...........................XO......OX...........................\n" +
            "....................X......XX......OX...........................\n" +
            "....................XO.....XO......OX...........................\n" +
            "....................XO.....XO......XX......X....................\n" +
            "............O.......OO.....XO......XX......X....................\n" +
            "....X.......X.......XO.....XX......XX......X....................\n" +
            "....X.......X......OOO.....XX......XX......X....................\n" +
            "....X......XX......XOO.....XX......XX......X....................\n" +
            "....X......XX.....OOOO.....XX......XX......X....................\n" +
            "....X.....XXX.....OXOO.....XX......XX......X....................\n";

    private static String valMovs =
            "X2,4X3,4 X5,3X4,3 X3,5X3,4 X4,2X4,3 RESIGN\n" +
            "O2,3O3,3 O4,5O4,4 O2,5O3,4 RESIGN\n" +
            "X5,3X4,3 X3,5X3,4 X4,2X4,3 X2,6X2,5 RESIGN\n" +
            "O5,2O4,3 O5,4O4,4 O3,2O3,3 O1,4O2,4 RESIGN O2,3O2,4\n" +
            "X3,5X3,4 X1,6X3,4,2,5 X1,5X2,4 X0,4X3,4,2,4,1,4 RESIGN\n" +
            "O0,3O1,4 O5,2O4,3,3,4 RESIGN O2,3O2,4\n" +
            "X3,2X2,3 X3,6X2,5 X1,3X2,3 X1,5X2,4 X1,6X2,5 X1,2X2,3 RESIGN\n" +
            "O4,2O3,3 O5,4O4,4,3,4 O0,2O1,3 O0,3O1,4 O5,2O4,3,3,4 RESIGN O2,2O2,3\n" +
            "X3,5X2,4 X3,2X2,3 X3,6X2,5 X1,1X2,2 X3,1X2,2 X1,5X2,4 X1,6X2,5 X1,2X2,3 RESIGN\n" +
            "O4,2O3,3 O5,5O3,3,4,4 O5,4O4,4,3,4 O0,3O1,4 O0,2O1,2,1,3 O5,2O4,3,3,4 RESIGN\n";

    private static String movs =
            "1 X2,4X3,4\n" +
            "2 O2,5O3,4\n" +
            "1 X5,3X4,3\n" +
            "2 O1,4O2,4\n" +
            "1 X0,4X3,4,2,4,1,4\n" +
            "2 O2,3O2,4\n" +
            "1 X1,3X2,3\n" +
            "2 O2,2O2,3\n" +
            "1 X1,2X2,3\n" +
            "2 RESIGN\n";
}