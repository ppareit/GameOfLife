package be.ppareit.gameoflife

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.util.ArrayList

import static extension be.ppareit.android.AndroidUtils.*

class DrawerListAdapter extends BaseAdapter {

    Context mContext

    static class Item {
        int id
        String title
        int iconId
    }
    private ArrayList<Item> mItems

    public new(Context context) {
        mContext = context
        mItems = new ArrayList<Item>()

        val res = mContext.resources
        val ids = res.obtainTypedArray(R.array.drawer_ids)
        val titles = res.getStringArray(R.array.drawer_titles)
        val icons = res.obtainTypedArray(R.array.drawer_icons)

        for (i : 0..titles.size-1) {
            var item = new Item()
            item.id = ids.getResourceId(i, -1)
            item.title = titles.get(i)
            item.iconId = icons.getResourceId(i, -1)
            mItems.add(item)
        }
    }

    override getCount() {
        return mItems.size
    }

    override getItem(int position) {
        return mItems.get(position)
    }

    override getItemId(int position) {
        return mItems.get(position).id
    }

    override getView(int position, View convertView, ViewGroup parent) {
        var View view
        if (convertView == null) {
            var mInflater = mContext.getSystemService(LayoutInflater)
            view = mInflater.inflate(R.layout.drawer_item, null)
        } else {
            view = convertView
        }

        var iconView = view.findView(R.id.icon) as ImageView
        iconView.imageResource = mItems.get(position).iconId

        var titleView = view.findView(R.id.title) as TextView
        titleView.text = mItems.get(position).title

        return view
    }

}