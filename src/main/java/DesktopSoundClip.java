import jouvieje.bass.Bass;
import jouvieje.bass.defines.BASS_ATTRIB;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.BufferUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import java.applet.AudioClip;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static jouvieje.bass.Bass.BASS_ErrorGetCode;
import static jouvieje.bass.defines.BASS_ACTIVE.BASS_ACTIVE_PLAYING;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;

/**
 * An implementation of AudioClip, optimized for desktop apps.
 * The Sun-provided AudioClip for Applets is a bit buggy.
 * @author DragShot
 */
public class DesktopSoundClip implements AudioClip {

    Clip clip = null;
    AudioInputStream sound;
    boolean loaded = false;
    int lfrpo = -1;
    int cntcheck = 0;

    /**
     * Creates an unloaded, empty SoundClip.
     */
    public DesktopSoundClip() {
        System.out.println("Making empty soundclip!");
        new Throwable().printStackTrace();
    }

    private HSTREAM _chan;

    private static List<DesktopSoundClip> SoundPool = new ArrayList<>();

    public DesktopSoundClip(byte[] is)
    {
        boolean isLooping = false;
        if ((_chan = Bass.BASS_StreamCreateFile(true, BufferUtils.copyByteBuffer(ByteBuffer.wrap(is)), 0, is.length, isLooping ? BASS_SAMPLE_LOOP : 0)) == null)
        {
            // it ain't playable
            throw new RuntimeException(getBassError(BASS_ErrorGetCode()));
        }
        SoundPool.add(this);
    }

    public void play()
    {
        if (Bass.BASS_ChannelIsActive(_chan.asInt()) == BASS_ACTIVE_PLAYING) return;
        // https://github.com/ManagedBass/ManagedBass/blob/bcd197f35d7d5a495a226be5a93643674df80b92/src/Bass/Shared/Bass/PInvoke/Channels.cs#L396
        // remove loop flag
        Bass.BASS_ChannelFlags(_chan.asInt(), 0, BASS_SAMPLE_LOOP);
        Bass.BASS_ChannelPlay(_chan.asInt(), false);//TODO see restart parameter
    }

    public void checkopen()
    {
    }

    public void loop()
    {
        if (Bass.BASS_ChannelIsActive(_chan.asInt()) == BASS_ACTIVE_PLAYING) return;
        // https://github.com/ManagedBass/ManagedBass/blob/bcd197f35d7d5a495a226be5a93643674df80b92/src/Bass/Shared/Bass/PInvoke/Channels.cs#L389
        // add loop flag
        Bass.BASS_ChannelFlags(_chan.asInt(), BASS_SAMPLE_LOOP, BASS_SAMPLE_LOOP);
        Bass.BASS_ChannelPlay(_chan.asInt(), false);//TODO see restart parameter
    }

    public void stop()
    {
        if (Bass.BASS_ChannelIsActive(_chan.asInt()) != BASS_ACTIVE_PLAYING) return;
        Bass.BASS_ChannelStop(_chan.asInt());
    }

    public static void stopAll()
    {
        for (DesktopSoundClip s : SoundPool)
        {
            s.stop();
        }
    }

    public static void setAllVolumes(float vol)
    {
        for (DesktopSoundClip s : SoundPool)
        {
            Bass.BASS_ChannelSetAttribute(s._chan.asInt(), BASS_ATTRIB.BASS_ATTRIB_VOL, vol);
        }
    }

    public static String getBassError(int error)
    {
        int errCode = error;
        String errStr;
        String errDesc;

        switch (errCode)
        {
            case 0:
                errStr = "BASS_OK";
                errDesc = "All is OK";
                break;
            case 1:
                errStr = "BASS_ERROR_MEM";
                errDesc = "Memory error";
                break;
            case 2:
                errStr = "BASS_ERROR_FILEOPEN";
                errDesc = "Can't open the file";
                break;
            case 3:
                errStr = "BASS_ERROR_DRIVER";
                errDesc = "Can't find a free/valid driver";
                break;
            case 4:
                errStr = "BASS_ERROR_BUFLOST";
                errDesc = "The sample buffer was lost";
                break;
            case 5:
                errStr = "BASS_ERROR_HANDLE";
                errDesc = "Invalid handle";
                break;
            case 6:
                errStr = "BASS_ERROR_FORMAT";
                errDesc = "Unsupported sample format";
                break;
            case 7:
                errStr = "BASS_ERROR_POSITION";
                errDesc = "Invalid playback position";
                break;
            case 8:
                errStr = "BASS_ERROR_INIT";
                errDesc = "BASS_Init has not been successfully called";
                break;
            case 9:
                errStr = "BASS_ERROR_START";
                errDesc = "BASS_Start has not been successfully called";
                break;
            case 12:
                errStr = "BASS_ERROR_NOCD";
                errDesc = "No CD in drive";
                break;
            case 13:
                errStr = "BASS_ERROR_CDTRACK";
                errDesc = "Invalid track number";
                break;
            case 14:
                errStr = "BASS_ERROR_ALREADY";
                errDesc = "Already initialized/paused/whatever";
                break;
            case 16:
                errStr = "BASS_ERROR_NOPAUSE";
                errDesc = "Not paused";
                break;
            case 17:
                errStr = "BASS_ERROR_NOTAUDIO";
                errDesc = "Not an audio track";
                break;
            case 18:
                errStr = "BASS_ERROR_NOCHAN";
                errDesc = "Can't get a free channel";
                break;
            case 19:
                errStr = "BASS_ERROR_ILLTYPE";
                errDesc = "An illegal type was specified";
                break;
            case 20:
                errStr = "BASS_ERROR_ILLPARAM";
                errDesc = "An illegal parameter was specified";
                break;
            case 21:
                errStr = "BASS_ERROR_NO3D";
                errDesc = "No 3D support";
                break;
            case 22:
                errStr = "BASS_ERROR_NOEAX";
                errDesc = "No EAX support";
                break;
            case 23:
                errStr = "BASS_ERROR_DEVICE";
                errDesc = "Illegal device number";
                break;
            case 24:
                errStr = "BASS_ERROR_NOPLAY";
                errDesc = "Not playing";
                break;
            case 25:
                errStr = "BASS_ERROR_FREQ";
                errDesc = "Illegal sample rate";
                break;
            case 27:
                errStr = "BASS_ERROR_NOTFILE";
                errDesc = "The stream is not a file stream";
                break;
            case 29:
                errStr = "BASS_ERROR_NOHW";
                errDesc = "No hardware voices available";
                break;
            case 31:
                errStr = "BASS_ERROR_EMPTY";
                errDesc = "The MOD music has no sequence data";
                break;
            case 32:
                errStr = "BASS_ERROR_NONET";
                errDesc = "No internet connection could be opened";
                break;
            case 33:
                errStr = "BASS_ERROR_CREATE";
                errDesc = "Couldn't create the file";
                break;
            case 34:
                errStr = "BASS_ERROR_NOFX";
                errDesc = "Effects are not available";
                break;
            case 35:
                errStr = "BASS_ERROR_PLAYING";
                errDesc = "The channel is playing";
                break;
            case 37:
                errStr = "BASS_ERROR_NOTAVAIL";
                errDesc = "Requested data is not available";
                break;
            case 38:
                errStr = "BASS_ERROR_DECODE";
                errDesc = "The channel is a 'decoding channel'";
                break;
            case 39:
                errStr = "BASS_ERROR_DX";
                errDesc = "A sufficient DirectX version is not installed";
                break;
            case 40:
                errStr = "BASS_ERROR_TIMEOUT";
                errDesc = "Connection timedout";
                break;
            case 41:
                errStr = "BASS_ERROR_FILEFORM";
                errDesc = "Unsupported file format";
                break;
            case 42:
                errStr = "BASS_ERROR_SPEAKER";
                errDesc = "Unavailable speaker";
                break;
            case 43:
                errStr = "BASS_ERROR_VERSION";
                errDesc = "Invalid BASS version (used by add-ons)";
                break;
            case 44:
                errStr = "BASS_ERROR_CODEC";
                errDesc = "Codec is not available/supported";
                break;
            case 45:
                errStr = "BASS_ERROR_ENDED";
                errDesc = "The channel/file has ended";
                break;
            case 46:
                errStr = "BASS_ERROR_BUSY";
                errDesc = "The device is busy (eg. in \"exclusive\" use by another process)";
                break;
            case -1:
                errStr = "BASS_ERROR_UNKNOWN";
                errDesc = "Some other mystery error";
                break;
            case 1000:
                errStr = "BASS_ERROR_WMA_LICENSE";
                errDesc = "BassWma: the file is protected";
                break;
            case 1001:
                errStr = "BASS_ERROR_WMA_WM9";
                errDesc = "BassWma: WM9 is required";
                break;
            case 1002:
                errStr = "BASS_ERROR_WMA_DENIED";
                errDesc = "BassWma: access denied (user/pass is invalid)";
                break;
            case 1003:
                errStr = "BASS_ERROR_WMA_CODEC";
                errDesc = "BassWma: no appropriate codec is installed";
                break;
            case 1004:
                errStr = "BASS_ERROR_WMA_INDIVIDUAL";
                errDesc = "BassWma: individualization is needed";
                break;
            case 2000:
                errStr = "BASS_ERROR_ACM_CANCEL";
                errDesc = "BassEnc: ACM codec selection cancelled";
                break;
            case 2100:
                errStr = "BASS_ERROR_CAST_DENIED";
                errDesc = "BassEnc: Access denied (invalid password)";
                break;
            case 3000:
                errStr = "BASS_VST_ERROR_NOINPUTS";
                errDesc = "BassVst: the given effect has no inputs and is probably a VST instrument and no effect";
                break;
            case 3001:
                errStr = "BASS_VST_ERROR_NOOUTPUTS";
                errDesc = "BassVst: the given effect has no outputs";
                break;
            case 3002:
                errStr = "BASS_VST_ERROR_NOREALTIME";
                errDesc = "BassVst: the given effect does not support realtime processing";
                break;
            case 5000:
                errStr = "BASS_ERROR_WASAPI";
                errDesc = "BASSWASAPI: no WASAPI available";
                break;
            case 6000:
                errStr = "BASS_ERROR_MP4_NOSTREAM";
                errDesc = "BASS_AAC: non-streamable due to MP4 atom order ('mdat' before 'moov') ";
                break;
            default:
                errStr = "<Unknown>";
                errDesc = "No info available";
                break;
        }

        return errStr +":"+errDesc;
    }
}
