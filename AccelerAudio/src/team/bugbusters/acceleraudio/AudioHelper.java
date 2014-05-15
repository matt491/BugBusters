package team.bugbusters.acceleraudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioHelper
{
   AudioTrack track;
   short[] buffer = new short[1024];
 
   public AudioHelper( )
   {
      int minSize =AudioTrack.getMinBufferSize( 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT );        
      track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 
    		  				  minSize, AudioTrack.MODE_STREAM);
      track.play();        
   }	   
 
   public void stop(){
	   track.stop();
	   track.release();
   }
   
   public void writeSamples(short[] samples) 
   {	
      fillBuffer( samples );
      track.write( buffer, 0, buffer.length );
   }
 
   private void fillBuffer( short[] samples )
   {
      if( buffer.length < samples.length )
         buffer = new short[samples.length];
 
      for( int i = 0; i < samples.length; i++ )
         buffer[i] = samples[i];
   }	
   
   
}
