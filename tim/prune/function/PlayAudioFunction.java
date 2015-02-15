package tim.prune.function;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import tim.prune.App;
import tim.prune.GenericFunction;
import tim.prune.data.AudioClip;

/**
 * Class to play the current audio clip
 */
public class PlayAudioFunction extends GenericFunction implements Runnable
{
	/** Audio clip used for playing within java */
	private Clip _clip = null;


	/**
	 * Constructor
	 * @param inApp app object
	 */
	public PlayAudioFunction(App inApp) {
		super(inApp);
	}

	/**
	 * @return name key
	 */
	public String getNameKey() {
		return "function.playaudio";
	}

	/**
	 * Perform function
	 */
	public void begin()
	{
		// Launch new thread if clip isn't currently playing
		if (_clip == null) {
			new Thread(this).start();
		}
	}

	/**
	 * Play the audio in a new thread
	 */
	public void run()
	{
		AudioClip audio = _app.getTrackInfo().getCurrentAudio();
		File audioFile = audio.getFile();
		boolean played = false;
		if (audioFile != null && audioFile.exists() && audioFile.isFile() && audioFile.canRead())
		{
			// First choice is to play using java
			played = playClip(audio);
			// Second choice is to try the Desktop library from java 6, if available
			if (!played) {
				try {
					Class<?> d = Class.forName("java.awt.Desktop");
					d.getDeclaredMethod("open", new Class[] {File.class}).invoke(
						d.getDeclaredMethod("getDesktop").invoke(null), new Object[] {audioFile});
					//above code mimics: Desktop.getDesktop().open(audioFile);
					played = true;
				}
				catch (Exception ignore) {
					played = false;
				}
			}
			// If the Desktop call failed, need to try backup methods
			if (!played)
			{
				// If system looks like a Mac, try open command
				String osName = System.getProperty("os.name").toLowerCase();
				boolean isMacOsx = osName.indexOf("mac os") >= 0 || osName.indexOf("darwin") >= 0;
				if (isMacOsx) {
					String[] command = new String[] {"open", audioFile.getAbsolutePath()};
					try {
						Runtime.getRuntime().exec(command);
						played = true;
					}
					catch (IOException ioe) {}
				}
			}
		}
		else if (audioFile == null && audio.getByteData() != null) {
			// Try to play audio clip using byte array (can't use Desktop or Runtime)
			played = playClip(audio);
		}
		if (!played)
		{
			// If still not worked, show error message
			_app.showErrorMessage(getNameKey(), "error.playaudiofailed");
		}
	}

	/**
	 * Try to play the sound file using built-in java libraries
	 * @param inAudio audio clip to play
	 * @return true if play was successful
	 */
	private boolean playClip(AudioClip inClip)
	{
		boolean success = false;
		AudioInputStream audioInputStream = null;
		_clip = null;
		try
		{
			if (inClip.getFile() != null)
				audioInputStream = AudioSystem.getAudioInputStream(inClip.getFile());
			else if (inClip.getByteData() != null)
				audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(inClip.getByteData()));
			else return false;
			_clip = AudioSystem.getClip();
			_clip.open(audioInputStream);
			// play the clip
			_clip.start();
			_clip.drain();
			success = true;
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + " - " + e.getMessage());
		} finally {
			// close the stream to clean up
			try {
				_clip.close();
				audioInputStream.close();
			} catch (Exception e) {}
			_clip = null;
		}
		return success;
	}

	/**
	 * Try to stop a currently playing clip
	 */
	public void stopClip()
	{
		if (_clip != null && _clip.isActive()) {
			try {
				_clip.stop();
				_clip.flush();
			}
			catch (Exception e) {}
		}
	}

	/**
	 * @return percentage of clip currently played, or -1 if not playing
	 */
	public int getPercentage()
	{
		int percent = -1;
		if (_clip != null && _clip.isActive())
		{
			long clipLen = _clip.getMicrosecondLength();
			if (clipLen > 0) {
				percent = (int) (_clip.getMicrosecondPosition() * 100.0 / clipLen);
			}
		}
		return percent;
	}
}
