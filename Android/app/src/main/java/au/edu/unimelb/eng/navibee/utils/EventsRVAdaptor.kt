package au.edu.unimelb.eng.navibee.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.edu.unimelb.eng.navibee.R
import kotlinx.android.synthetic.main.recycler_view_error_message.view.*
import kotlinx.android.synthetic.main.recycler_view_event_list_divider.view.*
import kotlinx.android.synthetic.main.recycler_view_event_list_entry.view.*

class EventsRVAdaptor(private val data: ArrayList<EventRVItem>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val layout = when (viewType) {
            1 -> R.layout.recycler_view_event_list_divider
            2 -> R.layout.recycler_view_event_list_entry
            3 -> R.layout.recycler_view_indefinite_progress
            4 -> R.layout.recycler_view_error_message
            else -> R.layout.recycler_view_indefinite_progress
        }
        return SimpleRVViewHolder(LayoutInflater.from(parent.context)
                .inflate(layout, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is EventRVDivider -> 1
            is EventRVEntry -> 2
            is EventRVIndefiniteProgressBar -> 3
            is EventRVErrorMessage -> 4
            else -> 0
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val data = data[position]
        when (data) {
            is EventRVDivider -> {
                holder.itemView.recycler_view_events_list_divider_caption.text = data.text
            }
            is EventRVErrorMessage -> {
                holder.itemView.recycler_view_error_message_text_view.text = data.text
            }
            is EventRVEntry -> {
                holder.itemView.recycler_view_events_list_entry_title.text = data.name
                holder.itemView.recycler_view_events_list_entry_subtitle.text = data.location
                holder.itemView.setOnClickListener(data.onClick)
//                holder.itemView.recycler_view_events_list_entry_preview.
                // TODO event image
            }
        }
    }
}

abstract class EventRVItem

class EventRVIndefiniteProgressBar: EventRVItem()
data class EventRVDivider(
        val text: CharSequence
): EventRVItem()
data class EventRVErrorMessage(
        val text: CharSequence
): EventRVItem()
data class EventRVEntry(
        val name: CharSequence,
        val location: CharSequence,
        val onClick: View.OnClickListener
): EventRVItem()

