package be.ppareit.gameoflife

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class DrawerListAdapter(private val context: Context) : BaseAdapter() {
    private data class Item(val id: Int, val title: String, val iconId: Int)

    private val items: List<Item>

    init {
        val res = context.resources
        val ids = res.obtainTypedArray(R.array.drawer_ids)
        val icons = res.obtainTypedArray(R.array.drawer_icons)
        val titles = res.getStringArray(R.array.drawer_titles)
        items = try {
            titles.indices.map { index ->
                Item(
                    ids.getResourceId(index, -1),
                    titles[index],
                    icons.getResourceId(index, -1),
                )
            }
        } finally {
            ids.recycle()
            icons.recycle()
        }
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.drawer_item, parent, false)
        val item = items[position]
        view.findViewById<ImageView>(R.id.icon).setImageResource(item.iconId)
        view.findViewById<TextView>(R.id.title).text = item.title
        return view
    }
}
