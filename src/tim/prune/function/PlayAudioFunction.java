package tim.prune.function;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
			// If this didn't work, then try to play the file another way
			if (!played) {
				played = playAudioFile(audioFile);
			}
		}
		else if (audioFile == null && audio.getByteData() != null)
		{
			// Try to play audio clip using byte array
			played = playClip(audio);
			// If this didn't work, then need to copy the byte data to a file and play it from there
			if (!played)
			{
				try
				{
					String suffix = getSuffix(audio.getName());
					File tempFile = File.createTempFile("gpsaudio", suffix);
					tempFile.deleteOnExit();
					// Copy byte data to this file
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
					bos.write(audio.getByteData(), 0, audio.getByteData().length);
					bos.close();
					played = playAudioFile(tempFile);
				}
				catch (IOException ignore) {
					System.err.println("Error: " + ignore.getClass().getName() + " - " + ignore.getMessage());
				}
			}
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
	 * Try to play the specified audio file
	 * @param inFile file to play
	 * @return true if play was successful
	 */
	private boolean playAudioFile(File inFile)
	{
		boolean played = false;
		// Try the Desktop library from java 6, if available
		if (!played)
		{
			try
			{
				Desktop.getDesktop().open(inFile);
				played = true;
			}
			catch (IOException ignore) {
				System.err.println(ignore.getClass().getName() + " - " + ignore.getMessage());
				played = false;
			}
		}

		// If the Desktop call failed, need to try backup methods
		if (!played)
		{
			// If system looks like a Mac, try the open command
			String osName = System.getProperty("os.name").toLowerCase();
			boolean isMacOsx = osName.indexOf("mac os") >= 0 || osName.indexOf("darwin") >= 0;
			if (isMacOsx)
			{
				String[] command = new String[] {"open", inFile.getAbsolutePath()};
				try {
					Runtime.getRuntime().exec(command);
					played = true;
				}
				catch (IOException ioe) {}
			}
		}
		return played;
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

	/**
	 * @param inName name of audio file
	 * @return suffix (rest of name after the dot) - expect mp3, wav, ogg
	 */
	private static final String getSuffix(String inName)
	{
		if (inName == null || inName.equals("")) {return ".tmp";}
		final int dotPos = inName.lastIndexOf('.');
		if (dotPos < 0) {return inName;} // no dot found
		return inName.substring(dotPos);
	}
}
