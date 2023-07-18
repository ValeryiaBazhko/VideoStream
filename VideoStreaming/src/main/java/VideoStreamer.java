import java.io.IOException;

public class VideoStreamer {
    private Process process;

    public void start() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("ffmpeg", "-f", "v4l2", "-i", "/dev/video0", "-b:v", "2000k", "-b:a", "128k", "-f", "mpegts", "udp://192.168.0.12:5000");
        process = builder.start();
    }

    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }
}
