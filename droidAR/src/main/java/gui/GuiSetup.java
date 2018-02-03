package gui;

import system.Setup;
import system.TaskManager;
import util.Log;
import util.Wrapper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import commands.Command;
import commands.logic.CommandSetWrapperToValue;
import commands.system.CommandDeviceVibrate;

import de.rwth.R;


public class GuiSetup {

	private static final String LOG_TAG = "GuiSetup";
	private static final long VIBRATION_DURATION_IN_MS = 20;
	// private static final int BUTTON_BACKGROUND =
	// android.R.drawable.alert_light_frame;
	private LinearLayout topOuter;
	private LinearLayout bottomOuter;
	private LinearLayout leftOuter;
	private LinearLayout rightOuter;
	private LinearLayout bottomView;
	private LinearLayout topView;
	private LinearLayout leftView;
	private LinearLayout rightView;
	private LinearLayout bottomRightView;

	private RelativeLayout main;
	private Setup mySetup;
	/**
	 * will be set to true in {@link GuiSetup} constructor on default
	 */
	private boolean vibrationEnabled;
	private CommandDeviceVibrate vibrateCommand;

	/**
	 * @param setup
	 * @param source
	 *            the xml layout converted into a view
	 */
	public GuiSetup(Setup setup, View source) {

		mySetup = setup;
		Log.d(LOG_TAG, "GuiSetup init");
		setVibrationFeedbackEnabled(true);

		main = (RelativeLayout) source.findViewById(R.id.main_view);

		bottomOuter = (LinearLayout) source.findViewById(R.id.LLA_bottom);
		topOuter = (LinearLayout) source.findViewById(R.id.LLA_top);
		leftOuter = (LinearLayout) source.findViewById(R.id.LLA_left);
		rightOuter = (LinearLayout) source.findViewById(R.id.LLA_right);

		bottomView = (LinearLayout) source.findViewById(R.id.LinLay_bottom);
		topView = (LinearLayout) source.findViewById(R.id.LinLay_top);
		leftView = (LinearLayout) source.findViewById(R.id.LinLay_left);
		rightView = (LinearLayout) source.findViewById(R.id.LinLay_right);

		bottomRightView = (LinearLayout) source
				.findViewById(R.id.LinLay_bottomRight);

	}

	public void addButtonToBottomView(Command a, String buttonText) {
		addButtonToView(bottomView, a, buttonText);
	}

	public void addButtonToLeftView(Command a, String buttonText) {
		addButtonToView(leftView, a, buttonText);
	}

	public void addButtonToRightView(Command a, String buttonText) {
		addButtonToView(rightView, a, buttonText);
	}

	public void addImangeButtonToRightView(int imageId, Command command) {
		addImageButtonToView(rightView, command, imageId);
	}

	public void addImangeButtonToBottomView(int imageId, Command command) {
		addImageButtonToView(bottomView, command, imageId);
	}

	public void addImangeButtonToTopView(int imageId, Command command) {
		addImageButtonToView(topView, command, imageId);
	}

	public void addButtonToTopView(Command a, String buttonText) {
		addButtonToView(topView, a, buttonText);
	}

	public void addImageButtonToView(LinearLayout target, final Command c,
			int imageId) {
		if (target != null) {
			ImageButton b = new ImageButton(target.getContext());

			b.setImageResource(imageId);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isVibrationFeedbackEnabled() && vibrateCommand != null) {
						vibrateCommand.execute();
					}
					c.execute();
				}
			});
			target.addView(b);

			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) b.getLayoutParams();
			params.height = 125;
			params.width = 300;
			b.setLayoutParams(params);
			b.setScaleType(ImageView.ScaleType.FIT_CENTER);
			b.setBackgroundColor(Color.LTGRAY);
			//b.setBackgroundResource(0);
			b.getBackground().setAlpha(128);
		} else {
			Log.e(LOG_TAG, "No target specified (zwas null) "
					+ "to add the image-button to.");
		}
	}

	public void addSeekbarToTopView(Drawable imageDrawable, Command c ) {
		addSeekbarToView(topView, c, imageDrawable);
	}

	public void addSeekbarToView(LinearLayout target, final Command c,
									 Drawable imageDrawable) {
		if (target != null) {
			SeekBar b = new SeekBar(target.getContext());

			b.setThumb(imageDrawable);

			LayoutParams lp = new LayoutParams(600, 125);
			b.setLayoutParams(lp);


			b.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			b.setPadding(15,0,15,0);
			b.setThumbOffset(15);

			b.setVisibility(View.GONE);
			b.setBackgroundColor(Color.LTGRAY);
			b.getBackground().setAlpha(128);
			final Context mycontext = target.getContext();

			b.setOnTouchListener(new SeekBar.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					int action = event.getAction();
					switch (action)
					{
						case MotionEvent.ACTION_DOWN:
							// Disallow ScrollView to intercept touch events.
							v.getParent().requestDisallowInterceptTouchEvent(true);
							break;

						case MotionEvent.ACTION_UP:
							// Allow ScrollView to intercept touch events.
							v.getParent().requestDisallowInterceptTouchEvent(false);
							break;
					}

					// Handle Seekbar touch events.
					v.onTouchEvent(event);
					return true;
				}
			});

			b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

				public void onStopTrackingTouch(SeekBar arg0) {
					// TODO Auto-generated method stub
					System.out.println(".....111.......");
				}

				public void onStartTrackingTouch(SeekBar arg0) {

					// TODO Auto-generated method stub
					Toast.makeText(mycontext, "started sliding", Toast.LENGTH_SHORT);

					Log.d("seekbar", " started");
				}

				public void onProgressChanged(SeekBar arg0, int distance, boolean arg2) {
					// TODO Auto-generated method stub
					c.execute();
					System.out.println(".....333......."+distance);
				}
			});
			target.addView(b);
		} else {
			Log.e(LOG_TAG, "No target specified (zwas null) "
					+ "to add the image-button to.");
		}
	}

	/**
	 * @param target
	 * @param onClickCommand
	 * @param buttonText
	 */
	public void addButtonToView(LinearLayout target,
			final Command onClickCommand, String buttonText) {
		if (target != null) {
			Button b = new Button(target.getContext());
			// b.setBackgroundResource(BUTTON_BACKGROUND);
			// b.setTextColor(gl.Color.blackTransparent().toIntRGB());
			b.setText(buttonText);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isVibrationFeedbackEnabled() && vibrateCommand != null) {
						vibrateCommand.execute();
					}
					onClickCommand.execute();
				}

			});
			target.addView(b);
		} else {
			// TODO then main.xml damaged or changed?
		}
	}

	private boolean isVibrationFeedbackEnabled() {
		return vibrationEnabled;
	}

	public void setVibrationFeedbackEnabled(boolean vibrate) {
		this.vibrationEnabled = vibrate;
		if (vibrate && vibrateCommand == null) {
			try {
				Log.d(LOG_TAG,
						"Trying to enable vibration feedback for UI actions");
				vibrateCommand = new CommandDeviceVibrate(
						mySetup.myTargetActivity, VIBRATION_DURATION_IN_MS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addCheckBoxToView(LinearLayout v, String text,
			boolean initFlag, final Command isCheckedCommand,
			final Command isNotCheckedCommand) {
		CheckBox c = new CheckBox(v.getContext());

		c.setText(text);
		c.setChecked(initFlag);
		c.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					isCheckedCommand.execute();
				} else {
					isNotCheckedCommand.execute();
				}

			}
		});
		v.addView(c);
	}

	public void addCheckBoxToView(LinearLayout v, String string,
			Wrapper wrapperWithTheBooleanToSwitchInside) {
		CommandSetWrapperToValue setTrue = new CommandSetWrapperToValue(
				wrapperWithTheBooleanToSwitchInside, true);
		CommandSetWrapperToValue setFalse = new CommandSetWrapperToValue(
				wrapperWithTheBooleanToSwitchInside, false);
		addCheckBoxToView(v, string,
				wrapperWithTheBooleanToSwitchInside.getBooleanValue(), setTrue,
				setFalse);
	}

	/**
	 * @param v
	 * @param weight
	 *            2 or 3 is a good value
	 * "@param height
	 *            <150
	 * @return
	 */
	public void addViewToBottomRight(View v, float weight, int heightInPixels) {
		bottomRightView.addView(v);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, heightInPixels);
		p.weight = weight;
		bottomRightView.setLayoutParams(p);
	}

	public EditText addSearchbarToView(LinearLayout v,
			final Command commandOnSearch, String clearText) {
		final EditText t = new EditText(v.getContext());
		t.setHint(clearText);
		t.setHintTextColor(Color.GRAY);
		t.setMinimumWidth(200);
		t.setSingleLine();

		//slimbo: added this action done line to close keyboard
		t.setImeOptions(EditorInfo.IME_ACTION_DONE);

		//slimbo: added this to make text gray
		t.setTextColor(Color.GRAY);
		t.setSelectAllOnFocus(true);

		t.setOnEditorActionListener(new EditText.OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					return commandOnSearch.execute();
				}

				return false;
			}
		});


		v.addView(t);
		return t;
	}

	public void addTaskmanagerToView(LinearLayout v) {
		addTaskmanagerToView(v, "", " <", "/", "> ");
	}

	/**
	 * @param v
	 * @param idleText
	 * @param workingPrefix
	 *            the text in front of the current working progress. Example:
	 *            The < in <2/10>
	 * @param workingMiddleText
	 *            the text in the middle of the current working progress.
	 *            Example: The / in <2/10>
	 * @param workingSuffix
	 *            the text at the end of the current working progress. Example:
	 *            The > in <2/10>
	 */
	public void addTaskmanagerToView(LinearLayout v, String idleText,
			String workingPrefix, String workingMiddleText, String workingSuffix) {
		v.addView(TaskManager.getInstance().getProgressWheel(v.getContext()));
		v.addView(TaskManager.getInstance().getProgressTextView(v.getContext(),
				idleText, workingPrefix));
		v.addView(TaskManager.getInstance().getProgressSizeText(v.getContext(),
				idleText, workingMiddleText, workingSuffix));
	}

	public void addViewToBottom(View v) {
		bottomView.addView(v);
	}

	public void addViewToTop(View v) {
		topView.addView(v);
	}

	public void addViewToRight(View v) {
		rightView.addView(v);
	}

	public View getMainContainerView() {
		return main;
	}

	public LinearLayout getLeftView() {
		return leftView;
	}

	public LinearLayout getLeftOuter() {
		return leftOuter;
	}

	public LinearLayout getRightView() {
		return rightView;
	}

	public LinearLayout getBottomView() {
		return bottomView;
	}
	public LinearLayout getBottomOuter() {
		return bottomOuter;
	}

	public LinearLayout getTopView() {
		return topView;
	}

	public void setBackroundColor(LinearLayout target, int color) {
		target.setBackgroundColor(color);
	}

	public void setBottomBackroundColor(int color) {
		setBackroundColor(bottomOuter, color);
	}

	public void setBottomMinimumHeight(int height) {
		setMinimumHeight(bottomOuter, height);
	}

	public void setBottomViewCentered() {
		/*
		 * TODO doesnt work anymore because of the additional linlayout in the
		 * right bottom corner! fix it
		 */


		bottomOuter.setGravity(Gravity.CENTER);
		bottomView.setGravity(Gravity.CENTER);

		//bottomOuter.setOrientation(LinearLayout.HORIZONTAL);

		bottomOuter.getChildAt(0).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));

		//bottomView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		//bottomOuter.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
	}

	public void setLeftBackroundColor(int color) {
		setBackroundColor(leftOuter, color);
	}

	public void setLeftViewCentered() {
		leftOuter.setGravity(Gravity.CENTER);
	}

	public void setLeftWidth(int width) {
		setMinimumWidth(leftView, width);
	}

	public void setMinimumHeight(LinearLayout target, int height) {
		target.setMinimumHeight(height);
	}

	public void setMinimumWidth(LinearLayout target, int width) {
		target.setMinimumWidth(width);
	}

	public void setRightBackroundColor(int color) {
		setBackroundColor(rightOuter, color);
	}

	public void setRightViewCentered() {
		rightOuter.setGravity(Gravity.CENTER);
	}

	public void setRightViewAllignBottom() {
		rightOuter.setGravity(Gravity.BOTTOM);
	}

	public void setRightWidth(int width) {
		setMinimumWidth(rightView, width);
	}

	public void setTopBackroundColor(int color) {
		setBackroundColor(topOuter, color);
	}

	public void setTopHeight(int height) {
		setMinimumHeight(topView, height);
	}

	public void setTopViewAllignRight() {
		topOuter.setGravity(Gravity.RIGHT);
	}

	public void setTopViewCentered() {
		topOuter.setGravity(Gravity.CENTER);
	}

	/**
	 * This method does the same thing as
	 * {@link Setup#addItemToOptionsMenu(Command, String)}!
	 * 
	 * @param commandToAdd
	 * @param menuItemText
	 */
	public void addItemToOptionsMenu(Command commandToAdd, String menuItemText) {
		mySetup.addItemToOptionsMenu(commandToAdd, menuItemText);
	}

}
