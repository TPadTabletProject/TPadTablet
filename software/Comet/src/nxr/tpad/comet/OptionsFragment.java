package nxr.tpad.comet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class OptionsFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener {
	public DrawviewFragment drawView;
	public ServerFragment serverView;
	public MainActivity mainActivity;
	
	SeekBar decayBar;
	SeekBar ageBar;
	SeekBar radiusBar;
	Button ownTouch;
	Button otherTouch;
	Button timeDecay;
	
   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	   View view = inflater.inflate(R.layout.options_fragment, container, false);
	   Button b = (Button) view.findViewById(R.id.clean_button);
	   b.setOnClickListener(this);
	   b = (Button) view.findViewById(R.id.options_hide_button);
	   b.setOnClickListener(this);
	   
	   ownTouch = (Button) view.findViewById(R.id.show_own_touch);
	   ownTouch.setOnClickListener(this);
	   ownTouch.setText("Hide Self");
	   otherTouch = (Button) view.findViewById(R.id.show_other_touch);
	   otherTouch.setOnClickListener(this);
	   otherTouch.setText("Hide Other");
	   timeDecay = (Button) view.findViewById(R.id.time_decay);
	   timeDecay.setOnClickListener(this);
	   timeDecay.setText("Time Decay OFF");
	   
	   decayBar = (SeekBar) view.findViewById(R.id.decay_bar);
	   decayBar.setOnSeekBarChangeListener(this);
	   radiusBar = (SeekBar) view.findViewById(R.id.radius_bar);
	   radiusBar.setOnSeekBarChangeListener(this);
	   
	   decayBar.setProgress(2);
	   radiusBar.setProgress(50);
		
	   return view;
   }
   
   public void clean() {
	   drawView.clean();
   }

	public void hide() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();  
		ft.hide(this);  
		ft.commit();  
	}
	
	public void show() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();  
		ft.show(this);  
		ft.commit();  
	}
   
	public void showOwnTouch() {
		if (ownTouch.getText() == "Show Self") {
			ownTouch.setText("Hide Self");
			drawView.toggleOwn(true);
		} else {
			ownTouch.setText("Show Self");
			drawView.toggleOwn(false);
		}
	}
	
	public void timeDecayToggle() {
		if (timeDecay.getText() == "Time Decay ON") {
			timeDecay.setText("Time Decay OFF");
			drawView.toggleDecay(false);
		} else {
			timeDecay.setText("Time Decay ON");
			drawView.toggleDecay(true);
		}
	}
	
	public void showOtherTouch() {
		if (otherTouch.getText() == "Show Other") {
			otherTouch.setText("Hide Other");
			drawView.toggleOther(true);
		} else {
			otherTouch.setText("Show Other");
			drawView.toggleOther(false);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clean_button:
			drawView.clean();
			break;
		case R.id.options_hide_button:
			hide();
			break;
		case R.id.show_own_touch:
			showOwnTouch();
			break;
		case R.id.show_other_touch:
			showOtherTouch();
			break;
		case R.id.time_decay:
			timeDecayToggle();
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar s) {
		switch (s.getId()) {
		case R.id.radius_bar:
			drawView.setSelfRadius(radiusBar.getProgress());
			serverView.sendSize(radiusBar.getProgress());
			break;
		case R.id.decay_bar:
			drawView.setSelfDecay(decayBar.getProgress()+1);
			serverView.sendDecay(decayBar.getProgress()+1);
			break;
		}
	}
}
