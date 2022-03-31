package com.example.anew

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.anew.models.BoardSize
import com.example.anew.models.MemoryCard
import com.squareup.picasso.Picasso
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    /*create a margin between the card view
       use a companion object so as to access the constants
    */

    companion object{
        private const val Margin_Size = 10
        private const val TAG = "MemoryBoardAdapter"
    }


    //notifying the game when the user has clicked the card so as to change the state
    interface CardClickListener{
        fun cardClicked(position: Int)

    }


    //creates  view of the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //setting height and with dynamically

        val cardWidth = parent.width /boardSize.getWidth() - (2 * Margin_Size )
        val cardHeight = parent.height /boardSize.getHeight() - (2 * Margin_Size)
        val cardSideLength = min(cardWidth, cardHeight)


        //also referenced to image selector adaptor
       val view =  LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)

        // setting the view to be cardside length
       val layoutParams =  view.findViewById<CardView>(R.id.cardView).layoutParams
               as ViewGroup.MarginLayoutParams

        layoutParams.height = cardSideLength
        layoutParams.width = cardSideLength
        layoutParams.setMargins(Margin_Size, Margin_Size, Margin_Size, Margin_Size)


       return ViewHolder(view)
    }

    //Taking data at the position and binding to the view holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(position)
    }

    //How many elements are in the recycler view
    override fun getItemCount() = boardSize.numCards

    inner class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //setting  click on the image button
        private val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int) {

            val memoryCard = cards[position]

            //PICASSO
            //use picasso o render image at that url

            if (memoryCard.isFaceUp){
                if (memoryCard.imageUrl != null) {
                    //use picasso
                    Picasso.get().load(memoryCard.imageUrl).into(imageButton)
                }else{
                    imageButton.setImageResource(memoryCard.identifier)
                }
            }else{
            imageButton.setImageResource(R.drawable.ic_launcher_background)
            }
            imageButton.alpha = if (memoryCard.isMatched) .4f else 1.0f
            val colorStateList = if (memoryCard.isMatched)
                ContextCompat.getColorStateList(context,R.color.color_orange)
            else null

            ViewCompat.setBackgroundTintList(imageButton,colorStateList)

            imageButton.setOnClickListener {
                Log.i(TAG, "Clicked on position $position") // log i ->  represent info level
                cardClickListener.cardClicked(position)
            }
        }
    }
}


