package gapp.ulg.test;

import gapp.ulg.game.board.*;
import gapp.ulg.games.Othello;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by maxmo on 22/05/2016.
 */
public class CheckStampa {

    public static void main(String[] args) {
        /*
        String[] mArr = movs.split("\n");
        String[] bArr = boards.split("\n");
        String[] vmArr = valMovs.split("\n");
        int k = 0;

        while(k < movs.split("\n").length) {
            if(eqBoard(o1.getBoard(), bArr[k])){
                System.out.println("Turn di Othello:"+o1.turn()+" | Turn del grader:"+mArr[k].split("\\s")[0]);
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
                if(k == mArr.length) { break; }
            }
        }
        if(o1.result() == 2) { System.out.println("Punteggio finale ok"); }
        else System.out.println("Punteggio non corrisponde: "+o1.result());
        */
/*
        printer(o1.getBoard());
        o1.move(stringToMove(null, "X3,5X3,4"));
        System.out.println("----------------");
        printer(o1.getBoard());
        System.out.println("----------------");
        o1.unMove();
        printer(o1.getBoard());
*/

    }

    public static String posPrinter(Pos p) {
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

    public static void printer(Board b) {
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
                    "...........................XXX.....OX...........................\n" +
                    ".....................O.....XOX.....OX...........................\n" +
                    "....................XO.....XXX.....OX...........................\n" +
                    "...................OOO.....OXX.....OX...........................\n" +
                    "..........X........XOO.....OXX.....OX...........................\n" +
                    "..........X........XOO.....OXO.....OOO..........................\n" +
                    "..........X........XOO.....OXO.....OOX........X.................\n" +
                    "..........X........XOO.....OXO.....OOO.......OX.................\n" +
                    "..........X........XOO.....OXO.....OXO......XXX.................\n" +
                    "..........X........XOO.....OOO.....OOO......OXX.....O...........\n" +
                    "..........X........XOO.....XOO.....XOO.....XXXX.....O...........\n" +
                    "..........X........XOO.....OOO....OOOO.....OXXX.....O...........\n" +
                    "..........X.X......XXO.....OXO....OOXO.....OXXX.....O...........\n" +
                    "....O.....X.O......XOO.....OOO....OOOO.....OOXX.....O...........\n" +
                    "....O.....X.O......XOO.....XOO....OXOO.....XOXX....XO...........\n" +
                    "....O.....X.O......OOO....OOOO....OOOO.....XOXX....XO...........\n" +
                    "....O.....XXO......XOO....OXOO....OXOO.....XOXX....XO...........\n" +
                    "....O.....XOO.....OOOO....OOOO....OXOO.....XOXX....XO...........\n" +
                    "...XO.....XXO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
                    "..OOO.....OOO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
                    "..OOO.....OOO.....OXOO....OXOO....OXOO.....XXXX....XXX..........\n" +
                    "..OOO.....OOO.....OOOO....OOOO....OOOO.....OXXX....OXX.....O....\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOX.....OXXX....OXX.....O....\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOO.....O.O..\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOX.....O.OX.\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOOO....O.OX.\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OXXX.\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOOX....OXXX....OXXO....OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXO....OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXXX...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOOO...OOXOO...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO...XXXOOO...OOXOO...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO...OOXOO...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO..XXXXOO...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO.OOOOOOO...OOOOO\n";

    private static String valMovs =
                    "X2,4X3,4 X5,3X4,3 X3,5X3,4 X4,2X4,3 RESIGN\n" +
                    "O2,3O3,3 O4,5O4,4 O2,5O3,4 RESIGN\n" +
                    "X2,4X3,4 X5,3X4,3 X4,2X4,3 X1,5X2,5 RESIGN\n" +
                    "O4,5O4,4,3,5 O2,3O3,3,2,4 RESIGN\n" +
                    "X3,2X3,3 X2,2X3,3 X4,2X4,3 X5,2X4,3 X1,4X2,4 X1,5X2,5 X1,6X2,5 X1,2X2,3 X1,3X2,4 RESIGN\n" +
                    "O4,5O4,4,3,5 O5,5O4,4 O5,4O4,4,3,4 O3,6O3,4,3,5 O4,6O3,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
                    "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X5,4X4,4 X5,6X4,5 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 RESIGN\n" +
                    "O5,5O4,5 O4,6O4,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
                    "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 X5,4X5,5,4,4 RESIGN\n" +
                    "O6,3O5,4 O6,7O5,6 O5,3O4,4 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O6,6O4,4,5,5 O6,4O5,4,4,4,3,4 RESIGN O1,3O2,3 O2,2O2,3\n" +
                    "X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X1,5X4,5,3,5,2,5 X5,3X5,4,4,3,3,3 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,2O5,3 O6,3O5,4 O6,7O5,6 O4,2O4,3,3,3,5,3 O5,2O4,3 O0,1O2,3,1,2 O4,6O5,5 O3,2O3,3 O2,2O3,3,2,3 RESIGN\n" +
                    "X3,2X4,3 X1,4X4,4,3,4,2,4 X5,2X5,3 X3,6X4,5 X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X7,4X6,4 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
                    "O6,3O5,4 O6,7O5,6 O6,6O4,4,5,5 O1,3O2,3,2,4 O1,5O2,4 O5,7O5,4,5,5,5,6 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O0,3O1,4 O4,6O5,5 O2,2O2,3,2,4 O0,4O5,4,4,4,3,4,2,4,1,4 RESIGN\n" +
                    "X2,2X4,4,3,3 X0,5X1,4 X7,3X6,4 X2,6X2,4,2,5 X5,2X5,4,5,3 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,7O5,6 O3,2O4,3,3,3,2,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O4,6O5,5 O2,2O3,3,2,3 RESIGN\n" +
                    "X3,1X4,2 X6,5X6,4 X1,3X4,3,3,3,2,3 X3,6X5,4,4,5 X2,2X4,4,3,3 X2,6X4,4,3,5 X7,3X6,4 X7,5X6,4 X1,5X4,5,3,5,2,5 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,7O5,6 O5,7O5,5,5,6 O2,2O3,3,2,3,1,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O0,1O2,3,1,2 O0,2O1,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
                    "X2,1X3,2 X3,6X5,4,4,5 X4,6X4,4,4,5 X2,6X4,4,3,5 X4,1X4,2 X1,5X4,5,1,3,3,5,1,4,2,5 X1,1X4,4,3,3,2,2 X5,2X3,2,2,2,4,2 X0,3X3,3,2,3,1,3 X3,1X4,2 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,7O5,6 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O0,2O1,2,1,3,0,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
                    "X2,6X4,4,2,4,3,5,2,5 X3,1X3,2,4,2 X4,6X4,4,4,5 X3,6X5,4,3,4,4,5,3,5 X1,5X4,5,3,5,2,4,2,5 X1,1X2,2 X2,1X3,2,2,2 X5,1X4,2 X4,1X3,2,4,2 X0,5X1,4 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 X0,1X1,2 RESIGN\n" +
                    "O6,6O5,5 O6,2O5,3 O6,7O5,6 O7,5O6,5,5,5,5,3,6,4 O5,2O4,3 O7,2O5,4,6,3 O7,4O5,4,6,4 O7,6O4,3,5,4,6,5 RESIGN O7,3O4,3,3,3,2,3,6,3,5,3\n" +
                    "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X3,6X4,5 X6,2X6,3 X3,1X5,3,4,2 X7,2X6,3 X1,1X4,4,3,3,2,2 X1,5X4,5,3,5,2,5 RESIGN\n" +
                    "O5,7O5,4,5,5,5,6 O6,6O6,5,5,5,6,4 O2,7O5,4,4,5,3,6 O6,7O4,5,5,6 O3,7O3,6 O7,4O5,4,6,4 O7,5O6,5,5,5,4,5,6,4 O7,6O5,4,6,5 O4,6O5,5,4,5,6,4 O4,7O3,6 RESIGN\n" +
                    "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,6X6,5 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN X7,4X6,5,6,4\n" +
                    "O6,7O5,6 O6,6O6,5 O7,7O7,6 O3,7O3,6 O5,7O5,6 O4,7O3,6 RESIGN O2,7O3,6\n" +
                    "X7,4X6,5,7,5,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN\n" +
                    "O6,7O5,6 O3,7O3,6 O4,6O5,6 O5,7O5,6 O7,7O7,6,7,4,7,5 O4,7O3,6 RESIGN O2,7O3,6\n" +
                    "X6,7X6,6 X3,1X3,2,3,3,3,4,3,5,5,3,4,2 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X6,2X6,3 X4,6X5,5 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
                    "O2,6O5,6,4,6,3,6 O5,7O5,4,5,5,5,6,4,6 O6,7O5,6 O3,7O5,5,4,6,3,6,6,4 O4,7O6,5,5,6,4,6,3,6 RESIGN O2,7O3,6\n" +
                    "X6,7X6,6 X6,2X6,3,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X2,6X4,6,3,6 X2,7X4,5,3,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
                    "O5,7O6,6,5,6,6,7 O4,7O6,5,5,6 RESIGN\n" +
                    "X6,2X6,3,6,4 X2,1X4,3,3,2 X2,7X4,5,3,6 X4,7X5,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
                    "O4,1O5,2 O5,1O5,4,5,2,5,3 O6,1O5,2 O6,2O5,2,5,3 RESIGN\n" +
                    "X6,2X6,3,6,4 X4,7X5,6 X1,5X5,5,4,5,3,5,2,5 X2,1X5,4,4,3,3,2 RESIGN\n" +
                    "O7,2O6,2,6,3 O7,1O6,2 O6,1O6,5,6,2,6,3,6,4 RESIGN\n";

    private static String movs =
                    "1 X3,5X3,4\n" +
                    "2 O2,5O3,4\n" +
                    "1 X2,4X3,4\n" +
                    "2 O2,3O3,3,2,4\n" +
                    "1 X1,2X2,3\n" +
                    "2 O4,5O4,4,3,5\n" +
                    "1 X5,6X4,5\n" +
                    "2 O5,5O4,5\n" +
                    "1 X5,4X5,5,4,4\n" +
                    "2 O6,4O5,4,4,4,3,4\n" +
                    "1 X5,3X5,4,4,3,3,3\n" +
                    "2 O4,2O4,3,3,3,5,3\n" +
                    "1 X1,4X4,4,3,4,2,4\n" +
                    "2 O0,4O5,4,4,4,3,4,2,4,1,4\n" +
                    "1 X6,3X4,3,3,3,5,3\n" +
                    "2 O3,2O4,3,3,3,2,3\n" +
                    "1 X1,3X4,3,3,3,2,3\n" +
                    "2 O2,2O3,3,2,3,1,3\n" +
                    "1 X0,3X3,3,2,3,1,3\n" +
                    "2 O0,2O1,2,1,3,0,3\n" +
                    "1 X6,5X5,4,6,4\n" +
                    "2 O7,3O4,3,3,3,2,3,6,3,5,3\n" +
                    "1 X3,6X4,5\n" +
                    "2 O7,5O6,5,5,5,4,5,6,4\n" +
                    "1 X7,6X6,5\n" +
                    "2 O6,6O6,5\n" +
                    "1 X7,4X6,5,7,5,6,4\n" +
                    "2 O7,7O7,6,7,4,7,5\n" +
                    "1 X4,6X5,5\n" +
                    "2 O3,7O5,5,4,6,3,6,6,4\n" +
                    "1 X6,7X6,6\n" +
                    "2 O5,7O6,6,5,6,6,7\n" +
                    "1 X5,2X5,3\n" +
                    "2 O5,1O5,4,5,2,5,3\n" +
                    "1 X6,2X6,3,6,4\n" +
                    "2 O6,1O6,5,6,2,6,3,6,4\n";
}

/* Game 1
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
*/

/* Game 2
    private static String boards =
                    "...........................XO......OX...........................\n" +
                    "...........................XXX.....OX...........................\n" +
                    ".....................O.....XOX.....OX...........................\n" +
                    "....................XO.....XXX.....OX...........................\n" +
                    "...................OOO.....OXX.....OX...........................\n" +
                    "..........X........XOO.....OXX.....OX...........................\n" +
                    "..........X........XOO.....OXO.....OOO..........................\n" +
                    "..........X........XOO.....OXO.....OOX........X.................\n" +
                    "..........X........XOO.....OXO.....OOO.......OX.................\n" +
                    "..........X........XOO.....OXO.....OXO......XXX.................\n" +
                    "..........X........XOO.....OOO.....OOO......OXX.....O...........\n" +
                    "..........X........XOO.....XOO.....XOO.....XXXX.....O...........\n" +
                    "..........X........XOO.....OOO....OOOO.....OXXX.....O...........\n" +
                    "..........X.X......XXO.....OXO....OOXO.....OXXX.....O...........\n" +
                    "....O.....X.O......XOO.....OOO....OOOO.....OOXX.....O...........\n" +
                    "....O.....X.O......XOO.....XOO....OXOO.....XOXX....XO...........\n" +
                    "....O.....X.O......OOO....OOOO....OOOO.....XOXX....XO...........\n" +
                    "....O.....XXO......XOO....OXOO....OXOO.....XOXX....XO...........\n" +
                    "....O.....XOO.....OOOO....OOOO....OXOO.....XOXX....XO...........\n" +
                    "...XO.....XXO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
                    "..OOO.....OOO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
                    "..OOO.....OOO.....OXOO....OXOO....OXOO.....XXXX....XXX..........\n" +
                    "..OOO.....OOO.....OOOO....OOOO....OOOO.....OXXX....OXX.....O....\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOX.....OXXX....OXX.....O....\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOO.....O.O..\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOX.....O.OX.\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOOO....O.OX.\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OXXX.\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOX...OOOOX....OXXX....OXXO....OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXO....OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXXX...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOOO...OOXOO...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO...XXXOOO...OOXOO...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO...OOXOO...OOOOO\n" +
                    "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO..XXXXOO...OOOOO\n";

    private static String valMovs =
                    "X2,4X3,4 X5,3X4,3 X3,5X3,4 X4,2X4,3 RESIGN\n" +
                    "O2,3O3,3 O4,5O4,4 O2,5O3,4 RESIGN\n" +
                    "X2,4X3,4 X5,3X4,3 X4,2X4,3 X1,5X2,5 RESIGN\n" +
                    "O4,5O4,4,3,5 O2,3O3,3,2,4 RESIGN\n" +
                    "X3,2X3,3 X2,2X3,3 X4,2X4,3 X5,2X4,3 X1,4X2,4 X1,5X2,5 X1,6X2,5 X1,2X2,3 X1,3X2,4 RESIGN\n" +
                    "O4,5O4,4,3,5 O5,5O4,4 O5,4O4,4,3,4 O3,6O3,4,3,5 O4,6O3,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
                    "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X5,4X4,4 X5,6X4,5 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 RESIGN\n" +
                    "O5,5O4,5 O4,6O4,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
                    "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 X5,4X5,5,4,4 RESIGN\n" +
                    "O6,3O5,4 O6,7O5,6 O5,3O4,4 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O6,6O4,4,5,5 O6,4O5,4,4,4,3,4 RESIGN O1,3O2,3 O2,2O2,3\n" +
                    "X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X1,5X4,5,3,5,2,5 X5,3X5,4,4,3,3,3 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,2O5,3 O6,3O5,4 O6,7O5,6 O4,2O4,3,3,3,5,3 O5,2O4,3 O0,1O2,3,1,2 O4,6O5,5 O3,2O3,3 O2,2O3,3,2,3 RESIGN\n" +
                    "X3,2X4,3 X1,4X4,4,3,4,2,4 X5,2X5,3 X3,6X4,5 X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X7,4X6,4 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
                    "O6,3O5,4 O6,7O5,6 O6,6O4,4,5,5 O1,3O2,3,2,4 O1,5O2,4 O5,7O5,4,5,5,5,6 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O0,3O1,4 O4,6O5,5 O2,2O2,3,2,4 O0,4O5,4,4,4,3,4,2,4,1,4 RESIGN\n" +
                    "X2,2X4,4,3,3 X0,5X1,4 X7,3X6,4 X2,6X2,4,2,5 X5,2X5,4,5,3 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,7O5,6 O3,2O4,3,3,3,2,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O4,6O5,5 O2,2O3,3,2,3 RESIGN\n" +
                    "X3,1X4,2 X6,5X6,4 X1,3X4,3,3,3,2,3 X3,6X5,4,4,5 X2,2X4,4,3,3 X2,6X4,4,3,5 X7,3X6,4 X7,5X6,4 X1,5X4,5,3,5,2,5 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,7O5,6 O5,7O5,5,5,6 O2,2O3,3,2,3,1,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O0,1O2,3,1,2 O0,2O1,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
                    "X2,1X3,2 X3,6X5,4,4,5 X4,6X4,4,4,5 X2,6X4,4,3,5 X4,1X4,2 X1,5X4,5,1,3,3,5,1,4,2,5 X1,1X4,4,3,3,2,2 X5,2X3,2,2,2,4,2 X0,3X3,3,2,3,1,3 X3,1X4,2 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 RESIGN\n" +
                    "O6,5O5,5 O6,6O5,5 O6,7O5,6 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O0,2O1,2,1,3,0,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
                    "X2,6X4,4,2,4,3,5,2,5 X3,1X3,2,4,2 X4,6X4,4,4,5 X3,6X5,4,3,4,4,5,3,5 X1,5X4,5,3,5,2,4,2,5 X1,1X2,2 X2,1X3,2,2,2 X5,1X4,2 X4,1X3,2,4,2 X0,5X1,4 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 X0,1X1,2 RESIGN\n" +
                    "O6,6O5,5 O6,2O5,3 O6,7O5,6 O7,5O6,5,5,5,5,3,6,4 O5,2O4,3 O7,2O5,4,6,3 O7,4O5,4,6,4 O7,6O4,3,5,4,6,5 RESIGN O7,3O4,3,3,3,2,3,6,3,5,3\n" +
                    "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X3,6X4,5 X6,2X6,3 X3,1X5,3,4,2 X7,2X6,3 X1,1X4,4,3,3,2,2 X1,5X4,5,3,5,2,5 RESIGN\n" +
                    "O5,7O5,4,5,5,5,6 O6,6O6,5,5,5,6,4 O2,7O5,4,4,5,3,6 O6,7O4,5,5,6 O3,7O3,6 O7,4O5,4,6,4 O7,5O6,5,5,5,4,5,6,4 O7,6O5,4,6,5 O4,6O5,5,4,5,6,4 O4,7O3,6 RESIGN\n" +
                    "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,6X6,5 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN X7,4X6,5,6,4\n" +
                    "O6,7O5,6 O6,6O6,5 O7,7O7,6 O3,7O3,6 O5,7O5,6 O4,7O3,6 RESIGN O2,7O3,6\n" +
                    "X7,4X6,5,7,5,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN\n" +
                    "O6,7O5,6 O3,7O3,6 O4,6O5,6 O5,7O5,6 O7,7O7,6,7,4,7,5 O4,7O3,6 RESIGN O2,7O3,6\n" +
                    "X6,7X6,6 X3,1X3,2,3,3,3,4,3,5,5,3,4,2 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X6,2X6,3 X4,6X5,5 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
                    "O2,6O5,6,4,6,3,6 O5,7O5,4,5,5,5,6,4,6 O6,7O5,6 O3,7O5,5,4,6,3,6,6,4 O4,7O6,5,5,6,4,6,3,6 RESIGN O2,7O3,6\n" +
                    "X6,7X6,6 X6,2X6,3,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X2,6X4,6,3,6 X2,7X4,5,3,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
                    "O5,7O6,6,5,6,6,7 O4,7O6,5,5,6 RESIGN\n" +
                    "X6,2X6,3,6,4 X2,1X4,3,3,2 X2,7X4,5,3,6 X4,7X5,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
                    "O4,1O5,2 O5,1O5,4,5,2,5,3 O6,1O5,2 O6,2O5,2,5,3 RESIGN\n" +
                    "X6,2X6,3,6,4 X4,7X5,6 X1,5X5,5,4,5,3,5,2,5 X2,1X5,4,4,3,3,2 RESIGN\n" +
                    "O7,2O6,2,6,3 O7,1O6,2 O6,1O6,5,6,2,6,3,6,4 RESIGN\n";

    private static String movs =
                    "1 X3,5X3,4\n" +
                    "2 O2,5O3,4\n" +
                    "1 X2,4X3,4\n" +
                    "2 O2,3O3,3,2,4\n" +
                    "1 X1,2X2,3\n" +
                    "2 O4,5O4,4,3,5\n" +
                    "1 X5,6X4,5\n" +
                    "2 O5,5O4,5\n" +
                    "1 X5,4X5,5,4,4\n" +
                    "2 O6,4O5,4,4,4,3,4\n" +
                    "1 X5,3X5,4,4,3,3,3\n" +
                    "2 O4,2O4,3,3,3,5,3\n" +
                    "1 X1,4X4,4,3,4,2,4\n" +
                    "2 O0,4O5,4,4,4,3,4,2,4,1,4\n" +
                    "1 X6,3X4,3,3,3,5,3\n" +
                    "2 O3,2O4,3,3,3,2,3\n" +
                    "1 X1,3X4,3,3,3,2,3\n" +
                    "2 O2,2O3,3,2,3,1,3\n" +
                    "1 X0,3X3,3,2,3,1,3\n" +
                    "2 O0,2O1,2,1,3,0,3\n" +
                    "1 X6,5X5,4,6,4\n" +
                    "2 O7,3O4,3,3,3,2,3,6,3,5,3\n" +
                    "1 X3,6X4,5\n" +
                    "2 O7,5O6,5,5,5,4,5,6,4\n" +
                    "1 X7,6X6,5\n" +
                    "2 O6,6O6,5\n" +
                    "1 X7,4X6,5,7,5,6,4\n" +
                    "2 O7,7O7,6,7,4,7,5\n" +
                    "1 X4,6X5,5\n" +
                    "2 O3,7O5,5,4,6,3,6,6,4\n" +
                    "1 X6,7X6,6\n" +
                    "2 O5,7O6,6,5,6,6,7\n" +
                    "1 X5,2X5,3\n" +
                    "2 O5,1O5,4,5,2,5,3\n" +
                    "1 X6,2X6,3,6,4\n" +
                    "2 O6,1O6,5,6,2,6,3,6,4\n" +
                    "2";
*/

/*
            "...........................XO......OX...........................\n" +
            "X2,4X3,4 X5,3X4,3 X3,5X3,4 X4,2X4,3 RESIGN\n" +
            "1 X3,5X3,4\n" +
            "...........................XXX.....OX...........................\n" +
            "O2,3O3,3 O4,5O4,4 O2,5O3,4 RESIGN\n" +
            "2 O2,5O3,4\n" +
            ".....................O.....XOX.....OX...........................\n" +
            "X2,4X3,4 X5,3X4,3 X4,2X4,3 X1,5X2,5 RESIGN\n" +
            "1 X2,4X3,4\n" +
            "....................XO.....XXX.....OX...........................\n" +
            "O4,5O4,4,3,5 O2,3O3,3,2,4 RESIGN\n" +
            "2 O2,3O3,3,2,4\n" +
            "...................OOO.....OXX.....OX...........................\n" +
            "X3,2X3,3 X2,2X3,3 X4,2X4,3 X5,2X4,3 X1,4X2,4 X1,5X2,5 X1,6X2,5 X1,2X2,3 X1,3X2,4 RESIGN\n" +
            "1 X1,2X2,3\n" +
            "..........X........XOO.....OXX.....OX...........................\n" +
            "O4,5O4,4,3,5 O5,5O4,4 O5,4O4,4,3,4 O3,6O3,4,3,5 O4,6O3,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
            "2 O4,5O4,4,3,5\n" +
            "..........X........XOO.....OXO.....OOO..........................\n" +
            "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X5,4X4,4 X5,6X4,5 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 RESIGN\n" +
            "1 X5,6X4,5\n" +
            "..........X........XOO.....OXO.....OOX........X.................\n" +
            "O5,5O4,5 O4,6O4,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
            "2 O5,5O4,5\n" +
            "..........X........XOO.....OXO.....OOO.......OX.................\n" +
            "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 X5,4X5,5,4,4 RESIGN\n" +
            "1 X5,4X5,5,4,4\n" +
            "..........X........XOO.....OXO.....OXO......XXX.................\n" +
            "O6,3O5,4 O6,7O5,6 O5,3O4,4 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O6,6O4,4,5,5 O6,4O5,4,4,4,3,4 RESIGN O1,3O2,3 O2,2O2,3\n" +
            "2 O6,4O5,4,4,4,3,4\n" +
            "..........X........XOO.....OOO.....OOO......OXX.....O...........\n" +
            "X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X1,5X4,5,3,5,2,5 X5,3X5,4,4,3,3,3 RESIGN\n" +
            "1 X5,3X5,4,4,3,3,3\n" +
            "..........X........XOO.....XOO.....XOO.....XXXX.....O...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,2O5,3 O6,3O5,4 O6,7O5,6 O4,2O4,3,3,3,5,3 O5,2O4,3 O0,1O2,3,1,2 O4,6O5,5 O3,2O3,3 O2,2O3,3,2,3 RESIGN\n" +
            "2 O4,2O4,3,3,3,5,3\n" +
            "..........X........XOO.....OOO....OOOO.....OXXX.....O...........\n" +
            "X3,2X4,3 X1,4X4,4,3,4,2,4 X5,2X5,3 X3,6X4,5 X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X7,4X6,4 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
            "1 X1,4X4,4,3,4,2,4\n" +
            "..........X.X......XXO.....OXO....OOXO.....OXXX.....O...........\n" +
            "O6,3O5,4 O6,7O5,6 O6,6O4,4,5,5 O1,3O2,3,2,4 O1,5O2,4 O5,7O5,4,5,5,5,6 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O0,3O1,4 O4,6O5,5 O2,2O2,3,2,4 O0,4O5,4,4,4,3,4,2,4,1,4 RESIGN\n" +
            "2 O0,4O5,4,4,4,3,4,2,4,1,4\n" +
            "....O.....X.O......XOO.....OOO....OOOO.....OOXX.....O...........\n" +
            "X2,2X4,4,3,3 X0,5X1,4 X7,3X6,4 X2,6X2,4,2,5 X5,2X5,4,5,3 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
            "1 X6,3X4,3,3,3,5,3\n" +
            "....O.....X.O......XOO.....XOO....OXOO.....XOXX....XO...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,7O5,6 O3,2O4,3,3,3,2,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O4,6O5,5 O2,2O3,3,2,3 RESIGN\n" +
            "2 O3,2O4,3,3,3,2,3\n" +
            "....O.....X.O......OOO....OOOO....OOOO.....XOXX....XO...........\n" +
            "X3,1X4,2 X6,5X6,4 X1,3X4,3,3,3,2,3 X3,6X5,4,4,5 X2,2X4,4,3,3 X2,6X4,4,3,5 X7,3X6,4 X7,5X6,4 X1,5X4,5,3,5,2,5 RESIGN\n" +
            "1 X1,3X4,3,3,3,2,3\n" +
            "....O.....XXO......XOO....OXOO....OXOO.....XOXX....XO...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,7O5,6 O5,7O5,5,5,6 O2,2O3,3,2,3,1,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O0,1O2,3,1,2 O0,2O1,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
            "2 O2,2O3,3,2,3,1,3\n" +
            "....O.....XOO.....OOOO....OOOO....OXOO.....XOXX....XO...........\n" +
            "X2,1X3,2 X3,6X5,4,4,5 X4,6X4,4,4,5 X2,6X4,4,3,5 X4,1X4,2 X1,5X4,5,1,3,3,5,1,4,2,5 X1,1X4,4,3,3,2,2 X5,2X3,2,2,2,4,2 X0,3X3,3,2,3,1,3 X3,1X4,2 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 RESIGN\n" +
            "1 X0,3X3,3,2,3,1,3\n" +
            "...XO.....XXO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,7O5,6 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O0,2O1,2,1,3,0,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
            "2 O0,2O1,2,1,3,0,3\n" +
            "..OOO.....OOO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
            "X2,6X4,4,2,4,3,5,2,5 X3,1X3,2,4,2 X4,6X4,4,4,5 X3,6X5,4,3,4,4,5,3,5 X1,5X4,5,3,5,2,4,2,5 X1,1X2,2 X2,1X3,2,2,2 X5,1X4,2 X4,1X3,2,4,2 X0,5X1,4 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 X0,1X1,2 RESIGN\n" +
            "1 X6,5X5,4,6,4\n" +
            "..OOO.....OOO.....OXOO....OXOO....OXOO.....XXXX....XXX..........\n" +
            "O6,6O5,5 O6,2O5,3 O6,7O5,6 O7,5O6,5,5,5,5,3,6,4 O5,2O4,3 O7,2O5,4,6,3 O7,4O5,4,6,4 O7,6O4,3,5,4,6,5 RESIGN O7,3O4,3,3,3,2,3,6,3,5,3\n" +
            "2 O7,3O4,3,3,3,2,3,6,3,5,3\n" +
            "..OOO.....OOO.....OOOO....OOOO....OOOO.....OXXX....OXX.....O....\n" +
            "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X3,6X4,5 X6,2X6,3 X3,1X5,3,4,2 X7,2X6,3 X1,1X4,4,3,3,2,2 X1,5X4,5,3,5,2,5 RESIGN\n" +
            "1 X3,6X4,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOX.....OXXX....OXX.....O....\n" +
            "O5,7O5,4,5,5,5,6 O6,6O6,5,5,5,6,4 O2,7O5,4,4,5,3,6 O6,7O4,5,5,6 O3,7O3,6 O7,4O5,4,6,4 O7,5O6,5,5,5,4,5,6,4 O7,6O5,4,6,5 O4,6O5,5,4,5,6,4 O4,7O3,6 RESIGN\n" +
            "2 O7,5O6,5,5,5,4,5,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOO.....O.O..\n" +
            "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,6X6,5 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN X7,4X6,5,6,4\n" +
            "1 X7,6X6,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOX.....O.OX.\n" +
            "O6,7O5,6 O6,6O6,5 O7,7O7,6 O3,7O3,6 O5,7O5,6 O4,7O3,6 RESIGN O2,7O3,6\n" +
            "2 O6,6O6,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOOO....O.OX.\n" +
            "X7,4X6,5,7,5,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN\n" +
            "1 X7,4X6,5,7,5,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OXXX.\n" +
            "O6,7O5,6 O3,7O3,6 O4,6O5,6 O5,7O5,6 O7,7O7,6,7,4,7,5 O4,7O3,6 RESIGN O2,7O3,6\n" +
            "2 O7,7O7,6,7,4,7,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OOOOO\n" +
            "X6,7X6,6 X3,1X3,2,3,3,3,4,3,5,5,3,4,2 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X6,2X6,3 X4,6X5,5 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
            "1 X4,6X5,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOOX....OXXX....OXXO....OOOOO\n" +
            "O2,6O5,6,4,6,3,6 O5,7O5,4,5,5,5,6,4,6 O6,7O5,6 O3,7O5,5,4,6,3,6,6,4 O4,7O6,5,5,6,4,6,3,6 RESIGN O2,7O3,6\n" +
            "2 O3,7O5,5,4,6,3,6,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXO....OOOOO\n" +
            "X6,7X6,6 X6,2X6,3,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X2,6X4,6,3,6 X2,7X4,5,3,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
            "1 X6,7X6,6\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXXX...OOOOO\n" +
            "O5,7O6,6,5,6,6,7 O4,7O6,5,5,6 RESIGN\n" +
            "2 O5,7O6,6,5,6,6,7\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOOO...OOXOO...OOOOO\n" +
            "X6,2X6,3,6,4 X2,1X4,3,3,2 X2,7X4,5,3,6 X4,7X5,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
            "1 X5,2X5,3\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO...XXXOOO...OOXOO...OOOOO\n" +
            "O4,1O5,2 O5,1O5,4,5,2,5,3 O6,1O5,2 O6,2O5,2,5,3 RESIGN\n" +
            "2 O5,1O5,4,5,2,5,3\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO...OOXOO...OOOOO\n" +
            "X6,2X6,3,6,4 X4,7X5,6 X1,5X5,5,4,5,3,5,2,5 X2,1X5,4,4,3,3,2 RESIGN\n" +
            "1 X6,2X6,3,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO..XXXXOO...OOOOO\n" +
            "O7,2O6,2,6,3 O7,1O6,2 O6,1O6,5,6,2,6,3,6,4 RESIGN\n" +
            "2 O6,1O6,5,6,2,6,3,6,4\n" +
            "2";
 */