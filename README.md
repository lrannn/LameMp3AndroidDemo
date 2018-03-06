# README

使用AudioRecord进行录音，并使用lameMP3进行转码的demo。

Lamemp3的编码使用CMake方式进行编译


- [x] Recording PCM audio data
- [x] Encoding to MP3 data
- [x] Decoding to PCM data (使用了MediaCodec api实现)
- [ ] Convert PCM data to wav file( add wav header)