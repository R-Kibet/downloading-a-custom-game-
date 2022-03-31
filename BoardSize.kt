package com.example.anew.models

enum class BoardSize (val numCards:Int){

    //classifying the options of games
    EASY(8),
    MEDIUM(18),
    HARD(24);

    //how to know the difficulty level when downloading a game
    companion object{
        fun getByValue(value: Int) = values().first { it.numCards == value }
    }

    //info of width and height corresponding to the game
    fun getWidth(): Int{
        return when (this) {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight():Int{
        return numCards / getWidth()
    }

    //how many pairs of cards are there
    fun getPairs() : Int{
        return numCards / 2
    }
}