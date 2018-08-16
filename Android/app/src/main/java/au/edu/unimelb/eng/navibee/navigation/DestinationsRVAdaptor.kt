package au.edu.unimelb.eng.navibee.navigation

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.edu.unimelb.eng.navibee.R
import kotlinx.android.synthetic.main.recycler_view_destination_list_button.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_divider.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_entry.view.*

class DestinationsRVAdaptor(private val dataset: ArrayList<DestinationRVItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class DividerViewHolder(view: ConstraintLayout) :
            RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            1 -> R.layout.recycler_view_destination_list_divider
            2 -> R.layout.recycler_view_destination_list_entry
            3 -> R.layout.recycler_view_destination_list_button
            else -> R.layout.recycler_view_destination_list_divider
        }
        return DividerViewHolder(LayoutInflater.from(parent.context)
                    .inflate(layout, parent, false) as ConstraintLayout)
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position]) {
            is DestinationRVDivider -> 1
            is DestinationRVEntry -> 2
            is DestinationRVButton -> 3
            else -> 0
        }
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataset[position]
        when (data) {
            is DestinationRVDivider -> {
                holder.itemView.recycler_view_destinations_list_divider_caption.text = data.text
            }
            is DestinationRVButton -> {
                holder.itemView.recycler_view_destinations_list_button_button.text = data.text
                // Set Icon to the button to "Start"
                holder.itemView.recycler_view_destinations_list_button_button.setCompoundDrawablesRelativeWithIntrinsicBounds(data.icon, 0, 0, 0)
                holder.itemView.recycler_view_destinations_list_button_button.setOnClickListener(data.onClick)
            }
            is DestinationRVEntry -> {
                holder.itemView.recycler_view_destinations_list_divider_caption_entry_title.text = data.name
                holder.itemView.recycler_view_destinations_list_divider_caption_entry_subtitle.text = data.location
                // TODO: Load image.
                holder.itemView.setOnClickListener(data.onClick)
            }
        }
        // holder.itemView.text = dataset[position]
    }
}

abstract class DestinationRVItem

data class DestinationRVDivider(val text: String): DestinationRVItem()
data class DestinationRVEntry(val name: String,
                              val location: String,
                              val thumbnail: String,
                              val onClick: View.OnClickListener): DestinationRVItem()
data class DestinationRVButton(val text: String,
                               val icon: Int,
                               val onClick: View.OnClickListener): DestinationRVItem()
