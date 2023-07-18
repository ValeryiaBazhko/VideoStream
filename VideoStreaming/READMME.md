

### ffmpeg video broadcasting:
  ```ffmpeg -f v4l2 -i /dev/video0 -f mpegts udp://192.168.0.12:5000?```

### ffmpeg playing video:
```ffplay -fflags nobuffer udp://192.168.0.12:5000```

### playback with small buffer
```ffplay -buffer_size 1024 udp://@192.168.0.12:5000```

### better quality video broadcast:
```ffmpeg -f v4l2 -i /dev/video0 -b:v 2000k -b:a 128k -f mpegts udp://192.168.0.12:5000```


### audio broadcasting:
- ```ffmpeg -f pulse -i default -acodec libmp3lame -ab 128k -f rtp rtp://localhost:1234```
- ```ffplay -i rtp://localhost:1234```