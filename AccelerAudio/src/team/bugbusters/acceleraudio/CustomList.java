package team.bugbusters.acceleraudio;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
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
	
	public List<String[]> getList(){
		return this.toFill;
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
		int alpha = Integer.parseInt(s[1].substring(0, 3));
		int red = Integer.parseInt(s[1].substring(3, 6));
		int green = Integer.parseInt(s[1].substring(6, 9));
		int blue = Integer.parseInt(s[1].substring(9, 12));
		holder.thumbnail.setBackgroundColor(Color.argb(alpha, red, green, blue));
		
		switch(Integer.parseInt(s[1].substring(11))) {
		case 0:
			holder.thumbnail.setImageResource(R.drawable.ic_music_0);
			break;
		case 1:
			holder.thumbnail.setImageResource(R.drawable.ic_music_1);
			break;
		case 2:
			holder.thumbnail.setImageResource(R.drawable.ic_music_2);
			break;
		case 3:
			holder.thumbnail.setImageResource(R.drawable.ic_music_3);
			break;
		case 4:
			holder.thumbnail.setImageResource(R.drawable.ic_music_4);
			break;
		case 5: 
			holder.thumbnail.setImageResource(R.drawable.ic_music_5);
			break;
		case 6:
			holder.thumbnail.setImageResource(R.drawable.ic_music_6);
			break;
		case 7:
			holder.thumbnail.setImageResource(R.drawable.ic_music_7);
			break;
		case 8:
			holder.thumbnail.setImageResource(R.drawable.ic_music_8);
			break;
		case 9:
			holder.thumbnail.setImageResource(R.drawable.ic_music_9);
			break;
		}
		
		holder.nameText.setText(s[2]);
		holder.lastText.setText(s[3]);
		float dur =Float.parseFloat(s[4])/1000;
		holder.durationText.setText(String.format("%.2fs", dur));
		
		return rowView;
	}
}
