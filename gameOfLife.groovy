#! /usr/bin/env groovy

//  Game of Life -- a Groovy implementation of Conway's seminal "game".
//
//  Copyright © 2011 Russel Winder
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the GNU
//  General Public License as published by the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
//  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
//  License for more details.
//
//  You should have received a copy of the GNU General Public License along with this program.  If not, see
//  <http://www.gnu.org/licenses/>.

/**
 *  Each cell in the rectangular 2-d "board" is realized by an instace of one of these.
 *
 *  Beacuse the "board" has to be created as a phase before the connections are set out, the class cannot
 *  have a constructor other than the nullary one (so no need for one), and must have a method to set the
 *  neighbours in the second phase of initialization.
 */
class Cell {
  /**
   *  The list of nearest neighbours:  West, East , North , South.
   */
  private List<Cell> neighbours
  /**
   *  Whether the cell is alive or dead.
   */
  Boolean state
  /**
   *  Give this cell knowledge of its four nearest neighbours.
   */
  void connect ( List<Cell> n ) { neighbours = n }
  /**
   *  The "callback" for this cell to respond to the march of time.
   */
  void tick ( ) {
    def count = neighbours.collect { it?.state ? 1 : 0 }.sum ( )
    state = ( count == 2 ) || ( count == 3 )
  }
}

/**
 *  The universe is a two dimensional rectangular array of cells of size speficied at construction time.
 */
class Universe {
  /**
   *  Storage for the "board".  Normally people talk of rows and columns and the temptation is to always
   *  have the rows as the first index, this though means that when people give coordinates they are
   *  actually ( y , x ) which might be surprising.  So index columns first so that there is an ( x , y )
   *  feel to the coordinate and indexing.
   */
  final Cell[][] board
  /**
   *  The number of rows of the "board".  The y-axis size.
   */
  final int rows
  /**
   *  The nubmer of columns of the "board".  The x-axis size.
   */
  final int columns
  /**
   *  Constructor of the universe.
   *
   *  @param cs the number of columns of the "board".  The size of the x axis.
   *  @param rs the number of rows of the "board". The size of the y axis.
   */
  Universe ( cs , rs ) {
    columns = cs
    rows = rs
    board = new Cell [columns][rows]
    ( 0 ..< columns ).each { column ->
      ( 0 ..< rows ).each { row ->
        board[column][row] = new Cell ( )
      }
    }
    ( 0 ..< columns ).each { column ->
      ( 0 ..< rows ).each { row ->
        def neighbours = [ null , null , null , null ]
        if ( row != 0 ) { neighbours[0] = board[column][row-1] }
        if ( row != rows - 1 ) { neighbours[1] = board[column][row+1] }
        if ( column != 0 ) { neighbours[2] = board[column-1][row] }
        if ( column != columns - 1 ) { neighbours[3] = board[column+1][row] }
        board[column][row].connect ( neighbours )
      }
    }
  }
  /**
   *  Send a tick to all the cells contained in the "board".
   */
  void tick ( ) {
    ( 0 ..< columns ).each { column ->
      ( 0 ..< rows ).each { row ->
        board[column][row].tick ( )
      }
    }
  }
  /**
   *  Create a string representation of the universe.
   *
   *  @return a string representating the state of the universe.
   */
  String render ( ) {
    def result = new StringBuilder ( )
    ( 0 ..< columns ).each { column ->
      ( 0 ..< rows ).each { row ->
        result += board[column][row].state ? '*' : ' '
      }
      result += '\n'
    }
    result.toString ( )
  }
  /**
   *  Set the initial state of the universe.
   *
   *  @param l a list of lists each entry having two integers specifying the coordinates of the cell to set
   *  to be live.  If the list is empty all cells are made live.
   */
  void populate ( List l ) {
    if ( l.size ( ) == 0 ) {
      ( 0 ..< columns ).each { column ->
        ( 0 ..< rows ).each { row ->
          board[column][row].state = true
        }
      }
    }
    else { l.each { board[it[0]][it[1]].state = true } }
  }
}

//  A little driver code.  Create a universe.  Populate it then tick every 2s.

def universe = new Universe  ( 20 , 20 )
def startData
switch ( 2 ) {
 case 0 :
   startData = [ ]
   break
 case 1 :
   //  Becomes the open square at tick 3.
   startData = [
     [ 2 , 2 ] , [ 2 , 3 ] , [ 2 , 4 ] , [ 2 , 5 ] ,
     [ 3 , 2 ] , [ 3 , 3 ] ,
     [ 4 , 2 ] ,
     [ 5 , 3 ]
   ]
   break
 case 2 :
   startData = [
     [ 2 , 2 ] , [ 2 , 3 ] , [ 2 , 4 ] ,
     [ 3 , 2 ] , [ 3 , 3 ] , [ 3 , 5 ] ,
     [ 4 , 2 ] ,
     [ 5 , 2 ]
   ]
}
universe.populate ( startData )
def i = 0
while ( true ) {
  println ( 'tick ' + (i++) + '\n' )
  println ( universe.render ( ) )
  println ( '------------------------------------------------------------------' )
  Thread.sleep ( 2000 )
  universe.tick ( )
}
