import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;

public class RecordTest {

    public static void main(String[] args) {
        try{
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,2,4,44100,false);

            DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            if(!AudioSystem.isLineSupported(dataInfo)){
                System.out.println("Not supported");
            }

            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataInfo);
            targetDataLine.open();

            JOptionPane.showMessageDialog(null, "Hit ok to start recording");
            targetDataLine.start();

            Thread audioRecorderThread = new Thread() {
                @Override
                public void run() {
                    AudioInputStream recordingStream = new AudioInputStream(targetDataLine);
                    File outputFile = new File("record.wav");
                    try{
                        AudioSystem.write(recordingStream,AudioFileFormat.Type.WAVE, outputFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("Stopped recording");
                }
            };

            audioRecorderThread.start();
            JOptionPane.showMessageDialog(null, "Hit ok to stop recording");
            targetDataLine.stop();
            targetDataLine.close();

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
