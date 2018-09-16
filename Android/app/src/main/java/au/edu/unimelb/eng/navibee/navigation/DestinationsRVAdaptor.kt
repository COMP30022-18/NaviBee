package au.edu.unimelb.eng.navibee.navigation

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.DownloadImageToImageViewAsyncTask
import au.edu.unimelb.eng.navibee.utils.SimpleRVViewHolder
import kotlinx.android.synthetic.main.recycler_view_attributions.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_button.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_divider.view.*
import kotlinx.android.synthetic.main.recycler_view_destination_list_entry.view.*
import kotlinx.android.synthetic.main.recycler_view_error_message.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class DestinationsRVAdaptor(private val data: ArrayList<DestinationRVItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            1 -> R.layout.recycler_view_destination_list_divider
            2 -> R.layout.recycler_view_destination_list_entry
            3 -> R.layout.recycler_view_destination_list_button
            4 -> R.layout.recycler_view_indefinite_progress
            5 -> R.layout.recycler_view_error_message
            6 -> R.layout.recycler_view_attributions
            else -> R.layout.recycler_view_indefinite_progress
        }
        return SimpleRVViewHolder(LayoutInflater.from(parent.context)
                    .inflate(layout, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is DestinationRVDivider -> 1
            is DestinationRVEntry -> 2
            is DestinationRVButton -> 3
            is DestinationRVIndefiniteProgressBar -> 4
            is DestinationRVErrorMessage -> 5
            is DestinationRVAttributions -> 6
            else -> 0
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = data[position]
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
                holder.itemView.recycler_view_destinations_list_button_button.setIconResource(data.icon)
                holder.itemView.recycler_view_destinations_list_button_button.setOnClickListener(data.onClick)
            }
            is DestinationRVEntry -> {
                holder.itemView.recycler_view_destinations_list_entry_title.text = data.name
                holder.itemView.recycler_view_destinations_list_entry_subtitle.text = data.location
                holder.itemView.recycler_view_destinations_list_entry_preview.visibility = View.GONE
                val imageView = holder.itemView.recycler_view_destinations_list_entry_preview
                when {
                    data.thumbnail != null ->
                        DownloadImageToImageViewAsyncTask(imageView).execute(data.thumbnail)
                    data.googlePhotoReference != null -> {
                        val viewHeight = Resources.getSystem().displayMetrics.heightPixels
                        GoogleMapsPhotoReferenceCacheImageLoader(
                                data.googlePhotoReference,
                                imageView,
                                viewHeight
                        ).execute("${data.googlePlaceId}-0")
                    }
                    data.googlePlaceId != null -> {
                        val viewHeight = Resources.getSystem().displayMetrics.heightPixels
                        launch(UI) {
                            GoogleMapsPlaceIdCacheImageLoader(
                                    data.googlePlaceId,
                                    imageView,
                                    viewHeight
                                ).execute()
                        }
                    }
                }
                holder.itemView.setOnClickListener(data.onClick)
            }
            is DestinationRVAttributions -> {
                holder.itemView.recycler_view_attribution_text_view.text = data.attributes
            }

        }
    }
}

abstract class DestinationRVItem

class DestinationRVIndefiniteProgressBar: DestinationRVItem()
data class DestinationRVDivider(val text: CharSequence): DestinationRVItem()
data class DestinationRVErrorMessage(val text: CharSequence): DestinationRVItem()
data class DestinationRVEntry(val name: CharSequence,
                              val location: CharSequence,
                              val thumbnail: String? = null,
                              val wikiData: String? = null,
                              val googlePhotoReference: String? = null,
                              val googlePlaceId: String? = null,
                              val onClick: View.OnClickListener): DestinationRVItem()
data class DestinationRVButton(val text: CharSequence,
                               val icon: Int,
                               val onClick: View.OnClickListener): DestinationRVItem()
data class DestinationRVAttributions(val attributes: CharSequence): DestinationRVItem()
