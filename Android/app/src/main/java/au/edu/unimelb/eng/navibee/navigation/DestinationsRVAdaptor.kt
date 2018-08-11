package au.edu.unimelb.eng.navibee.navigation

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import au.edu.unimelb.eng.navibee.R
import kotlinx.android.synthetic.main.recycler_view_destination_list_divider.view.*
import java.net.URL

class DestinationsRVAdaptor(private val dataset: ArrayList<DestinationRVItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class DividerViewHolder(view: ConstraintLayout) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> DividerViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_destination_list_divider,
                            parent, false) as ConstraintLayout)
            else -> DividerViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_destination_list_divider,
                            parent, false) as ConstraintLayout)
        }
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
        when (holder) {
            is DividerViewHolder -> {
                holder.itemView.caption.text = (dataset[position] as DestinationRVDivider).text
            }
        }
        // holder.itemView.text = dataset[position]
    }

}

abstract class DestinationRVItem()

data class DestinationRVDivider(val text: String): DestinationRVItem()
data class DestinationRVEntry(val name: String,
                              val location: String,
                              val thumbnail: URL): DestinationRVItem()
data class DestinationRVButton(val text: String,
                               val icon: String,
                               val id: String): DestinationRVItem()
