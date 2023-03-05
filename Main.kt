package chess

import chess.Game.*
import kotlin.math.abs

val board = Array(8) {Array(8) { Pawn(Color.NULL) } }
val whitePawns = MutableList(8) { Pawn(Color.WHITE, 6, it) }
val blackPawns = MutableList(8) { Pawn(Color.BLACK, 1, it) }

var enPassantPawn: Pawn? = null
var win = false
var draw = false

class Game {
    private var player1: Player
    private var player2: Player
    private val regex = Regex("[a-h][1-8][a-h][1-8]")
    private var turn1 = true

    init {
        println(" Pawns-Only Chess")
        println("First Player's name:")
        player1 = Player(readln(), Color.WHITE)
        println("Second Player's name:")
        player2 = Player(readln(), Color.BLACK)
    }

    fun start() {
        arrangeFigures()
        printBoard()
        play()
        gameOver()
    }

    private fun arrangeFigures() {
        for (i in 0..7) {
            board[1][i] = blackPawns[i]
            board[6][i] = whitePawns[i]
        }
    }

    private fun play() {
        while (true) {
            val player = if (turn1) player1 else player2
            draw = checkDraw(player.color)
            if (draw) break
            println("${player.name}'s turn:")
            val input = readln()
            if (input == "exit") break
            if (makeMove(player, input)) {
                printBoard()
                if (win) break
                turn1 = !turn1
            }
        }
    }

    private fun makeMove(player: Player, input: String): Boolean {
        if (!regex.matches(input)) {
            println("Invalid Input")
            return false
        }
        val x1 = abs(input[1].digitToInt() - 8)
        val y1 = input[0] - 'a'
        val pawn = board[x1][y1]
        if (pawn.color != player.color) {
            println("No ${player.color.name.lowercase()} pawn at ${input.substring(0, 2)}")
            return false
        }
        val x2 = abs(input[3].digitToInt() - 8)
        val y2 = input[2] - 'a'
        if ((x2 - x1) * pawn.getDirection() < 1) {
            println("Invalid Input")
            return false
        }
        return if (y1 == y2) {
            pawn.move(x2)
        } else {
            pawn.capture(x2, y2)
        }
    }

    private fun printBoard() {
        println("  +---+---+---+---+---+---+---+---+")
        for (i in 0..7) {
            print("${abs(i - 8)} |")
            for (j in 0..7) {
                print(" ${board[i][j]} |")
            }
            println("\n  +---+---+---+---+---+---+---+---+")
        }
        println("    a   b   c   d   e   f   g   h\n")
    }

    private fun checkDraw(color: Color): Boolean {
        val list = if (color == Color.WHITE) whitePawns else blackPawns
        val colorEnemy = if (color == Color.WHITE) Color.BLACK else Color.WHITE
        for (pawn in list) {
            if (board[pawn.x + pawn.getDirection()][pawn.y].color == Color.NULL ||
                pawn.y - 1 in 0..7 && board[pawn.x + pawn.getDirection()][pawn.y - 1].color == colorEnemy ||
                pawn.y + 1 in 0..7 && board[pawn.x + pawn.getDirection()][pawn.y + 1].color == colorEnemy) {
                return false
            }
        }
        if (enPassantPawn != null) {
            if (enPassantPawn!!.y - 1 in 0..7 &&
                board[enPassantPawn!!.x][enPassantPawn!!.y - 1].color == color ||
                enPassantPawn!!.y + 1 in 0..7 &&
                board[enPassantPawn!!.x][enPassantPawn!!.y + 1].color == color) {
                return false
            }
        }
        return true
    }

    private fun gameOver() {
        if (win) {
            val winner = if (turn1) "White" else "Black"
            println("$winner Wins!")
        } else if (draw) {
            println("Stalemate!")
        }
        println("Bye!")
    }

    class Player(val name: String, val color: Color)

    class Pawn(val color: Color, var x: Int = -1, var y: Int = -1) {
        private var startPosition = true

        fun move(move: Int): Boolean {
            val step = abs(x - move)
            if (isImpossibleMove(step)) {
                println("Invalid Input")
                return false
            }
            board[x][y] = Pawn(Color.NULL)
            x = move
            board[x][y] = this
            if (startPosition) {
                startPosition = false
                enPassantPawn = if (step == 2) {
                    this
                } else {
                    null
                }
            } else {
                enPassantPawn = null
            }
            win = if (color == Color.WHITE) move == 0 else move == 7
            return true
        }

        private fun isImpossibleMove(step: Int): Boolean {
            val maxStep = if (startPosition) 2 else 1
            return step !in 1..maxStep ||
                    board[x + step * getDirection()][y].color != Color.NULL ||
                    step == 2 && board[x + getDirection()][y].color != Color.NULL
        }

        fun capture(targetX: Int, targetY: Int): Boolean {
            if (isImpossibleCapture(targetX, targetY)) {
                println("Invalid Input")
                return false
            }
            val enemies = if (color == Color.WHITE) blackPawns else whitePawns
            if (board[targetX][targetY].color == Color.NULL) {
                enemies.remove(board[targetX - getDirection()][targetY])
                board[targetX - getDirection()][targetY] = Pawn(Color.NULL)
            }
            enemies.remove(board[targetX][targetY])
            board[x][y] = Pawn(Color.NULL)
            x = targetX
            y = targetY
            board[x][y] = this
            enPassantPawn = null
            startPosition = false
            win = enemies.size == 0
            return true
        }

        private fun isImpossibleCapture(targetX: Int, targetY: Int): Boolean {
            val enemyColor = if (color == Color.WHITE) Color.BLACK else Color.WHITE
            return targetX - x != getDirection() ||
                    abs(targetY - y) != 1 ||
                    board[targetX][targetY].color != enemyColor &&
                    board[targetX - getDirection()][targetY].color != enemyColor ||
                    board[targetX - getDirection()][targetY].color == enemyColor &&
                    board[targetX - getDirection()][targetY] != enPassantPawn
        }

        fun getDirection(): Int {
            return when (color) {
                Color.WHITE -> -1
                Color.BLACK -> 1
                else -> 0
            }
        }

        @Override
        override fun toString(): String {
            return when (color) {
                Color.WHITE -> "W"
                Color.BLACK -> "B"
                else -> " "
            }
        }
    }

    enum class Color {
        WHITE, BLACK, NULL
    }
}

fun main() {
    val game = Game()
    game.start()
}

