package edu.kaist.mskers.toondra;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.OverScroller;
import android.widget.TextView;

public class AutoScroller extends AppCompatActivity
  implements View.OnClickListener {
  private String TAG = "AUTOSCROLLER";
  private TextView textArea;
  private OverScroller overScroller;
  private Button button;
  private TextView xposView;
  private TextView yposView;
  private ScrollRun scrollRun;
  private int currY;
  private int elapseTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    currY = 0;
    setContentView(R.layout.autoscroll_test);
    textArea = (TextView) findViewById(R.id.textView2);
    xposView = (TextView) findViewById(R.id.xpos_view);
    yposView = (TextView) findViewById(R.id.ypos_view);
    button = (Button) findViewById(R.id.button2);
    button.setOnClickListener(this);

    elapseTime = 10000;

    overScroller = new OverScroller(AutoScroller.this);
    scrollRun = new ScrollRun();
    String temp = "Lorem ipsum dolor sit amet, vel minimum conceptam constituto eu, cum no iusto doming molestie, te qui vero putent labitur. Justo facete pri no. Labitur dignissim reprimique ne nam, id sit amet interpretaris. Has at ornatus principes elaboraret, ridens fabulas voluptua ne qui, patrioque persequeris efficiantur et nam. Ius audire offendit ne. Pri posse deserunt ad, ius ex minim omittam petentium.\n" +
        "\n" + "Ne has erat aliquando inciderint, cetero placerat ex eos, errem eleifend eam ne. Vel petentium omittantur ex, splendide assentior cu has. Dicat ludus quo ex, no quo amet oratio dissentias. Qui cu tollit integre, omnis erant complectitur eum ut. Graeci integre et quo, vim ut detracto euripidis.\n" +
        "\n" + "Ex facer vitae maiorum est. Habeo splendide moderatius duo id. Quo in viris euripidis. Qui graeci recteque accusamus in, ut qui diam sint, saperet mentitum vel ut.\n" +
        "\n" + "Duo modus nihil eripuit eu, eu malorum labitur consequat mea, veri omnesque patrioque at mea. Vis timeam patrioque id. Nam putant aperiri ad, at assum delectus mea, sonet nominavi periculis sea an. Sit admodum apeirian ex, congue tritani repudiare ei ius. Virtute omittam mea ex, hinc consul cu his, ea dicit homero principes quo. Dicant consul gubergren quo no, ea has sonet salutandi.\n" +
        "\n" + "Ad mea libris blandit adversarium, ignota inermis instructior ei mei, est no causae alienum verterem. An ius dico nobis feugait, scaevola recteque consequat eu cum. Pro ei sale appetere facilisis. Detracto indoctum te quo, discere dolorum impedit no sed. Sea aperiri honestatis ad, eos tale prompta dolorum no.";

    textArea.setText(temp + temp + temp);
  }

  @Override
  public void onClick(View clickedView) {
    Log.i(TAG, "onClick");
    if (clickedView.getId() == R.id.button2) {
      Log.i(TAG, "runnable created");

      elapseTime /= 2;
      scrollRun.forceFinish();
      scrollRun.start(elapseTime);

      textArea.post(scrollRun);
      Log.i(TAG, "button touched!");
    }
  }

  // runnable for scroller
  private class ScrollRun implements Runnable {
    private final OverScroller overScroller;

    ScrollRun() {
      overScroller = new OverScroller(AutoScroller.this, new LinearInterpolator());
    }

    public void start(int totaltime) {
      overScroller.startScroll(0, currY, 0, 5000, totaltime);
    }

    public void forceFinish() {
      overScroller.forceFinished(true);
    }

    @Override
    public void run() {
      if (!overScroller.isFinished()) {
        overScroller.computeScrollOffset();
        currY = overScroller.getCurrY();
        textArea.scrollTo(0, currY);
        textArea.post(this);
      }
    }

    public boolean isFinished() {
      return overScroller.isFinished();
    }
  }
}
