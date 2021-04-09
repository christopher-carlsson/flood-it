import java.util.ArrayList;

import java.util.Arrays;

import java.util.Random;

import tester.*;

import javalib.impworld.*;

import java.awt.Color;

import javalib.worldimages.*;

// Represents a single square of the game area
interface CONSTANTS {
  static final int CELL_SIZE = 20;
}

class Cell implements CONSTANTS {

  // In logical coordinates, with the origin at the top-left corner of the screen

  int x;

  int y;

  Color color;

  boolean flooded;

  // the four adjacent cells to this one

  Cell left;

  Cell top;

  Cell right;

  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {

    this.x = x;

    this.y = y;

    this.color = color;

    this.flooded = flooded;

  }

  Cell(int x, int y, Color color, boolean flooded, Cell left, Cell top, Cell right, Cell bottom) {

    this.x = x;

    this.y = y;

    this.color = color;

    this.flooded = flooded;

    this.left = left;

    this.top = top;

    this.right = right;

    this.bottom = bottom;

  }

  Cell(int x, int y, Color color, boolean flooded, Cell left) {

    this.x = x;

    this.y = y;

    this.color = color;

    this.flooded = flooded;

    this.left = left;

  }

  Cell(int x, int y, Color color, Cell top, boolean flooded) {

    this.x = x;

    this.y = y;

    this.color = color;

    this.flooded = flooded;

    this.top = top;

  }

  // Draws a cell
  public RectangleImage drawCell() {

    return new RectangleImage(this.CELL_SIZE, this.CELL_SIZE, "solid", this.color);

  }

  // Sets this left cell to the given
  public void setLeft(Cell given) {

    given.right = this;

    this.left = given;

  }

  // Sets this top cell to the given
  public void setTop(Cell given) {

    given.bottom = this;

    this.top = given;

  }

}

// FloodItWorld class
class FloodItWorld extends World implements CONSTANTS {

  // All the cells of the game

  int size;

  ArrayList<ArrayList<Cell>> board;

  ArrayList<Color> listColors = new ArrayList<Color>(

      Arrays.asList(Color.MAGENTA, Color.red, Color.pink, Color.blue, Color.green, Color.yellow));

  Random rand;

  int clicksCount;

  boolean allFlooded;

  boolean won;

  boolean isPlaying;

  Color floodColor;

  boolean flooding;

  double startTick = System.currentTimeMillis();

  double tickTime;

  int numberOfColors;

  FloodItWorld(ArrayList<ArrayList<Cell>> board) {
    this.board = board;

  }

  FloodItWorld(int size, int numberOfColors) {
    this.size = size;
    this.rand = new Random();
    this.numberOfColors = numberOfColors;

    this.board = this.makeBoard();

    this.clicksCount = 0;

    this.allFlooded = false;

    this.won = false;

    this.isPlaying = true;

    this.floodColor = Color.black;

    this.flooding = false;

  }

  // Convenience constructor used for testing (colors stay constant)
  FloodItWorld(int size, int numberOfColors, Random rand) {
    this.size = size;

    this.rand = rand;

    this.numberOfColors = numberOfColors;
    this.board = this.makeBoard();

    this.clicksCount = 0;

    this.floodColor = Color.black;

    this.allFlooded = false;

    this.flooding = false;

    this.won = false;

    this.isPlaying = true;

  }

  // Visualizes the board
  // top left.
  // (0,0) - (20,20)
  public WorldScene makeScene() {
    WorldScene initial = new WorldScene(500, 500);
    initial.placeImageXY(
        new TextImage(
            "Chances: " + Integer.toString(this.clicksCount) + "/"
                + Integer.toString(Math.round(2 * this.size) + (Math.floorDiv(this.size, 3))),
                15, FontStyle.BOLD_ITALIC, Color.black),
        (500 - (this.CELL_SIZE + this.size) * 4), (500 - (this.CELL_SIZE + this.size) * 4));
    initial.placeImageXY(
        new TextImage("Time: " + this.timeconv(Math.round(tickTime)), 15, FontStyle.BOLD_ITALIC,
            Color.black),
        (500 - (this.CELL_SIZE + this.size) * 4), (500 - (this.CELL_SIZE + this.size) * 2));
    // initial.placeImageXY(background, 250, 0);
    if (isPlaying) {
      for (ArrayList<Cell> c : board) {

        int offset = this.CELL_SIZE / 2;
        // int offset = 250 - ((this.size * this.cell_size) / 2);

        for (Cell s : c) {

          initial.placeImageXY(s.drawCell(), offset + (this.CELL_SIZE * s.x),
              offset + (this.CELL_SIZE * s.y));

        }

      }

      return initial;

    }
    else {
      return this.endScreen(won);
    }
  }

  public String timeconv(long l) {
    if (l < 60) {
      return Long.toString(l);
    }
    else {
      int min = Math.round((l / 60));
      long seconds = l % 60;
      if (seconds < 10) {
        return Integer.toString(min) + ":0" + Long.toString(seconds);
      }
      else {
        return Integer.toString(min) + ":" + Long.toString(seconds);
      }
    }
  }

  // Creates board
  public ArrayList<ArrayList<Cell>> makeBoard() {

    this.board = new ArrayList<>();

    // ArrayList<ArrayList<Cell>> res = new ArrayList<ArrayList<Cell>>();

    for (int i = 0; i < size; i++) {

      this.board.add(makeBoardHelper(i));

    }
    this.board.get(0).get(0).flooded = true;

    return board;

  }

  // Creates board
  ArrayList<Cell> makeBoardHelper(int y) {

    ArrayList<Cell> result = new ArrayList<Cell>();

    for (int k = 0; k < size; k++) {

      result
      .add(new Cell(k, y, this.listColors.get(this.rand.nextInt(this.numberOfColors)), false));

    }

    return result;

  }

  // Points neighboring cells to each other
  public void connect() {

    for (int i = 0; i < size; i++) {

      for (int j = 0; j < size; j++) {

        if (i == 0 && j != 0) {

          this.board.get(i).get(j).setLeft(this.board.get(i).get(j - 1));

        }

        else if (i != 0 && j == 0) {

          this.board.get(i).get(j).setTop(this.board.get(i - 1).get(j));

        }

        else if (i != 0 && j != 0) {

          this.board.get(i).get(j).setLeft(this.board.get(i).get(j - 1));

          this.board.get(i).get(j).setTop(this.board.get(i - 1).get(j));

        }
      }

    }

  }

  // the method that allows the user to reset the game
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = new ArrayList<ArrayList<Cell>>();
      this.clicksCount = 0;
      this.won = false;
      this.isPlaying = true;
      this.allFlooded = false;
      this.startTick = System.currentTimeMillis();

      this.makeBoard();
    }
  }

  //// if the neighbors share the same color, then set flooded to true
  // void setFloodedNeighborsIfSameColor(Cell c1) {
  // setToFloodedIfSameColor(c1.color, c1.left);
  // setToFloodedIfSameColor(c1.color, c1.right);
  // setToFloodedIfSameColor(c1.color, c1.bottom);
  // setToFloodedIfSameColor(c1.color, c1.top);
  // }
  //// helper for setFloodedNeighborsIfSameColor
  // void setToFloodedIfSameColor(Color c, Cell cell) {
  // if (cell != null) {
  // if (cell.color.equals(c)) {
  // cell.flooded = true;
  // }
  // }
  //
  // }
  // determines if any of the neighbors are flooded already
  public boolean hasFloodedNeighbors(Cell curr) {
    Cell right = curr.right;
    Cell left = curr.left;
    Cell top = curr.top;
    Cell bottom = curr.bottom;

    return left != null && left.flooded || top != null && top.flooded
        || right != null && right.flooded || bottom != null && bottom.flooded;
  }

  // fills in the cells with the color to flood
  public void floodFill() {
    this.board.get(0).get(0).flooded = true;
    // boolean weFloodedAtLeastSomething = false;
    for (ArrayList<Cell> c : this.board) {
      for (Cell curr : c) {
        if (curr.flooded && curr.color != this.floodColor) {
          curr.color = this.floodColor;
          return;
        }
        if (!curr.flooded && curr.color == this.floodColor && hasFloodedNeighbors(curr)) {
          curr.flooded = true;
          return;
        }
        // this.setFloodedNeighborsIfSameColor(c1);
        // this.waterfall(this.floodColor);
        // weFloodedAtLeastSomething = true;
        // return;
      }

    }
    // have we completed the flooding. this.flooding = false;
    this.flooding = false;
  }

  //// sets a cells color to the given color if it's flooded
  // void waterfall(Color color) {
  // boolean allFlood1 = true;
  // for (ArrayList<Cell> c : this.board) {
  // for (Cell c1 : c) {
  // if (c1.flooded == true) {
  // c1.color = color;
  // }
  // else {
  // allFlood1 = false;
  // }
  //
  // }
  // if (allFlood1) {
  // allFlooded = true;
  // }
  // }
  // }
  // determines what cell was clicked
  public void onMouseClicked(Posn pos) {

    // this.cellSize) - 11;
    int grid_size = ((this.size * this.CELL_SIZE));
    // int posx = ((Math.round(pos.x - leftside) / cellSize));
    // int posy = ((Math.round(pos.y - leftside) / cellSize));

    int posx = pos.x / this.CELL_SIZE;
    int posy = pos.y / this.CELL_SIZE;

    // if (pos.x >= leftside && pos.x <= rightside && pos.y >= leftside && pos.y <=
    // rightside) {
    if (pos.x <= grid_size && pos.x <= grid_size && pos.y >= 0 && pos.y > 0) {
      this.connect();
      Cell cell = this.board.get(posy).get(posx);
      if (!cell.color.equals(this.board.get(0).get(0).color)) {
        this.clicksCount = 1 + this.clicksCount;
        this.floodColor = cell.color;
        this.flooding = true;
      }
      else {
        this.flooding = false;
      }

      System.out.println(this.clicksCount);
    }
    if (this.clicksCount > (Math.round(2 * this.size) + (Math.floorDiv(this.size, 3)))) {
      this.won = false;
      this.isPlaying = false;
    }
    if (this.allFlooded()) {
      this.won = true;
      this.isPlaying = false;
    }
    else {
      return;
    }

  }

  // visibily fills in the cells once per tick
  public void onTick() {
    if (this.flooding) {
      this.floodFill();
    }
    double currTime = System.currentTimeMillis();
    //    if (this.tickTime % 60 == 0) {
    //
    //    }
    if (isPlaying) {
      this.tickTime = (currTime - this.startTick) / 1000;
    }

  }

  // the end screen for when the game is over
  public WorldScene endScreen(boolean won) {
    WorldScene blank = this.getEmptyScene();
    WorldImage background = new RectangleImage(500, 250, "solid", Color.green);
    blank.placeImageXY(background, 250, 250);
    double stopTime = this.tickTime;
    String message = "You lost! ";
    if (won) {
      message = "You won! ";
    }
    blank.placeImageXY((new TextImage(
        message + "Press R to play again. " + " Time: " + (this.timeconv(Math.round(stopTime))),
        this.CELL_SIZE, Color.BLACK)), 250, 250);
    return blank;
  }

  // determines if all the cells in the list are flooded
  boolean allFlooded() {
    boolean flooded = true;
    for (ArrayList<Cell> c : this.board) {
      for (Cell c1 : c) {
        if (!c1.flooded) {
          flooded = false;
        }
      }
    }
    return flooded;
  }
}

// Examples Class
class ExamplesFlood {

  FloodItWorld flw;
  FloodItWorld flw6;
  FloodItWorld flw10;

  FloodItWorld flwF;

  FloodItWorld flw7;

  FloodItWorld flwFe;

  Cell cell1;

  Cell cell2;

  Cell cell3;

  Cell cell4;

  Cell cell5;

  Cell cell6;

  Cell cell7;

  Cell cell8;

  Cell cell1a;

  Cell cell2a;

  Cell cell3a;

  Cell cell4a;

  Cell cell5a;

  Cell cell6a;

  Cell cell7a;

  Cell cell8a;
  Cell cell9a;

  ArrayList<Color> listColors = new ArrayList<Color>(

      Arrays.asList(Color.MAGENTA, Color.red, Color.pink, Color.blue, Color.green, Color.yellow));
  ArrayList<Color> listColorsF = new ArrayList<Color>();
  ArrayList<Color> listOfFourColors = new ArrayList<Color>(
      Arrays.asList(Color.RED, Color.yellow, Color.blue, Color.magenta));

  WorldScene DuplicateBoard = new WorldScene(500, 500);
  WorldScene DuplicateWS = new WorldScene(500, 500);

  // Initializes Data
  void initData() {

    this.cell1 = new Cell(0, 0, Color.blue, true);

    this.cell2 = new Cell(1, 0, Color.green, false);

    this.cell8 = new Cell(0, 0, Color.green, true);

    this.cell3 = new Cell(0, 1, Color.yellow, false);

    this.cell4 = new Cell(1, 1, Color.DARK_GRAY, false);

    this.cell6 = new Cell(1, 0, Color.pink, false);

    this.cell7 = new Cell(0, 2, Color.MAGENTA, false);

    this.cell1a = new Cell(0, 0, Color.red, true);
    this.cell2a = new Cell(0, 1, Color.magenta, false);
    this.cell3a = new Cell(0, 2, Color.magenta, false);
    this.cell4a = new Cell(1, 0, Color.red, false);
    this.cell5a = new Cell(1, 1, Color.magenta, false);
    this.cell6a = new Cell(1, 2, Color.magenta, false);
    this.cell7a = new Cell(2, 0, Color.magenta, false);
    this.cell8a = new Cell(2, 1, Color.magenta, false);
    this.cell9a = new Cell(2, 1, Color.red, false);

    // this.cell5 = new Cell(0, 0, Color.red, true, cell4, cell3, cell2, cell1);

    this.flw = new FloodItWorld(2, 4, new Random(1));

    this.flw6 = new FloodItWorld(3, 3, new Random(2));

    this.flw7 = new FloodItWorld(1, 5, new Random(2));

    this.flw10 = new FloodItWorld(15, 2, new Random(2));

    this.flwF = new FloodItWorld(3, 5, new Random(2));

    this.flwFe = new FloodItWorld(5, 6);

  }

  // Tests makeBoard method
  boolean testmakeBoard(Tester t) {

    initData();
    this.cell1.color = Color.pink;
    this.cell2.color = Color.magenta;
    this.cell7.color = Color.red;

    return t.checkExpect(this.flw.board.size(), 2)
        && t.checkExpect(this.flw.board.get(0).get(0), cell1)
        && t.checkExpect(this.flw.board.get(0).get(1), cell2)
        && t.checkExpect(this.flw6.board.size(), 3)
        && t.checkExpect(this.flw6.board.get(0).get(0), cell1a)
        && t.checkExpect(this.flw6.board.get(2).get(0), cell3a)
        && t.checkExpect(this.flwF.board.get(2).get(0), cell7);

  }

  boolean testfloodFill(Tester t) {
    initData();
    this.flw.floodColor = Color.red;
    return t.checkExpect(this.flw.board.get(0).get(0).color, Color.pink);
  }

  // Tests makeBoardHelper method
  boolean testmakeBoardHelper(Tester t) {

    initData();
    this.cell1.color = Color.pink;
    this.cell2.color = Color.magenta;

    return t.checkExpect(this.flw.board.size(), 2)
        && t.checkExpect(this.flw.board.get(0).get(0), cell1)
        && t.checkExpect(this.flw.board.get(0).get(1), cell2)
        && t.checkExpect(this.flw6.board.size(), 3)
        && t.checkExpect(this.flw6.board.get(0).get(0), cell1a)
        && t.checkExpect(this.flw6.board.get(2).get(0), cell3a);

  }

  // Tests for the allFlooded method

  boolean testallFlooded(Tester t) {

    initData();
    flw.board.get(0).get(0).flooded = true;
    flw.board.get(0).get(1).flooded = true;
    flw.board.get(1).get(0).flooded = true;
    flw.board.get(1).get(1).flooded = true;
    return t.checkExpect(this.flw.allFlooded(), true)
        && t.checkExpect(this.flw10.allFlooded(), false)
        && t.checkExpect(this.flw6.allFlooded(), false);

  }

  boolean testhasFloodedNeighbors(Tester t) {
    initData();
    this.flw.connect();
    this.flw6.connect();
    this.flw6.board.get(0).get(1).right.flooded = true;
    return t.checkExpect(this.flw.hasFloodedNeighbors(this.flw.board.get(0).get(0)), false)
        && t.checkExpect(this.flw6.hasFloodedNeighbors(this.flw6.board.get(0).get(1)), true)
        && t.checkExpect(this.flw6.hasFloodedNeighbors(this.flw6.board.get(0).get(1).bottom),
            false);
  }

  // Tests draw1Cell method
  boolean testdraw1Cell(Tester t) {

    initData();

    return t.checkExpect(this.cell1.drawCell(), new RectangleImage(20, 20, "solid", Color.blue))
        && t.checkExpect(this.cell2.drawCell(), new RectangleImage(20, 20, "solid", Color.green))
        && t.checkExpect(this.cell3.drawCell(), new RectangleImage(20, 20, "solid", Color.yellow))
        && t.checkExpect(this.cell4.drawCell(),
            new RectangleImage(20, 20, "solid", Color.DARK_GRAY));

  }

  // Tests for makeScene
  void testmakeScene(Tester t) {

    initData();
    WorldScene ws1 = new WorldScene(500, 500);
    RectangleImage firstcell = new RectangleImage(20, 20, "Solid", Color.pink);
    RectangleImage secondcell = new RectangleImage(20, 20, "Solid", Color.magenta);
    RectangleImage thirdcell = new RectangleImage(20, 20, "Solid", Color.red);
    RectangleImage fourthcell = new RectangleImage(20, 20, "Solid", Color.red);
    TextImage chances = new TextImage("Chances: 0/4", 15.0, FontStyle.BOLD_ITALIC, Color.black);
    TextImage time = new TextImage("Time: 0", 15.0, FontStyle.BOLD_ITALIC, Color.black);

    ws1.placeImageXY(chances, 412, 412);
    ws1.placeImageXY(time, 412, 456);
    ws1.placeImageXY(firstcell, 10, 10);
    ws1.placeImageXY(secondcell, 30, 10);
    ws1.placeImageXY(thirdcell, 10, 30);
    ws1.placeImageXY(fourthcell, 30, 30);
    t.checkExpect(flw.makeScene(), ws1);
  }

  // Draws Board
  // boolean testDrawBoard(Tester t) {
  //
  // initData();
  //
  // WorldCanvas c = new WorldCanvas(500, 500);
  //
  // return c.drawScene(this.flw.makeScene())
  //
  // && c.show();
  // }

  // tests for setLeft
  boolean testsetLeft(Tester t) {

    initData();

    cell2.setLeft(cell1);

    cell4.setLeft(cell3);

    return t.checkExpect(cell2, new Cell(1, 0, Color.green, false, cell1))

        && t.checkExpect(cell4, new Cell(1, 1, Color.DARK_GRAY, false, cell3));

  }

  // tests for connect
  boolean testConnect(Tester t) {

    initData();

    flw.connect();
    flw6.connect();
    return t.checkExpect(flw.board.get(0).get(0).right, flw.board.get(0).get(1))

        && t.checkExpect(flw.board.get(0).get(1).left, flw.board.get(0).get(0))

        && t.checkExpect(flw6.board.get(2).get(1).right, flw6.board.get(2).get(2))

        && t.checkExpect(flw.board.get(1).get(1).top, flw.board.get(0).get(1))

        && t.checkExpect(flw6.board.get(0).get(0).left, null)

        && t.checkExpect(flw.board.get(0).get(0).top, null)

        && t.checkExpect(flw.board.get(1).get(0).bottom, null)

        && t.checkExpect(flw.board.get(1).get(1).right, null);

  }

  // tests for set top
  boolean testsetTop(Tester t) {

    initData();

    cell3.setTop(cell1);

    cell4.setTop(cell2);

    return t.checkExpect(cell3, new Cell(0, 1, Color.yellow, cell1, false))

        && t.checkExpect(cell4, new Cell(1, 1, Color.DARK_GRAY, cell2, false));

  }

  void testBigBang(Tester t) {
    initData();
    FloodItWorld w = this.flwFe;
    w.connect();
    int worldWidth = 500;
    int worldHeight = 500;
    double tickRate = 0.00001;
    w.bigBang(worldWidth, worldHeight, tickRate);
  }
}
