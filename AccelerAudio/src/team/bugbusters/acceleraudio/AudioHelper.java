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
      track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, 
                                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 
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
   
   public void writeSamples(short[] s, short[] p, short[] q ) 
   {	
      fillBuffer( s,p,q );
      track.write( buffer, 0, buffer.length );
   }
   
   private void fillBuffer( short[] s, short[] p, short[] q ){
	   if( buffer.length < s.length+p.length+q.length )
	         buffer = new short[s.length+p.length+q.length];
	   int j=0;
	   for(int i=0;i < s.length; i++) { buffer[j] = s[i]; j++;}
	   for(int i=0; i < p.length; i++) { buffer[j] = p[i]; j++;}
	   for(int i=0; i < q.length; i++) { buffer[j] = q[i]; j++;}
	   
	   
   }
   
}
