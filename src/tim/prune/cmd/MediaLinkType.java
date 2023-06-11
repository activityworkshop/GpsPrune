package tim.prune.cmd;

/**
 * Enum for photos, audios or both
 */
public enum MediaLinkType
{
	LINK_PHOTOS, LINK_AUDIOS, LINK_BOTH;

	public boolean handlePhotos() {
		return this == LINK_PHOTOS || this == LINK_BOTH;
	}

	public boolean handleAudios() {
		return this == LINK_AUDIOS || this == LINK_BOTH;
	}
}
