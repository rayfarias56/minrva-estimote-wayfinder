package edu.illinois.ugl.minrvaestimote;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Maxx on 11/24/2015.
 */
public class GridMap {

    private Cell[][] grid;

    // The dimensions of the library floor in centimeters, based on the UGL CAD map.
    private double LIB_X_CM = 5624.40;
    private double LIB_Y_CM = 4985.90;

    public GridMap() {
        createGrid();
    }

    /**
     * Finds the coordinates (in centimeters) of the nearest legal cell in the grid.
     * @param x The user's current x position in centimeters.
     * @param y The user's current y position in centimeters.
     * @return The legal (x, y) coordinates in centimeters from the origin/the top left corner.
     */
    public double[] getClosestLegalCoords(double x, double y) {
        Cell curCell = convertUserCoordsToGridCell(x, y);
        Cell legalCell = getClosestLegalCell(curCell);
        return convertCellToUserCoords(legalCell);
    }

    /**
     * Checks if user is currently in a legal cell in the grid.
     * @param x The user's current x position in centimeters.
     * @param y The user's current y position in centimeters.
     * @return Whether or not the user is in a legal cell.
     */
    public boolean isInLegalCell(double x, double y) {
        Cell curCell = convertUserCoordsToGridCell(x, y);
        return curCell.legal;
    }

    /**
     * Initializes the grid of cells by using data from the UGL CAD map.
     */
    private void createGrid() {
        int numCellsX = 18;
        int numCellsY = 16;
        grid = new Cell[numCellsX][numCellsY];
        for(int i = 0; i < numCellsX; i++)
            for(int j = 0; j < numCellsY; j++)
                grid[i][j] = new Cell(i, j, true);

        // Right now only marking grids that were marked on CAD map.
        markIllegalCells(1, 14, 0, 1);
        markIllegalCells(0, 0, 3, 1);
        markIllegalCells(6, 0, 5, 1);
        markIllegalCells(6, 6, 5, 5);
        markIllegalCells(10, 14, 6, 1);
    }

    /**
     * Helper function that sets a rectangle of cells to be illegal.
     * @param x The x position of the first cell
     * @param y The y position of the first cell
     * @param dx The number of cells in the x direction to traverse
     * @param dy The number of cells in the y direction to traverse
     */
    private void markIllegalCells(int x, int y, int dx, int dy) {
        for (int i = x; i <= x + dx; i++)
            for (int j = y; j <= y + dy; j++)
                grid[i][j].legal = false;
    }

    /**
     * Finds the corresponding cell in the grid based on the user's coordinates
     * @param x The user's current x position in centimeters.
     * @param y The user's current y position in centimeters.
     * @return The cell in the grid that the user is in.
     */
    private Cell convertUserCoordsToGridCell(double x, double y) {
        double gridXCm = LIB_X_CM / grid.length;
        double gridYCm = LIB_Y_CM / grid[0].length;
        int cellX = (int) (x / gridXCm);
        int cellY = (int) (y / gridYCm);
        return grid[cellX][cellY];
    }

    /**
     * Finds the real coordinates (in centimeters) of a cell.
     * @param cell The cell whose coordinates need to be found.
     * @return The coordinates of the cell in the library.
     */
    private double[] convertCellToUserCoords(Cell cell) {
        double gridXCm = LIB_X_CM / grid.length;
        double gridYCm = LIB_Y_CM / grid[0].length;
        double newX = (gridXCm * cell.x) + (gridXCm / 2);
        double newY = (gridYCm * cell.y) + (gridYCm / 2);
        return new double[]{newX, newY};
    }

    /**
     * From the cell the user is in, finds the nearest legal cell.
     * @param startCell The cell that the user is in
     * @return The nearest legal cell in the grid or null if there are none.
     */
    private Cell getClosestLegalCell(Cell startCell) {
        boolean[][] processed = new boolean[grid.length][grid[0].length];
        Queue<Cell> queue = new LinkedList<Cell>();
        queue.add(startCell);
        while(!queue.isEmpty()) {
            Cell curCell = queue.remove();
            if (curCell.legal)
                return curCell;
            tryAddingNeighbor(curCell.x + 1, curCell.y, processed, queue); // East
            tryAddingNeighbor(curCell.x - 1, curCell.y, processed, queue); // West
            tryAddingNeighbor(curCell.x, curCell.y + 1, processed, queue); // South
            tryAddingNeighbor(curCell.x, curCell.y - 1, processed, queue); // North
        }
        return null; // Shouldn't happen unless the entire grid is illegal.
    }

    /**
     * Helper function that adds a cell to the queue if it is valid.
     * @param x The x coordinate of the cell in the grid
     * @param y The y coordinate of the cell in the grid
     * @param processed The table of all cells' processed status
     * @param queue The queue of cells used in the breadth-first search
     */
    private void tryAddingNeighbor(int x, int y, boolean[][] processed, Queue<Cell> queue) {
        if (x < 0 || y < 0 || x >= grid.length || y >= grid[0].length || processed[x][y])
            return;
        processed[x][y] = true;
        queue.add(grid[x][y]);
    }

    /**
     * Private class that is used to represent a cell in the libary's grid.
     */
    private class Cell {
        public int x;
        public int y;
        public boolean legal;

        public Cell(int x, int y, boolean legal) {
            this.x = x;
            this.y = y;
            this.legal = legal;
        }
    }
}
