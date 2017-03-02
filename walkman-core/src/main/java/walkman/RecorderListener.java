package walkman;

public interface RecorderListener {
  void onRecorderStart(Tape tape);
  void onRecorderStop();
}
