package significant.minesweeperbotjava;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
/**
 *
 * @author tompp
 */
public class BoardAnalyzer {
    private BufferedImage boardImage;
    private int rows;
    private int cols;
    private int cellSize;
    private int boardTopLeftX;
    private int boardTopLeftY;
    private Cell[][] board;
    private String selectedResolution;
    private BufferedImage[] templates;
    private ActionExecutor actionExecutor = new ActionExecutor();
    
    public BoardAnalyzer(BufferedImage boardImage, int rows, int cols, int cellSize, int boardTopLeftX, int boardTopLeftY, String selectedResolution, BufferedImage[] templates) {
        this.boardImage = boardImage;
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.boardTopLeftX = boardTopLeftX;
        this.boardTopLeftY = boardTopLeftY;
        this.board = new Cell[rows][cols];
        this.selectedResolution = selectedResolution;
        this.templates = templates;
        
        analyzeBoard();
    }
    
    private void analyzeBoard() {
    // Iterate through each cell
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                BufferedImage cellImage = getCellImage(row, col);
                if (cellImage == null) {
                    System.out.println("Skipping invalid cell [" + row + "][" + col + "]");
                    try {
                        this.saveCapture(cellImage, "Error");
                    } catch (Exception e){
                        System.out.println("Error while saving the sub image");
                    }
                continue;
                }
                String state = detectCellState(cellImage);
                board[row][col] = new Cell(state, row, col);
            }
        }
    }
    
    public BufferedImage getCellImage(int row, int col) {
        // Calculate cell position
        int x = col * cellSize;
        int y = row * cellSize;

        
            // Validate coordinates
        if (x + cellSize > boardImage.getWidth() || y + cellSize > boardImage.getHeight()) {
            System.out.println("Error: Cell coordinates out of bounds!");
            return null; // Or throw an exception
        }

        // Extract the cell image
        return boardImage.getSubimage(x, y, cellSize, cellSize);
    }
    
    public String detectCellState(BufferedImage cellImage) {
        double tolerancePercentage = 50.0;
        int bestMatchIndex = -1;
        double bestMatchPercentage = 0.0;
        
        
        
        // Compare the cell image with each template
        for (int i = 0; i < templates.length; i++) {
            
            double matchPercentage = calculateMatchPercentage(cellImage, templates[i]);

            if (matchPercentage > bestMatchPercentage) {
                bestMatchPercentage = matchPercentage;
                bestMatchIndex = i;
            }
        }
        
        if (bestMatchPercentage >= tolerancePercentage) {
            return switch (bestMatchIndex) {
                case 0 -> "Empty";
                case 7 -> "Flagged";
                case 8 -> "Covered";
                default -> "Number " + bestMatchIndex;
            };
        }
        return "Covered";
    }
    
    private double calculateMatchPercentage(BufferedImage img1, BufferedImage img2) {
        // Check dimensions first
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return 0.0; // Completely different if dimensions don't match
        }

        int width = img1.getWidth();
        int height = img1.getHeight();
        int totalPixels = width * height;
        int matchingPixels = 0;

        // Define a pixel similarity threshold
        int pixelDifferenceThreshold = 30; // Adjust as needed (lower is stricter)

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

            // Extract RGB values
                int r1 = (rgb1 >> 16) & 0xFF, g1 = (rgb1 >> 8) & 0xFF, b1 = rgb1 & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF, g2 = (rgb2 >> 8) & 0xFF, b2 = rgb2 & 0xFF;

                // Calculate pixel difference
                int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);

                if (diff < pixelDifferenceThreshold) {
                    matchingPixels++;
                }
            }
        }
    // Calculate match percentage
    return (double) matchingPixels / totalPixels * 100;
    }
    
    public void findSafeMovesAndFlags() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = board[row][col];
                if (cell.getState().startsWith("Number")) {
                    int number = Integer.parseInt(cell.getState().split(" ")[1]);
                    processNumberedCell(cell, number);
                }
            }
        }
        
    }
    
    private void processNumberedCell(Cell cell, int number) {
        int flaggedCount = 0;
        int coveredCount = 0;
        Cell[] neighbors = getNeighbors(cell);
        
        for (Cell neighbor : neighbors) {
            if (neighbor != null) {
                if ("Flagged".equals(neighbor.getState())) {
                    flaggedCount++;
                } else if ("Covered".equals(neighbor.getState())) {
                    coveredCount++;
                }
            }
        }
        
        if (flaggedCount == number) {
            // All mines are flagged, the rest are safe to click
            for (Cell neighbor : neighbors) {
                if (neighbor != null && "Covered".equals(neighbor.getState())) {
                    int x = neighbor.getCol() * cellSize + cellSize / 2 + boardTopLeftX;
                    int y = neighbor.getRow() * cellSize + cellSize / 2 + boardTopLeftY;
                    actionExecutor.clickCell(x, y);
                }
            }
        } else if (flaggedCount + coveredCount == number) {
            // All remaining covered cells are mines
            for (Cell neighbor : neighbors) {
                if (neighbor != null && "Covered".equals(neighbor.getState())) {
                    int x = neighbor.getCol() * cellSize + cellSize / 2 + boardTopLeftX;
                    int y = neighbor.getRow() * cellSize + cellSize / 2 + boardTopLeftY;
                    actionExecutor.flagCell(x, y);
                    neighbor.setState("Flagged");
                }
            }
        }
    }
    
    private Cell[] getNeighbors(Cell cell) {
        int row = cell.getRow();
        int col = cell.getCol();
        Cell[] neighbors = new Cell[8];
        int index = 0;
        
        for (int r = -1; r <= 1; r++) {
            for (int c = -1; c <= 1; c++) {
                if (r == 0 && c == 0) continue; // Skip the cell itself
                int newRow = row + r;
                int newCol = col + c;
                
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    neighbors[index++] = board[newRow][newCol];
                }
            }
        }
        return neighbors;
    }
    
    public boolean hasPendingMoves() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = board[row][col];
                if (cell.getState().startsWith("Number")) {
                    int number = Integer.parseInt(cell.getState().split(" ")[1]);
                    int flaggedCount = 0;
                    int coveredCount = 0;
                    Cell[] neighbors = getNeighbors(cell);

                    for (Cell neighbor : neighbors) {
                        if (neighbor != null) {
                            if ("Flagged".equals(neighbor.getState())) {
                                flaggedCount++;
                            } else if ("Covered".equals(neighbor.getState())) {
                                coveredCount++;
                            }
                        }
                    }

                    // If there are safe moves or flags to make, return true
                    if (flaggedCount < number && coveredCount > 0) {
                        return true;
                    }
                }
            }
        }
        return false; // No moves left
    }
    
    public void saveCapture (BufferedImage image, String fileName) throws Exception{
        File outputfile = new File(fileName);
        ImageIO.write(image, "png", outputfile);
        System.out.println("Screenshot saved: " + fileName);
    }
}
