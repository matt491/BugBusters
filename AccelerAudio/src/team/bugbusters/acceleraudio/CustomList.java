package team.bugbusters.acceleraudio;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String[]> {

	private final Activity context;
	private List<String[]> toFill;
	
	static class ViewHolder {
		public ImageView thumbnail;
		public TextView nameText;
		public TextView lastText;
		public TextView durationText;
	}
	
	public CustomList(Activity context, List<String[]> toFill) {
		super(context, R.layout.row, toFill);
		this.context = context;
		this.toFill = toFill;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		
		if(rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.row, null);
			
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.thumbnail = (ImageView) rowView.findViewById(R.id.thubnail);
			viewHolder.nameText = (TextView) rowView.findViewById(R.id.name);
			viewHolder.lastText = (TextView) rowView.findViewById(R.id.dateLastModified);
			viewHolder.durationText = (TextView) rowView.findViewById(R.id.duration);
			rowView.setTag(viewHolder);
		}
		
		ViewHolder holder = (ViewHolder) rowView.getTag();
		String[] s = toFill.get(position);
		int alpha = Integer.parseInt(s[1].substring(0));
		int red = Integer.parseInt(s[1].substring(0, 3));
		int green = Integer.parseInt(s[1].substring(3, 6));
		int blue = Integer.parseInt(s[1].substring(6, 9));
		holder.thumbnail.setBackgroundColor(Color.argb(alpha, red, green, blue));
		holder.nameText.setText(s[2]);
		holder.lastText.setText(s[3]);
		holder.durationText.setText(s[4]);
		
		return rowView;
	}	

}
