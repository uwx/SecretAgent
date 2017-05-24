package jouvieje.bass;

import jouvieje.bass.structures.HMUSIC;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.Pointer;

public class BassUtils extends Pointer {
    private BassUtils() {
    }

    public static HMUSIC BASS_MusicLoad_FromBytes(boolean mem, byte[] file, long offset, int length, int flags, int freq) {
        if(!mem) {
            throw new IllegalArgumentException("BASS_MusicLoad_FromBytes was called with mem as false, BASS_MusicLoad should be used instead");
        } else {
            long javaResult = BassJNI.Bass_BASS_MusicLoad(mem, file, offset, length, flags, freq);
            return javaResult == 0L?null:HMUSIC.asHMUSIC(Pointer.newPointer(javaResult));
        }
    }

    public static HSTREAM BASS_StreamCreateFile_FromBytes(boolean mem, byte[] file, long offset, long length, int flags) {
        if(!mem) {
            throw new IllegalArgumentException("BASS_StreamCreateFile_FromBytes was called with mem as false, BASS_StreamCreateFile should be used instead");
        } else {
            long javaResult = BassJNI.Bass_BASS_StreamCreateFile(mem, file, offset, length, flags);
            return javaResult == 0L?null:HSTREAM.asHSTREAM(Pointer.newPointer(javaResult));
        }
    }
}
