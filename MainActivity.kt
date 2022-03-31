@file:Suppress("DEPRECATION")

package com.example.anew

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anew.models.BoardSize
import com.example.anew.models.MemoryGame
import com.example.anew.models.UserImageList
import com.example.anew.utils.EXTRA_GAME_NAME
import com.example.anew.utils.EXTRA_SIZE
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {




    //lateinit - variables will not be created at the time of initialization || only happen on create
    private lateinit var memoryGame: MemoryGame
    private lateinit var mroot : ConstraintLayout
    private lateinit var rvBoard : RecyclerView
    private lateinit var  moves: TextView
    private lateinit var  pairs :TextView
    private lateinit var adapter: MemoryBoardAdapter

    //setting the game board side dynamically depending on   game option
    private var  boardSize: BoardSize = BoardSize.HARD
    private val db = Firebase.firestore
    private var gameName:String? = null //only set when user is playing a custom games
    private var customGameImages :List<String>? = null

    companion object{
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 123
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //initialize
        rvBoard = findViewById(R.id.recView)
        moves = findViewById(R.id.moves)
        pairs = findViewById(R.id.pairs)
        mroot = findViewById(R.id.mroot)

        //switching up to the custom mode easily

        //explicit intent - launch other activities within the app
        val intent = Intent(this, CustomActivity::class.java)

        intent.putExtra(EXTRA_SIZE, BoardSize.MEDIUM)
        startActivity(intent)

        setUpBoard()

    }

    //inflate layout menu

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    //options for the menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.refresh -> {
                //give user a warning before restarting the game
                if (memoryGame.getMoves() > 0 && !memoryGame.haveWon()) {
                    showAlertDialog("Refresh game", null, View.OnClickListener {
                        setUpBoard()
                    })
                }
                else {
                //restart the game again
                setUpBoard()
                }
                return true
            }

            R.id.size -> {
               showNewSizeDialog()
                return true
            }
            R.id.custom -> {
                showCreationDialog()
                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customName = data?.getStringExtra(EXTRA_GAME_NAME) //retrieve the game name when the game is created
            if (customName == null){
                Log.e(TAG, "Got no custom game name created ")
                return
            }
            downloadgame(customName)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadgame(customName: String) {
        db.collection("games").document(customName).get().addOnSuccessListener {
                document ->
           val userImageList = document.toObject(UserImageList :: class.java)

            if (userImageList?.images == null){
                Log.e(TAG, "Invalid custom game data from firestore")
                Snackbar.make(mroot, "Sorry we could not find such game , $customName", Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }//pass this if block we have found a game successfully


            //setting up recycler iew with custom data

        //how many cards are in the memory gaame
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            customGameImages =userImageList.images
            setUpBoard()
            gameName = customName

        }.addOnFailureListener{
            exception ->
            Log.e(TAG, "Error has ocured" , exception)
        }

    }

    // customizing by adding personal images from galery
    private fun showCreationDialog() {

    // first we need to know the board size to be used hence copy from showsize dialod
        val boardSizeView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_board_size, null)

        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioG)

        showAlertDialog("Create your own board", boardSizeView,View.OnClickListener {
            //set new value for board size

            //only one button can be chosen at a time
            val desiredBoardSize =  when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                R.id.rbHard-> BoardSize.HARD

                else -> BoardSize.EASY
            }
            //navigate user to new activity
            val intent = Intent(this, CustomActivity::class.java)

            intent.putExtra(EXTRA_SIZE, desiredBoardSize) //Extra board size is a constant import it as it will be used as a shared file

            //used startActivityForResult as we want to take the user data back
            startActivityForResult(intent , CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_board_size, null)

        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioG)

        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog("Choose level", boardSizeView,View.OnClickListener {
            //set new value for board size

            //only one button can be chosen at a time
            boardSize =  when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                R.id.rbHard-> BoardSize.HARD

                else -> BoardSize.EASY
            }

            //before reaching the setupboard debugg this 2
            //reset this values when a user goes back to default
            gameName =null
            customGameImages= null

            setUpBoard()// makes sure when u choose it refreshes
        })
    }

    //creating a checking dialog
    private fun showAlertDialog(title : String , view : View?
                                , positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK")
            { _,_ ->
              positiveClickListener.onClick(null)
            }.show()
    }


    private fun setUpBoard() {

        //setting up the game name
        supportActionBar?.title = gameName ?: getString(R.string.app_name)

        when(boardSize){

            BoardSize.EASY -> {
                moves.text = "Easy: 4 * 2"
                pairs.text = "Pairs: 0/4"
            }

            BoardSize.MEDIUM -> {
                moves.text = "Medium: 6 * 3"
                pairs.text = "Pairs: 0/9"
            }

            BoardSize.HARD -> {
                moves.text = "Hard: 6 * 4"
                pairs.text = "Pairs: 0/1"
            }

        }



        //setting up the game logicc

        //setting the color to be default at the start of the game
        pairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))

        //make this property of a class so to be referenced
        memoryGame = MemoryGame(boardSize, customGameImages)

        //recyclerview = contains layout manager and adapter

        //creating adapter
        adapter = MemoryBoardAdapter(this,boardSize , memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun cardClicked(position: Int) {
                    //  Log.i(TAG , "Card clicked $position")

                    //creating a game logic
                    updateGameWithFlip(position)
                }

            }

        ) //context and how many total elements in the grid


        rvBoard.adapter = adapter

        //for performance optimization
        rvBoard.setHasFixedSize(true)


        //creating layout manager
        //grid layout manager creates the grid effect - takes 2 parameters context , columns
        rvBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())//hard coded 2 columns but later shall change to dynamic


        //edge effect - overscroll mode= never

    }



    private fun updateGameWithFlip(position: Int) {

        //Error management
        if (memoryGame.haveWon()){
            //Alert user of an invalid move

           Snackbar.make(mroot,"you already won",Snackbar.LENGTH_LONG).show()
            return
        }

        if (memoryGame.isCardFaceUp(position)){
            //alert the user invalid move
                Snackbar.make(mroot,"Invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }



        //for this function created memory game and adapter parameters

      if(  memoryGame.flipCard(position)){
          Log.i(TAG, "Match ${memoryGame.pairsFound}")

          //setting up colour for the test view to view progress

          //shows progress at teh ex view as  color from red - blue
          val color = ArgbEvaluator().evaluate(
              memoryGame.pairsFound.toFloat() / boardSize.getPairs(),
              ContextCompat.getColor(this, R.color.color_progress_none),
              ContextCompat.getColor(this, R.color.color_progress_full)
          )as Int


          pairs.setTextColor(color)

          //updating text views to show progress of the game
          pairs.text = "pairs: ${memoryGame.pairsFound}/${boardSize.getPairs()}"
          if (memoryGame.haveWon()){
              Snackbar.make(mroot, "You have won the game", Snackbar.LENGTH_SHORT).show()
          }
      }

        //number of moves user has made
        moves.text = "Moves: ${memoryGame.getMoves()}"

        //changes that occur in position of the card
        adapter.notifyDataSetChanged()

    }
}




