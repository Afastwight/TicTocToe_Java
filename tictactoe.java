package TicTocToe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class tictactoe{
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel();
            TicTacToeView view = new TicTacToeView();
            new GameController(model, view).start();
            view.setVisible(true);
        });
    }

    // =============== SONIDO (simple, sin archivos) ==================
    static class SoundPlayer {
        private static boolean enabled = true;
        static void setEnabled(boolean v){ enabled = v; }
        static void move(){ tone(880, 60); }           // beep corto
        static void win(){ tone(660, 120); tone(880,120); tone(990,180); }
        static void draw(){ tone(440, 200); }
        static void tone(int hz, int msec){
            if (!enabled) return;
            new Thread(() -> playTone(hz, msec)).start();
        }
        private static void playTone(int hz, int msec){
            try {
                float sr = 44100;
                byte[] buf = new byte[(int)(msec * sr / 1000)];
                for (int i=0;i<buf.length;i++){
                    double ang = i / (sr / hz) * 2.0 * Math.PI;
                    buf[i] = (byte)(Math.sin(ang) * 127.0);
                }
                javax.sound.sampled.AudioFormat af = new javax.sound.sampled.AudioFormat(sr, 8, 1, true, false);
                javax.sound.sampled.SourceDataLine line = javax.sound.sampled.AudioSystem.getSourceDataLine(af);
                line.open(af);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.stop();
                line.close();
            } catch (Exception ignored) {}
        }
    }

    // =============== THEME ==================
    static class Theme {
        final String name; final Color bg; final Color cellBg; final Color winBg; final Color xColor; final Color oColor; final Color panelBg; final Color text;
        Theme(String name, Color bg, Color panelBg, Color cellBg, Color winBg, Color xColor, Color oColor, Color text){
            this.name=name; this.bg=bg; this.panelBg=panelBg; this.cellBg=cellBg; this.winBg=winBg; this.xColor=xColor; this.oColor=oColor; this.text=text;
        }
        @Override public String toString(){ return name; }
    }
    static final Theme THEME_CLARO = new Theme(
        "Claro",
        new Color(245,245,245), new Color(250,250,250), new Color(255,255,255), new Color(255,240,170),
        new Color(30, 30, 30), new Color(15, 102, 230), new Color(20,20,20)
    );
    static final Theme THEME_OSCURO = new Theme(
        "Oscuro",
        new Color(30,31,34), new Color(36,37,41), new Color(46,48,54), new Color(78, 59, 17),
        new Color(220,220,220), new Color(89, 177, 250), new Color(230,230,230)
    );
    static final Theme THEME_OCEANO = new Theme(
        "Océano",
        new Color(6, 33, 56), new Color(18, 51, 78), new Color(24, 69, 105), new Color(255, 206, 84),
        new Color(255,255,255), new Color(161, 238, 255), new Color(240,240,240)
    );

    // =============== MODEL ==================
    enum Cell { EMPTY, X, O; }

    static class GameModel {
        private Cell[][] board = new Cell[3][3];
        private Cell current = Cell.X; // jugador en turno
        private boolean vsCPU = true;  // modo por defecto: contra CPU
        private Cell cpuSide = Cell.O; // de qué lado juega la CPU
        private boolean gameOver = false;
        private int xWins = 0, oWins = 0, draws = 0;
        private int[] winLine = null; // línea ganadora para resaltar

        private static final int[][] LINES = new int[][]{
            {0,1,2}, {3,4,5}, {6,7,8}, // filas
            {0,3,6}, {1,4,7}, {2,5,8}, // columnas
            {0,4,8}, {2,4,6}           // diagonales
        };

        GameModel(){ resetBoard(); }

        void resetBoard(){
            for (int r=0;r<3;r++) for (int c=0;c<3;c++) board[r][c] = Cell.EMPTY;
            current = Cell.X;
            gameOver = false;
            winLine = null;
        }

        // Getters/Setters
        boolean isGameOver(){ return gameOver; }
        boolean isVsCPU(){ return vsCPU; }
        void setVsCPU(boolean v){ this.vsCPU = v; }
        Cell getCurrent(){ return current; }
        void setCurrent(Cell c){ this.current = c; }
        Cell getCpuSide(){ return cpuSide; }
        void setCpuSide(Cell c){ this.cpuSide = c; }
        Cell getCell(int r, int c){ return board[r][c]; }
        int getXWins(){ return xWins; }
        int getOWins(){ return oWins; }
        int getDraws(){ return draws; }
        int[] getWinLine(){ return winLine; }

        boolean place(int r, int c){
            if (gameOver || board[r][c] != Cell.EMPTY) return false;
            board[r][c] = current;
            evaluateAfterMove();
            if (!gameOver) current = (current == Cell.X ? Cell.O : Cell.X);
            return true;
        }

        private void evaluateAfterMove(){
            Cell w = winner();
            if (w != Cell.EMPTY){
                gameOver = true;
                if (w == Cell.X) xWins++; else oWins++;
                return;
            }
            if (isFull()) { gameOver = true; draws++; }
        }

        private boolean isFull(){
            for (int r=0;r<3;r++) for (int c=0;c<3;c++) if (board[r][c] == Cell.EMPTY) return false;
            return true;
        }

        Cell winner(){
            for (int[] line : LINES){
                Cell a = board[line[0]/3][line[0]%3];
                Cell b = board[line[1]/3][line[1]%3];
                Cell c = board[line[2]/3][line[2]%3];
                if (a != Cell.EMPTY && a == b && b == c){
                    winLine = line.clone();
                    return a;
                }
            }
            winLine = null;
            return Cell.EMPTY;
        }

        // Movimientos disponibles
        java.util.List<int[]> availableMoves(){
            java.util.List<int[]> moves = new ArrayList<>();
            for (int r=0;r<3;r++) for (int c=0;c<3;c++) if (board[r][c] == Cell.EMPTY) moves.add(new int[]{r,c});
            return moves;
        }
        GameModel copy(){
            GameModel g = new GameModel();
            for (int r=0;r<3;r++) for (int c=0;c<3;c++) g.board[r][c] = this.board[r][c];
            g.current = this.current; g.vsCPU = this.vsCPU; g.cpuSide = this.cpuSide; g.gameOver = this.gameOver;
            g.xWins = this.xWins; g.oWins = this.oWins; g.draws = this.draws;
            g.winLine = (this.winLine==null? null : this.winLine.clone());
            return g;
        }
    }

    // =============== VIEW ==================
    static class TicTacToeView extends JFrame {
        final JButton[][] btn = new JButton[3][3];
        final JLabel lblStatus = new JLabel("Turno: X", SwingConstants.LEFT);
        final JLabel lblScore  = new JLabel("X: 0 | O: 0 | Empates: 0", SwingConstants.RIGHT);
        final JCheckBox chkCPU = new JCheckBox("Jugar vs CPU", true);
        final JCheckBox chkSound = new JCheckBox("Sonido", true);
        final JButton btnNew   = new JButton("Nueva partida");
        final JComboBox<String> cmbStarts = new JComboBox<>(new String[]{"Empieza X", "Empieza O"});
        final JComboBox<String> cmbCpuSide = new JComboBox<>(new String[]{"CPU: X", "CPU: O"});
        final JComboBox<Theme> cmbTheme = new JComboBox<>(new Theme[]{THEME_CLARO, THEME_OSCURO, THEME_OCEANO});

        Theme theme = THEME_CLARO;
        final Color CELL_BG_DEFAULT; // respaldo

        TicTacToeView(){
            setTitle("Tic Tac Toe – Java");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(520, 620);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(10,10));

            // Top bar: estado y marcador
            JPanel top = new JPanel(new BorderLayout());
            top.setBorder(new EmptyBorder(10,10,0,10));
            lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 16f));
            lblScore.setFont(lblScore.getFont().deriveFont(Font.PLAIN, 14f));
            top.add(lblStatus, BorderLayout.WEST);
            top.add(lblScore, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);

            // Centro: grilla 3x3
            JPanel center = new JPanel(new GridLayout(3,3,8,8));
            center.setBorder(new EmptyBorder(10,10,10,10));
            Font f = new Font(Font.SANS_SERIF, Font.BOLD, 64);
            JButton sample = new JButton();
            CELL_BG_DEFAULT = sample.getBackground();
            for (int r=0;r<3;r++){
                for (int c=0;c<3;c++){
                    JButton b = new JButton("");
                    b.setFont(f);
                    b.setFocusPainted(false);
                    btn[r][c] = b;
                    center.add(b);
                }
            }
            add(center, BorderLayout.CENTER);

            // Bottom: controles
            JPanel bottom = new JPanel(new GridBagLayout());
            bottom.setBorder(new EmptyBorder(0,10,10,10));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(4,4,4,4); gc.gridy=0;
            bottom.add(new JLabel("Tema:"), gc);
            bottom.add(cmbTheme, gc);
            bottom.add(chkSound, gc);
            bottom.add(chkCPU, gc);
            bottom.add(cmbCpuSide, gc);
            bottom.add(cmbStarts, gc);
            bottom.add(btnNew, gc);
            add(bottom, BorderLayout.SOUTH);

            applyTheme(theme);
        }

        void applyTheme(Theme t){
            this.theme = t;
            getContentPane().setBackground(t.bg);
            for (Component c : getContentPane().getComponents()) c.setBackground(t.panelBg);
            lblStatus.setForeground(t.text);
            lblScore.setForeground(t.text);
            for (JButton[] row : btn) for (JButton b : row){ b.setBackground(t.cellBg); b.setForeground(t.text); }
            repaint();
        }

        void setStatus(String s){ lblStatus.setText(s); }
        void setScore(int xWins, int oWins, int draws){ lblScore.setText("X: "+xWins+" | O: "+oWins+" | Empates: "+draws); }
        void setCell(int r, int c, Cell mark){
            JButton b = btn[r][c];
            if (mark==Cell.EMPTY){ b.setText(""); }
            else if (mark==Cell.X){ b.setText("X"); b.setForeground(theme.xColor); }
            else { b.setText("O"); b.setForeground(theme.oColor); }
        }
        void enableBoard(boolean v){ for (JButton[] row : btn) for (JButton b: row) b.setEnabled(v); }
        void clearHighlights(){ for (JButton[] row: btn) for (JButton b: row) b.setBackground(theme.cellBg); }
        void highlightWin(int[] line){ if (line==null) return; for (int k: line){ int r=k/3, c=k%3; btn[r][c].setBackground(theme.winBg);} }
    }

    // =============== CONTROLLER + AI ==================
    static class GameController {
        private final GameModel model;
        private final TicTacToeView view;
        private final Random rnd = new Random();

        GameController(GameModel m, TicTacToeView v){ this.model = m; this.view = v; wire(); }

        void start(){ refreshAll("Turno: "+ (model.getCurrent()==Cell.X?"X":"O")); maybeCpuStart(); }

        private void wire(){
            // Clicks en celdas
            for (int r=0;r<3;r++){
                for (int c=0;c<3;c++){
                    final int rr=r, cc=c;
                    view.btn[r][c].addActionListener(e -> onCell(rr, cc));
                }
            }
            // Nueva partida
            view.btnNew.addActionListener(e -> newGame());
            // Cambiar tema
            view.cmbTheme.addActionListener(e -> {
                Theme t = (Theme) view.cmbTheme.getSelectedItem();
                if (t != null) view.applyTheme(t);
            });
            // Sonido
            view.chkSound.addActionListener(e -> SoundPlayer.setEnabled(view.chkSound.isSelected()));
            // Modo CPU
            view.chkCPU.addActionListener(e -> {
                model.setVsCPU(view.chkCPU.isSelected());
                updateCpuControlsEnabled();
                newGame();
            });
            // Lado de la CPU
            view.cmbCpuSide.addActionListener(e -> {
                String sel = (String) view.cmbCpuSide.getSelectedItem();
                if (sel != null) model.setCpuSide(sel.contains("X")? Cell.X : Cell.O);
            });
            // Quién empieza
            view.cmbStarts.addActionListener(e -> {
                // Solo aplica en la siguiente nueva partida
            });
        }

        private void updateCpuControlsEnabled(){
            boolean en = view.chkCPU.isSelected();
            view.cmbCpuSide.setEnabled(en);
        }

        private void newGame(){
            model.resetBoard();
            // quién empieza
            String startSel = (String) view.cmbStarts.getSelectedItem();
            Cell starter = (startSel!=null && startSel.contains("O")) ? Cell.O : Cell.X;
            model.setCurrent(starter);
            view.clearHighlights();
            refreshAll("Turno: "+(starter==Cell.X?"X":"O"));
            maybeCpuStart();
        }

        private void maybeCpuStart(){
        if (model.isVsCPU() && model.getCurrent() == model.getCpuSide()){
        // Pequeño delay para UX: usar SIEMPRE el Timer de Swing
        javax.swing.Timer t = new javax.swing.Timer(150, e -> {
            cpuMove();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
        }
    }

        private void onCell(int r, int c){
            if (model.isGameOver()) return;
            if (!model.place(r,c)) return; // jugada inválida
            SoundPlayer.move();
            afterMove();
        }

        private void afterMove(){
            refreshAll(null);
            if (model.isGameOver()) { endRound(); return; }
            if (model.isVsCPU() && model.getCurrent() == model.getCpuSide()){
                cpuMove();
            }
        }

        private void cpuMove(){
            int[] best = minimaxBestMove(model, model.getCpuSide());
            if (best == null){ // fallback (no debería pasar)
                java.util.List<int[]> moves = model.availableMoves();
                if (!moves.isEmpty()) best = moves.get(rnd.nextInt(moves.size()));
            }
            if (best != null) model.place(best[0], best[1]);
            SoundPlayer.move();
            refreshAll(null);
            if (model.isGameOver()) endRound();
        }

        private void endRound(){
            Cell w = model.winner();
            if (w != Cell.EMPTY) {
                view.setStatus("Ganó " + (w==Cell.X? "X" : "O"));
                view.highlightWin(model.getWinLine());
                SoundPlayer.win();
            } else {
                view.setStatus("Empate");
                SoundPlayer.draw();
            }
            view.enableBoard(false);
        }

        private void refreshAll(String statusOverride){
            for (int r=0;r<3;r++) for (int c=0;c<3;c++) view.setCell(r,c, model.getCell(r,c));
            String s = statusOverride!=null? statusOverride : ("Turno: " + (model.getCurrent()==Cell.X? "X":"O"));
            view.setStatus(s);
            view.setScore(model.getXWins(), model.getOWins(), model.getDraws());
            view.enableBoard(!model.isGameOver());
        }

        // =============== IA: Minimax con poda ===============
        private static int[] minimaxBestMove(GameModel model, Cell ai){
            int bestScore = Integer.MIN_VALUE; int[] bestMove = null;
            for (int[] mv : model.availableMoves()){
                GameModel g = model.copy();
                g.place(mv[0], mv[1]); // juega quien está en turno (debe ser 'ai')
                int score = minimax(g, ai, false, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
                if (score > bestScore){ bestScore = score; bestMove = mv; }
            }
            return bestMove;
        }

        private static int minimax(GameModel g, Cell ai, boolean maximizing, int alpha, int beta, int depth){
            Cell w = g.winner();
            if (w != Cell.EMPTY){
                if (w == ai) return 10 - depth;    // ganar más rápido es mejor
                else return -10 + depth;           // perder más tarde es menos malo
            }
            java.util.List<int[]> moves = g.availableMoves();
            if (moves.isEmpty()) return 0; // empate

            if (g.getCurrent() == ai){ // el jugador en turno es el AI ⇒ maximiza
                int best = Integer.MIN_VALUE;
                for (int[] mv : moves){
                    GameModel c = g.copy();
                    c.place(mv[0], mv[1]);
                    int sc = minimax(c, ai, false, alpha, beta, depth+1);
                    best = Math.max(best, sc); alpha = Math.max(alpha, sc);
                    if (beta <= alpha) break; // poda
                }
                return best;
            } else { // turno del rival ⇒ minimiza
                int best = Integer.MAX_VALUE;
                for (int[] mv : moves){
                    GameModel c = g.copy();
                    c.place(mv[0], mv[1]);
                    int sc = minimax(c, ai, true, alpha, beta, depth+1);
                    best = Math.min(best, sc); beta = Math.min(beta, sc);
                    if (beta <= alpha) break;
                }
                return best;
            }
        }
    }
}

