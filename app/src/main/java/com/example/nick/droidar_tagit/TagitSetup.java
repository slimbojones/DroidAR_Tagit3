package com.example.nick.droidar_tagit;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import actions.Action;
import actions.ActionBufferedCameraAR;
import actions.ActionCalcRelativePos;
import actions.ActionMoveCameraBuffered;
import actions.ActionPlaceObject;
import actions.ActionRotateCameraBuffered;
import commands.Command;
import commands.ui.CommandShowToast;
import geo.GeoObj;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.animations.AnimationFaceToCamera;
import gl.scenegraph.MeshComponent;
import gui.GuiSetup;
import system.ErrorHandler;
import system.EventManager;
import system.Setup;
import util.IO;
import util.Vec;
import util.Wrapper;
import worldData.Obj;
import worldData.SystemUpdater;
import worldData.World;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TagitSetup extends Setup implements View.OnLongClickListener, View.OnClickListener {

	private GLCamera camera;
	private World world;
	private Wrapper placeObjectWrapper;
	private static int RESULT_LOAD_IMAGE = 1;

	String textToDisplay = "enter text here";
	public Obj placerContainer;
	MeshComponent arrow;
	Bitmap myBitmap;
	private NameViewModel mModel;
	private GuiSetup thisGuiSetup;
	private RecyclerViewAdapter recyclerViewAdapter;





	private void toggleViews(){

		if(thisGuiSetup.getBottomView().getChildAt(0).getVisibility() == View.VISIBLE){
			thisGuiSetup.getBottomView().getChildAt(0).setVisibility(View.GONE);
			thisGuiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);
			thisGuiSetup.getTopView().getChildAt(0).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(1).setVisibility(View.VISIBLE);
		}
		else{
			thisGuiSetup.getBottomView().getChildAt(0).setVisibility(View.VISIBLE);
			thisGuiSetup.getBottomView().getChildAt(1).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(0).setVisibility(View.INVISIBLE);
			thisGuiSetup.getTopView().getChildAt(1).setVisibility(View.INVISIBLE);
		}
	}


	@Override
	public void _a_initFieldsIfNecessary() {

		// allow the user to send error reports to the developer:
		ErrorHandler.enableEmailReports("droidar.rwth@gmail.com",
				"Error in DroidAR App");

		placeObjectWrapper = new Wrapper();
	}

	@Override
	public void _b_addWorldsToRenderer(GL1Renderer renderer,
			GLFactory objectFactory, GeoObj currentPosition) {
		camera = new GLCamera(new Vec(0, 0, 10));
		world = new World(camera);



		GeoObj placerContainer = currentPosition;

		placerContainer.setComp(objectFactory.newArrow());
		world.add(placerContainer);
		placeObjectWrapper.setTo(placerContainer);
		renderer.addRenderElement(world);

		loadFromPublicWorld();

	}

	@Override
	public void _c_addActionsToEvents(EventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {
		arView.addOnTouchMoveAction(new ActionBufferedCameraAR(camera));
		Action rot1 = new ActionRotateCameraBuffered(camera);
		//slimbo:  changed maxDistance from 50 to 20
		Action rot2 = new ActionPlaceObject(camera, placeObjectWrapper, 20);

		updater.addObjectToUpdateCycle(rot1);
		updater.addObjectToUpdateCycle(rot2);

		eventManager.addOnOrientationChangedAction(rot1);
		eventManager.addOnOrientationChangedAction(rot2);
		eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera,
				5, 25));
		eventManager.addOnLocationChangedAction(new ActionCalcRelativePos(
				world, camera));
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater worldUpdater) {
		worldUpdater.addObjectToUpdateCycle(world);
	}

	@Override
	public void _e2_addElementsToGuiSetup(final GuiSetup guiSetup, Activity context) {

		thisGuiSetup = guiSetup;

		//CREATE IMAGE PICKER BUTTON
		guiSetup.addImangeButtonToTopView(R.drawable.ic_insert_photo_black_24px, new Command() {

			@Override
			public boolean execute() {

				Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
				mediaChooser.setType("video/*, image/*");
				((Activity) myTargetActivity).startActivityForResult(mediaChooser,RESULT_LOAD_IMAGE );

				return true;
			}

		});

		//CREATE TEXT PICKER BUTTON
		guiSetup.addImangeButtonToTopView(R.drawable.ic_insert_comment_black_24px, new Command() {

			@Override
			public boolean execute() {

				View ibView = guiSetup.getRightView().getChildAt(0);
				if (ibView instanceof EditText) {

					ibView.setVisibility(View.VISIBLE);

					ibView.requestFocusFromTouch();
					InputMethodManager lManager = (InputMethodManager)getActivity().getSystemService(getActivity().getBaseContext().INPUT_METHOD_SERVICE);
					lManager.showSoftInput((ibView), 0);
				}
				return true;
			}
		});

		//CREATE INVISIBLE EDITTEXT
		guiSetup.addSearchbarToView(guiSetup.getRightView(), new Command() {

			public boolean execute() {
				newTextObject();
				return false;
			}

			private void newTextObject() {

				TextView v = new TextView(getActivity().getBaseContext());
				v.setBackgroundColor(Color.DKGRAY);
				View ibView = guiSetup.getRightView().getChildAt(0);
				if (ibView instanceof EditText) {

					if(((EditText) ibView).getText().toString().matches("")) {
						textToDisplay = "Tagit";
					}
					else {
						textToDisplay = ((EditText) ibView).getText().toString();
					}
				}
				v.setText(textToDisplay);
				myBitmap = IO.loadBitmapFromView(v);
				String textBitmapString = BitMapToString(myBitmap);

				ArActivity myActivity = (ArActivity) myTargetActivity;
				myActivity.addToUriPaths(textBitmapString, "type2");

			}
		}, textToDisplay);

		View myEditText = guiSetup.getRightView().getChildAt(0);
		if (myEditText instanceof EditText) {

			((EditText) myEditText).setWidth (0);
			((EditText) myEditText).setHeight (0);
			((EditText) myEditText).getBackground().setAlpha(0);
			((EditText) myEditText).setClickable(false);

		}

		//CREATE CHECK BUTTON TO PLACE OBJECT
		guiSetup.addImangeButtonToBottomView(R.drawable.ic_check_circle_black_24px, new Command() {

			@Override
			public boolean execute() {

				final Obj placerContainer = new Obj();
				placerContainer.setComp(null);
				world.add(placerContainer);
				placeObjectWrapper.setTo(placerContainer);

				saveToPublicWorld(placerContainer);

				toggleViews();
				return true;
			}
		});

		//CREATE CANCEL BUTTON TO EXIT PLACEMENT
		guiSetup.addImangeButtonToBottomView(R.drawable.ic_cancel_black_24px, new Command() {

			@Override
			public boolean execute() {

				placerContainer.remove(arrow);
				toggleViews();
				return true;
			}
		});

		guiSetup.setTopViewCentered();
		guiSetup.setBottomViewCentered();

		guiSetup.getBottomView().getChildAt(0).setVisibility(View.GONE);
		guiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);

		android.view.ViewGroup.LayoutParams params2 = guiSetup.getBottomView().getChildAt(0).getLayoutParams();
		params2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		guiSetup.getTopView().getChildAt(0).setLayoutParams(params2);

		//TODO need a better way to set button size which is responsive to screen size
		guiSetup.getBottomView().getChildAt(0).setPadding(100,37,100,37);
		guiSetup.getBottomView().getChildAt(1).setPadding(100,37,100,37);
		guiSetup.getTopView().getChildAt(0).setPadding(100,37,100,37);
		guiSetup.getTopView().getChildAt(1).setPadding(100,37,100,37);

		guiSetup.getLeftOuter().removeAllViews();
		RecyclerView DynamicListView = new RecyclerView(getActivity().getBaseContext());
		//DynamicListView.setDivider(null);
		//DynamicListView.setDividerHeight(0);
		//DynamicListView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
		guiSetup.getLeftOuter().addView(DynamicListView);

		ViewGroup.LayoutParams params = guiSetup.getLeftOuter().getLayoutParams();
		//TODO change width to something dynamic and scalable
		params.width = 160;
		guiSetup.getLeftOuter().setLayoutParams(params);
		guiSetup.getLeftOuter().requestLayout();

		View.OnLongClickListener myLongListener = this;
		View.OnClickListener myListener = this;

		mModel = ViewModelProviders.of((ArActivity) myTargetActivity).get(NameViewModel.class);

		recyclerViewAdapter = new RecyclerViewAdapter(new ArrayList<Tagpost>(), myLongListener, myListener  );
		DynamicListView.setLayoutManager(new LinearLayoutManager((ArActivity) myTargetActivity));
		DynamicListView.setAdapter(recyclerViewAdapter);

		mModel.getUriPathList().observe((ArActivity) myTargetActivity, new Observer<List<Tagpost>>() {
			@Override
			public void onChanged(@Nullable List<Tagpost> thisUriPathList) {
				recyclerViewAdapter.addItems(thisUriPathList);
			}
		});

	}
	public void placeObjectFromSelector(Bitmap selectedBitmap) {

		placerContainer = newPlacedObject(selectedBitmap);
		world.add(placerContainer);
		placeObjectWrapper.setTo(placerContainer);
	}

	public Obj newPlacedObject(Bitmap selectedBitmap) {

		placerContainer = new Obj();
		arrow = TagitFactory.getInstance().newTexturedSquare(selectedBitmap.toString(),selectedBitmap,5);


		arrow.setOnClickCommand(new CommandShowToast(myTargetActivity,
				"Item Found +1"));

		arrow.addAnimation(new AnimationFaceToCamera(camera));

		placerContainer.setComp(arrow);

		//placerContainer.getGraphicsComponent()


		return placerContainer;
	}

	public String BitMapToString(Bitmap bitmap){
		ByteArrayOutputStream baos=new  ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
		byte [] b=baos.toByteArray();
		String temp= Base64.encodeToString(b, Base64.DEFAULT);
		return temp;
	}


	@Override
	public boolean onLongClick(View view) {
		final CharSequence[] items = {"Edit","Delete","Nevermind"};
		AlertDialog.Builder builder = new AlertDialog.Builder((ArActivity) myTargetActivity);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getActivity().getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();

				switch(item) {
					case 0:
						Toast.makeText(getActivity().getApplicationContext(), "Edits Coming Soon", Toast.LENGTH_SHORT).show();
						break;
					case 1:

						Tagpost tagpost = (Tagpost) view.getTag();
						mModel.deleteTagpost(tagpost);
						break;
					default:

				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

		return true;
	}

	@Override
	public void onClick(View view) {

		ImageView imageView = (ImageView) view.findViewById(R.id.icon);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
		Bitmap yourBitmap = bitmapDrawable.getBitmap();
		Toast.makeText(getActivity().getApplicationContext(), yourBitmap.toString(), Toast.LENGTH_SHORT).show();
		placeObjectFromSelector(yourBitmap);

		if(thisGuiSetup.getBottomView().getChildAt(0).getVisibility() == View.GONE){
			toggleViews();
		}

	}

	public void saveToPublicWorld(Obj myPlacerContainer){

		Toast.makeText(getActivity().getApplicationContext(), "saveToPublicWorld started ", Toast.LENGTH_SHORT).show();

		SharedPreferences mPrefs = myTargetActivity.getPreferences(MODE_PRIVATE);

		SharedPreferences.Editor prefsEditor = mPrefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(myPlacerContainer);

		Toast.makeText(getActivity().getApplicationContext(), "saveToPublicWorld: " + json, Toast.LENGTH_SHORT).show();
		prefsEditor.putString("myPlacerContainer", json);
		prefsEditor.commit();


	}

	public void loadFromPublicWorld(){



		SharedPreferences mPrefs = myTargetActivity.getPreferences(MODE_PRIVATE);

		Gson gson = new Gson();
		String json = mPrefs.getString("myPlacerContainer", "");

		Toast.makeText(getActivity().getApplicationContext(), "loadFromPublicWorld: " + json, Toast.LENGTH_LONG).show();
		Obj obj = gson.fromJson(json, Obj.class);

		//final Obj placerContainer = new Obj();
		//placerContainer.setComp(null);
		world.add(obj);
		placeObjectWrapper.setTo(obj);


	}
}
