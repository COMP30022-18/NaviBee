package au.edu.unimelb.eng.navibee

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import au.edu.unimelb.eng.navibee.utils.SimpleRVViewHolder
import kotlinx.android.synthetic.main.activity_sandbox.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.matchParent
import java.util.*

class SandboxActivity : AppCompatActivity() {


    // Recycler view
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>
    private lateinit var viewManager: androidx.recyclerview.widget.RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sandbox)

        viewManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        viewAdapter = SandboxRVAdaptor()

        recyclerView = sandbox_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }
    }

}

private class SandboxRVAdaptor: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val SIZE = 1000
        private const val IMG_SIZE = 300
        private const val TIMEOUT = 2000
        private val random = Random()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val txt = TextView(parent.context)
        txt.layoutParams = ViewGroup.LayoutParams(
            matchParent,
            IMG_SIZE
        )
        txt.textSize = 60f
        txt.gravity = Gravity.CENTER
        txt.setTextColor(0xffffffff.toInt())
        return SimpleRVViewHolder(txt)
    }

    override fun getItemCount(): Int = SIZE

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val txt = holder.itemView as TextView
        if (txt.tag is Job) {
            (txt.tag as Job).cancel()
        }
        txt.backgroundColor = 0xffffffff.toInt()
        txt.text = ""
        txt.tag = launch(UI) {
            delay(random.nextInt() % TIMEOUT)
            txt.text = "$position"
            txt.backgroundColor = Color.HSVToColor(floatArrayOf(position / SIZE.toFloat() * 360f, 0.99f, 0.61f))
            txt.tag = null
        }
    }
}