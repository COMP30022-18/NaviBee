package au.edu.unimelb.eng.navibee.navigation

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.DownloadImageToImageViewAsyncTask
import kotlinx.android.synthetic.main.recycler_view_destination_list_attributions.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_button.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_divider.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_entry.view.*
import kotlinx.android.synthetic.main.recycler_view_error_message.view.*

class DestinationsRVAdaptor(private val dataset: ArrayList<DestinationRVItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class DividerViewHolder(view: View) :
            RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            1 -> R.layout.recycler_view_destination_list_divider
            2 -> R.layout.recycler_view_destination_list_entry
            3 -> R.layout.recycler_view_destination_list_button
            4 -> R.layout.recycler_view_indefinite_progress
            5 -> R.layout.recycler_view_error_message
            6 -> R.layout.recycler_view_destination_list_attributions
            else -> R.layout.recycler_view_indefinite_progress
        }
        return DividerViewHolder(LayoutInflater.from(parent.context)
                    .inflate(layout, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position]) {
            is DestinationRVDivider -> 1
            is DestinationRVEntry -> 2
            is DestinationRVButton -> 3
            is DestinationRVIndefiniteProgressBar -> 4
            is DestinationRVErrorMessage -> 5
            is DestinationRVAttributes -> 6
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
            is DestinationRVErrorMessage -> {
                holder.itemView.recycler_view_error_message_text_view.text = data.text
            }
            is DestinationRVButton -> {
                holder.itemView.recycler_view_destinations_list_button_button.text = data.text
                // Set Icon to the button to "Start"
                holder.itemView.recycler_view_destinations_list_button_button.setCompoundDrawablesRelativeWithIntrinsicBounds(data.icon, 0, 0, 0)
                holder.itemView.recycler_view_destinations_list_button_button.setOnClickListener(data.onClick)
            }
            is DestinationRVEntry -> {
                holder.itemView.recycler_view_destinations_list_entry_title.text = data.name
                holder.itemView.recycler_view_destinations_list_entry_subtitle.text = data.location
                holder.itemView.recycler_view_destinations_list_entry_preview.visibility = View.GONE
                when {
                    data.thumbnail != null ->
                        DownloadImageToImageViewAsyncTask(holder.itemView.recycler_view_destinations_list_entry_preview).execute(data.thumbnail)
                    data.googlePhotoReference != null -> {
                        val viewHeight = Resources.getSystem().displayMetrics.heightPixels
                        LoadImageFromGoogleMapsByPhotoReferenceToImageView(
                                data.googlePhotoReference,
                                holder.itemView.recycler_view_destinations_list_entry_preview,
                                viewHeight
                        ).execute()
                    }
                    data.wikiData != null ->
                        DownloadImageFromWikiDataToImageViewAsyncTask(holder.itemView.recycler_view_destinations_list_entry_preview).execute(data.wikiData)
                }
                holder.itemView.setOnClickListener(data.onClick)
            }
            is DestinationRVAttributes -> {
                holder.itemView.recycler_view_attribution_text_view.text = data.attributes
            }
        }
    }
}

abstract class DestinationRVItem

class DestinationRVIndefiniteProgressBar: DestinationRVItem()
data class DestinationRVDivider(val text: String): DestinationRVItem()
data class DestinationRVErrorMessage(val text: String): DestinationRVItem()
data class DestinationRVEntry(val name: String,
                              val location: String,
                              val thumbnail: String? = null,
                              val wikiData: String? = null,
                              val googlePhotoReference: String? = null,
                              val onClick: View.OnClickListener): DestinationRVItem()
data class DestinationRVButton(val text: String,
                               val icon: Int,
                               val onClick: View.OnClickListener): DestinationRVItem()
data class DestinationRVAttributes(val attributes: CharSequence): DestinationRVItem()
