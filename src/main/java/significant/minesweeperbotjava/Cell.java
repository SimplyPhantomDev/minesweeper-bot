package significant.minesweeperbotjava;

/**
 *
 * @author tompp
 */
public class Cell {
    private String state;
    private int row;
    private int col;
    
    public Cell(String state, int row, int col) {
        this.state = state;
        this.row = row;
        this.col = col;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
}
