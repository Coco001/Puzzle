package cqupt.third.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

import cqupt.third.utils.ScreenUtil;

public class GridPicListAdapter extends BaseAdapter {
    private List<Bitmap> picList;
    private Context context;

    public GridPicListAdapter(List<Bitmap> picList, Context context) {
        this.picList = picList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return picList.size();
    }

    @Override
    public Object getItem(int position) {
        return picList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = null;
        int density = (int) ScreenUtil.getDeviceDensity(context);
        if (convertView == null) {
            view = new ImageView(context);
            view.setLayoutParams(new GridView.LayoutParams(80 * density, 100 * density));
            view.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            view = (ImageView) convertView;
        }
        view.setBackgroundColor(Color.BLACK);
        view.setImageBitmap(picList.get(position));

        return view;
    }
}
