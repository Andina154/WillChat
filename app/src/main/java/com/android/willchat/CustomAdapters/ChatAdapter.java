package com.android.willchat.CustomAdapters;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.willchat.ChatActivity;
import com.android.willchat.PlayVideoActivity;
import com.android.willchat.R;
import com.android.willchat.ViewImageActivity;
import com.android.willchat.Entities.Message;
import com.android.willchat.util.FileUtilities;

public class ChatAdapter extends BaseAdapter {
	private List<Message> listMessage;
	private LayoutInflater inflater;
	public static Bitmap bitmap;
	private Context mContext;
	private HashMap<String,Bitmap> mapThumb;
	public TextView chatName;
	public TextView text;
	public ImageView image;
	public RelativeLayout relativeLayout;
	public ImageView audioPlayer;
	public ImageView videoPlayer;
	public ImageView videoPlayerButton;
	public ImageView fileSavedIcon;
	public TextView fileSaved;
	public TextView time;
	boolean aku = false;
	String name1 = "";

	public ChatAdapter(Context context, List<Message> listMessage){
		this.listMessage = listMessage;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mapThumb = new HashMap<String, Bitmap>();
	}

	@Override
	public int getCount() {
		return listMessage.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View view, ViewGroup parent) {
		//View view = convertView;

		Message mes = listMessage.get(position);
		int type = mes.getmType();
		
			if(listMessage.get(position).isMine()){
				view = inflater.inflate(R.layout.chat_row, null);
			}
			else{
				view = inflater.inflate(R.layout.chat_row_in, null);
			}

			chatName = (TextView) view.findViewById(R.id.chatName);
			text = (TextView) view.findViewById(R.id.text);
			image = (ImageView) view.findViewById(R.id.image);
			relativeLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout);
			audioPlayer = (ImageView) view.findViewById(R.id.playAudio);
			videoPlayer = (ImageView) view.findViewById(R.id.playVideo);
			fileSaved = (TextView) view.findViewById(R.id.fileSaved);
			videoPlayerButton = (ImageView) view.findViewById(R.id.buttonPlayVideo);
			fileSavedIcon = (ImageView) view.findViewById(R.id.file_attached_icon);
			time = (TextView) view.findViewById(R.id.time);
			//view.setTag(cache);

		//Retrive the items from cache
		chatName.setText(listMessage.get(position).getChatName());

		//We disable all the views and enable certain views depending on the message's type
		disableAllMediaViews();

		Date now = new Date();
		String chatTime = new SimpleDateFormat("h:mm a").format(now);
		time.setText(chatTime);

		/***********************************************
		 Text Message
		 ***********************************************/
		if(type == Message.TEXT_MESSAGE){
			enableTextView(mes.getmText());
		}

		/***********************************************
		 Image Message
		 ***********************************************/
		else if(type == Message.IMAGE_MESSAGE){
			enableTextView(mes.getmText());
			image.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);

			if(!mapThumb.containsKey(mes.getFileName())){
				Bitmap thumb = mes.byteArrayToBitmap(mes.getByteArray());
				mapThumb.put(mes.getFileName(), thumb);
			}
			image.setImageBitmap(mapThumb.get(mes.getFileName()));
			image.setTag(position);

			image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message mes = listMessage.get((Integer) v.getTag());
					bitmap = mes.byteArrayToBitmap(mes.getByteArray());

					Intent intent = new Intent(mContext, ViewImageActivity.class);
					String fileName = mes.getFileName();
					intent.putExtra("fileName", fileName);

					mContext.startActivity(intent);
				}
			});
		}

		/***********************************************
		 Audio Message
		 ***********************************************/
		else if(type == Message.AUDIO_MESSAGE){
			enableTextView(mes.getmText());
			audioPlayer.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);
			audioPlayer.setTag(position);
			audioPlayer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					MediaPlayer mPlayer = new MediaPlayer();
					Message mes = listMessage.get((Integer) v.getTag());
					try {
						mPlayer.setDataSource(mes.getFilePath());
						mPlayer.prepare();
						mPlayer.start();

						//Disable the button when the audio is playing
						v.setEnabled(false);
						((ImageView)v).setImageDrawable(mContext.getResources().getDrawable(R.drawable.play_audio_in_progress));

						mPlayer.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								//Re-enable the button when the audio has finished playing
								v.setEnabled(true);
								((ImageView)v).setImageDrawable(mContext.getResources().getDrawable(R.drawable.play_audio));
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			});
		}

		/***********************************************
		 Video Message
		 ***********************************************/
		else if(type == Message.VIDEO_MESSAGE){
			enableTextView(mes.getmText());
			videoPlayer.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);
			videoPlayerButton.setVisibility(View.VISIBLE);

			if(!mapThumb.containsKey(mes.getFilePath())){
				Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mes.getFilePath(), Thumbnails.MINI_KIND);
				mapThumb.put(mes.getFilePath(), thumb);
			}
			videoPlayer.setImageBitmap(mapThumb.get(mes.getFilePath()));

			videoPlayerButton.setTag(position);
			videoPlayerButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message mes = listMessage.get((Integer) v.getTag());
					Intent intent = new Intent(mContext, PlayVideoActivity.class);
					intent.putExtra("filePath", mes.getFilePath());
					mContext.startActivity(intent);
				}
			});
		}

		/***********************************************
		 File Message
		 ***********************************************/
		else if(type == Message.FILE_MESSAGE){
			enableTextView(mes.getmText());
			fileSavedIcon.setVisibility(View.VISIBLE);
			fileSaved.setVisibility(View.VISIBLE);
			fileSaved.setText(mes.getFileName());
		}

		/***********************************************
		 Drawing Message
		 ***********************************************/
		else if(type == Message.DRAWING_MESSAGE){
			enableTextView(mes.getmText());
			image.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);

			if(!mapThumb.containsKey(mes.getFileName())){
				Bitmap thumb = FileUtilities.getBitmapFromFile(mes.getFilePath());
				mapThumb.put(mes.getFileName(), thumb);
			}
			image.setImageBitmap(mapThumb.get(mes.getFileName()));
			image.setTag(position);

			image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message mes = listMessage.get((Integer) v.getTag());
					bitmap = mes.byteArrayToBitmap(mes.getByteArray());

					Intent intent = new Intent(mContext, ViewImageActivity.class);
					String fileName = mes.getFileName();
					intent.putExtra("fileName", fileName);

					mContext.startActivity(intent);
				}
			});
		}

		return view;
	}

	private void disableAllMediaViews(){
		text.setVisibility(View.GONE);
		image.setVisibility(View.GONE);
		audioPlayer.setVisibility(View.GONE);
		videoPlayer.setVisibility(View.GONE);
		fileSaved.setVisibility(View.GONE);
		videoPlayerButton.setVisibility(View.GONE);
		fileSavedIcon.setVisibility(View.GONE);
		time.setVisibility(View.GONE);
	}

	private void enableTextView(String message){
		if(!text.equals("")){
			time.setVisibility(View.VISIBLE);
			text.setVisibility(View.VISIBLE);
			text.setText(message);
			Linkify.addLinks(text, Linkify.PHONE_NUMBERS);
			Linkify.addLinks(text, Patterns.WEB_URL, "myweburl:"); //xxx
		}
	}
}
